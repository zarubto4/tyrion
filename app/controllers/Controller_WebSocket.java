package controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import models.Model_CompilationServer;
import models.Model_Person;
import models.Model_HomerServer;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.WebSocket;
import utilities.logger.Class_Logger;
import utilities.logger.ServerLogger;
import utilities.login_entities.Secured_API;
import utilities.login_entities.TokenCache;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.swagger.outboundClass.Swagger_Websocket_Token;
import web_socket.message_objects.common.WS_Token;
import web_socket.message_objects.common.service_class.WS_Message_Tyrion_restart_echo;
import web_socket.message_objects.compilator_with_tyrion.WS_Message_Ping_compilation_server;
import web_socket.message_objects.homer_with_tyrion.WS_Message_Homer_ping;
import web_socket.services.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_WebSocket extends Controller {

    
// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_WebSocket.class);
    
/* VALUES  -------------------------------------------------------------------------------------------------------------*/

    /*
     *      Připojené servery, kde běží Homer instance jsou drženy v homer_cloud_server. Jde jen o jednoduché čisté spojení
     *      a několik servisních metod. Ale aby bylo dosaženo toho, že Homer jak v cloudu tak i na fyzickém počítači byl obsluhován stejně
     *      je redundantně (jen ukazateli) vytvořeno virtuální spojení na každou instanci blocko programu v cloudu.
     *
     *      <Model_HomerServer.id, WS_HomerServer>
     */
    public static Map<String, WS_HomerServer> homer_servers = new HashMap<>();                  // Sem se vkládají servery z not_synchronize_homer_servers, kde úspěšně proběhla synchronizace
    public static Map<String, WS_HomerServer> not_synchronize_homer_servers = new HashMap<>();  // Sem se vkládají servery, které jsou připojené, ale ještě nejsou synchronizované

    /*
     *      Komnpilační servery, které mají být při kompilaci rovnoměrně zatěžovány - nastřídačku. Ale předpokladem je, že všechny dělají vždy totéž.
     *
     *      <Model_CompilationServer.id, WS_CompilerServer>
     */
    public static Map<String, WS_CompilerServer> compiler_cloud_servers = new HashMap<>();

    /*
     *      Becki (frontend) spojení na realtime synchronizaci blocka, objektů, notifikací atd.
     *      Je zde podporován režim multipřihlášení. To znamená, že Uživatel se přihlásí na dvou počítačích. Objekt WS_Becki_Website obsahuje pole připojení
     *      jednotlivých počítačů a je vrstvou, zakrývající exekutivitu odeslání - udělá to že když má odeslat zprávu, jen projede for cyklem všechny obejkty.
     *
     *      <Person.id, WS_Becki_Website>
     *
     *     (Person_id - Identificator, List of Websocket connections kde Identificator je Token přihlášeného uživatele, který Becki dostane při login)
     */
    public static Map<String, WS_Becki_Website> becki_website = new HashMap<>();


    /*
     *      Tokeny pro ověření uživatele
     */
    public static TokenCache tokenCache = new TokenCache( (long) 5, (long) 500, 50000);



/* PUBLIC API ----------------------------------------------------------------------------------------------------------*/

    @ApiOperation(value = "get Websocket Access Token",
            tags = {"Access", "WebSocket"},
            notes = "For connection to websocket, you have to connect with temporary unique token. This Api return Token"+
                    "with a maximum lifetime of 5 seconds. After the token is deactivated. After logging in, or the connection"+
                    "lost is token deactivated also. ",
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
            token.person_id = Controller_Security.get_person_id();

            tokenCache.put(web_socket_token, token);

            Swagger_Websocket_Token swagger_websocket_token = new Swagger_Websocket_Token();
            swagger_websocket_token.websocket_token = web_socket_token;

            return GlobalResult.result_ok(Json.toJson(swagger_websocket_token));
        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }



/* WEB-SOCKET CONNECTION -----------------------------------------------------------------------------------------------*/

    @ApiOperation(value = "Homer Server Connection", hidden = true, tags = {"WebSocket"})
    public  WebSocket<String>  homer_cloud_server_connection(String connection_identificator){
        try{

            terminal_logger.debug("homer_cloud_server_connection:: Incoming connection: Server:  "+ connection_identificator);

            //Find object (only ID)
            Model_HomerServer homer_server_selected = Model_HomerServer.find.where().eq("connection_identificator", connection_identificator).select("id").findUnique();


            if(homer_server_selected== null){
                // Připojím se
                terminal_logger.warn("homer_cloud_server_connection:: Incoming connection: Server:  "+ connection_identificator + " is not registred in database!!!!!");
                return WebSocket.reject(forbidden("Server side error - already connected"));
            }

            // Get Object from Cache
            Model_HomerServer homer_server =  Model_HomerServer.get_byId(homer_server_selected.id.toString());

            if(homer_servers.containsKey(homer_server.id.toString())) {
                terminal_logger.warn("homer_cloud_server_connection::  Server is connected -> Tyrion try to send ping");

                WS_Message_Homer_ping result = homer_server.ping();
                if(!result.status.equals("success")){
                    terminal_logger.error("homer_cloud_server_connection:: Ping Failed - Tyrion remove previous connection");
                    if(homer_servers.containsKey(homer_server.id.toString())){
                        homer_servers.get(homer_server.id.toString()).onClose();
                    }
                    return null;
                }

                terminal_logger.warn("homer_cloud_server_connection:: Connection:: Server is already connected and working!! Its prohibited connected to Tyrion with same name");
                return WebSocket.reject(forbidden("Server side error - already connected"));
            }


            terminal_logger.trace("homer_cloud_server_connection:: Tyrion initialize connection for Homer Server");
            WS_HomerServer server = new WS_HomerServer(homer_server);
            not_synchronize_homer_servers.put(homer_server.id.toString(), server);

            // Připojím se
            terminal_logger.trace("homer_cloud_server_connection:: Connection is successful");
            WebSocket<String> webSocket = server.connection();

            terminal_logger.trace("homer_cloud_server_connection:: Successfully connected");
            return webSocket;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return WebSocket.reject(forbidden());
        }
    }

    @ApiOperation(value = "Compilation Server Connection", hidden = true, tags = {"WebSocket"})
    public  WebSocket<String> code_server_connection(String connection_identificator){
        try{

            terminal_logger.debug("code_server_connection:: Server is connecting. Server: "+ connection_identificator);

            terminal_logger.trace("code_server_connection:: Control Server and its unique names!");

            //Find object (only ID)
            Model_CompilationServer cloud_compilation_server_selected = Model_CompilationServer.find.where().eq("connection_identificator", connection_identificator).select("id").findUnique();

            if(cloud_compilation_server_selected == null) {
                terminal_logger.warn("code_server_connection:: unrecognized identificator {}", connection_identificator);
                return WebSocket.reject(forbidden("Server side error - unrecognized name"));
            }

            Model_CompilationServer cloud_compilation_server =  Model_CompilationServer.get_byId(cloud_compilation_server_selected.id.toString());


            if(compiler_cloud_servers.containsKey(cloud_compilation_server.id.toString())) {

                try {
                    terminal_logger.warn("code_server_connection:: At Tyrion is already connected cloud_blocko_server compilation of the same name - will not allow another connection");

                    WS_CompilerServer ws_compilerServer = compiler_cloud_servers.get(cloud_compilation_server.id.toString());
                    WS_Message_Ping_compilation_server result = ws_compilerServer.server.ping();
                    if (!result.status.equals("success")) {
                        terminal_logger.warn("code_server_connection:: Ping Failed - Tyrion remove previous connection");
                        if (compiler_cloud_servers.containsKey(cloud_compilation_server.id.toString())){
                            compiler_cloud_servers.get(cloud_compilation_server.id.toString()).onClose();
                        }
                        return null;
                    }

                    terminal_logger.warn("code_server_connection:: Server is already connected and working!! Its prohibited connected to Tyrion with same unique name");
                    return WebSocket.reject(forbidden("Server side error - already connected"));

                }catch (NullPointerException e){

                    terminal_logger.warn("code_server_connection:: Ping Failed - Tyrion remove previous connection");
                    if(compiler_cloud_servers.containsKey(cloud_compilation_server.id.toString())) compiler_cloud_servers.get(cloud_compilation_server.id.toString()).onClose();

                }
            }

            // Inicializuji Websocket pro Homera
            WS_CompilerServer server = new WS_CompilerServer(cloud_compilation_server);

            cloud_compilation_server.check_after_connection();

            // Připojím se
            terminal_logger.debug("code_server_connection:: Sever connect");
            return server.connection();

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return WebSocket.reject(forbidden("Server side error"));
        }
    }

    @ApiOperation(value = "FrontEnd Becki Connection", hidden = true, tags = {"WebSocket"})
    public  WebSocket<String>  becki_website_connection (String security_token){
        try{

            terminal_logger.debug("becki_website_connection:: Incoming connection: "+ security_token);


            WS_Token token = tokenCache.get(security_token);

            if(token == null ) {
              terminal_logger.warn("becki_website_connection:: Incoming token "+ security_token + "is invalid! Probably too late for access");
              return WebSocket.reject(forbidden());
            }

            String person_id = token.person_id;
            tokenCache.remove(security_token);


            terminal_logger.trace("becki_website_connection:: Controlling of incoming token "+ security_token);
            Model_Person person = Model_Person.get_byId(person_id);
            if(person == null){
                terminal_logger.warn("becki_website_connection: Person with this id not exist!");
                return WebSocket.reject(forbidden());
            }

            WS_Becki_Website website;

            if(becki_website.containsKey(person.id)) {
                website = becki_website.get(person.id);
            }else{
                website = new WS_Becki_Website(person);
                becki_website.put(person.id , website);
            }

            terminal_logger.trace("becki_website_connection: Check if token is already connected");
            if(website.all_person_Connections != null && website.all_person_Connections.containsKey(security_token)) return WebSocket.reject(forbidden());
            WS_Becki_Single_Connection website_connection = new WS_Becki_Single_Connection(security_token, website);

            terminal_logger.trace("becki_website_connection:  Connection successful");
            return website_connection.connection();

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return WebSocket.reject(forbidden("Server side error"));
        }
    }

/* Test & Control API --------------------------------------------------------------------------------------------------*/

    public static void server_violently_terminate_terminal(WS_Interface_type terminal){

        terminal.write_without_confirmation(new WS_Message_Tyrion_restart_echo().make_request());

        try {
            terminal.close();
        }catch(Exception e){}
    }

    public static void disconnectHomerServers() {

        terminal_logger.warn("disconnectHomerServers:  Trying to safely disconnect all Homer Servers");

        for (Map.Entry<String, WS_HomerServer> entry :  Controller_WebSocket.homer_servers.entrySet()) {
            server_violently_terminate_terminal(entry.getValue());
        }
    }

    public static void disconnectCodeServers() {

        terminal_logger.warn("disconnectCodeServers: Trying to safely disconnect all Code Servers");

        for (Map.Entry<String, WS_CompilerServer> entry :  Controller_WebSocket.compiler_cloud_servers.entrySet()) {
            server_violently_terminate_terminal(entry.getValue());
        }
    }

    public static void disconnectBeckiApplications() {

        terminal_logger.warn("disconnectBeckiApplications: Trying to safely disconnect all Becki applications");

        for (Map.Entry<String, WS_Becki_Website> entry :  Controller_WebSocket.becki_website.entrySet()) {
            entry.getValue().onClose();
        }
    }

}