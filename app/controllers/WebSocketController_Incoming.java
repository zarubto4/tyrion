package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.project.b_program.B_Program;
import models.project.b_program.B_Program_Homer;
import models.project.m_program.M_Project;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import utilities.response.GlobalResult;
import utilities.webSocket.developing.WS_Homer_Cloud;
import utilities.webSocket.developing.WS_Homer_Local;
import utilities.webSocket.developing.WS_Terminal_Local;
import utilities.webSocket.developing.WebSCType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;


public class WebSocketController_Incoming extends Controller {

// Values  -------------------------------------------------------------------------------------------------------------

    private static Map<String, WebSCType > incomingConnections_homers              = new HashMap<>(); // (Identificator, Websocket)
    private static Map<String, WebSCType > incomingConnections_terminals           = new HashMap<>(); // (Identificator, Websocket)
    public static  Map< String, Map<String, WebSCType> > cloud_servers             = new HashMap<>(); // (Identificator, Websocket)

    // Sem podle ID homera uložím seznam zařízení, na která by se měl opět připojit
    private static Map<String,  ArrayList<String> > terminal_lost_connection_homer = new HashMap< String , ArrayList<String> >();  // (Homer Identificator, List<Terminal Identifikator>)


// PUBLIC API ---------------------------------------------------------------------------------------------------------

    public WebSocket<String> homer_connection(String homer_mac_address){
        System.out.println("Příchozí připojení Homer " + homer_mac_address);

        // Inicializuji Websocket pro Homera
        WS_Homer_Local homer = new WS_Homer_Local(homer_mac_address, incomingConnections_homers);

        // Připojím se
        WebSocket<String> webSocket = homer.connection();

        // Podívám se, zda nemám už připojené a čekající terminály. se kterými bych chtěl komunikovat
        if(  terminal_lost_connection_homer.containsKey(homer_mac_address)  ){
            System.out.println("Pro Homer existuje ztracené připojení a tak se pokusím navázat spojení");

            // Pookud ano, tak na seznamu jmen terminálů vázajících se k tomuto Homerovi provedu
            for(String terminal_name : terminal_lost_connection_homer.get(homer_mac_address) ){

                // kontrolu zda terminál je stále přopojený
                if( incomingConnections_terminals.containsKey(terminal_name)){

                    // V případě že tedy nějaké temrinály jsou připravené komunikovat, musím upozornit homera, že chci odebírat jeho změny
                    // Takže ho u prvního oběratele budu informovat
                    if( homer.subscribers.isEmpty()){

                        System.out.println("Spojení bylo obnoveno mezi Homer: " + homer_mac_address + " a terminalem: " + terminal_name);
                        System.out.println("Budu taktéž Homera informovat o tom, že má odběratele");
                        ask_for_receiving(homer);
                        homer.subscribers.add(incomingConnections_terminals.get(terminal_name));
                        incomingConnections_terminals.get(terminal_name).subscribers.add(homer);

                    }else {

                        System.out.println("Spojení bylo obnoveno mezi Homer: " + homer_mac_address + " a terminalem: " + terminal_name);
                        homer.subscribers.add(incomingConnections_terminals.get(terminal_name));
                        incomingConnections_terminals.get(terminal_name).subscribers.add(homer);


                    }

                    System.out.println("Upozorňuji terminál o znovu připojení: ");
                    homer_reconnection ( incomingConnections_terminals.get(terminal_name) );
                }
                // Pokud není připojený - vyřadím ho ze seznamu
                else {
                    System.out.println("Terminál se mezitím odpojil, tak ho vyřazuji z listu: ");
                    terminal_lost_connection_homer.get(homer_mac_address).remove(terminal_name);
                }
            }

            // Po obnově spojení se všem ztracenými připojeními vyhazuji objekt ztraceného spojení z mapy vázaný na tento Homer
            System.out.println ("Odstraňuji ze seznamu ztracených spojení");
            terminal_lost_connection_homer.remove(homer_mac_address);
        }else homer_all_terminals_are_gone(homer);


        return webSocket;
    }

    public WebSocket<String> mobile_connection(String m_project_id, String terminal_id){
        System.out.println ("Příchozí připojení Terminal " + m_project_id + " a zařízení se jménem " + terminal_id);

        M_Project m_project = M_Project.find.byId(m_project_id);
        if(m_project == null ) {
            System.out.println("Příchozí M_Projekt neexistuje");
            return WebSocket.reject(forbidden());
        }

        if(m_project.b_program_version == null){
            System.out.println("Příchozí připojení M_Projektu - M_programu není propojeno s Blocko Programem");
            return WebSocket.reject(forbidden());
        }

        // POKUD JE B_PROGRAM V CLOUDU
        if( m_project.b_program_version.b_program_cloud != null) {


            System.out.println ("Blocko Program je v Cloudu a to zatím není plně otestované!!");

            String instance_name = m_project.b_program_version.b_program_cloud.blocko_instance_name;
            String server_name   = m_project.b_program_version.b_program_cloud.blocko_server_name;


            WebSCType terminal = new WS_Terminal_Local(terminal_id, incomingConnections_terminals);
            WebSocket<String> ws = terminal.connection();

            if(!cloud_servers.containsKey(server_name)) {
                System.out.println("BLocko program je provozován na serveru, který není připojen...");
                System.out.println ("Měl bych ho zařadit terminál do seznamu ztracených připojení");

                if(terminal_lost_connection_homer.containsKey(instance_name)){
                    System.out.println("Ztracené spojení už bylo dávno vytvořeno s cloud programem s jiným prvkem ale pořád nejsem spojen a tak přidávám další hodnotu: " + instance_name );
                    terminal_lost_connection_homer.get(instance_name).add(terminal_id);
                }else {
                    System.out.println("Ještě žádné ztracené spojení nebylo vytvořeno s " + instance_name  + " A tak vytvářím a přidávám první hodnotu");
                    ArrayList<String> list = new ArrayList<>(4);
                    list.add(terminal_id);
                    terminal_lost_connection_homer.put(instance_name, list);
                }
                System.out.println("Chystám se upozornit terminál že Cloud Homer není připojený");
                homer_is_not_connected_yet(terminal);
                System.out.println("Upozornil jsem terminál že Homer není připojený");
                return ws;
            }

            if(!cloud_servers.get(server_name).containsKey(instance_name)){
                System.out.println("Konkrétní instance B_programu není na serveru zprovozněna!");
                System.out.println ("Měl bych ho zprovoznit? Ještě asi nejsem na to připraven"); // TODO - zprovoznit připojení

                return WebSocket.reject(badRequest());
            }

            System.out.println("Budu propojovat s Homererem v cloudu protože je připojený a instance běží: ");

            WS_Homer_Cloud homer = (WS_Homer_Cloud) cloud_servers.get(server_name).get(instance_name);


            terminal.subscribers.add(homer);
            if(homer.subscribers.isEmpty()) ask_for_receiving(homer);
            homer.subscribers.add(terminal);

            return ws;

        // POKUD JE B_PROGRAM NA RPI
        } else {
            System.out.println("Blocko Program je na počítači");

            if(m_project.b_program_version.b_program_homer == null) {
                System.out.println("Grid program byl navázán na jinou verzi, než aktuálně běží v Homerovi!");

                System.out.println("Byl Grid projekt  nastaven tak, aby sám iteroval na nejvyšší verzi? ");
                if(!m_project.auto_incrementing)  return WebSocket.reject(forbidden());

                System.out.println("Ano byl a tak beru nejvyšší verzi a přepisuju propojení");

                B_Program b_program = m_project.b_program_version.b_program;
                B_Program_Homer b_program_homer =  b_program.versionObjects.get(0).b_program_homer;

                System.out.println("Budu posunovat na vyšší verzi! a to na id: " + b_program_homer.id);

                m_project.b_program_version = b_program.versionObjects.get(0);
                m_project.update();


            }



            String homer_id = m_project.b_program_version.b_program_homer.homer.homer_id;
            System.out.println("Budu propojovat s Homer: " + homer_id);

            if(!incomingConnections_homers.containsKey(homer_id)) {
                System.out.println ("Homer na který se chci připojit není připojen a proto budu zařazovat do mapy ztracených spojení");
                WS_Terminal_Local terminal = new WS_Terminal_Local(terminal_id, incomingConnections_terminals);
                WebSocket<String> ws = terminal.connection();

                if(terminal_lost_connection_homer.containsKey(homer_id)){
                    System.out.println("Ztracené spojení už bylo dávno vytvořeno ale pořád nejsem spojen a tak přidávám další hodnotu: " + homer_id );
                    terminal_lost_connection_homer.get(homer_id).add(terminal_id);

                }else  {
                    System.out.println("Ještě žádné ztracené spojení nebylo vytvořeno s " + homer_id  + " A tak vytvářím a přidávám první hodnotu");
                    ArrayList<String> list = new ArrayList<>(4);
                    list.add(terminal_id);
                    terminal_lost_connection_homer.put(homer_id, list);
                }

                System.out.println("Chystám se upozornit terminál že Local Homer není připojený");
                homer_is_not_connected_yet(terminal);
                System.out.println("Upozornil jsem terminál že Homer není připojený");

                return ws;
            }

            System.out.println("Budu propojovat s Homer protože je připojený: " + homer_id);

            WebSCType homer = incomingConnections_homers.get(homer_id);

            WS_Terminal_Local terminal = new WS_Terminal_Local(terminal_id, incomingConnections_terminals);
            WebSocket<String> ws = terminal.connection();

            terminal.subscribers.add(incomingConnections_homers.get(homer_id));

            if(homer.subscribers.isEmpty()) ask_for_receiving(homer);

            homer.subscribers.add(terminal);

            return ws;
        }



    }

// PRIVATE Homer ---------------------------------------------------------------------------------------------------------

    /** incoming Json from Homer */
    public static void incoming_message_homer(WebSCType homer, JsonNode json){
        System.out.println ("Zpráva od Homera" + homer.identifikator +  " Obsah " + json.toString());

        if(json.has("messageChannel")){
            System.out.println("Zpráva obsahuje message channel: " + json.get("messageChannel").asText());

            switch (json.get("messageChannel").asText()){

                case "the-grid" : {
                    for( WebSCType webSCType : homer.subscribers) webSCType.write_without_confirmation(json);
                    return;
                }

                case "tyrion" : {
                    System.out.println ("Tyrion Dále zpracovávám -- ale nic tu zatím není!!");

                }

            }
        }

        System.out.println("Příchozí zpráva neobsahovala messageChannel a tak se nic neprovedlo");
    }

    public static void homer_KillInstance(String homer_id) {
        try {

            System.out.println("homer_KillInstance " + homer_id);
            incomingConnections_homers.get(homer_id).write_without_confirmation(Json.toJson("Kill Instance"));

            String messageId = UUID.randomUUID().toString();

            ObjectNode result = Json.newObject();
            result.put("messageType", "Kill Instance");
            result.put("messageId", messageId);
           // result.put("messageChannel", "tyrion");

            JsonNode answare = incomingConnections_homers.get(homer_id).write_with_confirmation(messageId, result);

        } catch (TimeoutException e){
            System.out.println("TimeoutException");
        } catch (InterruptedException e){
            System.out.println("InterruptedException");
        }
    }

    public static void homer_UploadInstance(String homer_id, String program) throws TimeoutException, InterruptedException {
        try {

            System.out.println ("homer_UploadInstance " + homer_id);

            String messageId =  UUID.randomUUID().toString();

            ObjectNode result = Json.newObject();
            result.put("messageType", "loadProgram");
            result.put("messageId", messageId);
            result.put("messageChannel", "tyrion");
            result.put("program", program);

            JsonNode answare =  incomingConnections_homers.get(homer_id).write_with_confirmation(messageId ,result );

        } catch (TimeoutException e){
            System.out.println("TimeoutException");
        } catch (InterruptedException e){
            System.out.println("InterruptedException");
        }
    }

    public static boolean homer_is_online(String homer_id){
        return incomingConnections_homers.containsKey(homer_id);
    }

    // Homer se odpojil
    public static void homer_is_disconnect(WebSCType homer) {
        System.out.println("Homer se odlásil - mažu z mapy připojení ale přidávám do mapy ztracených připojení" + homer.identifikator);
        homer.maps.remove(homer);

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

                    if(terminal.subscribers.isEmpty()) terminal_you_have_not_folowers(terminal);
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

        System.out.println("Chci zaslat zprávu že homer není propojen a jsem v metodě homer_is_not_connected_yet");

        Thread t1 = new Thread(){
            @Override
            public void run() {
                try {
                    System.out.println("Zapínám čekací vlákno");

                    Integer breaker = 10;

                    while(breaker > 0){
                        breaker--;
                        Thread.sleep(250);
                        if(terminal.isReady()) {
                            terminal.write_without_confirmation(Json.toJson("homer není propojen a stihnul jsem to do breaker: " + breaker));
                            System.out.println("Upozornil jsem terminál že homer není propojen ");
                            break;
                        }
                    }

                }catch (Exception e){
                    System.out.println("Během informování terminlu, že homer není připojen se to pokazilo ");
                    // e.printStackTrace();
                }

            }
        };

        t1.start();


    }

    public static void echo_that_home_was_disconnect(WebSCType terminal){
        System.out.println("Chci zaslat zprávu že se homer odpojil");
        terminal.write_without_confirmation(Json.toJson("Homer se odpojil"));
    }

    public static void terminal_you_have_not_folowers(WebSCType terminal){
        System.out.println("Terminál se pokusil zaslat zpváu na Blocko ale žádné blocko nemá na sobě připojené - tedy zbytečné a packet zahazuji");
        terminal.write_without_confirmation(Json.toJson("NEmáš žádné Grid odběratele - zprává nebyla přeposlána "));
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

        return  GlobalResult.okResult();
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


}
