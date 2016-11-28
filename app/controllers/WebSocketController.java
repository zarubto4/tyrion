package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import models.compiler.Cloud_Compilation_Server;
import models.compiler.Version_Object;
import models.notification.Notification;
import models.person.Person;
import models.project.b_program.instnace.Homer_Instance;
import models.project.b_program.servers.Cloud_Homer_Server;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.WebSocket;
import utilities.enums.Compile_Status;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured_API;
import utilities.loginEntities.TokenCache;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.swagger.outboundClass.Swagger_Websocket_Token;
import utilities.webSocket.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Api(value = "Not Documented API - InProgress or Stuck")
public class WebSocketController extends Controller {

// Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

// Values  --------------------------------------------- ----------------------------------------------------------------

    // Připojené servery, kde běží Homer instance jsou drženy v homer_cloud_server. Jde jen o jednoduché čisté spojení a
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

    @ApiOperation(value = "Homer Server Connection", tags = {"WebSocket"})
    public  WebSocket<String>  homer_cloud_server_connection(String server_name){
        try{

            logger.debug("Homer Server:: Connection:: Incoming connection: Server:  " + server_name);

            Cloud_Homer_Server homer_server = Cloud_Homer_Server.find.where().eq("server_name", server_name).findUnique();
            if(homer_server== null) return WebSocket.reject(forbidden("Server side error - unrecognized name"));

            if(blocko_servers.containsKey(server_name)) {
                logger.warn("Homer Server:: Connection:: Server is connected -> Tyrion try to send ping");

                WS_BlockoServer ws_blockoServer = (WS_BlockoServer) blocko_servers.get(server_name);
                JsonNode result = homer_server.ping();
                if(!result.get("status").asText().equals("success")){
                    logger.warn("Homer Server:: Connection:: Ping Failed - Tyrion remove previous connection");
                    blocko_servers.get(server_name).onClose();
                    return null;
                }

                logger.warn("Homer Server:: Connection:: Server is already connected and working!! Its prohibited connected to Tyrion with same name");
                return WebSocket.reject(forbidden("Server side error - already connected"));
            }


            logger.debug("Homer Server:: Connection:: Tyrion inicialize connection for Homer Server");
            WS_BlockoServer server = new WS_BlockoServer(homer_server, blocko_servers);
            blocko_servers.put(server_name, server);

            // Připojím se
            logger.debug("Homer Server:: Connection:: Connection is successful");
            WebSocket<String> webSocket = server.connection();

            // Procedury kontroly - informovat třeba všechny klienty o tom, že se cloud_blocko_server připojil. Kontzrola co tam běží a další píčoviny
            logger.debug("Homer Server:: Connection:: Tyrion have to control what is on the cloud_blocko_server side ");

            // GET state - a vyhodnocením v jakém stavu se cloud_blocko_server nachází a popřípadě
            // na něj nahraji nebo smažu nekonzistenntí clou dprogramy, které by na něm měly být

            class Control_Blocko_Server_Thread extends Thread{

                @Override
                public void run() {
                   Long interrupter = (long) 6000;
                    try {

                        while (interrupter > 0) {

                            sleep(1000);
                            interrupter -= 500;

                            if (server.isReady()) {

                                logger.trace("Homer Server:: Connection::  Tyrion send to Blocko Server request for listInstances");

                                JsonNode result = homer_server.get_homer_server_listOfInstance();
                                if (!result.get("status").asText().equals("success")) {interrupt();}

                                // Vylistuji si seznam instnancí, které běží na serveru
                                List<String> instances_on_server = new ArrayList<>();
                                final JsonNode arrNode = result.get("instances");
                                for (final JsonNode objNode : arrNode) instances_on_server.add(objNode.asText());
                                logger.trace("Homer Server:: Connection:: Number of instances on cloud_blocko_server: " + instances_on_server.size());


                                // Vylistuji si seznam instnancí, které by měli běžet na serveru

                                List<Homer_Instance> instances_in_database_for_uploud = new ArrayList<>();

                                // Přidám všechny reálné instance, které mají běžet.
                                instances_in_database_for_uploud.addAll( Homer_Instance.find.where().eq("cloud_homer_server.id", homer_server.id).eq("virtual_instance", false).isNotNull("actual_instance").select("blocko_instance_name").findList());

                                // Přidám všechny virtuální instance, kde je ještě alespoň jeden Yoda
                                instances_in_database_for_uploud.addAll( Homer_Instance.find.where().eq("cloud_homer_server.id", homer_server.id).eq("virtual_instance", true).isNotNull("boards_in_virtual_instance").select("blocko_instance_name").findList());


                                List<String> instances_for_removing = new ArrayList<>();

                                // Vytvořím kopii seznamu instancí, které by měli běžet na Homer Serveru
                                for(String  identificator : instances_on_server){
                                    if(Homer_Instance.find.where().eq("id",identificator ).isNotNull("actual_instance").findRowCount() < 1){
                                        instances_for_removing.add(identificator);
                                    }
                                }

                                logger.debug("Blocko Server: The number of instances for removing from homer server: ");


                                if (!instances_for_removing.isEmpty()) {
                                    for (String identificator : instances_for_removing) {
                                        JsonNode remove_result = homer_server.remove_instance(identificator);
                                        if(!remove_result.has("status") || !remove_result.get("status").asText().equals("success"))   logger.error("Blocko Server: Removing instance Error: ", remove_result.toString());
                                    }
                                }


                                // Nahraji tam ty co tam patří
                                logger.trace("Homer Server:: Connection::Starting to uploud new instances to cloud_blocko_server");
                                for (Homer_Instance instance : instances_in_database_for_uploud) {

                                    if(instances_on_server.contains(instance.blocko_instance_name)){
                                        logger.debug("Homer Server:: Connection:: ", instance.blocko_instance_name , " is on server already");
                                    }else {
                                        JsonNode add_instance = instance.add_instance_to_server();
                                        logger.debug("add_instance: " + add_instance.toString());

                                        if (add_instance.get("status").asText().equals("error")) {
                                            logger.error("Blocko Server: Fail when Tyrion try to add instance from Blocko cloud_blocko_server:: ", add_instance.toString());
                                        }

                                        sleep(50); // Abych Homer server tolik nevytížil
                                    }
                                }


                                logger.debug("Blocko Server: Successfully finished connection procedure");
                                interrupter = (long) 0;

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
            WS_CompilerServer server = new WS_CompilerServer(cloud_compilation_server, compiler_cloud_servers );

            // Vlákno které hledá záznamy nezkompilovaných programů a dodělá je se zpožděním.
            // Vyhledá vždy jedno! aby když se připojí více kompilačních serverů aby nekompilovaly tentýž program
            class Compilation_Thread extends Thread{

                @Override
                public void run() {
                    try {
                        while (true) {

                            sleep(400);
                            Version_Object version_object = Version_Object.find.where().eq("c_compilation.status", "server_was_offline").order().desc("date_of_create").setMaxRows(1).findUnique();
                            if(version_object == null) break;

                            version_object.c_compilation.status = Compile_Status.compilation_in_progress;
                            version_object.c_compilation.update();

                           JsonNode jsonNode = version_object.compile_program_procedure();
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }

            logger.debug("Blocko Server: Starting connection control procedure");
            new Compilation_Thread().start();


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
    public static void homer_server_incoming_message(WS_BlockoServer blockoServer, ObjectNode json){

    logger.debug("BlockoServer: "+ blockoServer.identifikator + " Incoming message: " + json.toString());

    if(json.has("messageChannel")){

        switch (json.get("messageChannel").asText()){

            case "homer-server" : {

                switch (json.get("messageType").asText()){

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


// PRIVATE Compiler-Server --------------------------------------------------------------------------------------------------------
    public static void compilation_server_incoming_message(WS_CompilerServer server, ObjectNode json){
        logger.warn("Speciálně  server2server přišla zpráva: " + json.asText() );
        logger.warn("Zatím není implementovaná žádná reakce na příchozá zprávu z Compilačního serveru !!");
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

                        default: {
                            logger.error("ERROR \n");
                            logger.error("Becki: "+ becki.identifikator + " Incoming message on messageChannel \"becki\" has not unknown messageType!!!!");
                            logger.error("ERROR \n");
                        }/**/

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
                result.set("notification_body", Json.toJson(notification.notification_body()));
                result.set("buttons", Json.toJson(notification.buttons()));
                result.put("confirmation_required", notification.confirmation_required);
                result.put("confirmed", notification.confirmed);
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
        public static JsonNode becki_ping(WebSCType webSCType) throws TimeoutException, InterruptedException, ExecutionException {

            ObjectNode result = Json.newObject();
            result.put("messageType", "ping");
            result.put("messageChannel", "tyrion");

            return webSCType.write_with_confirmation(result, 1000 * 3, 0, 3);
        }

        // Reakce na odhlášení blocka
        public static void becki_disconnect(WebSCType becki){
             System.out.println("Becki se mi odpojilo");
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

    public static void disconnect_all_Blocko_Servers() {

        logger.warn("Tyrion is shutting down: Trying safety disconnect all connected Homer");

        for (Map.Entry<String, WebSCType> entry :  WebSocketController.blocko_servers.entrySet())
        {
            server_violently_terminate_terminal(entry.getValue());
        }
    }

    public static void disconnect_all_Compilation_Servers() {

        logger.warn("Tyrion is shutting down: Trying safety disconnect all connected Homer");

        for (Map.Entry<String, WebSCType> entry :  WebSocketController.compiler_cloud_servers.entrySet())
        {
            server_violently_terminate_terminal(entry.getValue());
        }
    }



}
