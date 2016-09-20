package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import models.compiler.Board;
import models.compiler.Cloud_Compilation_Server;
import models.compiler.FileRecord;
import models.compiler.Version_Object;
import models.notification.Notification;
import models.person.Person;
import models.project.b_program.B_Pair;
import models.project.b_program.B_Program;
import models.project.b_program.B_Program_Hw_Group;
import models.project.b_program.Homer_Instance;
import models.project.b_program.servers.Cloud_Homer_Server;
import models.project.m_program.Grid_Terminal;
import models.project.m_program.M_Project;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.WebSocket;
import utilities.enums.Firmware_type;
import utilities.enums.TypeOfCommand;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured_API;
import utilities.loginEntities.TokenCache;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.swagger.outboundClass.Swagger_Instance_HW_Group;
import utilities.swagger.outboundClass.Swagger_Websocket_Token;
import utilities.webSocket.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@Api(value = "Not Documented API - InProgress or Stuck")
public class WebSocketController_Incoming extends Controller {

// Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

// Values  --------------------------------------------- ----------------------------------------------------------------

    public static Map<String, WebSCType> incomingConnections_homers    = new HashMap<>(); // (Identificator, Websocket)
    public static Map<String, WebSCType> incomingConnections_terminals = new HashMap<>(); // (Identificator, Websocket)

    //TODO - tohle lze teoreticky dát do databáze
    // Sem podle ID homera uložím seznam zařízení, na která by se měl opět připojit
    public static Map<String, ArrayList<String>> terminal_lost_connection_homer = new HashMap<>();  // (Homer Identificator, List<Terminal Identificator>)

    // Ztracené připojení s Becki když se homer odpojil
    public static Map<String, ArrayList<String>> becki_lost_connection_homer = new HashMap<>();

    // Připojené servery, kde běží Homer instance jsou drženy v homer_cloud_server. Jde jen o jendoduché čisté spojení a
    // několik servisních metod. Ale aby bylo dosaženo toho, že Homer jak v cloudu tak i na fyzickém počítači byl obsluhován stejně
    // je redundantně (jen ukazateli) vytvořeno virtuální spojení na každou instanci blocko programu v cloudu.
    public static Map<String, WebSCType> blocko_servers = new HashMap<>(); // (<Server-Identificator, Websocket> >)

    // Komnpilační servery, které mají být při kompilaci rovnoměrně zatěžovány - nastřídačku. Ale předpokladem je, že všechny dělají vždy totéž.
    public static Map<String, WebSCType> compiler_cloud_servers = new HashMap<>(); // (Server-Identificator, Websocket)

    // Becki (frontend) spojení na synchronizaci blocka atd.. - Podporován režim multipřihlášení.
    public static Map<String, WebSCType> becki_website = new HashMap<>(); // (Person_id - Identificator, List of Websocket connections - Identificator je Token)

    public static TokenCache tokenCache = new TokenCache( (long) 5, (long) 500, 500);

    // PUBLIC API -------------------------------------------------------------------------------------------------------------------

    @ApiOperation(value = "get temporary Connection Token",
            tags = {"Access", "WebSocket"},
            notes = "for connection to websocket, you have to connect with temporary unique token. This Api return ",
            produces = "application/json",
            consumes = "text/plain",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Token succesfuly generated",   response = Swagger_Websocket_Token.class),
            @ApiResponse(code = 401, message = "Unauthorized request",         response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result get_Websocket_token() {
        try {

            String web_socket_token = UUID.randomUUID().toString();

            tokenCache.put(web_socket_token, SecurityController.getPerson().id);

            Swagger_Websocket_Token swagger_websocket_token = new Swagger_Websocket_Token();
            swagger_websocket_token.websocket_token = web_socket_token;

            return GlobalResult.result_ok(Json.toJson(swagger_websocket_token));
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


    // Připojení Homera (Cloud i Local)
    @ApiOperation(value = "Homer websocket Connection", hidden = true)
    public  WebSocket<String>  homer_connection(String homer_mac_address){

        logger.debug("Homer Connection on mac_address: " + homer_mac_address);

        // Inicializace Websocketu pro Homera
        WS_Homer_Local homer = new WS_Homer_Local(homer_mac_address, incomingConnections_homers);

        // Vtvoří se zástupný objekt připojení
        WebSocket<String> webSocket = homer.connection();

        logger.debug("Connection created on: " + homer_mac_address);
        homer_connection_procedure(homer);

        logger.debug("Returning connection on: " + homer_mac_address);
        return  webSocket;
    }
    public static void  homer_connection_procedure(WebSCType homer) {

        logger.debug("Homer connection procedure on: " + homer.identifikator);

        try {

            // Podívám se, zda nemám už připojené a čekající terminály se kterými bych chtěl komunikovat
            if (terminal_lost_connection_homer.containsKey(homer.identifikator)) {

                logger.debug("Homer -> Lost Connection found with Terminal. Starting procedure for re-connection ");

                // Pokud ano, tak na seznamu jmen terminálů vázajících se k tomuto Homerovi provedu následující operace
                for (String terminal_name : terminal_lost_connection_homer.get(homer.identifikator)) {

                    logger.debug("Homer -> Lost Connection: Terminal found: " + terminal_name);

                    // zkontrolu zda terminál je stále přopojený
                    if (incomingConnections_terminals.containsKey(terminal_name)) {

                        // V případě že tedy nějaké temrinály jsou připravené komunikovat, musím upozornit homera, že chci odebírat jeho změny. Takže ho u prvního oběratele budu informovat
                        if (homer.subscribers_grid.isEmpty()) {

                            ask_for_receiving_for_Grid(homer);
                            homer.subscribers_grid.add(incomingConnections_terminals.get(terminal_name));
                            incomingConnections_terminals.get(terminal_name).subscribers_grid.add(homer);

                        } else {

                            homer.subscribers_grid.add(incomingConnections_terminals.get(terminal_name));
                            incomingConnections_terminals.get(terminal_name).subscribers_grid.add(homer);

                        }

                        // Zasílám na terminál informaci o znovu připojení
                        terminal_homer_reconnection(incomingConnections_terminals.get(terminal_name));
                    }
                    // Pokud není připojený - vyřadím ho ze seznamu
                    else {
                        logger.debug("Homer -> Lost Connection: Terminal "+ terminal_name +" is no more connected" );
                        terminal_lost_connection_homer.get(homer.identifikator ).remove(terminal_name);
                    }
                }

                // Po obnově spojení se všem ztracenými připojeními vyhazuji objekt ztraceného spojení z mapy vázaný na tento Homer
                logger.debug("Homer -> Lost Connection: Server removing " +homer.identifikator + " lost connection");
                terminal_lost_connection_homer.remove(homer.identifikator );

            } else if(becki_lost_connection_homer.containsKey(homer.identifikator)){

                logger.debug("Homer -> Lost Connection: Becki found: " + homer.identifikator);

                // Pookud ano, tak na seznamu jmen becki vázajících se k tomuto Homerovi provedu becki identificator je id uživatele (protože zakrývá víc připojených instancí)
                for (String becki_identificator : becki_lost_connection_homer.get(homer.identifikator)) {

                    // kontrolu zda je becki stále připojená
                    if (becki_website.containsKey(becki_identificator)) {

                        // V případě že tedy nějaké temrinály jsou připravené komunikovat, musím upozornit homera, že chci odebírat jeho změny Takže ho u prvního oběratele budu informovat
                        if (homer.subscribers_becki.isEmpty()) {

                            logger.debug("Homer -> Lost Connection: Connection with Homer: " + becki_identificator + " was restored");
                            homer_subscribe_blocko_instance(homer);

                            homer.subscribers_becki.add(becki_website.get(becki_identificator));
                            becki_website.get(becki_identificator).subscribers_becki.add(homer);

                        } else {

                            logger.debug("Homer -> Lost Connection: Connection with Becki: " + becki_identificator + " was restored");
                            homer.subscribers_becki.add(becki_website.get(becki_identificator));
                            becki_website.get(becki_identificator).subscribers_becki.add(homer);
                        }

                        logger.debug("Homer -> Lost Connection: Sending to Becki :" + becki_identificator + " that they are new receiver");
                        terminal_homer_reconnection(becki_website.get(becki_identificator));
                    }
                    // Pokud není připojený - vyřadím ho ze seznamu
                    else {
                        logger.debug("Homer -> Lost Connection: Becki " + becki_identificator + " is gone :( So system remove that from list");
                        becki_lost_connection_homer.get( homer.identifikator ).remove(becki_identificator);
                    }
                }

            } else homer_all_terminals_are_gone(homer);

        }catch (Exception e){
            logger.error("Homer Connection Exception", e);
        }
    }

    @ApiOperation(value = "Terminal connection", tags = {"WebSocket"})
    public  WebSocket<String>  mobile_connection(String m_project_id, String terminal_id) {
        try {

            logger.debug("Terminal: Incoming connection on terminal: " + terminal_id + " on project " + m_project_id);

            if (incomingConnections_terminals.containsKey(terminal_id)) {
                logger.debug("Terminal: Incoming connection on terminal: " + terminal_id + " is already used");
                return WebSocket.reject(forbidden());
            }
            if (Grid_Terminal.find.where().eq("terminal_id", terminal_id).findUnique() == null) {
                logger.debug("Terminal: Incoming connection on terminal: " + terminal_id + " is not registered in database");
                return WebSocket.reject(forbidden());
            }

            M_Project m_project = M_Project.find.byId(m_project_id);

            if (m_project == null) {
                logger.debug("Terminal: Incoming connection on terminal: " + terminal_id + " where "+ m_project_id +" is not registered in database");
                return WebSocket.reject(forbidden());
            }
            if (m_project.b_program_version == null) {
                logger.debug("Terminal: Incoming connection on terminal: " + terminal_id + " where "+ m_project_id +" where M_Project is not connected with B_program");
                return WebSocket.reject(forbidden());
            }

            //----------------------------------------------------------------------------------------------------------------------

            WS_Grid_Terminal terminal = new WS_Grid_Terminal(terminal_id, m_project_id, incomingConnections_terminals);
            WebSocket<String> ws = terminal.connection();

            // Tohle je cloudové nasazení B programu
            //-----------------------------------------------------------------------------------------------------------

            // POKUD JE B_PROGRAM V CLOUDU ale shodná verze neexistuje ale je povoleno auto increment
            if (m_project.b_program_version.homer_instance == null &&  m_project.auto_incrementing &&  B_Program.find.where().isNotNull("version_objects.b_program_cloud").findUnique() != null) {
                logger.debug("Terminal: Program on Cloud win but system have to iterate to new version: ");

                m_project.b_program_version = m_project.b_program.where_program_run();
                m_project.update();

            }

            // POKUD JE B_PROGRAM V CLOUDU a má shodnou verzi
            if (m_project.b_program_version.homer_instance != null) {


                logger.debug("Terminal: Program on Cloud win");

                String homer_identificator = m_project.b_program_version.homer_instance.blocko_instance_name;
                String server_name =  Cloud_Homer_Server.find.where().eq("cloud_programs.version_object.m_project.id", m_project.id ).findUnique().server_name;

                logger.debug("Terminal: Connection with Server " + server_name);
                logger.debug("Terminal: Connection with Homer " + homer_identificator);

                logger.debug("Tyrion: Is CLoud cloud_blocko_server connected?");

                if (!blocko_servers.containsKey(server_name)) {

                    logger.debug("Tyrion: No its not - So Server make procedure for archive this state");

                    if (terminal_lost_connection_homer.containsKey(homer_identificator)) {

                        terminal_lost_connection_homer.get(homer_identificator).add(terminal_id);

                    } else {
                        ArrayList<String> list = new ArrayList<>(4);
                        list.add(terminal_id);
                        WebSocketController_Incoming.terminal_lost_connection_homer.put(homer_identificator, list);
                    }

                    logger.debug("Tyrion: Sending to Terminal, that homer is nto connected yet");
                    WebSocketController_Incoming.terminal_homer_is_not_connected_yet(terminal);

                    return ws;
                }

                logger.debug("Tyrion: Homer is online, so connection will be successful");
                WebSCType homer = WebSocketController_Incoming.incomingConnections_homers.get(homer_identificator);

                terminal.subscribers_grid.add(WebSocketController_Incoming.incomingConnections_homers.get(homer_identificator));
                if (homer.subscribers_grid.isEmpty()) WebSocketController_Incoming.ask_for_receiving_for_Grid(homer);

                homer.subscribers_grid.add(terminal);
                return ws;

            // TODO POKUD JE B_PROGRAM V CLOUDU ale nemám schodnou verzi a zakázal jsem autoincrementování
            } else if (! m_project.auto_incrementing && B_Program.find.where().isNotNull("version_objects.b_program_cloud").findUnique() != null){

                logger.debug("Tyrion: Warning: B_program is in cloud, but autoincrement is prohibited!!!");
                logger.warn ("Tyrion: Warning: B_program is in cloud, but autoincrement is prohibited!!!");

                m_project_is_connected_with_older_version(terminal);
                return ws;
            }



            // Tohle je fyzické nasazení B programu na PC
            //-----------------------------------------------------------------------------------------------------------

             // POKUD JE B_PROGRAM NA PC ale shodná verze neexistuje a povolil jsem auto inkrementaci
              if( m_project.b_program_version.homer_instance.private_server == null && m_project.auto_incrementing  && B_Program.find.where().isNotNull("version_objects.b_program_homer").findUnique() != null) {

                  logger.debug("Tyrion: Homer is on local computer, but Tyrion have to repair versions");
                  m_project.b_program_version = m_project.b_program.where_program_run();
                  m_project.update();

              }
              // POKUD JE B_PROGRAM NA PC a má shodnou verzi
              if (m_project.b_program_version.homer_instance.private_server != null) {

                  logger.debug("Tyrion: Homer is on local computer");

                  String homer_identificator = m_project.b_program_version.homer_instance.private_server.id;

                  logger.debug("Tyrion: Connection will be on Homer: " + homer_identificator);

                  logger.debug("Tyrion: is Homer online?");

                  if (!WebSocketController_Incoming.incomingConnections_homers.containsKey(homer_identificator)) {

                      logger.debug("Tyrion: No its not - So Server make procedure for archive this state");

                      if (WebSocketController_Incoming.terminal_lost_connection_homer.containsKey(homer_identificator)) {
                         WebSocketController_Incoming.terminal_lost_connection_homer.get(homer_identificator).add(terminal_id);

                      } else {
                          ArrayList<String> list = new ArrayList<>(4);
                          list.add(terminal_id);
                          WebSocketController_Incoming.terminal_lost_connection_homer.put(homer_identificator, list);
                      }

                      logger.debug("Tyrion: Sending to Terminal, that homer is nto connected yet");
                      WebSocketController_Incoming.terminal_homer_is_not_connected_yet(terminal);

                      return ws;
                  }

                  logger.debug("Tyrion: Homer is online, so connection will be successful");
                  WebSCType homer = WebSocketController_Incoming.incomingConnections_homers.get(homer_identificator);

                  terminal.subscribers_grid.add(WebSocketController_Incoming.incomingConnections_homers.get(homer_identificator));
                  if (homer.subscribers_grid.isEmpty()) WebSocketController_Incoming.ask_for_receiving_for_Grid(homer);

                  homer.subscribers_grid.add(terminal);
                  return ws;


                  // POKUD JE B_PROGRAM NA PC ale shodná verze neexistuje a zakázal jsem auto inkrementaci
                } else if( ! m_project.auto_incrementing && B_Program.find.where().isNotNull("version_objects.b_program_homer").findUnique() != null){

                  logger.debug("Tyrion: Warning: B_program is in cloud, but autoincrement is prohibited!!!");
                  logger.warn ("Tyrion: Warning: B_program is in cloud, but autoincrement is prohibited!!!");

                  m_project_is_connected_with_older_version(terminal);
                   return ws;

                }

                 logger.warn ("Tyrion: Warning!!! homer is not in cloud and also on local computer!");

                 terminal_blocko_program_not_running_anywhere(terminal);
                 return ws;


        }catch (Exception e){
            logger.error("Mobile / terminal Web Socket connection", e);
            return WebSocket.reject(forbidden());
        }
    }
    @ApiOperation(value = "Homer Server Connection", tags = {"WebSocket"})
    public  WebSocket<String>  homer_cloud_server_connection(String server_name){
        try{

            logger.debug("Cloud Server: Incoming connection: Server:  " + server_name);

            Cloud_Homer_Server blocko_server = Cloud_Homer_Server.find.where().eq("server_name", server_name).findUnique();
            if(blocko_server== null) return WebSocket.reject(forbidden("Server side error - unrecognized name"));

            if(blocko_servers.containsKey(server_name)) {
                logger.warn("Server is connected -> Tyrion try to send ping");
                try {

                    WS_BlockoServer ws_blockoServer = (WS_BlockoServer) blocko_servers.get(server_name);
                    blocko_server_ping(ws_blockoServer);
                }catch (Exception e){
                    logger.warn("Ping Failed - Tyrion remove previous connection");
                    blocko_servers.get(server_name).onClose();
                }

                logger.warn("Server is already connected and working!! Its prohibited connected to Tyrion with same name");
                return WebSocket.reject(forbidden("Server side error - already connected"));
            }


            logger.debug("Tyrion inicialize connection for Homer Server");
            WS_BlockoServer server = new WS_BlockoServer(server_name, blocko_servers);

            // Připojím se
            logger.debug("Connection is successful");
            WebSocket<String> webSocket = server.connection();

            // Procedury kontroly - informovat třeba všechny klienty o tom, že se cloud_blocko_server připojil. Kontzrola co tam běží a další píčoviny
            logger.debug("Tyrion have to control what is on the cloud_blocko_server side ");

            // GET state - a vyhodnocením v jakém stavu se cloud_blocko_server nachází a popřípadě
            // na něj nahraji nebo smažu nekonzistenntí clou dprogramy, které by na něm měly být

            class Control_Blocko_Server_Thread extends Thread{

                @Override
                public void run() {
                   Long interrupter = (long) 6000;
                    try {

                        while (interrupter > 0) {


                            sleep(500);
                            interrupter-=500;

                            logger.debug("Blocko Server: In control cycle: Iteration: " + interrupter);

                            if (server.isReady()) {

                                logger.debug("Blocko Server: Tyrion send to Blocko Server request for listInstances");

                                ObjectNode result = Json.newObject();
                                result.put("messageType", "listInstances");
                                result.put("messageChannel", "homer-server");
                                JsonNode jsonNode = server.write_with_confirmation(result, 1000*25, 0, 3);

                                logger.debug("Blocko Server: In control cycle: Iteration: " + interrupter);

                                if (jsonNode.has("status")) {

                                    if (jsonNode.get("status").asText().equals("error")) {
                                        interrupt();
                                    }

                                    logger.debug("Blocko Server: Respond with list of instances");

                                    // Vylistuji si seznam instnancí, které běží na serveru
                                    List<String> instances_on_server = new ArrayList<>();
                                    final JsonNode arrNode = jsonNode.get("instances");
                                    for (final JsonNode objNode : arrNode) instances_on_server.add(objNode.asText());
                                    logger.debug("Blocko Server: Number of instances on cloud_blocko_server: " + instances_on_server.size());


                                    // Vylistuji si seznam instnancí, které by měli běžet na serveru
                                    List<String> instances_in_database_for_uploud = new ArrayList<>();
                                    for (Homer_Instance cloud_program : Homer_Instance.find.where().eq("cloud_homer_server.id", blocko_server.id).select("blocko_instance_name").findList())
                                        instances_in_database_for_uploud.add(cloud_program.blocko_instance_name);
                                    logger.debug("Blocko Server: The number of instances that should run on Homer server by Database: " + instances_in_database_for_uploud.size());



                                    List<String> instances_for_upload_to_homer = new ArrayList<>();
                                    List<String> instances_for_removing_from_homer = new ArrayList<>();
                                    List<String> instancis_for_tyrion_memory = new ArrayList<>();


                                    // Vytvořím seznam instnancí k nahrátí
                                    for(String instance : instances_in_database_for_uploud){
                                        if(!instances_on_server.contains(instance))instances_for_upload_to_homer.add(instance);
                                    }

                                    // Vytvořím seznam instnancí ke smazání
                                    for(String instance : instances_on_server){
                                        if(!instances_in_database_for_uploud.contains(instance))instances_for_removing_from_homer.add(instance);
                                    }

                                    // Vytvořím seznam instancní nutných pro nahrátí do tyriona
                                    for(String instance : instances_in_database_for_uploud){
                                        if(instances_on_server.contains(instance) && !incomingConnections_homers.containsKey(instance)) instancis_for_tyrion_memory.add(instance);
                                    }


                                    // Vytvořím kopii seznamu instancí, které by měli běžet na Homer Serveru

                                    logger.debug("Blocko Server: The number of instances for uploading on homer server: "  + instances_for_upload_to_homer.size());
                                    logger.debug("Blocko Server: The number of instances for removing from homer server: " + instances_for_removing_from_homer.size());
                                    logger.debug("Blocko Server: The number of instances for reading to Tyrion RAM: "      + instancis_for_tyrion_memory.size());



                                    List<Homer_Instance> instances_for_uploud = Homer_Instance.find.where().in("blocko_instance_name", instances_for_upload_to_homer).findList();

                                    if (!instances_for_uploud.isEmpty()) {

                                        logger.debug("Blocko Server: Starting to uploud new instnces to cloud_blocko_server");

                                        for (Homer_Instance instance : instances_for_uploud) {
                                            try {

                                                 logger.debug("Instnace: " + instance.blocko_instance_name + " is not on homer server - it will be also uploaded to Homer server");
                                                 WebSocketController_Incoming.blocko_server_add_instance(server, instance, true);

                                            } catch (Exception e) {
                                                logger.warn("Instance " + instance.blocko_instance_name + "  failed to upload properly on the cloud_blocko_server Blocko");
                                            }
                                        }
                                    }

                                    List<Homer_Instance> instances_for_reading = Homer_Instance.find.where().in("blocko_instance_name", instancis_for_tyrion_memory).findList();
                                    if (!instances_for_reading.isEmpty()) {

                                        logger.debug("Blocko Server: Starting reading instnces to Tyrion Memory");

                                        for (Homer_Instance instance : instances_for_reading) {
                                            try {

                                                logger.debug("Instance: " + instance.blocko_instance_name + " will be read to Tyrion Memmory");
                                                WebSocketController_Incoming.blocko_server_add_instance(server, instance, false);

                                            } catch (Exception e) {
                                                logger.warn("Instance " + instance.blocko_instance_name + "  failed to upload properly on the cloud_blocko_server Blocko");
                                            }
                                        }
                                    }

                                    if(!instances_for_removing_from_homer.isEmpty()) {

                                        logger.debug("Blocko Server: Starting to remove the cloud_blocko_server instance ");

                                        for (String blocko_instance_name : instances_for_removing_from_homer) {
                                            try {

                                                JsonNode remove_instance = blocko_server_remove_instance(server, blocko_instance_name);
                                                if (remove_instance.get("status").asText().equals("error")) {
                                                    logger.debug("Blocko Server: Fail when Tyrion try to remove instance from Blocko cloud_blocko_server");
                                                }
                                            } catch (Exception e) {
                                                logger.error("Blocko Server: Eception wher Tyrion try to remove instance from Blocko cloud_blocko_server", e);
                                            }
                                        }
                                    }

                                    logger.debug("Blocko Server: Successfully finished connection procedure");
                                    interrupter = (long) 0;




                                }else throw new Exception("Result hasn't status in Json");
                            }
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }

            logger.debug("Blocko Server: Starting connection control procedure");
            new Control_Blocko_Server_Thread().start();

            logger.debug("Blocko Server: Succesfuly connected");
            return webSocket;

        }catch (Exception e){
            logger.error("Blocko Server: Fail connection");
            logger.error("Something was wrong", e);
            return WebSocket.reject(forbidden());
        }
    }
    @ApiOperation(value = "Compilation Server Conection", tags = {"WebSocket"})
    public  WebSocket<String>  compilator_server_connection (String server_name){
        try{
            logger.debug("Compilation cloud_blocko_server is connecting. Server: " + server_name);

            logger.debug("Control Server and its unique names!"); // TODO - přidat ověření ještě pomocí HASHe co už je v objektu definován
            Cloud_Compilation_Server cloud_compilation_server = Cloud_Compilation_Server.find.where().eq("server_name", server_name).findUnique();
            if(cloud_compilation_server == null) return WebSocket.reject(forbidden("Server side error - unrecognized name"));

            if(compiler_cloud_servers.containsKey(server_name)) {
                logger.debug("At Tyrion is already connected cloud_blocko_server compilation of the same name - will not allow another connection");
                return WebSocket.reject(forbidden("Server side error - already connected"));
            }

            // Inicializuji Websocket pro Homera
            WS_CompilerServer server = new WS_CompilerServer( cloud_compilation_server.server_name, cloud_compilation_server.destination_address, compiler_cloud_servers );

            // Připojím se
            logger.debug("Compiling cloud_blocko_server connect");
            return server.connection();

        }catch (Exception e){
            logger.error("Cloud Compiler Server Web Socket connection", e);
            return WebSocket.reject(forbidden("Server side error"));
        }
    }
    @ApiOperation(value = "FrontEnd Becki Connection", tags = {"WebSocket"})
    public  WebSocket<String>  becki_website_connection (String security_token){
        try{

            logger.debug("Becki: Incoming connection: " + security_token);

            String person_id = tokenCache.get(security_token);
            tokenCache.remove(security_token);

            if(person_id == null ) {
                logger.warn("Becki: Incoming token " + security_token + " is invalid! Probably to late for axcess");
                return WebSocket.reject(forbidden());
            }


            logger.debug("Becki: Controlling of incoming token " + security_token);
            Person person = Person.find.byId(person_id);
            if(person == null){
                logger.warn("Person with this id not exist!");
                return WebSocket.reject(forbidden());
            }

            WS_Becki_Website website;

            if(becki_website.containsKey(person.id)) {
                website = (WS_Becki_Website) becki_website.get(person.id);
            }else{
                website = new WS_Becki_Website(person);
                becki_website.put(person.id , website);
            }

            logger.warn("Becki: Check if token is connected already");
            if(website.all_person_Connections != null && website.all_person_Connections.containsKey(security_token)) return WebSocket.reject(forbidden());
            WS_Becki_Single_Connection website_connection = new WS_Becki_Single_Connection(security_token, website);

            logger.warn("Becki: Connection successfully");
            return website_connection.connection();

        }catch (Exception e){
            e.printStackTrace();
            Loggy.error("Cloud Compiler Server Web Socket connection", e);
            return WebSocket.reject(forbidden("Server side error"));
        }

    }

// PRIVATE Homer-Server ---------------------------------------------------------------------------------------------------------
    public static void blocko_server_incoming_message(WS_BlockoServer blockoServer, ObjectNode json){

    logger.debug("BlockoServer: "+ blockoServer.identifikator + " Incoming message: " + json.toString());

    if(json.has("messageChannel")){

        switch (json.get("messageChannel").asText()){

            case "homer-server" : {

                switch (json.get("messageType").asText()){

                    case "yodaDisconnected" : {
                        logger.debug("yodaDisconnected");
                        blocko_server_yodaDisconnected(blockoServer,json);
                        return;
                    }
                    case "yodaConnected" : {
                        logger.debug("yodaConnected");
                        blocko_server_yodaConnected(blockoServer,json);
                        return;
                    }
                    default: {
                        logger.error("ERROR");
                        logger.error("Blocko Server Incoming messageChanel homer-server not recognize messageType ->" + json.get("messageType").asText());
                        logger.error("ERROR");
                    }

                }
            }
            default: {
                logger.error("ERROR");
                logger.error("Blocko Server Incoming message not recognize incoming messageChanel!!! ->" + json.get("messageChannel").asText());
                logger.error("ERROR");
            }

        }
    }else {
        logger.error("ERROR");
        logger.error("Homer: "+ blockoServer.identifikator + " Incoming message has not messageChannel!!!!");
        logger.error("ERROR");
    }
}

    public static JsonNode blocko_server_listOfInstance(WS_BlockoServer blockoServer)  throws ExecutionException, TimeoutException, InterruptedException{

        logger.debug("Tyrion: Server want know instances on: " + blockoServer.identifikator);

        ObjectNode result = Json.newObject();
        result.put("messageType", "listInstances");
        result.put("messageChannel", "homer-server");

        return blockoServer.write_with_confirmation(result, 1000*4, 0, 3);
    }

    public static JsonNode blocko_server_isInstanceExist(WS_BlockoServer blockoServer, String instance_name)  throws ExecutionException, TimeoutException, InterruptedException{


        ObjectNode result = Json.newObject();
        result.put("messageType", "instanceExist");
        result.put("messageChannel", "homer-server");
        result.put("instanceId", instance_name);

        return blockoServer.write_with_confirmation(result, 1000*4, 0, 3);
    }

    public static WebSCType blocko_server_add_instance(WS_BlockoServer blockoServer, Homer_Instance instance, boolean with_uploud_to_server) throws Exception{

            System.out.println("\n \n");
            logger.debug("Tyrion uploud new instance to cloud_blocko_server" + blockoServer.identifikator);

            if (WebSocketController_Incoming.incomingConnections_homers.containsKey(instance.blocko_instance_name)) {

                logger.warn("Při přidávání instance do serveru: " + blockoServer.identifikator + " bylo zjištěno že v mapě už existuje jméno homera");
                return WebSocketController_Incoming.incomingConnections_homers.get(instance.blocko_instance_name);
            }

            logger.debug("Creating new  Homer");
            WS_Homer_Cloud homer = new WS_Homer_Cloud(instance.blocko_instance_name, instance.version_object != null ? instance.version_object.id : "null" , blockoServer);

            ObjectNode result = Json.newObject();
            result.put("messageType", "createInstance");
            result.put("messageChannel", "homer-server");
            result.put("instanceId", instance.blocko_instance_name);

            List<Swagger_Instance_HW_Group> hw_groups = new ArrayList<>();
            for(B_Program_Hw_Group b_program_hw_group : instance.version_object.b_program_hw_groups){

               if(b_program_hw_group.main_board_pair != null ){
                   Swagger_Instance_HW_Group group = new Swagger_Instance_HW_Group();
                   group.yodaId = (b_program_hw_group.main_board_pair.board.id);

                   for(B_Pair pair : b_program_hw_group.device_board_pairs){
                       group.devicesId.add(pair.board.id);
                   }
                   hw_groups.add(group);
               }
            }

            homer.group = hw_groups;

            result.set("devices", Json.toJson(hw_groups));

            if(with_uploud_to_server) {
                logger.debug("Sending to cloud_blocko_server request for new instance ");
                JsonNode result_instance = blockoServer.write_with_confirmation(result, 1000*5, 0, 3);


                logger.debug("Sending Blocko Program if Exist");
                if( instance.version_object != null) {

                    FileRecord fileRecord = FileRecord.find.where().eq("version_object.id", instance.version_object.id ).eq("file_name","program.js").findUnique();
                    if(fileRecord != null) {
                        JsonNode result_uploud = WebSocketController_Incoming.homer_upload_program(homer, instance.id, fileRecord.get_fileRecord_from_Azure_inString());
                    }else {
                        logger.warn("File program.js under server: " + blockoServer.identifikator + " instnace " + instance.id + " not found!" );
                    }
                }else {
                    logger.debug("Blocko Version not exist under Homer_Instance");
                }
            }

            logger.debug("Adding a new virtual Homer in private cloud_blocko_server maps in Controller");
            blockoServer.virtual_homers.put(instance.blocko_instance_name, homer);

            logger.debug("Initiating connection procedures");
            homer_connection_procedure(homer);

            incomingConnections_homers.put(homer.identifikator, homer);

            return homer;

    }

    public static JsonNode blocko_server_update_instance(WebSCType homer, Homer_Instance instance) throws Exception{

        logger.debug("Tyrion update instance on Homer" + homer.identifikator);


        WS_Homer_Cloud cloud_homer = (WS_Homer_Cloud) homer;

        logger.debug("Upravuji Version Id in Homer");
        if(instance.version_object != null) cloud_homer.version_id = instance.version_object.id;


        logger.debug("Sending Blocko Program if Exist");

        JsonNode result_uploud = WebSocketController_Incoming.homer_upload_program(homer, instance.id, instance.version_object.files.get(0).get_fileRecord_from_Azure_inString());

        if(result_uploud.get("status").asText().equals("success")){

            logger.debug("Uploading of new program to instance was successful");
         //   homer_connection_procedure(homer); - Nejsem si jist jestli není potřeba nějak updatovat a upozornit zařízení že se třeba změnila rozložení pro grid!
            return result_uploud;

        }else {
            logger.warn("Uploading of new program to instance wasnt successful!!");
            return result_uploud;
        }
    }

    public static JsonNode blocko_server_remove_instance( WS_BlockoServer blockoServer, String instance_name) throws  ExecutionException,  TimeoutException, InterruptedException{

        ObjectNode result = Json.newObject();
        result.put("messageType", "destroyInstance");
        result.put("messageChannel", "homer-server");
        result.put("instanceId", instance_name);

        return blockoServer.write_with_confirmation(result, 1000*5, 0, 3);

    }

    public static void blocko_server_ping(WS_BlockoServer blockoServer){

        ObjectNode result = Json.newObject();
        result.put("messageType", "ping");
        result.put("messageChannel", "homer-server");

        blockoServer.write_without_confirmation( result);
    }

    public static void blocko_server_unregistered_board_are_connected(WS_BlockoServer blockoServer, String macAddress){

        ObjectNode result = Json.newObject();
        result.put("messageType", "unregisteredHardware");
        result.put("messageChannel", "homer-server");
        result.put("macAddress", macAddress);

        blockoServer.write_without_confirmation(result);

    }

    public static void blocko_server_yodaConnected(WS_BlockoServer blockoServer, ObjectNode json){
        try {
            if(json.has("deviceId")) {

                Board board = Board.find.byId(json.get("deviceId").asText());

                if(board == null){
                    logger.warn("WARN! WARN! WARN! WARN!");
                    logger.warn("Unregistered Hardware connected to Blocko cloud_blocko_server - " + blockoServer.identifikator);
                    logger.warn("Unregistered Hardware: " +  json.get("macAddress").asText() );
                    logger.warn("WARN! WARN! WARN! WARN!");

                    blocko_server_unregistered_board_are_connected(blockoServer, json.get("deviceId").asText());
                    return;
                }

                logger.debug("Board connected to Blocko cloud_blocko_server");

                if(board.latest_know_server == null){

                    logger.debug("The Board is not yet matched the Server");
                    Cloud_Homer_Server server = Cloud_Homer_Server.find.where().eq("server_name", blockoServer.identifikator).findUnique();

                    if(server == null) {
                        logger.error("blocko_server_yodaConnected => cloud_blocko_server not exist!!!!");
                        return;
                    }


                    server.boards.add(board);
                    server.refresh();
                    server.update();

                    board.refresh();
                    board.latest_know_server = server;
                    board.isActive = true;
                    board.update();
                }

                List<Person> persons = Person.find.where().eq("owningProjects.boards.id", board.id).findList();
                for(Person person : persons){

                    if( becki_website.containsKey(person.id)) {
                        NotificationController.board_connect(person, board);
                    }

                }

                ActualizationController.hardware_connected(board);


            }else {
                logger.error("Incoming message: Yoda Connected: has not macAddress!!!!");
            }
        }catch (Exception e){
            logger.error("Blocko Server - Yoda Connected ERROR", e);
        }
    }

    public static void blocko_server_yodaDisconnected(WS_BlockoServer blockoServer, ObjectNode json){
        try {

            if(json.has("deviceId")) {

                Board board = Board.find.byId(json.get("deviceId").asText());
                if(board == null){
                    logger.warn("WARN! WARN! WARN! WARN!");
                    logger.warn("Unregistered Hardware disconnected to Blocko cloud_blocko_server - " + blockoServer.identifikator);
                    logger.warn("Unregistered Hardware: " +  json.get("deviceId").asText() );
                    logger.warn("WARN! WARN! WARN! WARN!");
                    return;
                }

                List<Person> persons = Person.find.where().eq("owningProjects.boards.id", board.id).findList();
                for(Person person : persons){
                    if( becki_website.containsKey(person.id)) {
                        NotificationController.board_disconnect(person, board);
                    }
                }

                ActualizationController.hardware_disconnected(board);

            }else {
                logger.error("Incoming message: Yoda Disconnected: has not deviceId!!!!");
            }
        }catch (Exception e){
            logger.error("Blocko Server - Yoda Connected ERROR", e);
        }
    }

    public static void blocko_server_is_disconnect(WS_BlockoServer blockoServer){
        logger.debug("Tyrion lost connection with blocko cloud_blocko_server: " + blockoServer.identifikator);
        blocko_servers.remove(blockoServer.identifikator);
    }

// PRIVATE Compiler-Server --------------------------------------------------------------------------------------------------------

    public static void compilation_server_incoming_message(WS_CompilerServer server, ObjectNode json){
        logger.warn("Speciálně  server2server přišla zpráva: " + json.asText() );
        logger.warn("Zatím není implementovaná žádná reakce na příchozá zprávu z Compilačního serveru !!");
    }

    // Vytvoř kompilaci
        public static JsonNode compiler_server_make_Compilation(Person compilator, ObjectNode request) throws  ExecutionException, TimeoutException, InterruptedException {

        List<String> keys      = new ArrayList<>(compiler_cloud_servers.keySet());
        WS_CompilerServer server = (WS_CompilerServer) compiler_cloud_servers.get( keys.get( new Random().nextInt(keys.size())) );


        ObjectNode compilation_request = server.write_with_confirmation(request, 1000*5, 0, 3);


        if(!compilation_request.get("status").asText().equals("success")) {

            logger.debug("Incoming message has not contains state = success");

            ObjectNode error_result = Json.newObject();
            error_result.put("error", "Something was wrong");
            return  error_result;
        }

        logger.debug("Start of compilation was successful - waiting for result");

        SendMessage get_compilation = new SendMessage(null, null, null, "compilation_message", 1000 * 35, 0, 1);
        server.sendMessageMap.put( compilation_request.get("buildId").asText(), get_compilation);
        ObjectNode result = get_compilation.send_with_response();
            result.set("interface",compilation_request.get("interface") ); // Přiřadím interface do zprávy
        return result;

    }

    // Ping
        public static void compiler_server_ping(WS_CompilerServer compilerServer) throws  ExecutionException, TimeoutException, InterruptedException {

        ObjectNode result = Json.newObject();
        result.put("messageType", "ping");
        result.put("messageChannel", "tyrion");

        compilerServer.write_without_confirmation(result);
    }

    // Reakce na odhlášení compilačního serveru
        public static void compiler_server_is_disconnect(WS_CompilerServer compilerServer)  {
        logger.debug("Connection lost with compilation cloud_blocko_server!: " + compilerServer.identifikator);
        compiler_cloud_servers.remove(compilerServer.identifikator);
    }

// PRIVATE Becki -----------------------------------------------------------------------------------------------------------------

    public static void becki_incoming_message(WS_Becki_Website becki, ObjectNode json){

        logger.debug("Becki: " + becki.identifikator + " Incoming message: " + json.toString() );

        if(json.has("messageChannel")) {

            switch (json.get("messageChannel").asText()) {

                case "becki": {
                    switch (json.get("messageType").asText()) {

                        case "notification"             :   {  becki_notification_confirmation_from_becki(becki, json); return;}
                        case "subscribe_notification"   :   {  becki_subscribe_notification(becki, json);               return;}
                        case "unsubscribe_notification" :   {  becki_unsubscribe_notification(becki, json);             return;}
                        case "subscribe_instance"       :   {  becki_subscribe_instance(becki, json);                   return;}
                        case "unsubscribe_instance"     :   {  becki_unsubscribe_instance(becki, json);                 return;}


                        default: {
                            logger.error("ERROR \n");
                            logger.error("Becki: "+ becki.identifikator + " Incoming message on messageChannel \"becki\" has not unknown messageType!!!!");
                            logger.error("ERROR \n");
                        }

                    }
                }
                case "tyrion": {
                    logger.warn("Homer: Incoming message: Tyrion: Server receive message: ");
                    logger.warn("Homer: Incoming message: Tyrion: Server don't know what to do!");
                    return;
                }

                default: {
                    // Přepošlu to na všehcny odběratele Becki
                    if (becki.subscribers_becki != null && !becki.subscribers_becki.isEmpty()) {
                        for (WebSCType ws : becki.subscribers_becki) {
                            ws.write_without_confirmation(json);
                        }
                    }
                }

            }

        }else {
            logger.error("ERROR \n");
            logger.error("Becki: "+ becki.identifikator + " Incoming message has not messageChannel!!!!");
            logger.error("ERROR \n");
        }
    }

    // Odebírání streamu notifikací z Tytiona
        public static void becki_subscribe_notification (WS_Becki_Website becki, ObjectNode json){
            try {

                WS_Becki_Single_Connection single_connection = (WS_Becki_Single_Connection) becki.all_person_Connections.get(json.get("single_connection_token").asText());
                single_connection.notification_subscriber = true;

                becki_approve_subscription_notification_success(single_connection, json.get("messageId").asText());

            }catch (Exception e){
                logger.error("becki_subscribe_notification", e);
            }

        }

        public static void becki_unsubscribe_notification (WS_Becki_Website becki, ObjectNode json){
            try{

                WS_Becki_Single_Connection single_connection = (WS_Becki_Single_Connection) becki.all_person_Connections.get( json.get("single_connection_token").asText());
                single_connection.notification_subscriber = true;

                becki_approve_unsubscription_notification_success(single_connection, json.get("messageId").asText() );

            }catch (Exception e){
                logger.error("becki_unsubscribe_notification", e);
            }
        }

            // Json Messages
            public static void becki_approve_subscription_notification_success(WS_Becki_Single_Connection becki, String messageId){
                ObjectNode result = Json.newObject();
                result.put("messageType", "subscribe_notification");
                result.put("messageChannel", "becki");
                result.put("status", "success");

                becki.write_without_confirmation( messageId, result);
            }

            public static void becki_approve_unsubscription_notification_success(WS_Becki_Single_Connection becki, String messageId){
                ObjectNode result = Json.newObject();
                result.put("messageType", "unsubscribe_notification");
                result.put("messageChannel", "becki");
                result.put("status", "success");

                becki.write_without_confirmation( messageId, result);
            }

            public static void becki_sendNotification(WS_Becki_Website becki, Notification notification){

                ObjectNode result = Json.newObject();
                result.put("messageType", "notification");
                result.put("messageChannel", "becki");
                result.put("id", notification.id);
                result.put("notification_level",   notification.notification_level.name());
                result.put("notification_importance", notification.notification_importance.name());
                result.put("confirmation_required", notification.confirmation_required);
                result.set("notification_body", Json.toJson(notification.notification_body()));
                result.put("was_read", notification.was_read);
                result.put("created", notification.created.getTime());

                for(String person_connection_token : becki.all_person_Connections.keySet()){
                    WS_Becki_Single_Connection single_connection =  (WS_Becki_Single_Connection) becki.all_person_Connections.get(person_connection_token);
                    if(single_connection.notification_subscriber) single_connection.write_without_confirmation(result);
                }

            }

            public static void becki_notification_confirmation_from_becki(WS_Becki_Website becki, JsonNode json){

            }

    // Ping
        public static void becki_ping(WebSCType webSCType) throws TimeoutException, InterruptedException {
 
        ObjectNode result = Json.newObject();
        result.put("messageType", "ping");
        result.put("messageChannel", "tyrion");

        webSCType.write_without_confirmation(result);
    }

    // Odebírání streamu aktualizací z Homer instance
        public static void becki_subscribe_instance(WS_Becki_Website becki, ObjectNode json){

            try {
                String version_id = json.get("version_id").asText();
                System.out.println("Ze serveru budu chtít dostávat všechny informace z blocko serveru na verzi" + version_id);

                // Najdu Version
                Version_Object version = Version_Object.find.byId(version_id);
                if (version == null) {
                    becki_disapprove_subscription_instance_state(becki, json.get("messageId").asText(), "Version not Exist");
                    return;
                }

                // Zjistím kde běží
                if (!blocko_servers.containsKey(version.homer_instance.cloud_homer_server.server_name)) {
                    becki_disapprove_subscription_instance_state(becki, json.get("messageId").asText(), "Server is not connected");
                    return;
                }

                WS_BlockoServer server = (WS_BlockoServer) blocko_servers.get(version.homer_instance.cloud_homer_server.server_name);

                // Zjistit jestli tam instance opravdu běží
                JsonNode result_instance = blocko_server_isInstanceExist(server, version.homer_instance.blocko_instance_name);
                if(result_instance.get("status").asText().equals("error"))  {
                    becki_disapprove_subscription_instance_state(becki, json.get("messageId").asText(), result_instance.get("error").asText());
                    return;
                }
                // Zjistím jestli existuje instnace
                if(!result_instance.get("exist").booleanValue())    {
                    becki_disapprove_subscription_instance_state(becki, json.get("messageId").asText(), "Instance of this version not running on this cloud_blocko_server!");
                    return;
                }

                // Zjistím jestli existuje virtuální homer
                System.out.println("Zjištuji jestli existuje virtuální homer");
                if(!incomingConnections_homers.containsKey(version.homer_instance.blocko_instance_name) ) {
                    System.out.println("Virtuální Homer neexistuje!!!");
                    becki_disapprove_subscription_instance_state(becki, json.get("messageId").asText(), "FATAL ERROR!!! Virtual Homer for this instance not exist!");
                    return;
                }

                WebSCType homer = incomingConnections_homers.get(version.homer_instance.blocko_instance_name);

                // 2 - Požádat Homera o zasílání informací
                JsonNode result_recive = homer_subscribe_blocko_instance(homer);
                if(result_recive.get("status").textValue().equals("error")) becki_disapprove_subscription_instance_state(becki, json.get("messageId").asText(), result_recive.get("error").textValue());

                // 1 - navázat propojení mezi instanci Homera a instancí Becki
                homer.subscribers_becki.add(becki);
                becki.subscribers_becki.add(homer);


                becki_approve_subscription_instance_state( becki, json.get("messageId").asText() );

            }catch (Exception e){
                e.printStackTrace();
                becki_disapprove_subscription_instance_state(becki, json.get("messageId").asText(), "Unknow Error");
            }
        }

        public static void becki_unsubscribe_instance(WS_Becki_Website becki, ObjectNode json){
            try{

                String version_id = json.get("version_id").asText();
                System.out.println("Becki nechce nadále příjmat požadavky z instance" + version_id);

                // Najdu Version
                Version_Object version = Version_Object.find.byId(version_id);
                if (version == null) {
                    becki_disapprove_un_subscription_instance_state(becki, json.get("messageId").asText(), "Version not Exist");
                    return;
                }

                // Zjistím kde běží
                if (!blocko_servers.containsKey(version.homer_instance.cloud_homer_server.server_name)) {
                    becki_disapprove_un_subscription_instance_state(becki, json.get("messageId").asText(), "Server is not connected");
                    return;
                }

                WS_BlockoServer server = (WS_BlockoServer) blocko_servers.get(version.homer_instance.cloud_homer_server.server_name);

                // Zjistit jestli tam instance opravdu běží
                JsonNode result_instance = blocko_server_isInstanceExist(server, version.homer_instance.blocko_instance_name);
                if(result_instance.get("status").asText().equals("error"))  {
                    becki_disapprove_un_subscription_instance_state(becki, json.get("messageId").asText(), result_instance.get("error").asText());
                    return;
                }
                // Zjistím jestli existuje instnace
                if(!result_instance.get("exist").booleanValue())    {
                    becki_disapprove_un_subscription_instance_state(becki, json.get("messageId").asText(), "Instance of this version not running on this cloud_blocko_server!");
                    return;
                }

                // Zjistím jestli existuje virtuální homer
                System.out.println("Zjištuji jestli existuje virtuální homer");
                if(!incomingConnections_homers.containsKey(version.homer_instance.blocko_instance_name) ) {
                    System.out.println("Virtuální Homer neexistuje!!!");
                    becki_disapprove_un_subscription_instance_state(becki, json.get("messageId").asText(), "FATAL ERROR!!! Virtual Homer for this instance not exist!");
                    return;
                }

                WebSCType homer = incomingConnections_homers.get(version.homer_instance.blocko_instance_name);

                if(! becki.subscribers_becki.contains( homer) ) becki_disapprove_un_subscription_instance_state(becki, json.get("messageId").asText(), "Homer is not listening Becki");


                // 1 - navázat propojení mezi instanci Homera a instancí Becki
                homer.subscribers_becki.remove(becki);
                becki.subscribers_becki.remove(homer);

                becki_approve_un_subscription_instance_state( becki, json.get("messageId").asText());


            }catch (Exception e){
                e.printStackTrace();
                becki_disapprove_un_subscription_instance_state(becki, json.get("messageId").asText(), "Unknow Error");
            }
        }

            // Json Messages
            public static void becki_approve_subscription_instance_state(WS_Becki_Website becki, String messageId){

                ObjectNode result = Json.newObject();
                result.put("messageType", "subscribe_instance");
                result.put("messageChannel", "blocko");
                result.put("status", "success");

                becki.write_without_confirmation( messageId, result);
            }

            public static void becki_approve_un_subscription_instance_state(WS_Becki_Website becki, String messageId){

                ObjectNode result = Json.newObject();
                result.put("messageType", "unsubscribe_instance");
                result.put("messageChannel", "blocko");
                result.put("status", "success");

                becki.write_without_confirmation( messageId, result);
            }

            public static void becki_disapprove_subscription_instance_state(WS_Becki_Website becki, String messageId, String error){

                ObjectNode result = Json.newObject();
                result.put("messageType", "subscribe_instace");
                result.put("messageChannel", "blocko");
                result.put("status", "error");
                result.put("error", error);
                becki.write_without_confirmation(messageId, result);

            }

            public static void becki_disapprove_un_subscription_instance_state(WS_Becki_Website becki, String messageId, String error){

                ObjectNode result = Json.newObject();
                result.put("messageType", "unsubscribe_instace");
                result.put("messageChannel", "blocko");
                result.put("status", "error");
                result.put("error", error);
                becki.write_without_confirmation(messageId, result);

            }

    // Reakce na odhlášení blocka
        public static void becki_disconnect(WebSCType webSCType){
        System.out.println("Becki se mi odpojitlo");

    }


// PRIVATE Homer -----------------------------------------------------------------------------------------------------------------

    public static void homer_incoming_message(WebSCType homer, ObjectNode json){

        logger.debug("Homer: "+ homer.identifikator + " Incoming message: " + json.toString());

        if(json.has("messageChannel")){

            switch (json.get("messageChannel").asText()){

                case "the-grid" : {
                    logger.debug("Homer: Incoming message: the-grid: Server send data to all connected terminals");
                    if(homer.subscribers_grid != null && !homer.subscribers_grid.isEmpty()) for( WebSCType webSCType : homer.subscribers_grid) webSCType.write_without_confirmation(json);
                    return;
                }

                case "tyrion" : {
                    logger.warn("Homer: Incoming message: tyrion: Server receive message: ");
                    logger.warn("Homer: Incoming message: tyrion: Server don't know what to do!");
                    return;
                }

                case "becki" : {
                    logger.debug("Homer: Incoming message: becki: Server send data to all connected browsers");

                    if(homer.subscribers_becki == null || homer.subscribers_becki.isEmpty() ){
                        logger.debug("Homer: Incoming message: becki: But we have no Becki connected");
                        return;
                    }

                    for( WebSCType webSCType : homer.subscribers_becki) {
                        webSCType.write_without_confirmation(json);
                    }
                    return;
                }

                case "homer-server" : {

                    switch (json.get("messageType").asText()){

                        case "yodaDisconnected" : {
                            WS_Homer_Cloud homer_cloud = (WS_Homer_Cloud) homer;

                            logger.debug("yodaDisconnected");
                            blocko_server_yodaDisconnected(homer_cloud.blockoServer ,json);
                            return;
                        }
                        case "yodaConnected" : {
                            WS_Homer_Cloud homer_cloud = (WS_Homer_Cloud) homer;

                            logger.debug("yodaConnected");
                            blocko_server_yodaConnected(homer_cloud.blockoServer ,json);
                            return;
                        }
                    }
                }

                default: {
                    logger.error("ERROR \n");
                    logger.error("Homer: Incoming message: Tyrion don't recognize incoming messageChanel!!!");
                    logger.error("ERROR \n");
                }

            }
        }else {
            logger.error("ERROR \n");
            logger.error("Homer: "+ homer.identifikator + " Incoming message has not messageChannel!!!!");
            logger.error("ERROR \n");
        }
    }

    public static JsonNode homer_getState(WebSCType homer) throws  ExecutionException, TimeoutException, InterruptedException {

        logger.debug("Tyrion: want to know state of Homer: " + homer.identifikator);

        ObjectNode result = Json.newObject();
        result.put("messageType", "getState");
        result.put("messageChannel", "tyrion");

        return homer.write_with_confirmation(result, 1000*3, 0, 4);
    }

    public static void homer_ping(WebSCType homer) throws  ExecutionException, TimeoutException, InterruptedException {

        logger.debug("Tyrion: Server want send ping to Homer: " + homer.identifikator);

        ObjectNode result = Json.newObject();
        result.put("messageType", "ping");
        result.put("messageChannel", "homer-server");

        homer.write_without_confirmation(result);

    }

    public static JsonNode homer_devices_commands(WebSCType homer, String targetId, TypeOfCommand command) throws  ExecutionException, TimeoutException, InterruptedException {

        logger.debug("Tyrion: Server want send command to Devices in Homer Instance: " + homer.identifikator);

        ObjectNode result = Json.newObject();
        result.put("messageChannel", "tyrion");
        result.put("messageType", "basicCommand");
        result.put("commandType", command.get_command());
        result.put("targetId", targetId);

        return  homer.write_with_confirmation(result, 1000*10, 0, 4);

    }

    public static JsonNode homer_add_Yoda_to_instance(WebSCType homer, String yoda_id) throws  ExecutionException, TimeoutException, InterruptedException{

        ObjectNode request = Json.newObject();
        request.put("messageType", "addYodaToInstance");
        request.put("messageChannel", "tyrion");
        request.put("yodaId", yoda_id);

        JsonNode result = homer.write_with_confirmation(request, 1000*3, 0, 4);
        if(result.has("status") && result.get("status").asText().equals("success")){

            WS_Homer_Cloud help = (WS_Homer_Cloud) homer;

            Swagger_Instance_HW_Group group = new Swagger_Instance_HW_Group();
            group.yodaId = yoda_id;
            help.group.add(group);
        }

       return result;
    }

    public static JsonNode homer_remove_Yoda_from_instance(WebSCType homer, String yoda_id) throws  ExecutionException, TimeoutException, InterruptedException{

        ObjectNode request = Json.newObject();
        request.put("messageType", "removeYodaFromInstance");
        request.put("messageChannel", "tyrion");
        request.put("yodaId", yoda_id);

        JsonNode result = homer.write_with_confirmation(request, 1000*3, 0, 4);
        if(result.has("status") && result.get("status").asText().equals("success")){

            WS_Homer_Cloud help = (WS_Homer_Cloud) homer;

            for(Swagger_Instance_HW_Group group : help.group){

                if(group.yodaId.equals(yoda_id)) {
                    help.group.remove(group);
                    break;
                }

            }
        }

        return request;
    }

    public static JsonNode homer_add_Device_to_instance(WebSCType homer, String yoda_id, String device_id) throws  ExecutionException, TimeoutException, InterruptedException{

        ObjectNode result = Json.newObject();
        result.put("messageType", "addDeviceToInstance");
        result.put("messageChannel", "tyrion");
        result.put("yodaId", yoda_id);
        result.put("deviceId", device_id);

        return homer.write_with_confirmation(result, 1000*3, 0, 4);
    }

    public static JsonNode homer_remove_Device_from_instance(WebSCType homer, String yoda_id, String device_id) throws  ExecutionException, TimeoutException, InterruptedException{

        ObjectNode result = Json.newObject();
        result.put("messageType", "removeDeviceFromInstance");
        result.put("messageChannel", "tyrion");
        result.put("yodaId", yoda_id);
        result.put("deviceId", device_id);

        return homer.write_with_confirmation(result, 1000*3, 0, 4);
    }




    public static JsonNode homer_upload_program(WebSCType homer, String program_id, String program) throws  ExecutionException, TimeoutException, InterruptedException {

            ObjectNode result = Json.newObject();
            result.put("messageType", "loadProgram");
            result.put("messageChannel", "tyrion");
            result.put("programId", program_id);
            result.put("program", program);

          return homer.write_with_confirmation(result, 1000*3, 0, 4);
    }

    public static boolean homer_online_state(String homer_name){
        return incomingConnections_homers.containsKey(homer_name);
    }

    public static JsonNode homer_get_device_list(WebSCType homer) throws  ExecutionException, TimeoutException, InterruptedException{

        ObjectNode result = Json.newObject();
        result.put("messageType", "getDeviceList");
        result.put("messageChannel", "tyrion");

        return homer.write_with_confirmation(result, 1000*3, 0, 4);
    }

    public static void homer_is_disconnect(WebSCType homer) {

        logger.debug("Lost connection with Homer: " + homer.identifikator + " deleting that from connection map and add to lost connection map");
        incomingConnections_homers.remove(homer.identifikator);

        ArrayList<String> list = new ArrayList<>();

        for (WebSCType terminal : homer.subscribers_grid) {
            list.add(terminal.identifikator);
            terminal.subscribers_grid.remove(homer);
            // Informuji že jsem ztratil spojení
            terminal_echo_that_home_was_disconnect(terminal);
        }

        terminal_lost_connection_homer.put(homer.identifikator, list);

        for (WebSCType becki : homer.subscribers_becki) {
            becki.subscribers_becki.remove(homer);
            homer.subscribers_becki.remove(becki);

            if(becki_lost_connection_homer.containsKey(homer.identifikator)) {
                becki_lost_connection_homer.get(homer.identifikator).add(becki.identifikator);
            }else{
                ArrayList<String> ws_list = new ArrayList<>();
                ws_list.add(becki.identifikator);
                becki_lost_connection_homer.put(homer.identifikator, ws_list);
            }
            terminal_echo_that_home_was_disconnect(becki);
        }



    }

    public static void homer_all_terminals_are_gone(WebSCType homer) throws  ExecutionException, TimeoutException, InterruptedException {

        logger.debug("Homer: " + homer.identifikator + "  does not have any subscribers:");

        ObjectNode result = Json.newObject();
        result.put("messageType", "unSubscribeChannel");
        result.put("messageChannel", "the-grid");

        homer.write_with_confirmation(result, 1000*3, 0, 4);
    }

    public static void ask_for_receiving_for_Grid(WebSCType homer) throws  ExecutionException, TimeoutException, InterruptedException {

        logger.debug("Homer: " + homer.identifikator + ", cloud_blocko_server want send you request for receiving for Grid:");

        ObjectNode result = Json.newObject();
        result.put("messageType", "subscribeChannel");
        result.put("messageChannel", "the-grid");

        homer.write_with_confirmation(result, 1000*3, 0, 4);
    }

    // Json Messages - Homer Odběr dat z Instaní blocka pro Becki
        public static JsonNode homer_subscribe_blocko_instance(WebSCType homer) throws  ExecutionException, TimeoutException, InterruptedException {

            ObjectNode result = Json.newObject();
            result.put("messageType", "subscribeChannel");
            result.put("messageChannel", "becki");
            result.put("instanceId", homer.identifikator);

            return homer.write_with_confirmation(result, 1000*3, 0, 4);
        }

        public static void     homer_unsubscribe_blocko_instance(WS_Homer_Cloud homer){

            ObjectNode result = Json.newObject();
            result.put("messageType",    "unSubscribeChannel");
            result.put("messageChannel", "becki");
            result.put("instanceId",     homer.identifikator);

            homer.write_without_confirmation(result);
        }

    public static JsonNode homer_update_Yoda_firmware(WebSCType homer, String targetId, Firmware_type firmware_type, String code) throws  ExecutionException, TimeoutException, InterruptedException {

        logger.debug("Homer: " + homer.identifikator + ", will update Yoda");

        ObjectNode result = Json.newObject();
        result.put("messageType", "updateDevice");
        result.put("firmwareType", firmware_type.get_firmwareType());
        result.put("targetId",   targetId);
        result.put("messageChannel", "tyrion");
        result.put("program", code );

        return homer.write_with_confirmation(result, 1000*15, 0, 3);
    }

    public static JsonNode homer_update_embeddedHW(WebSCType homer, List<String> board_id_list, String string_code) throws  ExecutionException, TimeoutException, InterruptedException, IOException {

        logger.debug("Tyrion: Sending to Hardware new Compilation of code");

        ObjectNode result = Json.newObject();
        result.put("messageType", "updateDevice");
        result.set("hardwareId", Json.toJson(board_id_list));
        result.put("base64Binary", string_code);

        return homer.write_with_confirmation(result, 1000*3, 0, 4);
    }

// PRIVATE Terminal ---------------------------------------------------------------------------------------------------------

    public static void terminal_incoming_message(WebSCType terminal, ObjectNode json){

        logger.debug("Terminal: "+ terminal.identifikator + " Incoming message: " + json.toString());

        if(json.has("messageChannel")){

            switch ( json.get("messageChannel").asText() ){

                case "the-grid" : {

                    if(terminal.subscribers_grid.isEmpty()) terminal_you_have_not_followers(terminal);
                    for( WebSCType webSCType :  terminal.subscribers_grid) {
                        webSCType.write_without_confirmation(json);
                    }
                    return;
                }
                case "tyrion" : {
                    logger.warn("Homer: Incoming message: Tyrion: Server receive message: ");
                    logger.warn("Homer: Incoming message: Tyrion: Server don't know what to do!");
                    return;
                }
                default: {
                    logger.error("ERROR \n");
                    logger.error("Homer: "+ terminal.identifikator + " Incoming message has not messageChannel!!!!");
                    logger.error("ERROR \n");
                }
            }

        }else {
            logger.error("ERROR \n");
            logger.error("Homer: "+ terminal.identifikator + " Incoming message has not messageChannel!!!!");
            logger.error("ERROR \n");
        }
    }

    public static void terminal_ping(WebSCType terminal) throws TimeoutException, InterruptedException {

        ObjectNode result = Json.newObject();
        result.put("messageType", "ping");
        result.put("messageChannel", "tyrion");

        terminal.write_without_confirmation(result);
    }

    public static void terminal_is_disconnected(WebSCType terminal){

        terminal.maps.remove(terminal.identifikator);

        for(WebSCType subscriber : terminal.subscribers_grid){
            logger.debug("Remove from subscriber list  " + subscriber.identifikator);

            subscriber.subscribers_grid.remove(terminal);
            if(subscriber.subscribers_grid.isEmpty()){

                try {
                    WebSocketController_Incoming.homer_all_terminals_are_gone(subscriber);
                }catch (Exception e){
                    logger.debug("When Tyrion try to send terminals, that homer is offline SHIT HAPPENS!!!", e);
                }
            }
        }

    }

    public static void terminal_homer_reconnection(WebSCType terminal){

        logger.debug("Terminal: "  + terminal.identifikator + " Homer is online now!");

        ObjectNode result = Json.newObject();
        result.put("messageType", "Homer se znovu připojil!!");
        result.put("TODO", "Tato zpráva není oficiálně definovaná");

        terminal.write_without_confirmation(result);

    }

    public static void terminal_homer_is_not_connected_yet(WebSCType terminal){

        logger.debug("Terminal: "  + terminal.identifikator + " your homer is still not connected yet");

        ObjectNode result = Json.newObject();
        result.put("messageType", "homer není pripojen!");
        result.put("TODO", "Tato zpráva není oficiálně definovaná");

        terminal.write_without_confirmation(result);

    }

    public static void terminal_echo_that_home_was_disconnect(WebSCType terminal){
        logger.debug("Terminal: "  + terminal.identifikator + " Homer is offline now - we don't know what happens!!");

        ObjectNode result = Json.newObject();
        result.put("messageType", "unSubscribeChannel");
        result.put("messageChannel", "the-grid");

        terminal.write_without_confirmation(result);
    }

    public static void terminal_you_have_not_followers(WebSCType terminal){
        logger.debug("Terminal: " + terminal.identifikator + " wanted send message to Blocko program in Homer - but terminal is not connected with any Blocko program ");

        ObjectNode result = Json.newObject();
        result.put("messageType", "NEmáš žádné Grid odběratele - zprává nebyla přeposlána ");
        result.put("TODO", "Tato zpráva není oficiálně definovaná");

        terminal.write_without_confirmation(result);
    }

    public static void terminal_blocko_program_not_running_anywhere(WebSCType terminal){
        logger.debug("Message for Terminal: " + terminal.identifikator + ": Blocko program not runing enywhere!");

        ObjectNode result = Json.newObject();
        result.put("messageType", "M_Program je sice spojený, ale program pro Homera nikde neběží a není tedy co kam zasílat");
        result.put("TODO", "Tato zpráva není oficiálně definovaná");


        terminal.write_without_confirmation(result);

    }

    public static void m_project_is_connected_with_older_version(WebSCType terminal) throws  ExecutionException, TimeoutException, InterruptedException{

        logger.warn ("Tyrion: Warning!");
        logger.warn ("Tyrion: Warning: M_Project is connected to version in Homer Server and without permission to auto-increment!");

        ObjectNode result = Json.newObject();
        result.put("messageType", "M Project je napevno navázaný na verzi, která neběží a auto-propojení je zakázáno!");
        result.put("messageChannel", "the-grid");

        terminal.write_without_confirmation(result);
    }

// Test & Control API ---------------------------------------------------------------------------------------------------------

    public static void server_violently_terminate_terminal(WebSCType terminal){

        ObjectNode result = Json.newObject();
        result.put("messageType", "Budeš něžně odpojen!");
        result.put("TODO", "Tato zpráva není oficiálně definovaná");

        terminal.write_without_confirmation(result);

        try {
            terminal.close();
        }catch (Exception e){}
    }

    public static void disconnect_all_homers(){

        logger.warn("Tyrion is shutting down: Trying safety disconnect all connected Homer");

        for (Map.Entry<String, WebSCType> entry :  WebSocketController_Incoming.incomingConnections_homers.entrySet())
        {
            server_violently_terminate_terminal(entry.getValue());
        }
    }

    public static void disconnect_all_mobiles() {

        logger.warn("Tyrion is shutting down: Trying safety disconnect all connected Homer");

        for (Map.Entry<String, WebSCType> entry :  WebSocketController_Incoming.incomingConnections_terminals.entrySet())
        {
            server_violently_terminate_terminal(entry.getValue());
        }
    }

    public static void disconnect_all_Blocko_Servers() {

        logger.warn("Tyrion is shutting down: Trying safety disconnect all connected Homer");

        for (Map.Entry<String, WebSCType> entry :  WebSocketController_Incoming.blocko_servers.entrySet())
        {
            server_violently_terminate_terminal(entry.getValue());
        }
    }

    public static void disconnect_all_Compilation_Servers() {

        logger.warn("Tyrion is shutting down: Trying safety disconnect all connected Homer");

        for (Map.Entry<String, WebSCType> entry :  WebSocketController_Incoming.compiler_cloud_servers.entrySet())
        {
            server_violently_terminate_terminal(entry.getValue());
        }
    }



}
