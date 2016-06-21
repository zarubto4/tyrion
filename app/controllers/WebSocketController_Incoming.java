package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.blocko.Cloud_Blocko_Server;
import models.compiler.Cloud_Compilation_Server;
import models.compiler.Version_Object;
import models.person.Person;
import models.project.b_program.B_Program;
import models.project.b_program.B_Program_Cloud;
import models.project.b_program.Homer;
import models.project.m_program.Grid_Terminal;
import models.project.m_program.M_Project;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import utilities.loggy.Loggy;
import utilities.webSocket.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;


public class WebSocketController_Incoming extends Controller {

// Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

// Values  -------------------------------------------------------------------------------------------------------------

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


// PUBLIC API -------------------------------------------------------------------------------------------------------------------

    // Připojení Homera (Cloud i Local)
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
                        homer_reconnection(incomingConnections_terminals.get(terminal_name));
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
                            ask_for_receiving_for_Becki(homer);

                            homer.subscribers_becki.add(becki_website.get(becki_identificator));
                            becki_website.get(becki_identificator).subscribers_becki.add(homer);

                        } else {

                            logger.debug("Homer -> Lost Connection: Connection with Becki: " + becki_identificator + " was restored");
                            homer.subscribers_becki.add(becki_website.get(becki_identificator));
                            becki_website.get(becki_identificator).subscribers_becki.add(homer);
                        }

                        logger.debug("Homer -> Lost Connection: Sending to Becki :" + becki_identificator + " that they are new receiver");
                        homer_reconnection(becki_website.get(becki_identificator));
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

    public  WebSocket<String>  mobile_connection(String m_project_id, String terminal_id) {
        try {

            logger.debug("Terminal: Incoming connection on terminal: " + terminal_id + " on project " + m_project_id);

            if (incomingConnections_terminals.containsKey(terminal_id)) {
                logger.debug("Terminal: Incoming connection on terminal: " + terminal_id + " is already used");
                return WebSocket.reject(forbidden());
            }
            if (Grid_Terminal.find.where().eq("terminal_id", terminal_id).findUnique() == null) {
                logger.debug("Terminal: Incoming connection on terminal: " + terminal_id + " is not registred in database");
                return WebSocket.reject(forbidden());
            }

            M_Project m_project = M_Project.find.byId(m_project_id);

            if (m_project == null) {
                logger.debug("Terminal: Incoming connection on terminal: " + terminal_id + " where "+ m_project_id +" is not registered in database");
                return WebSocket.reject(forbidden());
            }
            if (m_project.b_program_version == null) {
                logger.debug("Terminal: Incoming connection on terminal: " + terminal_id + " where "+ m_project_id +" where M_Project is not connected with B_progrm");
                return WebSocket.reject(forbidden());
            }

            //----------------------------------------------------------------------------------------------------------------------

            WS_Grid_Terminal terminal = new WS_Grid_Terminal(terminal_id, m_project_id, incomingConnections_terminals);
            WebSocket<String> ws = terminal.connection();

            // Tohle je cloudové nasazení B programu
            //-----------------------------------------------------------------------------------------------------------

            // POKUD JE B_PROGRAM V CLOUDU ale shodná verze neexistuje ale je povoleno auto increment
            if (m_project.b_program_version.b_program_cloud == null &&  m_project.auto_incrementing &&  B_Program.find.where().isNotNull("version_objects.b_program_cloud").findUnique() != null) {
                logger.debug("Terminal: Program on Cloud win but system have to iterate to new version: ");

                m_project.b_program_version = m_project.b_program.where_program_run();
                m_project.update();

            }

            // POKUD JE B_PROGRAM V CLOUDU a má shodnou verzi
            if (m_project.b_program_version.b_program_cloud != null) {


                logger.debug("Terminal: Program on Cloud win");

                String homer_identificator = m_project.b_program_version.b_program_cloud.blocko_instance_name;
                String server_name =  Cloud_Blocko_Server.find.where().eq("cloud_programs.version_object.m_project.id", m_project.id ).findUnique().server_name;

                logger.debug("Terminal: Connection with Server " + server_name);
                logger.debug("Terminal: Connection with Homer " + homer_identificator);

                logger.debug("Tyrion: Is CLoud server connected?");

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
                    WebSocketController_Incoming.homer_is_not_connected_yet(terminal);

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
              if( m_project.b_program_version.b_program_homer == null && m_project.auto_incrementing  && B_Program.find.where().isNotNull("version_objects.b_program_homer").findUnique() != null) {

                  logger.debug("Tyrion: Homer is on local computer, but Tyrion have to repair versions");
                  m_project.b_program_version = m_project.b_program.where_program_run();
                  m_project.update();

              }
              // POKUD JE B_PROGRAM NA PC a má shodnou verzi
              if (m_project.b_program_version.b_program_homer != null) {

                  logger.debug("Tyrion: Homer is on local computer");

                  String homer_identificator = m_project.b_program_version.b_program_homer.homer.id;

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
                      WebSocketController_Incoming.homer_is_not_connected_yet(terminal);

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
    public  WebSocket<String>  blocko_cloud_server_connection(String server_name){
        try{

            logger.debug("Cloud Server: Incoming connection: Server:  " + server_name);

            Cloud_Blocko_Server blocko_server = Cloud_Blocko_Server.find.where().eq("server_name", server_name).findUnique();
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

            // Procedury kontroly - informovat třeba všechny klienty o tom, že se server připojil. Kontzrola co tam běží a další píčoviny
            logger.debug("Tyrion have to control what is on the server side ");

            // GET state - a vyhodnocením v jakém stavu se server nachází a popřípadě
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
                                JsonNode jsonNode = server.write_with_confirmation(result, (long) 25000);

                                logger.debug("Blocko Server: In control cycle: Iteration: " + interrupter);

                                if (jsonNode.has("status")) {

                                    if (jsonNode.get("status").asText().equals("error")) {
                                        interrupt();
                                    }

                                    logger.debug("Blocko Server: Respond with list of instances");

                                    List<String> instances_on_server = new ArrayList<>();
                                    final JsonNode arrNode = jsonNode.get("instances");
                                    for (final JsonNode objNode : arrNode) instances_on_server.add(objNode.asText());

                                    logger.debug("Blocko Server: Number of instances on server");


                                    List<String> instances_in_database = new ArrayList<>();
                                    for (B_Program_Cloud cloud_program : B_Program_Cloud.find.where().eq("server.id", blocko_server.id).select("blocko_instance_name").findList())
                                        instances_in_database.add(cloud_program.blocko_instance_name);
                                    logger.debug("Blocko Server: The number of instances that should run on a server: " + instances_in_database.size());

                                    List<String> instances_on_server_copy = new ArrayList<>(instances_on_server);
                                    logger.debug("Blocko Server: The number of instances that should run on a server: " + instances_on_server_copy.size());


                                    List<String> instances_in_database_copy = new ArrayList<>(instances_in_database);
                                    logger.debug("Blocko Server: The number of instances from database: " + instances_in_database_copy.size());

                                    instances_in_database.removeAll(instances_on_server);

                                    logger.debug("Blocko Server: The number of instances that will be recorded on the server " + instances_in_database.size());

                                    instances_on_server_copy.removeAll(instances_in_database_copy);
                                    logger.debug("Blocko Server:  The number of instances to be deleted from the server" + instances_on_server_copy.size());

                                    List<B_Program_Cloud> b_programs = B_Program_Cloud.find.where().in("blocko_instance_name", instances_in_database).findList();

                                    if (!b_programs.isEmpty()) {

                                        logger.debug("Blocko Server: Starting to uploud new instnces to server");

                                        for (B_Program_Cloud b_program : b_programs) {
                                            try {
                                                WebSocketController_Incoming.blocko_server_add_instance(server, b_program);
                                            } catch (Exception e) {
                                                logger.warn("Instance " + b_program.blocko_instance_name + "  failed to upload properly on the server Blocko");
                                            }
                                        }
                                    }

                                    if(!instances_on_server_copy.isEmpty()) {

                                        logger.debug("Blocko Server: Starting to remove the server instance ");

                                        for (String blocko_instance_name : instances_on_server_copy) {
                                            try {

                                                JsonNode remove_instance = blocko_server_remove_instance(server, blocko_instance_name);
                                                if (remove_instance.get("status").asText().equals("error")) {
                                                    logger.debug("Blocko Server: Fail when Tyrion try to remove instance from Blocko server");
                                                }
                                            } catch (Exception e) {
                                                logger.error("Blocko Server: Eception wher Tyrion try to remove instance from Blocko server", e);
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
    public  WebSocket<String>  compilator_server_connection (String server_name){
        try{
            System.out.println("Připojuje se mi Kompilační server: " + server_name);

            System.out.println("Ověřuji zda je server platný a mohu ho nechat připojit"); // TODO - přidat ověření ještě pomocí HASHe co už je v objektu definován
            Cloud_Compilation_Server cloud_compilation_server = Cloud_Compilation_Server.find.where().eq("server_name", server_name).findUnique();
            if(cloud_compilation_server == null) return WebSocket.reject(forbidden("Server side error - unrecognized name"));

            if(compiler_cloud_servers.containsKey(server_name)) {
                System.out.println("Na Tyrionovi je už připojen cKompilační server se stejným jménem - nedovolím další připojení");
                return WebSocket.reject(forbidden("Server side error - already connected"));
            }

            // Inicializuji Websocket pro Homera
            WS_CompilerServer server = new WS_CompilerServer( cloud_compilation_server.server_name, cloud_compilation_server.destination_address, compiler_cloud_servers );

            // Připojím se
            System.out.println("Compilační server se připojit");
            return server.connection();

        }catch (Exception e){
            Loggy.error("Cloud Compiler Server Web Socket connection", e);
            return WebSocket.reject(forbidden("Server side error"));
        }
    }
    public  WebSocket<String>  becki_website_connection (String security_token){
        try{

            logger.debug("Becki: Incoming connection: " + security_token);

            logger.debug("Becki: Controlling of incoming token " + security_token);
            Person person = Person.findByAuthToken(security_token);
            if(person == null){
                logger.warn("Becki: Incoming token " + security_token + " is invalid!");
                return WebSocket.reject(forbidden());
            }

            WS_Becki_Website website;

            if(becki_website.containsKey(person.id)) {
                website = (WS_Becki_Website) becki_website.get(person.id);
            }else{
                website = new WS_Becki_Website(person.id);
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

// PRIVATE Blocko-Server ---------------------------------------------------------------------------------------------------------

    public static void blocko_server_is_disconnect(WS_BlockoServer blockoServer){
        logger.debug("Tyrion lost connection with blocko server: " + blockoServer.identifikator);
        blocko_servers.remove(blockoServer.identifikator);
    }

    public static JsonNode blocko_server_listOfInstance(WS_BlockoServer blockoServer)  throws TimeoutException, InterruptedException{

        logger.debug("Tyrion: Server want know instances on: " + blockoServer.identifikator);

        ObjectNode result = Json.newObject();
        result.put("messageType", "listInstances");
        result.put("messageChannel", "homer-server");

        return blockoServer.write_with_confirmation(result);
    }

    public static JsonNode blocko_server_isInstanceExist(WS_BlockoServer blockoServer, String instance_name)  throws TimeoutException, InterruptedException{


        ObjectNode result = Json.newObject();
        result.put("messageType", "instanceExist");
        result.put("messageChannel", "homer-server");
        result.put("instanceId", instance_name);

        return blockoServer.write_with_confirmation(result);
    }

    public static void blocko_server_incoming_message(WS_BlockoServer blockoServer, ObjectNode json){
        System.out.println("Speciálně  server2server přišla zpráva: " + json.asText() );
        System.out.println("Zatím není implementovaná žádná reakce na příchozá zprávu z Blocko serveru !!" );
    }

    public static JsonNode blocko_server_add_instance(WS_BlockoServer blockoServer, B_Program_Cloud program) throws Exception, TimeoutException, InterruptedException{

            logger.debug("Tyrion uploud new instance to server" + blockoServer.identifikator);

            if (WebSocketController_Incoming.incomingConnections_homers.containsKey(program.blocko_instance_name)) {

                System.out.println("Při přidávání instance do serveru: " + blockoServer.identifikator + " bylo zjištěno že v mapě už existuje jméno homera");
                ObjectNode result = Json.newObject();
                result.put("status", "success");
                return result;
            }

            System.out.println("Vytvářím nového virtuálního Homera");
            WS_Homer_Cloud homer = new WS_Homer_Cloud(program.blocko_instance_name, program.version_object.id, blockoServer);

            ObjectNode result = Json.newObject();
            result.put("messageType", "createInstance");
            result.put("messageChannel", "homer-server");
            result.put("instanceId", program.blocko_instance_name);
            result.put("macAddress", program.macAddress);

            System.out.println("Nahrávám ho na Blocko server novou instanci");
            JsonNode result_instance = blockoServer.write_with_confirmation( result);

            System.out.println("Nahrávám ho na Blocko server do vytvořené instnace program");
            JsonNode result_uploud = WebSocketController_Incoming.homer_UploadProgram(homer, program.id, program.version_object.files.get(0).get_fileRecord_from_Azure_inString());

            System.out.println("Přidávám nového virtuálního Homera do privátní mapy blocko serveru");
            blockoServer.virtual_homers.put(program.blocko_instance_name, homer);

            System.out.println("Spouštím připojovací proceduru");
            homer_connection_procedure(homer);

            incomingConnections_homers.put(homer.identifikator, homer);

            return result_uploud;

    }


    public static JsonNode blocko_server_remove_instance( WS_BlockoServer blockoServer, String instance_name) throws TimeoutException, InterruptedException{

        ObjectNode result = Json.newObject();
        result.put("messageType", "destroyInstance");
        result.put("messageChannel", "homer-server");
        result.put("instanceId", instance_name);

        return blockoServer.write_with_confirmation(result );

    }

    public static void blocko_server_ping(WS_BlockoServer blockoServer) throws TimeoutException, InterruptedException {

        ObjectNode result = Json.newObject();
        result.put("messageType", "ping");
        result.put("messageChannel", "tyrion");

        blockoServer.write_without_confirmation( result);
    }

    public static void blocko_server_disconnect(WS_BlockoServer blockoServer) throws TimeoutException, InterruptedException {
        System.out.println("Chystám se násilně odpojit server");
        blockoServer.onClose();
    }

// PRIVATE Compiler-Server --------------------------------------------------------------------------------------------------------

    public static void compiler_server_is_disconnect(WS_CompilerServer compilerServer)  {
        System.out.println("Ztráta spojení s Compilačním serverem: " + compilerServer.identifikator);
        System.out.println("Je nutná dodělat reakce na ztrátu spojení??? : ");
        compiler_cloud_servers.remove(compilerServer.identifikator);
    }

    public static JsonNode compiler_server_make_Compilation(Person compilator, ObjectNode jsonNodes) throws TimeoutException, InterruptedException {

        // 1. Vybrat náhodný server kde se provede kompilace
        System.out.println("Tyrion chce odeslat soubor ke kompilaci");

        List<String> keys      = new ArrayList<>(compiler_cloud_servers.keySet());
        WS_CompilerServer server = (WS_CompilerServer) compiler_cloud_servers.get( keys.get( new Random().nextInt(keys.size())) );

        System.out.println("Vybral příslušný server");

        System.out.println("Odeslal žádost o kompilaci");
        ObjectNode compilation_request = server.write_with_confirmation(jsonNodes);

        System.out.println("Přijal potvrzení žádost o kompilaci se  zprávou: " + compilation_request.asText() );

        if(!compilation_request.get("status").asText().equals("success")) {
            System.out.println("Zpráva neobsahovala success a tak vracím error json ");
            ObjectNode error_result = Json.newObject();
            error_result.put("error", "Something was wrong");
            return  error_result;
        }

        System.out.println("Zpráva obsahovala success");

        JsonNode blocko_interface = compilation_request.get("interface");

        System.out.println("Vkládám do zásobníku reqestů kompilace odchozí žádost s klíčem " + compilation_request.get("buildId").asText());
        server.compilation_request.put(compilation_request.get("buildId").asText()  , jsonNodes);


        class Confirmation_Thread implements Callable<ObjectNode> {

            Long breaker = (long) 1000*30;

            @Override
            public ObjectNode call() throws Exception {

                while(breaker > 0){
                    Thread.sleep(1000);
                    breaker-=1000;

                    if(server.compilation_results.containsKey( compilation_request.get("buildId").asText() )) {
                        System.out.println("Kompilace dokončena protože server zavěsil do Result odpověď");

                        System.out.println("Mažu žádost");
                        server.compilation_request.remove(compilation_request.get("buildId").asText());
                        ObjectNode compilation_result = server.compilation_results.get(compilation_request.get("buildId").asText());

                        System.out.println("Mažu odpověď");
                        server.compilation_results.remove(compilation_request.get("buildId").asText());

                        System.out.println("Vracím Odpověď");
                        return compilation_result;
                    }

                    System.out.println("Kompilace stále probíhá");
                }

                server.compilation_request.remove(compilation_request.get("buildId").asText());
                ObjectNode error_result = Json.newObject();
                error_result.put("error", "Something was wrong");
                return error_result;
            }
        }

        ExecutorService pool = Executors.newFixedThreadPool(3);

        Callable<ObjectNode> callable = new Confirmation_Thread();
        Future<ObjectNode> future = pool.submit(callable);

        try {
            ObjectNode final_result = future.get();


            System.out.println("\n\n\n Interface: " + blocko_interface);
            final_result.set("interface", blocko_interface);

            return final_result;
        } catch (Exception e) {
            System.out.println("write_with_confirmation NEstihlo se včas");
            throw new TimeoutException();
        }
    }

    public static void compiler_server_ping(WS_CompilerServer compilerServer) throws TimeoutException, InterruptedException {


        ObjectNode result = Json.newObject();
        result.put("messageType", "ping");
        result.put("messageChannel", "tyrion");


        compilerServer.write_without_confirmation(result);
    }

    public static void compilation_server_incoming_message(WS_CompilerServer server, ObjectNode json){
        System.out.println("Speciálně  server2server přišla zpráva: " + json.asText() );
        System.out.println("Zatím není implementovaná žádná reakce na příchozá zprávu z Compilačního serveru !!");
    }

// PRIVATE Becki -----------------------------------------------------------------------------------------------------------------

    public static void becki_incoming_message(WS_Becki_Website becki, ObjectNode json){

        logger.debug("Becki: " + becki.identifikator + " Incoming message: " + json.toString() );

        switch (json.get("messageChannel").asText()) {

            case "becki": {

                switch (json.get("messageType").asText()) {

                    case "subscribe_instance": {
                        becki_subscribe_channel(becki, json);
                        return;
                    }

                    // Více typů příkazů zatím není implementováno
                }
            }
            case  "tyrion" : {
                    System.out.println ("Z Becki přišla zpráva určená pro Tyrion. Nejsou ale implementovány žádné reakce." + json.asText());
                    return;
            }
            case "nevin" : {


            }

            default: {
                    System.out.println("Becki něco přeposílá do Blocka (není to žádost o propojení a ani pro Tyriona");

                    // Přepošlu to na všehcny odběratele Becki
                    if (becki.subscribers_becki != null && !becki.subscribers_becki.isEmpty()) {
                        for (WebSCType ws : becki.subscribers_becki) {
                            ws.write_without_confirmation(json);
                        }
                    }

                    return;
            }

         }
    }

    public static void becki_subscribe_channel(WS_Becki_Website becki, ObjectNode json){

            try {
                String version_id = json.get("version_id").asText();
                System.out.println("Ze serveru budu chtít dostávat všechny informace z blocko serveru na verzi" + version_id);

                // Najdu Version
                Version_Object version = Version_Object.find.byId(version_id);
                if (version == null) {
                    becki_disaprove_recive_instance_state(becki, json.get("messageId").asText(), "Version not Exist");
                    return;
                }

                // Zjistím kde běží
                if (!blocko_servers.containsKey(version.b_program_cloud.server.server_name)) {
                    becki_disaprove_recive_instance_state(becki, json.get("messageId").asText(), "Server is not connected");
                    return;
                }

                WS_BlockoServer server = (WS_BlockoServer) blocko_servers.get(version.b_program_cloud.server.server_name);

                // Zjistit jestli tam instance opravdu běží
                JsonNode result_instance = blocko_server_isInstanceExist(server, version.b_program_cloud.blocko_instance_name);
                if(result_instance.get("status").asText().equals("error"))  {
                    becki_disaprove_recive_instance_state(becki, json.get("messageId").asText(), result_instance.get("error").asText());
                    return;
                }
                // Zjistím jestli existuje instnace
                if(!result_instance.get("exist").booleanValue())    {
                    becki_disaprove_recive_instance_state(becki, json.get("messageId").asText(), "Instance of this version not running on this server!");
                    return;
                }

                // Zjistím jestli existuje virtuální homer
                System.out.println("Zjištuji jestli existuje virtuální homer");
                if(!incomingConnections_homers.containsKey(version.b_program_cloud.blocko_instance_name) ) {
                    System.out.println("Virtuální Homer neexistuje!!!");
                    becki_disaprove_recive_instance_state(becki, json.get("messageId").asText(), "FATAL ERROR!!! Virtual Homer for this instance not exist!");
                    return;
                }

                WebSCType homer = incomingConnections_homers.get(version.b_program_cloud.blocko_instance_name);

                // 2 - Požádat Homera o zasílání informací
                JsonNode result_recive = ask_for_receiving_for_Becki(homer);
                if(result_recive.get("status").textValue().equals("error")) becki_disaprove_recive_instance_state(becki, json.get("messageId").asText(), result_recive.get("error").textValue());

                // 1 - navázat propojení mezi instanci Homera a instancí Becki
                homer.subscribers_becki.add(becki);
                becki.subscribers_becki.add(homer);

                // 3 - Přeposílat informace na Becki
                // To zajistím v odběrném místě Homera!

                // Potvrdím Becki že vše je v cajku
                becki_aprove_recive_instance_state(becki, json.get("messageId").asText());

                return;
            }catch (Exception e){
                System.out.println("Došlo k chybě");
                e.printStackTrace();
                becki_disaprove_recive_instance_state(becki, json.get("messageId").asText(), "Unknow Error");
            }
    }

    public static void becki_ping(WebSCType webSCType) throws TimeoutException, InterruptedException {

        System.out.println("Budu zasílat na Becki Ping!!");

        ObjectNode result = Json.newObject();
        result.put("messageType", "ping");
        result.put("messageChannel", "tyrion");

        webSCType.write_without_confirmation(result);
    }

    // Tady odpovídám zpět na žádost o zasílání informací z Blocko serveru
    public static void becki_aprove_recive_instance_state(WebSCType webSCType, String messageId){

        System.out.println("Budu zasílat na Becki že jsem zpracoval úspěšně žádost o zasílání informací z Blocka");

        ObjectNode result = Json.newObject();
        result.put("messageType", "subscribe_instance");
        result.put("messageChannel", "blocko");
        result.put("status", "success");

        webSCType.write_without_confirmation(messageId, result);

    }

    // Tady odpovídám zpět na žádost o zasílání informací z Blocko serveru (ale něco se posralo a tak to nefunguje)
    public static void becki_disaprove_recive_instance_state(WebSCType webSCType, String messageId, String error){

        System.out.println("Budu zasílat na Becki že jsem zpracoval úspěšně žádost o zasílání informací z Blocka");

        ObjectNode result = Json.newObject();
        result.put("messageType", "subscribe_instace");
        result.put("messageChannel", "blocko");
        result.put("status", "error");
        result.put("error", error);
        webSCType.write_without_confirmation(messageId, result);

    }

    public static void becki_echo_that_becki_was_disconnect(WebSCType homer){
        System.out.println("Chci zaslat zprávu že se poslední becki odpojila a už neposlouchá blocko");


        ObjectNode result = Json.newObject();
        result.put("messageType", "unSubscribeChannel");
        result.put("messageChannel", "becki");

        homer.write_without_confirmation(result);
    }

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
                    logger.debug("Homer: Incoming message: tyrion: Server receive message: ");
                    logger.debug("Homer: Incoming message: tyrion: Server don't know what to do!");
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

                default: {
                    logger.error("ERROR \n");
                    logger.error("Homer: Incoming message: becki: Tyrion don't recognize incoming messageChanel!!!");
                    logger.error("ERROR \n");
                }

            }
        }else {
            logger.error("ERROR \n");
            logger.error("Homer: "+ homer.identifikator + " Incoming message has not messageChannel!!!!");
            logger.error("ERROR \n");
        }
    }

    public static JsonNode homer_getState(String homer_id) throws TimeoutException, InterruptedException {

        logger.debug("Tyrion: want to know state of Homer: " + homer_id);

        if(incomingConnections_homers.containsKey(homer_id)){

            ObjectNode result = Json.newObject();
            result.put("messageType", "getState");
            result.put("messageChannel", "homer-server");

            return incomingConnections_homers.get(homer_id).write_with_confirmation(result);

        }else {
            logger.error("ERROR \n");
            logger.error("Tyrion: Homer is not connected!");
            logger.error("ERROR \n");
            return  null;
        }
    }

    public static void homer_ping(String homer_id) throws TimeoutException, InterruptedException {

        logger.debug("Tyrion: Server want send ping to Homer: " + homer_id);

        if(incomingConnections_homers.containsKey(homer_id)){

            ObjectNode result = Json.newObject();
            result.put("messageType", "ping");
            result.put("messageChannel", "homer-server");

            incomingConnections_homers.get(homer_id).write_without_confirmation(result);

        }else {
            logger.error("ERROR \n");
            logger.error("Tyrion: Homer is not connected!");
            logger.error("ERROR \n");

        }

    }

    public static void homer_disconnect_homer(WebSCType homer) throws TimeoutException, InterruptedException {

        logger.debug("Tyrion: Homew will be disconnected: ");
        homer.onClose();
    }

    public static JsonNode homer_destroyInstance(String homer_id) throws TimeoutException, InterruptedException {

        logger.debug("Tyrion: Instruction for Homer in cloud: Destroy you instance!");

            ObjectNode result = Json.newObject();
            result.put("messageType", "destroyInstance");
            result.put("messageChannel", "homer-server");

            return incomingConnections_homers.get(homer_id).write_with_confirmation(result);
    }

    public static void homer_update_embeddedHW(String homer_id, List<String> board_id_list, byte[] fileInBase64) throws TimeoutException, InterruptedException, IOException {

        logger.debug("Tyrion: Sending to Hardware new Compilation of code");

            ObjectNode result = Json.newObject();
            result.put("messageType", "updateDevice");
            result.set("hardwareId", Json.toJson(board_id_list));
            result.put("base64Binary", fileInBase64);

            incomingConnections_homers.get(homer_id).write_with_confirmation(result);
    }

    public static JsonNode homer_UploadProgram(WebSCType homer, String program_id, String program) throws TimeoutException, InterruptedException {

            ObjectNode result = Json.newObject();
            result.put("messageType", "loadProgram");
            result.put("messageChannel", "tyrion");
            result.put("programId", program_id);
            result.put("program", program);

          return homer.write_with_confirmation(result);
    }

    public static boolean homer_is_online(String homer_id){
        return incomingConnections_homers.containsKey(homer_id);
    }

    public static JsonNode get_all_Connected_HW_to_Homer(Homer homer) throws TimeoutException, InterruptedException{

        ObjectNode result = Json.newObject();
        result.put("messageType", "getDeviceList");
        result.put("messageChannel", "tyrion");

        return incomingConnections_homers.get( homer.id).write_with_confirmation(result, (long) 250*25 );
    }

    public static void homer_is_disconnect(WebSCType homer) {

        logger.debug("Lost connection with Homer: " + homer.identifikator + " deleting that from connection map and add to lost connection map");
        incomingConnections_homers.remove(homer.identifikator);

        ArrayList<String> list = new ArrayList<>();

        for (WebSCType terminal : homer.subscribers_grid) {
            list.add(terminal.identifikator);
            terminal.subscribers_grid.remove(homer);
            // Informuji že jsem ztratil spojení
            echo_that_home_was_disconnect(terminal);
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
            echo_that_home_was_disconnect(becki);
        }



    }

    public static void homer_all_terminals_are_gone(WebSCType homer) throws TimeoutException, InterruptedException {

        logger.debug("Homer: " + homer.identifikator + "  does not have any subscribers:");

        ObjectNode result = Json.newObject();
        result.put("messageType", "unSubscribeChannel");
        result.put("messageChannel", "the-grid");

        homer.write_with_confirmation( result);
    }

    public static void ask_for_receiving_for_Grid(WebSCType homer) throws TimeoutException, InterruptedException {

        logger.debug("Homer: " + homer.identifikator + ", server want send you request for receiving for Grid:");

        ObjectNode result = Json.newObject();
        result.put("messageType", "subscribeChannel");
        result.put("messageChannel", "the-grid");

        homer.write_with_confirmation(result);
    }

    public static JsonNode ask_for_receiving_for_Becki(WebSCType homer) throws TimeoutException, InterruptedException {

        logger.debug("Homer: " + homer.identifikator + ", server want send you request for receiving for Becki:");

        ObjectNode result = Json.newObject();
        result.put("messageType", "subscribeChannel");
        result.put("messageChannel", "becki");

        return homer.write_with_confirmation(result);
    }

    public static void invalid_json_message(WebSCType ws){

        logger.debug("Invalid message from: " + ws.identifikator);

        ObjectNode result = Json.newObject();
        result.put("messageType", "JsonUnrecognized");
        ws.write_without_confirmation(result);
    }

// PRIVATE Terminal ---------------------------------------------------------------------------------------------------------

    /** incoming Json from Terminal */
    public static void incoming_message_terminal(WebSCType terminal, ObjectNode json){


        if(json.has("messageChannel")){

            System.out.println("messageChannel je: " + json.get("messageChannel").asText()  );
            switch ( json.get("messageChannel").asText() ){

                case "the-grid" : {
                    System.out.println("the-grid - Přeposílám na odběratele");

                    if(terminal.subscribers_grid.isEmpty()) terminal_you_have_not_followers(terminal);
                    for( WebSCType webSCType :  terminal.subscribers_grid) {

                        System.out.println( "Zpráva je přeposílána na + " + webSCType.identifikator);
                        webSCType.write_without_confirmation(json);
                    }
                    return;
                }
                case "tyrion" : {
                    System.out.println("Zprávu jsem poslal na Tyrion");

                }
            }
        }
        System.out.println("Příchozí zpráva neobsahuje messageChannel");
    }

    public static void server_violently_terminate_terminal(WebSCType terminal){
        System.out.println("Terminál se pokusil zaslat zpváu na Blocko ale žádné blocko nemá na sobě připojené - tedy zbyteční a packet zahazuji");


        ObjectNode result = Json.newObject();
        result.put("messageType", "NEmáš žádné Grid odběratele - zprává nebyla přeposlána \"");

        terminal.write_without_confirmation(result);

        try {
            terminal.close();
        }catch (Exception e){}
    }

    public static void terminal_ping(WebSCType terminal) throws TimeoutException, InterruptedException {

        ObjectNode result = Json.newObject();
        result.put("messageType", "ping");
        result.put("messageChannel", "tyrion");

        terminal.write_without_confirmation(result);
    }

    public static void terminal_disconnect(WebSCType terminal) throws TimeoutException, InterruptedException {
        System.out.println("Chystám se násilně odpojit terminal");
        terminal.onClose();
    }

    public static void server_plained_terminate_terminal(WebSCType terminal){
        System.out.println("Server odesílá zprávu, že bude plánované odstavení");

        ObjectNode result = Json.newObject();
        result.put("messageType", "Ahoj, server se restartuje mezi 5 a 9");

        terminal.write_without_confirmation(result);
    }

    public static void terminal_is_disconnected(WebSCType terminal){

        terminal.maps.remove(terminal.identifikator);

        for(WebSCType subscriber : terminal.subscribers_grid){
            System.out.println("Remove from subscriber list  " + subscriber.identifikator);
            subscriber.subscribers_grid.remove(terminal);
            if(subscriber.subscribers_grid.isEmpty()){

                try {
                    WebSocketController_Incoming.homer_all_terminals_are_gone(subscriber);
                }catch (Exception e){ System.out.println("Při odesílání informace so tom, že Homera, už nikoho neposlouchá se něco posralo");}
            }
        }

    }

    public static void homer_reconnection(WebSCType terminal){
        System.out.println("Chci upozornit terminál že se Homer Připojil: ");

        ObjectNode result = Json.newObject();
        result.put("messageType", "Homer se znovu připojil!!");

        terminal.write_without_confirmation(result);
    }

    public static void homer_is_not_connected_yet(WebSCType terminal){

        System.out.println("Chci zaslat zprávu že homer není pripojen a jsem v metodě homer_is_not_connected_yet");

        ObjectNode result = Json.newObject();
        result.put("messageType", "homer není pripojen!");

        terminal.write_without_confirmation(result);

    }

    public static void echo_that_home_was_disconnect(WebSCType terminal){
        System.out.println("Chci zaslat zprávu že se homer odpojil");

        ObjectNode result = Json.newObject();
        result.put("messageType", "unSubscribeChannel");
        result.put("messageChannel", "the-grid");

        terminal.write_without_confirmation(result);
    }

    public static void terminal_you_have_not_followers(WebSCType terminal){
        System.out.println("Terminál se pokusil zaslat zpváu na Blocko ale žádné blocko nemá na sobě připojené - tedy zbytečné a packet zahazuji");

        ObjectNode result = Json.newObject();
        result.put("messageType", "NEmáš žádné Grid odběratele - zprává nebyla přeposlána ");

        terminal.write_without_confirmation(result);
    }

    public static void terminal_blocko_program_not_running_anywhere(WebSCType terminal){
        System.out.println("Blocko program nikde něběží");

        ObjectNode result = Json.newObject();
        result.put("messageType", "M_Program je sice spojený, ale program pro Homera nikde neběží a není tedy co kam zasílat");


        terminal.write_without_confirmation(result);

    }

    public static void m_project_is_connected_with_older_version(WebSCType terminal) throws TimeoutException, InterruptedException{

        logger.warn ("Tyrion: Warning!");
        logger.warn ("Tyrion: Warning: M_Project is connected to version in Homer Server and without permission to auto-increment!");

        ObjectNode result = Json.newObject();
        result.put("messageType", "M Project je napevno navázaný na verzi, která neběží a auto-propojení je zakázáno!");
        result.put("messageChannel", "the-grid");

        terminal.write_without_confirmation(result);
    }

// Test & Control API ---------------------------------------------------------------------------------------------------------

    public static void disconnect_all_homers(){
        for (Map.Entry<String, WebSCType> entry :  WebSocketController_Incoming.incomingConnections_homers.entrySet())
        {
            server_violently_terminate_terminal(entry.getValue());
        }
    }

    public static void disconnect_all_mobiles() {
        for (Map.Entry<String, WebSCType> entry :  WebSocketController_Incoming.incomingConnections_terminals.entrySet())
        {
            server_violently_terminate_terminal(entry.getValue());
        }
    }

    public Result disconnect_all_mobiles_result() {
        for (Map.Entry<String, WebSCType> entry :  WebSocketController_Incoming.incomingConnections_terminals.entrySet())
        {
            server_violently_terminate_terminal(entry.getValue());
        }
        return ok();
    }

    public Result disconnect_all_homers_result() {
        for (Map.Entry<String, WebSCType> entry :  WebSocketController_Incoming.incomingConnections_homers.entrySet())
        {
            server_violently_terminate_terminal(entry.getValue());
        }
        return ok();
    }



}
