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
import java.util.concurrent.TimeoutException;


public class WebSocketController_Incoming extends Controller {
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

// Values  -------------------------------------------------------------------------------------------------------------

    public static Map<String, WebSCType> incomingConnections_homers = new HashMap<>(); // (Identificator, Websocket)
    public static Map<String, WebSCType> incomingConnections_terminals = new HashMap<>(); // (Identificator, Websocket)

    // Sem podle ID homera uložím seznam zařízení, na která by se měl opět připojit
    public static Map<String, ArrayList<String>> terminal_lost_connection_homer = new HashMap<>();  // (Homer Identificator, List<Terminal Identicikator>)

    // Připojené servery, kde běží Homer instance jsou drženy v homer_cloud_server. Jde jen o jendoduché čisté spojení a
    // několik servisních metod. Ale aby bylo dosaženo toho, že Homer jak v cloudu tak i na fyzickém počítači byl obsluhován stejně
    // je redundantně (jen ukazateli) vytvořeno virtuální spojení na každou instanci blocko programu v cloudu.
    public static Map<String, WebSCType> blocko_servers = new HashMap<>(); // (<Server-Identificator, Websocket> >)

    // Komnpilační servery, které mají být při kompilaci rovnoměrně zatěžovány - nastřídačku. Ale předpokladem je, že všechny dělají vždy totéž.
    public static Map<String, WS_CompilerServer> compiler_cloud_servers = new HashMap<>(); // (Server-Identificator, Websocket)


// PUBLIC API -------------------------------------------------------------------------------------------------------------------

    public  WebSocket<String>  homer_connection(String homer_mac_address){

        System.out.println("Příchozí připojení Homer " + homer_mac_address);

        // Inicializuji Websocket pro Homera
        WS_Homer_Local homer = new WS_Homer_Local(homer_mac_address, incomingConnections_homers);

        // Připojím se
        WebSocket<String> webSocket = homer.connection();
        homer_connection_procedure(homer);

        return  webSocket;
    }
    public static void homer_connection_procedure(WebSCType homer) {
        try {


            // Podívám se, zda nemám už připojené a čekající terminály. se kterými bych chtěl komunikovat
            if (terminal_lost_connection_homer.containsKey(homer.identifikator)) {
                System.out.println("Pro Homer existuje ztracené připojení a tak se pokusím navázat spojení");

                // Pookud ano, tak na seznamu jmen terminálů vázajících se k tomuto Homerovi provedu
                for (String terminal_name : terminal_lost_connection_homer.get(homer.identifikator)) {

                    // kontrolu zda terminál je stále přopojený
                    if (incomingConnections_terminals.containsKey(terminal_name)) {

                        // V případě že tedy nějaké temrinály jsou připravené komunikovat, musím upozornit homera, že chci odebírat jeho změny
                        // Takže ho u prvního oběratele budu informovat
                        if (homer.subscribers.isEmpty()) {

                            System.out.println("Spojení bylo obnoveno mezi Homer: " + homer.identifikator + " a terminalem: " + terminal_name);
                            System.out.println("Budu taktéž Homera informovat o tom, že má odběratele");
                            ask_for_receiving(homer);
                            homer.subscribers.add(incomingConnections_terminals.get(terminal_name));
                            incomingConnections_terminals.get(terminal_name).subscribers.add(homer);

                        } else {

                            System.out.println("Spojení bylo obnoveno mezi Homer: " + homer.identifikator  + " a terminalem: " + terminal_name);
                            homer.subscribers.add(incomingConnections_terminals.get(terminal_name));
                            incomingConnections_terminals.get(terminal_name).subscribers.add(homer);


                        }

                        System.out.println("Upozorňuji terminál o znovu připojení: ");
                        homer_reconnection(incomingConnections_terminals.get(terminal_name));
                    }
                    // Pokud není připojený - vyřadím ho ze seznamu
                    else {
                        System.out.println("Terminál se mezitím odpojil, tak ho vyřazuji z listu: ");
                        terminal_lost_connection_homer.get(homer.identifikator ).remove(terminal_name);
                    }
                }

                // Po obnově spojení se všem ztracenými připojeními vyhazuji objekt ztraceného spojení z mapy vázaný na tento Homer
                System.out.println("Odstraňuji ze seznamu ztracených spojení");
                terminal_lost_connection_homer.remove(homer.identifikator );
            } else homer_all_terminals_are_gone(homer);



        }catch (Exception e){
            logger.error("Homer Connection Exception", e);
        }
    }

    public  WebSocket<String>  mobile_connection(String m_project_id, String terminal_id) {

        try {
            System.out.println("Příchozí připojení Terminal " + m_project_id + " a zařízení se jménem " + terminal_id);

            if (incomingConnections_terminals.containsKey(terminal_id)) {
                System.out.println("Příchozí jméno terminálu už je aktuálně přihlášené!!");
                return WebSocket.reject(forbidden("Příchozí jméno terminálu už je aktuálně přihlášené!!"));
            }
            if (Grid_Terminal.find.where().eq("terminal_id", terminal_id).findUnique() == null) {
                System.out.println("Příchozí jméno zařízení není uloženo v databázi a je odmítnuto!!");
                return WebSocket.reject(forbidden("Příchozí jméno zařízení není uloženo v databázi a je odmítnuto!!"));
            }


            M_Project m_project = M_Project.find.byId(m_project_id);

            if (m_project == null) {
                System.out.println("Příchozí M_Projekt neexistuje");
                return WebSocket.reject(forbidden("Příchozí M_Projekt neexistuje"));
            }
            if (m_project.b_program_version == null) {
                System.out.println("Příchozí připojení M_Projektu - M_programu není propojeno s Blocko Programem");
                return WebSocket.reject(forbidden("Příchozí připojení M_Projektu - M_programu není propojeno s Blocko Programem"));
            }

            //----------------------------------------------------------------------------------------------------------------------

            System.out.println("Právě se pouštím do odhalování na co se terminál může připojit..................................");

            WS_Terminal terminal = new WS_Terminal(terminal_id, m_project_id, incomingConnections_terminals);
            WebSocket<String> ws = terminal.connection();

            // Tohle je cloudové nasazení B programu
            //-----------------------------------------------------------------------------------------------------------

            // POKUD JE B_PROGRAM V CLOUDU ale shodná verze neexistuje ale je povoleno auto increment
            if (m_project.b_program_version.b_program_cloud == null &&  m_project.auto_incrementing &&  B_Program.find.where().isNotNull("versionObjects.b_program_cloud").findUnique() != null) {

                System.out.println("Vyhrává program puštěný v cloudu s tím že propojení bude iterováno na vyšší verzi! Yeah!");
                m_project.b_program_version = m_project.b_program.where_program_run();
                m_project.update();

            }

            // POKUD JE B_PROGRAM V CLOUDU a má shodnou verzi
            if (m_project.b_program_version.b_program_cloud != null) {

                System.out.println("Vyhrává program puštěný v cloudu! Yeah!");

                String homer_identificator = m_project.b_program_version.b_program_cloud.blocko_instance_name;
                String server_name =  Cloud_Blocko_Server.find.where().eq("cloud_programs.version_object.m_project.id", m_project.id ).findUnique().server_name;

                System.out.println("Budu propojovat s Homer: " + homer_identificator);
                System.out.println("Budu propojovat se Serverem: " + server_name);

                System.out.println("Je Cloud Server Připojený?: " + blocko_servers.containsKey(server_name));
                if (!blocko_servers.containsKey(server_name)) {
                    System.out.println("   Cloud Blocko server není připojen a proto vytvořím ztracené spojení na Terminál");

                    if (terminal_lost_connection_homer.containsKey(homer_identificator)) {
                        System.out.println("   Ztracené spojení už bylo dávno vytvořeno ale pořád nejsem spojen a tak přidávám další hodnotu: " + homer_identificator);
                        terminal_lost_connection_homer.get(homer_identificator).add(terminal_id);

                    } else {
                        System.out.println("   Ještě žádné ztracené spojení nebylo vytvořeno s " + homer_identificator + " A tak vytvářím a přidávám první hodnotu");
                        ArrayList<String> list = new ArrayList<>(4);
                        list.add(terminal_id);
                        WebSocketController_Incoming.terminal_lost_connection_homer.put(homer_identificator, list);
                    }

                    System.out.println("   Chystám se upozornit terminál že Local Homer není připojený");
                    WebSocketController_Incoming.homer_is_not_connected_yet(terminal);
                    System.out.println("      Upozornil jsem terminál že Homer není připojený");

                    return ws;
                }

                System.out.println("Budu propojovat s Cloud Homerem protože je připojený: " + homer_identificator);
                WebSCType homer = WebSocketController_Incoming.incomingConnections_homers.get(homer_identificator);

                terminal.subscribers.add(WebSocketController_Incoming.incomingConnections_homers.get(homer_identificator));
                if (homer.subscribers.isEmpty()) WebSocketController_Incoming.ask_for_receiving(homer);
                homer.subscribers.add(terminal);
                return ws;

            // TODO POKUD JE B_PROGRAM V CLOUDU ale nemám schodnou verzi a zakázal jsem autoincrementování
            } else if (! m_project.auto_incrementing && B_Program.find.where().isNotNull("versionObjects.b_program_cloud").findUnique() != null){

                System.out.println("Běží nová verze v cloudu, ale je zakázáno autoincrementování!");
                m_project_is_connected_with_older_version(terminal);
                return ws;
            }



            // Tohle je fyzické nasazení B programu na PC
            //-----------------------------------------------------------------------------------------------------------



             // POKUD JE B_PROGRAM NA PC ale shodná verze neexistuje a povolil jsem auto inkrementaci
              if( m_project.b_program_version.b_program_homer == null && m_project.auto_incrementing  && B_Program.find.where().isNotNull("versionObjects.b_program_homer").findUnique() != null) {

                  System.out.println("Vyhrává program puštěný na lokálním PC ale je nutné provést opravu verzí!");
                  m_project.b_program_version = m_project.b_program.where_program_run();
                  m_project.update();

              }
              // POKUD JE B_PROGRAM NA PC a má shodnou verzi
              if (m_project.b_program_version.b_program_homer != null) {

                  System.out.println("Vyhrává program puštěný na lokálním PC! Yeah!");

                  String homer_identificator = m_project.b_program_version.b_program_homer.homer.id;
                  System.out.println("Budu propojovat s Homer: " + homer_identificator);

                  System.out.println("Je Homer Připojený?: " + incomingConnections_homers.containsKey(homer_identificator));
                  if (!WebSocketController_Incoming.incomingConnections_homers.containsKey(homer_identificator)) {
                      System.out.println("   Není připojen a proto budu zařazovat do mapy ztracených spojení");

                      if (WebSocketController_Incoming.terminal_lost_connection_homer.containsKey(homer_identificator)) {
                          System.out.println("   Ztracené spojení už bylo dávno vytvořeno ale pořád nejsem spojen a tak přidávám další hodnotu: " + homer_identificator);
                          WebSocketController_Incoming.terminal_lost_connection_homer.get(homer_identificator).add(terminal_id);

                      } else {
                          System.out.println("   Ještě žádné ztracené spojení nebylo vytvořeno s " + homer_identificator + " A tak vytvářím a přidávám první hodnotu");
                          ArrayList<String> list = new ArrayList<>(4);
                          list.add(terminal_id);
                          WebSocketController_Incoming.terminal_lost_connection_homer.put(homer_identificator, list);
                      }

                      System.out.println("   Chystám se upozornit terminál že Local Homer není připojený");
                      WebSocketController_Incoming.homer_is_not_connected_yet(terminal);
                      System.out.println("      Upozornil jsem terminál že Homer není připojený");

                      return ws;
                  }

                  System.out.println("Budu propojovat s Homer protože je připojený: " + homer_identificator);
                  WebSCType homer = WebSocketController_Incoming.incomingConnections_homers.get(homer_identificator);

                  terminal.subscribers.add(WebSocketController_Incoming.incomingConnections_homers.get(homer_identificator));
                  if (homer.subscribers.isEmpty()) WebSocketController_Incoming.ask_for_receiving(homer);

                  homer.subscribers.add(terminal);
                  return ws;


                  // POKUD JE B_PROGRAM NA PC ale shodná verze neexistuje a zakázal jsem auto inkrementaci
                } else if( ! m_project.auto_incrementing && B_Program.find.where().isNotNull("versionObjects.b_program_homer").findUnique() != null){

                    m_project_is_connected_with_older_version(terminal);
                    return ws;

                }


                 terminal_blocko_program_not_running_anywhere(terminal);
                 return ws;



        }catch (Exception e){
            logger.error("Mobile / terminal Web Socket connection", e);
            return WebSocket.reject(forbidden("Server side error"));
        }
    }
    public  WebSocket<String>  blocko_cloud_server_connection(String server_name){
        try{
            System.out.println("Připojuje se mi homer cloud server: " + server_name);

            System.out.println("Ověřuji zda je server "+ server_name + " platný a mohu ho nechat připojit"); // TODO - přidat ověření ještě pomocí HASHe co už je v objektu definován
            Cloud_Blocko_Server blocko_server = Cloud_Blocko_Server.find.where().eq("server_name", server_name).findUnique();
            if(blocko_server== null) return WebSocket.reject(forbidden("Server side error - unrecognized name"));

            if(blocko_servers.containsKey(server_name)) {
                System.out.println("Na Tyrionovi je už připojen Homer Cloud server a tak zkouším zaslat ping");
                try {
                    blocko_servers.get(server_name).out.write("ping");
                }catch (Exception e){
                    System.out.println("Ping se nezdařil - server ztratil spojení a tak ho odmazávám");
                    blocko_servers.get(server_name).onClose();
                }
                System.out.println("Na Tyrionovi je už připojen Homer Cloud server se stejným jménem - nedovolím další připojení");
                return WebSocket.reject(forbidden("Server side error - already connected"));
            }
            System.out.println("Inicializuji Websocket pro Blocko cloud server:");
            // Inicializuji Websocket pro Homera
            WS_BlockoServer server = new WS_BlockoServer(server_name, blocko_servers);

            // Připojím se
            System.out.println("Připojuji se:");
            WebSocket<String> webSocket = server.connection();

            // Procedury kontroly - informovat třeba všechny klienty o tom, že se server připojil. Kontzrola co tam běží a další píčoviny

            System.out.println("Dochází ke kontrole serveru");

            //1 GET state - a vyhodnocením v jakém stavu se server nachází a popřípadě
            // na něj nahraji nebo smažu nekonzistenntí clou dprogramy, které by na něm měly být

            class Control_Blocko_Server_Thread extends Thread{

                @Override
                public void run() {
                   Long interrupter = (long) 150000;
                    try {

                        while (interrupter > 0) {


                            sleep(500);
                            interrupter-=500;

                            System.out.println("Spouštím kontrolu zda na Blocko serveru běží vše tak jak má");
                            // System.out.println("Zbávý času " + interrupter);
                            if (server.isReady()) {


                                String messageId = UUID.randomUUID().toString();

                                ObjectNode result = Json.newObject();
                                result.put("messageType", "listInstances");
                                result.put("messageId", messageId);
                                result.put("messageChannel", "homer-server");

                                System.out.println("Zasílám žádost co na serveru běží");

                                JsonNode jsonNode = server.write_with_confirmation(messageId, result, (long) 15000);

                                System.out.println("Žádost o stavu serveru přijata");
                                if (jsonNode.has("status")) {

                                    if(jsonNode.get("status").equals("error")){
                                        System.out.println("Server odpověděl chybou - reakce nebude žádná");
                                        interrupt();
                                    }

                                    System.out.println("Server odpověděl a zaslal pole instancí");

                                    List<String> instances_on_server = new ArrayList<>();
                                        final JsonNode arrNode = jsonNode.get("instances");
                                        for (final JsonNode objNode : arrNode) instances_on_server.add(objNode.asText());
                                        System.out.println("Počet běžících instancí je " + instances_on_server.size());


                                    List<String> instances_in_database = new ArrayList<>();
                                        for(B_Program_Cloud cloud_program: B_Program_Cloud.find.where().eq("server.id", blocko_server.id).select("blocko_instance_name").findList() ) instances_in_database.add(cloud_program.blocko_instance_name);
                                        System.out.println("Počet instancí které by měly běžet na serveru " + instances_in_database.size());
                                    //

                                    List<String> instances_on_server_copy   = new ArrayList<>(instances_on_server);
                                    System.out.println("Počet obdržených instancí z Blocko serveru " + instances_on_server_copy.size());

                                    List<String> instances_in_database_copy = new ArrayList<>(instances_in_database);
                                    System.out.println("Počet instancí z Databáze " + instances_in_database_copy.size());

                                    instances_in_database.removeAll(instances_on_server);
                                    System.out.println("Počet instancí které se budou nahrávat na server " + instances_in_database.size());
                                    for(String instance : instances_in_database) System.out.println("Instance která by měla být na Blocko serveru " + instance);

                                    instances_on_server_copy.removeAll(instances_in_database_copy);
                                    System.out.println("Počet instancí které se budou mazat ze serveru " + instances_on_server_copy.size());
                                    for(String instance : instances_on_server_copy) System.out.println("Instance kterou bych měl smazat z blocko serveru " + instance);

                                    List<B_Program_Cloud> b_programs = B_Program_Cloud.find.where().in("blocko_instance_name",  instances_in_database ).findList();

                                    if(!b_programs.isEmpty()) System.out.println("Začínám na server nahrávat nové instance ");
                                    for (B_Program_Cloud b_program : b_programs) {
                                        try {
                                            WebSocketController_Incoming.blocko_server_add_instance(server, b_program);
                                        }catch (Exception e){
                                            e.printStackTrace();
                                            System.out.println("Instance " + b_program.blocko_instance_name + " se nepodařilo správně nahrát na blocko server");
                                        }
                                    }

                                    if(!instances_on_server_copy.isEmpty()) System.out.println("Začínám se zerveru odtraňovat instnance ");
                                    for (String blocko_instance_name : instances_on_server_copy) {
                                        try {
                                           JsonNode remove_instance = blocko_server_remove_instance(server, blocko_instance_name);
                                            // TODO teoreticky bych měl reagovat na to když se to nepovede (když přijde v Json "Error" ??
                                        }catch (Exception e){
                                            System.out.println("Nepodařilo se ze serveru odstranit instanci");
                                        }
                                    }

                                    System.out.println("Ukončuji úspěšně spouštěcí proceduru");
                                    interrupter = (long) 0;

                                }else throw new Exception("Result hasn't status in Json");
                            }
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }

            new Control_Blocko_Server_Thread().start();

            System.out.println("Připojil jsem se");
            return webSocket;

        }catch (Exception e){
            Loggy.error("Cloud Blocko Server  Web Socket connection", e);
            return WebSocket.reject(forbidden("Server side error"));
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
            WS_CompilerServer server = new WS_CompilerServer( cloud_compilation_server.server_name, cloud_compilation_server.destination_address);
            // Připojím se
            return server.connection();


        }catch (Exception e){
            Loggy.error("Cloud Compiler Server Web Socket connection", e);
            return WebSocket.reject(forbidden("Server side error"));
        }
    }

// PRIVATE Blocko-Server ---------------------------------------------------------------------------------------------------------

    public static void blocko_server_is_disconnect(WS_BlockoServer blockoServer){
        System.out.println("Ztráta spojení s Blocko serverem: " + blockoServer.identifikator);
        System.out.println("Je nutná dodělat reakce na ztrátu spojení??? : ");
        blocko_servers.remove(blockoServer.identifikator);
    }

    public static JsonNode blocko_server_listOfInstance(WS_BlockoServer ws_blockoServer)  throws TimeoutException, InterruptedException{
        String messageId = UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "listInstances");
        result.put("messageId", messageId);
        result.put("messageChannel", "homer-server");

        return ws_blockoServer.write_with_confirmation(messageId, result);
    }

    public static JsonNode blocko_server_isInstanceExist(WS_BlockoServer ws_blockoServer, String instance_name)  throws TimeoutException, InterruptedException{
        String messageId = UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "instanceExist");
        result.put("messageId", messageId);
        result.put("messageChannel", "homer-server");
        result.put("instanceId", instance_name);

        return ws_blockoServer.write_with_confirmation(messageId, result);
    }

    public static void blocko_server_incoming_message(WS_BlockoServer blockoServer, JsonNode json){
        System.out.println("Speciálně  server2server přišla zpráva: " + json.asText() );
        System.out.println("Zatím není implementovaná žádná reakce na příchozá zprávu z Blocko serveru !!" );
    }

    public static JsonNode blocko_server_add_instance(WS_BlockoServer blockoServer, B_Program_Cloud program) throws Exception, TimeoutException, InterruptedException{

            System.out.println("Nahrávám novou instanci na server " + blockoServer.identifikator);

            if (WebSocketController_Incoming.incomingConnections_homers.containsKey(program.blocko_instance_name)) {

                System.out.println("Při přidávání instance do serveru: " + blockoServer.identifikator + " bylo zjištěno že v mapě už existuje jméno homera");
                ObjectNode result = Json.newObject();
                result.put("status", "success");
                return result;
            }

            System.out.println("Vytvářím nového virtuálního Homera");
            WS_Homer_Cloud homer = new WS_Homer_Cloud(program.blocko_instance_name, blockoServer);


            String messageId = UUID.randomUUID().toString();

            ObjectNode result = Json.newObject();
            result.put("messageType", "createInstance");
            result.put("messageId", messageId);
            result.put("messageChannel", "homer-server");
            result.put("instanceId", program.blocko_instance_name);
            result.put("macAddress", program.blocko_instance_name);

            System.out.println("Nahrávám ho na Blocko server novou instanci");
            JsonNode result_instance = blockoServer.write_with_confirmation( messageId, result);

            System.out.println("Nahrávám ho na Blocko server do vytvořené instnace program");
            JsonNode result_uploud = WebSocketController_Incoming.homer_UploadProgram(homer, program.id, program.version_object.files.get(0).get_fileRecord_from_Azure_inString());

            System.out.println("Přidávám nového virtuálního Homera do privátní mapy blocko serveru");
            blockoServer.virtual_homers.put(program.blocko_instance_name, homer);

            System.out.println("Spouštím připojovací proceduru");
            homer_connection_procedure(homer);

            incomingConnections_homers.put(homer.identifikator, homer);

            return result;

    }

    public static JsonNode blocko_server_remove_instance( WS_BlockoServer blockoServer, String instance_name) throws TimeoutException, InterruptedException{

        String messageId =  UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "destroyInstance");
        result.put("messageId", messageId);
        result.put("messageChannel", "homer-server");
        result.put("instanceId", instance_name);

        return blockoServer.write_with_confirmation( messageId ,result );

    }

    public static void blocko_server_ping(WS_BlockoServer blockoServer) throws TimeoutException, InterruptedException {

        String messageId = UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "ping");
        result.put("messageId", messageId);
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

    public static JsonNode compiler_server_make_Compilation(Person compilator, Version_Object library_version, String code) throws TimeoutException, InterruptedException {

        // 1. Vybrat náhodný server kde se provede kompilace

        List<String> keys      = new ArrayList<>(compiler_cloud_servers.keySet());
        WS_CompilerServer  server  = compiler_cloud_servers.get( keys.get( new Random().nextInt(keys.size()) ));

        String messageId = UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "build");
        result.put("messageId", messageId);
        result.put("target", "SOME");
        result.put("libVersion", library_version.version_name);
        result.put("code", code);

        return server.write_with_confirmation(messageId, result, (long) 250*4*30 );
    }

    public static void compiler_server_ping(WS_CompilerServer compilerServer) throws TimeoutException, InterruptedException {

        String messageId = UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "ping");
        result.put("messageId", messageId);
        result.put("messageChannel", "tyrion");

        compilerServer.write_without_confirmation(result);
    }

// PRIVATE Homer -----------------------------------------------------------------------------------------------------------------

    /** incoming Json from Homer */
    public static void homer_incoming_message(WebSCType homer, ObjectNode json){
        if(json.has("messageChannel")){

            switch (json.get("messageChannel").asText()){

                case "the-grid" : {
                    if(homer.subscribers != null && !homer.subscribers.isEmpty()) for( WebSCType webSCType : homer.subscribers) webSCType.write_without_confirmation(json);
                    return;
                }

                case "tyrion" : {
                    System.out.println ("Tyrion Příchozí zpráva, která neměla svojí žádost ze strany Tyriona: " + json.asText());
                    return;
                }

                default: System.out.println("Příchozí zpráva nemá řídící string a tak nebyla nikam předána!!"  + json.asText() );

            }
        }
    }

    public static JsonNode homer_getState(String homer_id) throws TimeoutException, InterruptedException {
        String messageId = UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "getState");
        result.put("messageId", messageId);
        result.put("messageChannel", "homer-server");

        return incomingConnections_homers.get(homer_id).write_with_confirmation(messageId, result);
    }

    public static void homer_ping(String homer_id) throws TimeoutException, InterruptedException {
        String messageId = UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "ping");
        result.put("messageId", messageId);
        result.put("messageChannel", "homer-server");

        incomingConnections_homers.get(homer_id).write_without_confirmation(result);
    }

    public static void homer_disconnect(WebSCType homer) throws TimeoutException, InterruptedException {
        System.out.println("Chystám se násilně odpojit homer");
        homer.onClose();
    }

    public static JsonNode homer_destroyInstance(String homer_id) throws TimeoutException, InterruptedException {
            String messageId = UUID.randomUUID().toString();

            ObjectNode result = Json.newObject();
            result.put("messageType", "destroyInstance");
            result.put("messageId", messageId);
            result.put("messageChannel", "homer-server");

            return incomingConnections_homers.get(homer_id).write_with_confirmation(messageId, result);
    }

    public static void homer_update_embeddedHW(String homer_id, List<String> board_id_list, byte[] fileInBase64) throws TimeoutException, InterruptedException, IOException {

            System.out.println("Chci nahrát binární soubor na hardware ");

            String messageId = UUID.randomUUID().toString();

            ObjectNode result = Json.newObject();
            result.put("messageType", "updateDevice");
            result.put("messageId", messageId);
            result.set("hardwareId", Json.toJson(board_id_list));
            result.put("base64Binary", fileInBase64);

            incomingConnections_homers.get(homer_id).write_with_confirmation(messageId, result);
    }

    public static JsonNode homer_UploadProgram(WebSCType homer, String program_id, String program) throws TimeoutException, InterruptedException {

            String messageId =  UUID.randomUUID().toString();

            ObjectNode result = Json.newObject();
            result.put("messageType", "loadProgram");
            result.put("messageId", messageId);
            result.put("messageChannel", "tyrion");
            result.put("programId", program_id);
            result.put("program", program);

          return homer.write_with_confirmation(messageId ,result );
    }

    public static boolean homer_is_online(String homer_id){
        return incomingConnections_homers.containsKey(homer_id);
    }

    public static JsonNode get_all_Connected_HW_to_Homer(Homer homer) throws TimeoutException, InterruptedException{
        String messageId = UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "getDeviceList");
        result.put("messageId", messageId);
        result.put("messageChannel", "tyrion");

        return incomingConnections_homers.get( homer.id).write_with_confirmation(messageId, result, (long) 250*25 );
    }

    // Homer se odpojil
    public static void homer_is_disconnect(WebSCType homer) {

        System.out.println("Homer se odlásil - mažu z mapy připojení ale přidávám do mapy ztracených připojení" + homer.identifikator);
        incomingConnections_homers.remove(homer.identifikator);

        ArrayList<String> list = new ArrayList<>();

        for (WebSCType terminal : homer.subscribers) {
            list.add(terminal.identifikator);
            terminal.subscribers.remove(homer);

            echo_that_home_was_disconnect(terminal);
        }

        System.out.println("Vloženo do mapy připojení" + homer.identifikator);
        terminal_lost_connection_homer.put(homer.identifikator, list);
    }

    public static void homer_all_terminals_are_gone(WebSCType homer) throws TimeoutException, InterruptedException {

        System.out.println("homer nemá  už žádného odběratele: " + homer.identifikator);

        String messageId =  UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "unSubscribeChannel");
        result.put("messageId", messageId);
        result.put("messageChannel", "the-grid");

        homer.write_with_confirmation( messageId, result);
    }

    public static void ask_for_receiving(WebSCType homer) throws TimeoutException, InterruptedException {
        System.out.println ("Chci upozornit Homera: " + homer.identifikator + " že má prvního odběratele");

        String messageId =  UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "subscribeChannel");
        result.put("messageId", messageId);
        result.put("messageChannel", "the-grid");

        homer.write_with_confirmation(messageId, result);
    }

    public static void invalid_json_message(WebSCType homer){
        String messageId =  UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "JsonUnrecognized");
        result.put("messageId", messageId);

        homer.write_without_confirmation(result);
    }

// PRIVATE Terminal ---------------------------------------------------------------------------------------------------------

    /** incoming Json from Terminal */
    public static void incoming_message_terminal(WebSCType terminal, ObjectNode json){
        // To Terminals
        if(json.has("messageChannel")){


            System.out.println("messageChannel je: " + json.get("messageChannel").asText()  );
            switch ( json.get("messageChannel").asText() ){

                case "the-grid" : {
                    System.out.println("the-grid - Přeposílám na odběratele");

                    if(terminal.subscribers.isEmpty()) terminal_you_have_not_followers(terminal);
                    for( WebSCType webSCType :  terminal.subscribers) {

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



        String messageId =  UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "NEmáš žádné Grid odběratele - zprává nebyla přeposlána \"");
        result.put("messageId", messageId);

        terminal.write_without_confirmation(result);

        try {
            terminal.close();
        }catch (Exception e){}
    }

    public static void terminal_ping(WebSCType terminal) throws TimeoutException, InterruptedException {

        String messageId = UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "ping");
        result.put("messageId", messageId);
        result.put("messageChannel", "tyrion");

        terminal.write_without_confirmation(result);

    }

    public static void terminal_disconnect(WebSCType terminal) throws TimeoutException, InterruptedException {
        System.out.println("Chystám se násilně odpojit terminal");
        terminal.onClose();
    }

    public static void server_plained_terminate_terminal(WebSCType terminal){
        System.out.println("Server odesílá zprávu, že bude plánované odstavení");

        String messageId =  UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "Ahoj, server se restartuje mezi 5 a 9");
        result.put("messageId", messageId);



        terminal.write_without_confirmation(result);
    }

    public static void terminal_is_disconnected(WebSCType terminal){

        terminal.maps.remove(terminal.identifikator);

        for(WebSCType subscriber : terminal.subscribers){
            System.out.println("Remove from subscriber list  " + subscriber.identifikator);
            subscriber.subscribers.remove(terminal);
            if(subscriber.subscribers.isEmpty()){

                try {
                    WebSocketController_Incoming.homer_all_terminals_are_gone(subscriber);
                }catch (Exception e){ System.out.println("Při odesílání informace so tom, že Homera, už nikoho neposlouchá se něco posralo");}
            }
        }

    }

    public static void homer_reconnection(WebSCType terminal){
        System.out.println("Chci upozornit terminál že se Homer Připojil: ");

        String messageId =  UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "Homer se znovu připojil!!");
        result.put("messageId", messageId);

        terminal.write_without_confirmation(result);
    }

    public static void homer_is_not_connected_yet(WebSCType terminal){

        System.out.println("Chci zaslat zprávu že homer není pripojen a jsem v metodě homer_is_not_connected_yet");


        String messageId =  UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "homer není pripojen!");
        result.put("messageId", messageId);

        terminal.write_without_confirmation(result);

    }

    public static void echo_that_home_was_disconnect(WebSCType terminal){
        System.out.println("Chci zaslat zprávu že se homer odpojil");

        String messageId =  UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "unSubscribeChannel");
        result.put("messageId", messageId);
        result.put("messageChannel", "the-grid");

        terminal.write_without_confirmation(result);
    }

    public static void terminal_you_have_not_followers(WebSCType terminal){
        System.out.println("Terminál se pokusil zaslat zpváu na Blocko ale žádné blocko nemá na sobě připojené - tedy zbytečné a packet zahazuji");


        String messageId =  UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "NEmáš žádné Grid odběratele - zprává nebyla přeposlána ");
        result.put("messageId", messageId);

        terminal.write_without_confirmation(result);
    }

    public static void terminal_blocko_program_not_running_anywhere(WebSCType terminal){
        System.out.println("Blocko program nikde něběží");

        String messageId =  UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "M_Program je sice spojený, ale program pro Homera nikde neběží a není tedy co kam zasílat");
        result.put("messageId", messageId);

        terminal.write_without_confirmation(result);

    }

    public static void m_project_is_connected_with_older_version(WebSCType terminal) throws TimeoutException, InterruptedException{
        System.out.println("M Project je napevno navázaný na verzi, která neběží a auto-propojení je zakázáno!");

        String messageId =  UUID.randomUUID().toString();

        ObjectNode result = Json.newObject();
        result.put("messageType", "M Project je napevno navázaný na verzi, která neběží a auto-propojení je zakázáno!");
        result.put("messageId", messageId);

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
