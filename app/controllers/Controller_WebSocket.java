package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import models.compiler.Model_CompilationServer;
import models.person.Model_Person;
import models.project.b_program.servers.Model_HomerServer;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.WebSocket;
import utilities.loggy.Loggy;
import utilities.login_entities.Secured_API;
import utilities.login_entities.TokenCache;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.swagger.outboundClass.Swagger_Websocket_Token;
import utilities.web_socket.*;
import utilities.web_socket.message_objects.common.WS_Token;
import utilities.web_socket.message_objects.homer_tyrion.WS_Ping_server;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_WebSocket extends Controller {

// Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

// Values  --------------------------------------------- ----------------------------------------------------------------

    // Připojené servery, kde běží Homer instance jsou drženy v homer_cloud_server. Jde jen o jednoduché čisté spojení a
    // několik servisních metod. Ale aby bylo dosaženo toho, že Homer jak v cloudu tak i na fyzickém počítači byl obsluhován stejně
    // je redundantně (jen ukazateli) vytvořeno virtuální spojení na každou instanci blocko programu v cloudu.
    public static Map<String, WebSCType> homer_servers = new HashMap<>(); // (<Server-Identificator, Websocket> >)

    // Komnpilační servery, které mají být při kompilaci rovnoměrně zatěžovány - nastřídačku. Ale předpokladem je, že všechny dělají vždy totéž.
    public static Map<String, WebSCType> compiler_cloud_servers = new HashMap<>(); // (Server-Identificator, Websocket)

    // Becki (frontend) spojení na synchronizaci blocka atd.. - Podporován režim multipřihlášení.
    public static Map<String, WebSCType> becki_website = new HashMap<>(); // (Person_id - Identificator, List of Websocket connections - Identificator je Token)

    public static TokenCache tokenCache = new TokenCache( (long) 5, (long) 500, 50000); // Tokeny pro ověření uživatele



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
            @ApiResponse(code = 200, message = "Token successfully generated", response = Swagger_Websocket_Token.class),
            @ApiResponse(code = 401, message = "Unauthorized request",         response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result get_Websocket_token() {
        try {

            String web_socket_token = UUID.randomUUID().toString();

            WS_Token token = new WS_Token();
            token.token = web_socket_token;
            token.person_id = Controller_Security.getPerson().id;

            tokenCache.put(web_socket_token, token);

            Swagger_Websocket_Token swagger_websocket_token = new Swagger_Websocket_Token();
            swagger_websocket_token.websocket_token = web_socket_token;

            return GlobalResult.result_ok(Json.toJson(swagger_websocket_token));
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Homer Server Connection", tags = {"WebSocket"})
    public  WebSocket<String>  homer_cloud_server_connection(String unique_identificator){
        try{

            logger.debug("Homer Server:: Connection:: Incoming connection: Server:  " + unique_identificator);

            Model_HomerServer homer_server = Model_HomerServer.find.where().eq("unique_identificator", unique_identificator).findUnique();
            if(homer_server== null){

                // Připojím se
                logger.warn("Homer Server:: Connection:: Incoming connection: Server:  " + unique_identificator + " is not registred in database!!!!!");

                WS_HomerServer server = new WS_HomerServer(null, homer_servers);
                WebSocket<String> webSocket = server.connection();

                Thread not_valid = new Thread() {

                    @Override
                    public void run() {
                        try {

                            sleep(2000);
                            logger.warn("Homer Server:: Connection:: Incoming connection: Server:  " + unique_identificator + " Sending message about validation");
                            server.unique_connection_name_not_valid();

                            logger.warn("Homer Server:: Connection:: Incoming connection: Server:  " + unique_identificator + " Message sent");
                            sleep(1000 * 60);

                            logger.warn("Homer Server:: Connection:: Incoming connection: Server:  " + unique_identificator + " Closing connection");
                            server.close();

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };

                not_valid.start();
                return webSocket;
            }

            if(homer_servers.containsKey(unique_identificator)) {
                logger.warn("Homer Server:: Connection:: Server is connected -> Tyrion try to send ping");

                WS_HomerServer ws_blockoServer = (WS_HomerServer) homer_servers.get(unique_identificator);
                WS_Ping_server result = homer_server.ping();
                if(!result.status.equals("success")){
                    logger.warn("Homer Server:: Connection:: Ping Failed - Tyrion remove previous connection");
                    if(homer_servers.containsKey(unique_identificator)){
                        homer_servers.get(unique_identificator).onClose();
                    }
                    return null;
                }

                logger.warn("Homer Server:: Connection:: Server is already connected and working!! Its prohibited connected to Tyrion with same name");
                return WebSocket.reject(forbidden("Server side error - already connected"));
            }


            logger.debug("Homer Server:: Connection:: Tyrion initialize connection for Homer Server");
            WS_HomerServer server = new WS_HomerServer(homer_server, homer_servers);
            homer_servers.put(unique_identificator, server);

            // Připojím se
            logger.debug("Homer Server:: Connection:: Connection is successful");
            WebSocket<String> webSocket = server.connection();

            // Procedury kontroly - informovat třeba všechny klienty o tom, že se cloud_blocko_server připojil. Kontzrola co tam běží a další píčoviny
            logger.debug("Homer Server:: Connection:: Tyrion have to control what is on the cloud_blocko_server side ");


            // Ověřím IDentitiu serveru na jeho long_hash---------
            // Separatní vlákno je z důvodů nutnosti nejdříve vrátit (return webSocket) a nezávisle poté spustit ověření
            Thread check = new Thread() {

                @Override
                public void run() {
                    try {

                        sleep(500);
                        server.security_token_confirm_procedure();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

            check.start();


            homer_servers.put(homer_server.unique_identificator, server);

            logger.debug("Blocko Server: Successfully connected");
            return webSocket;

        }catch (Exception e){
            logger.error("Blocko Server: Fail connection");
            logger.error("Something was wrong", e);
            return WebSocket.reject(forbidden());
        }
    }

    @ApiOperation(value = "Compilation Server Connection", tags = {"WebSocket"})
    public  WebSocket<String>  compilator_server_connection (String unique_identificator){
        try{
            logger.debug("Controller_WebSocket:: compilator_server_connection:: Server is connecting. Server: " + unique_identificator);

            logger.debug("Control Server and its unique names!"); // TODO - přidat ověření ještě pomocí HASHe co už je v objektu definován
            Model_CompilationServer cloud_compilation_server = Model_CompilationServer.find.where().eq("unique_identificator", unique_identificator).findUnique();
            if(cloud_compilation_server == null) return WebSocket.reject(forbidden("Server side error - unrecognized name"));

            if(compiler_cloud_servers.containsKey(unique_identificator)) {
                logger.debug("Controller_WebSocket:: compilator_server_connection:: At Tyrion is already connected cloud_blocko_server compilation of the same name - will not allow another connection");
                return WebSocket.reject(forbidden("Server side error - already connected"));
            }

            // Inicializuji Websocket pro Homera
            WS_CompilerServer server = new WS_CompilerServer(cloud_compilation_server, compiler_cloud_servers );

            cloud_compilation_server.check_after_connection();

            // Připojím se
            logger.debug("Controller_WebSocket:: compilator_server_connection:: Sever connect");
            return server.connection();

        }catch (Exception e){
            logger.error("Controller_WebSocket:: compilator_server_connection:: Web Socket connection", e);
            return WebSocket.reject(forbidden("Server side error"));
        }
    }


    @ApiOperation(value = "FrontEnd Becki Connection", tags = {"WebSocket"})
    public  WebSocket<String>  becki_website_connection (String security_token){
        try{

            logger.debug("Controller_WebSocket:: becki_website_connection:: Incoming connection: " + security_token);


            WS_Token token = tokenCache.get(security_token);

            if(token == null ) {
              logger.warn("Controller_WebSocket:: becki_website_connection: Incoming token " + security_token + " is invalid! Probably too late for access");
              return WebSocket.reject(forbidden());
            }

            String person_id = token.person_id;
            tokenCache.remove(security_token);


            logger.debug("Controller_WebSocket:: becki_website_connection: Controlling of incoming token " + security_token);
            Model_Person person = Model_Person.find.byId(person_id);
            if(person == null){
                logger.warn("Controller_WebSocket:: becki_website_connection: Person with this id not exist!");
                return WebSocket.reject(forbidden());
            }

            WS_Becki_Website website;

            if(becki_website.containsKey(person.id)) {
                website = (WS_Becki_Website) becki_website.get(person.id);
            }else{
                website = new WS_Becki_Website(person);
                becki_website.put(person.id , website);
            }

            logger.warn("Becki: Check if token is already connected");
            if(website.all_person_Connections != null && website.all_person_Connections.containsKey(security_token)) return WebSocket.reject(forbidden());
            WS_Becki_Single_Connection website_connection = new WS_Becki_Single_Connection(security_token, website);

            logger.warn("Becki: Connection successful");
            return website_connection.connection();

        }catch (Exception e){
            e.printStackTrace();
            Loggy.error("Cloud Compiler Server Web Socket connection", e);
            return WebSocket.reject(forbidden("Server side error"));
        }

    }




// Test & Control API ---------------------------------------------------------------------------------------------------------

    public static void server_violently_terminate_terminal(WebSCType terminal){

        ObjectNode result = Json.newObject();
        result.put("messageType", "Budeš něžně odpojen!");
        result.put("TODO", "Tato zpráva není oficiálně definovaná");

        terminal.write_without_confirmation(result);

        try {
            terminal.close();
        }catch(Exception e){}
    }

    public static void disconnect_all_Blocko_Servers() {

        logger.warn("Tyrion is shutting down: Trying to safely disconnect all Blocko Servers");

        for (Map.Entry<String, WebSCType> entry :  Controller_WebSocket.homer_servers.entrySet())
        {
            server_violently_terminate_terminal(entry.getValue());
        }
    }

    public static void disconnect_all_Compilation_Servers() {

        logger.warn("Tyrion is shutting down: Trying to safety disconnect all Compilation Servers");

        for (Map.Entry<String, WebSCType> entry :  Controller_WebSocket.compiler_cloud_servers.entrySet())
        {
            server_violently_terminate_terminal(entry.getValue());
        }
    }



}
