package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.project.b_program.B_Program;
import models.project.global.Homer;
import models.project.m_program.Grid_Terminal;
import models.project.m_program.M_Project;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import utilities.UtilTools;
import utilities.response.GlobalResult;
import utilities.webSocket.developing.WS_Homer_Local;
import utilities.webSocket.developing.WS_Terminal_Local;
import utilities.webSocket.developing.WebSCType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;


public class WebSocketController_Incoming extends Controller {

// Values  -------------------------------------------------------------------------------------------------------------

    public static Map<String, WebSCType> incomingConnections_homers = new HashMap<>(); // (Identificator, Websocket)
    public static Map<String, WebSCType> incomingConnections_terminals = new HashMap<>(); // (Identificator, Websocket)
    public static Map<String, Map<String, WebSCType>> cloud_servers = new HashMap<>(); // (Identificator, Websocket)

    // Sem podle ID homera uložím seznam zařízení, na která by se měl opět připojit
    public static Map<String, ArrayList<String>> terminal_lost_connection_homer = new HashMap<String, ArrayList<String>>();  // (Homer Identificator, List<Terminal Identifikator>)


// PUBLIC API ---------------------------------------------------------------------------------------------------------

    public WebSocket<String> homer_connection(String homer_mac_address) {

        try {
            System.out.println("Příchozí připojení Homer " + homer_mac_address);

            // Inicializuji Websocket pro Homera
            WS_Homer_Local homer = new WS_Homer_Local(homer_mac_address, incomingConnections_homers);

            // Připojím se
            WebSocket<String> webSocket = homer.connection();

            // Podívám se, zda nemám už připojené a čekající terminály. se kterými bych chtěl komunikovat
            if (terminal_lost_connection_homer.containsKey(homer_mac_address)) {
                System.out.println("Pro Homer existuje ztracené připojení a tak se pokusím navázat spojení");

                // Pookud ano, tak na seznamu jmen terminálů vázajících se k tomuto Homerovi provedu
                for (String terminal_name : terminal_lost_connection_homer.get(homer_mac_address)) {

                    // kontrolu zda terminál je stále přopojený
                    if (incomingConnections_terminals.containsKey(terminal_name)) {

                        // V případě že tedy nějaké temrinály jsou připravené komunikovat, musím upozornit homera, že chci odebírat jeho změny
                        // Takže ho u prvního oběratele budu informovat
                        if (homer.subscribers.isEmpty()) {

                            System.out.println("Spojení bylo obnoveno mezi Homer: " + homer_mac_address + " a terminalem: " + terminal_name);
                            System.out.println("Budu taktéž Homera informovat o tom, že má odběratele");
                            ask_for_receiving(homer);
                            homer.subscribers.add(incomingConnections_terminals.get(terminal_name));
                            incomingConnections_terminals.get(terminal_name).subscribers.add(homer);

                        } else {

                            System.out.println("Spojení bylo obnoveno mezi Homer: " + homer_mac_address + " a terminalem: " + terminal_name);
                            homer.subscribers.add(incomingConnections_terminals.get(terminal_name));
                            incomingConnections_terminals.get(terminal_name).subscribers.add(homer);


                        }

                        System.out.println("Upozorňuji terminál o znovu připojení: ");
                        homer_reconnection(incomingConnections_terminals.get(terminal_name));
                    }
                    // Pokud není připojený - vyřadím ho ze seznamu
                    else {
                        System.out.println("Terminál se mezitím odpojil, tak ho vyřazuji z listu: ");
                        terminal_lost_connection_homer.get(homer_mac_address).remove(terminal_name);
                    }
                }

                // Po obnově spojení se všem ztracenými připojeními vyhazuji objekt ztraceného spojení z mapy vázaný na tento Homer
                System.out.println("Odstraňuji ze seznamu ztracených spojení");
                terminal_lost_connection_homer.remove(homer_mac_address);
            } else homer_all_terminals_are_gone(homer);


            return webSocket;
        }catch (Exception e){
            e.printStackTrace();
            return WebSocket.reject(forbidden("Posralo se to"));
        }
    }
    public WebSocket<String> mobile_connection(String m_project_id, String terminal_id) {

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

            WS_Terminal_Local terminal = new WS_Terminal_Local(terminal_id, incomingConnections_terminals);
            WebSocket<String> ws = terminal.connection();

            // Tohle je cloudové nasazení B programu
            //-----------------------------------------------------------------------------------------------------------

            // TODO POKUD JE B_PROGRAM V CLOUDU ale shodná verze neexistuje ale je povoleno auto increment
            if (m_project.b_program_version.b_program_cloud == null &&  m_project.auto_incrementing &&  B_Program.find.where().isNotNull("versionObjects.b_program_cloud").findUnique() != null) {

                System.out.println("Vyhrává program puštěný v cloudu s tím že propojení bude iterováno na vyšší verzi! Yeah!");
                m_project.b_program_version = m_project.b_program.where_program_run();
                m_project.update();

            }

            // TODO POKUD JE B_PROGRAM V CLOUDU a má shodnou verzi
            if (m_project.b_program_version.b_program_cloud != null) {

                System.out.println("Vyhrává program puštěný v cloudu! Yeah!");
                return UtilTools.b_program_in_cloud(m_project, terminal_id); // TODO <- Tuhle metodu smazat a dostat kod sem! jako u stejné podmínky níže!!

            // TODO POKUD JE B_PROGRAM V CLOUDU ale nemám schodnou verzi a zakázal jsem autoincrementování
            } else if (m_project.b_program_version.b_program_cloud != null) {

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

                  String homer_id = m_project.b_program_version.b_program_homer.homer.id;
                  System.out.println("Budu propojovat s Homer: " + homer_id);

                  System.out.println("Je Homer Připojený?: " + homer_id);
                  if (!WebSocketController_Incoming.incomingConnections_homers.containsKey(homer_id)) {
                      System.out.println("   Není připojen a proto budu zařazovat do mapy ztracených spojení");

                      if (WebSocketController_Incoming.terminal_lost_connection_homer.containsKey(homer_id)) {
                          System.out.println("   Ztracené spojení už bylo dávno vytvořeno ale pořád nejsem spojen a tak přidávám další hodnotu: " + homer_id);
                          WebSocketController_Incoming.terminal_lost_connection_homer.get(homer_id).add(terminal_id);

                      } else {
                          System.out.println("   Ještě žádné ztracené spojení nebylo vytvořeno s " + homer_id + " A tak vytvářím a přidávám první hodnotu");
                          ArrayList<String> list = new ArrayList<>(4);
                          list.add(terminal_id);
                          WebSocketController_Incoming.terminal_lost_connection_homer.put(homer_id, list);
                      }

                      System.out.println("   Chystám se upozornit terminál že Local Homer není připojený");
                      WebSocketController_Incoming.homer_is_not_connected_yet(terminal);
                      System.out.println("      Upozornil jsem terminál že Homer není připojený");

                      return ws;
                  }

                  System.out.println("Budu propojovat s Homer protože je připojený: " + homer_id);
                  WebSCType homer = WebSocketController_Incoming.incomingConnections_homers.get(homer_id);

                  terminal.subscribers.add(WebSocketController_Incoming.incomingConnections_homers.get(homer_id));
                  if (homer.subscribers.isEmpty()) WebSocketController_Incoming.ask_for_receiving(homer);

                  homer.subscribers.add(terminal);
                  return ws;


                  // POKUD JE B_PROGRAM NA PC ale shodná verze neexistuje a zakázal jsem auto inkrementaci
                } else if( ! m_project.auto_incrementing && B_Program.find.where().isNotNull("versionObjects.b_program_homer").findUnique() != null){

                    m_project_is_connected_with_older_version(terminal);
                    return ws;

                } else {

                    terminal_blocko_program_not_running_anywhere(terminal);
                    return ws;
                }


        }catch (Exception e){
            e.printStackTrace();
            return WebSocket.reject(forbidden("Posralo se to"));
        }
    }

// PRIVATE Homer ---------------------------------------------------------------------------------------------------------

    /** incoming Json from Homer */
    public static void incoming_message_homer(WebSCType homer, JsonNode json){
        if(json.has("messageChannel")){

            switch (json.get("messageChannel").asText()){

                case "the-grid" : {
                    for( WebSCType webSCType : homer.subscribers) webSCType.write_without_confirmation(json);
                    return;
                }

                case "tyrion" : {
                    System.out.println ("Tyrion Příchozí zpráva, která neměla svojí žádost ze strany Tyriona: " + json.asText());
                    return;
                }

            }
        }
    }

    public static JsonNode homer_KillInstance(String homer_id) throws TimeoutException, InterruptedException {
            String messageId = UUID.randomUUID().toString();

            ObjectNode result = Json.newObject();
            result.put("messageType", "Kill Instance");
            result.put("messageId", messageId);
            result.put("messageChannel", "tyrion");

            return incomingConnections_homers.get(homer_id).write_with_confirmation(messageId, result);
    }

    public static void homer_update_embeddedHW(String homer_id, String board_id, File file) throws TimeoutException, InterruptedException, IOException {

            System.out.println("Chci nahrát binární soubor na hardware ");

            String messageId = UUID.randomUUID().toString();

            ArrayList<String> board_id_list = new ArrayList<>();
            board_id_list.add(board_id);


            ObjectNode result = Json.newObject();
            result.put("messageType", "update_device");
            result.put("messageId", messageId);
            result.set("hardwareId", Json.toJson(board_id_list));
            result.put("program", UtilTools.loadFile(file));

            incomingConnections_homers.get(homer_id).write_with_confirmation(messageId, result);
    }

    public static JsonNode homer_UploadInstance(Homer homer, String program_id, String program) throws TimeoutException, InterruptedException {

            String messageId =  UUID.randomUUID().toString();

            ObjectNode result = Json.newObject();
            result.put("messageType", "loadProgram");
            result.put("messageId", messageId);
            result.put("messageChannel", "tyrion");
            result.put("programId", program_id);
            result.put("program", program);

          return incomingConnections_homers.get( homer.id).write_with_confirmation(messageId ,result );
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
        homer.maps.remove(homer.identifikator);

        ArrayList<String> list = new ArrayList<>();

        for (WebSCType terminal : homer.subscribers) {
            list.add(terminal.identifikator);
            terminal.subscribers.remove(homer);

            echo_that_home_was_disconnect(terminal);
        }

        System.out.println("Vloženo do mapy připojení" + homer.identifikator);
        terminal_lost_connection_homer.put(homer.identifikator, list);
    }

    public static void homer_all_terminals_are_gone(WebSCType homer) {
         System.out.println("homer nemá  už žádného odběratele: " + homer.identifikator);
         homer.write_without_confirmation(Json.toJson("NEMÁŠ ŽÁDNÉ ODBĚRATELE"));
    }

    public static void ask_for_receiving(WebSCType homer){

        System.out.println ("Chci upozornit Homera: " + homer.identifikator + " že má prvního odběratele");
        homer.write_without_confirmation( Json.toJson("Máš prvního odběratele! breaker"));

    }

    public static void invalid_json_message(WebSCType homer){
        System.out.println ("NEvalidní příchozí JSON formát ve zprávě na Homerovi: " + homer.identifikator);
        homer.write_without_confirmation(Json.toJson("Posílej jen JSONy ty pačmunde!"));
    }

// PRIVATE Terminal ---------------------------------------------------------------------------------------------------------

    /** incoming Json from Terminal */
    public static void incoming_message_terminal(WebSCType terminal, JsonNode json){
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
        terminal.write_without_confirmation(Json.toJson("NEmáš žádné Grid odběratele - zprává nebyla přeposlána "));
        terminal.close();
    }

    public static void server_plained_terminate_terminal(WebSCType terminal){
        System.out.println("Server odesílá zprávu, že bude plánované odstavení");
        terminal.write_without_confirmation(Json.toJson("Ahoj, server se restartuje mezi 5 a 9"));
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
        terminal.write_without_confirmation( Json.toJson("Homer se znovu připojil!!"));
    }

    public static void homer_is_not_connected_yet(WebSCType terminal){

        System.out.println("   Chci zaslat zprávu že homer není pripojen a jsem v metodě homer_is_not_connected_yet");
        terminal.write_without_confirmation(Json.toJson("homer není pripojen!"));

    }

    public static void echo_that_home_was_disconnect(WebSCType terminal){
        System.out.println("Chci zaslat zprávu že se homer odpojil");
        terminal.write_without_confirmation(Json.toJson("Homer se odpojil"));
    }

    public static void terminal_you_have_not_followers(WebSCType terminal){
        System.out.println("Terminál se pokusil zaslat zpváu na Blocko ale žádné blocko nemá na sobě připojené - tedy zbytečné a packet zahazuji");
        terminal.write_without_confirmation(Json.toJson("NEmáš žádné Grid odběratele - zprává nebyla přeposlána "));
    }

    public static void terminal_blocko_program_not_running_anywhere(WebSCType terminal){
        System.out.println("Blocko program nikde něběží :(");
        terminal.write_without_confirmation(Json.toJson("M_Program je sice spojený, ale program pro Homera nikde neběží a není tedy co kam zasílat"));

    }

    public static void m_project_is_connected_with_older_version(WebSCType terminal){
        System.out.println("M Project je napevno navázaný na verzi, která neběží a auto-propojení je zakázáno!");
        terminal.write_without_confirmation(Json.toJson("M Project je napevno navázaný na verzi, která neběží a auto-propojení je zakázáno!"));

    }

// Test & Control API ---------------------------------------------------------------------------------------------------------

    public Result getWebSocketStats(){

        System.out.println("IncomingConnections  homer           count= " + incomingConnections_homers.size());
        System.out.println("IncomingConnections  mobile device   count= " + incomingConnections_terminals.size());
        System.out.println("OutcomingConnections servers         count= " + cloud_servers.size());


        //1
        System.out.println("\n");
        System.out.println("IncomingConnections Homers.......................................................... ");
        int k = 1;
        for (Map.Entry<String, WebSCType> entry : incomingConnections_homers.entrySet()) {
            System.out.println(k++ + ". Connection name = " + entry.getKey());
        }

        System.out.println("............................................................................. ");
        System.out.println("\n");



        //2
        System.out.println("\n");
        System.out.println("IncomingConnections Mobile device................................................... ");
        int j = 1;
        for (Map.Entry<String, WebSCType> entry : incomingConnections_terminals.entrySet()) {
            System.out.println(j++ + ". Connection name = " + entry.getKey());
        }

        System.out.println("............................................................................. ");
        System.out.println("\n");


        //3
        System.out.println("\n");
        System.out.println("OutcomingConnections.......................................................... ");
        int i = 1;
        for (Map.Entry< String, Map<String, WebSCType> > entry : cloud_servers.entrySet()) {

            System.out.println(i++ + ". Server Name name = " + entry.getKey());

            for (Map.Entry<String, WebSCType> ss : entry.getValue().entrySet() ) {
                System.out.println("     " + i++ + ". Connection name = " + entry.getKey());
            }
        }

        System.out.println("............................................................................. ");
        System.out.println("\n");

        return  GlobalResult.result_ok();
    }

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
