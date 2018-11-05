package controllers;

import com.typesafe.config.Config;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import models.Model_CompilationServer;
import models.Model_HomerServer;
import models.Model_Person;
import org.ehcache.Cache;
import play.Environment;
import play.libs.F;
import play.libs.ws.WSClient;
import play.mvc.*;
import responses.Result_InternalServerError;
import responses.Result_Unauthorized;
import utilities.authentication.Authentication;
import utilities.logger.Logger;
import utilities.logger.YouTrack;
import utilities.permission.PermissionService;
import utilities.scheduler.SchedulerController;
import utilities.swagger.output.Swagger_Websocket_Token;
import websocket.WebSocketService;
import websocket.interfaces.*;
import websocket.interfaces.Compiler;
import websocket.messages.compilator_with_tyrion.WS_Message_Ping_compilation_server;
import websocket.messages.homer_with_tyrion.WS_Message_Homer_ping;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_WebSocket extends _BaseController {

/* LOGGER --------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Controller_WebSocket.class);

// CONTROLLER CONFIGURATION ############################################################################################

    private static List<String> list_of_portal_alowed_url_connection = null;

    private final WebSocketService webSocketService;


    @Inject
    public Controller_WebSocket(Environment environment, WSClient ws, _BaseFormFactory formFactory, YouTrack youTrack, Config config, SchedulerController scheduler, PermissionService permissionService, WebSocketService webSocketService) {
        super(environment, ws, formFactory, youTrack, config, scheduler, permissionService);
        this.webSocketService = webSocketService;
    }

/* STATIC --------------------------------------------------------------------------------------------------------------*/

    /**
     * Holds all connections of Homer servers
     */
    public static Map<UUID, WS_Homer> homers = new HashMap<>();

    /**
     * Holds all connections of Homer servers
     */
    public static Map<UUID, WS_Homer> homers_not_sync = new HashMap<>();

    /**
     * Holds all connections of Compiler servers
     */
    public static Map<UUID, WS_Compiler> compilers = new HashMap<>();

    /**
     * Holds all connections of Becki portals
     */
    public static Map<UUID, WS_Portal> portals = new HashMap<>();

    /**
     * Holds person connection tokens and ids
     */
    public static Cache<UUID, UUID> tokenCache;

    /**
     * Closes all WebSocket connections
     */
    public static void close() {
    }

    /* PUBLIC API ----------------------------------------------------------------------------------------------------------*/

    @ApiOperation(value = "get Websocket Access Token",
            tags = {"Access", "WebSocket"},
            notes = "For connection to websocket, you have to connect with temporary unique token. This Api return Token" +
                    "with a maximum lifetime of 5 seconds. After the token is deactivated. After logging in, or the connection" +
                    "lost is token deactivated also. ",
            produces = "application/json",
            consumes = "text/plain",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Token successfully generated",  response = Swagger_Websocket_Token.class),
            @ApiResponse(code = 401, message = "Unauthorized request",          response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",             response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result get_Websocket_token() {
        try {

                logger.trace("get_Websocket_token: request");
                UUID token = UUID.randomUUID();

                logger.trace("get_Websocket_token: token created:: {}", token);

                tokenCache.put(token, personId());

                logger.trace("get_Websocket_token: token assigned to person:: {}", personId());

                Swagger_Websocket_Token swagger_websocket_token = new Swagger_Websocket_Token();
                swagger_websocket_token.websocket_token = token;

                return ok(swagger_websocket_token);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "Homer Server Connection", hidden = true, tags = {"WebSocket"})
    public WebSocket homer(String token) {
        return WebSocket.Json.acceptOrResult(request -> {
            try {

                logger.trace("homer - incoming connection: " + token);

                //Find object (only ID)
                Model_HomerServer homer = Model_HomerServer.find.query().where().eq("connection_identifier", token).select("id").findOne();
                if (homer != null) {
                    if (this.webSocketService.isRegistered(homer.id)) {
                        logger.warn("homer - server is already connected, trying to ping previous connection");

                        WS_Message_Homer_ping result = homer.ping();
                        if (!result.status.equals("success")) {
                            logger.error("homer - ping failed, removing previous connection");
                            this.webSocketService.getInterface(homer.id).close();
                        } else {
                            logger.warn("homer - server is already connected, connection is working, cannot connect twice");
                            return CompletableFuture.completedFuture(F.Either.Left(forbidden()));
                        }
                    }

                    logger.info("homer - connection was successful. Server {}", homer.name);
                    return CompletableFuture.completedFuture(F.Either.Right(this.webSocketService.register(new Homer(homer.id))));

                } else {
                    logger.warn("homer - server with token: {} is not registered in the database, rejecting connection wtih token: {}", token);
                }

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            return CompletableFuture.completedFuture(F.Either.Left(forbidden()));
        });
    }

    @ApiOperation(value = "Compiler Server Connection", hidden = true, tags = {"WebSocket"})
    public WebSocket compiler(String token) {
        return WebSocket.Json.acceptOrResult(request -> {
            try {

                logger.debug("compiler - incoming connection: {}", token);

                //Find object (only ID)
                Model_CompilationServer compiler = Model_CompilationServer.find.query().where().eq("connection_identifier", token).select("id").findOne();
                if(compiler != null){

                    if (this.webSocketService.isRegistered(compiler.id)) {
                        logger.error("compiler - server is already connected, trying to ping previous connection");

                        WS_Message_Ping_compilation_server result = compiler.ping();

                        logger.trace("compiler:: Error::{} {}" , result.error , result.error_message);
                        if(!result.status.equals("success") && !result.error.equals("Missing field code.")){
                            logger.error("compiler - ping failed, removing previous connection");
                            this.webSocketService.getInterface(compiler.id).close();
                        } else {
                            logger.warn("compiler - server is already connected, connection is working, cannot connect twice");
                            return CompletableFuture.completedFuture(F.Either.Left(forbidden()));
                        }
                    }

                    logger.info("compiler - connection was successful");
                    return CompletableFuture.completedFuture(F.Either.Right(this.webSocketService.register(new Compiler(compiler.id))));

                } else {
                    logger.warn("compiler - server with token: {} is not registered in the database, rejecting token: {}", token);
                }

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            return CompletableFuture.completedFuture(F.Either.Left(forbidden()));
        });
    }

    @ApiOperation(value = "Portal Server Connection", hidden = true, tags = {"WebSocket"})
    public WebSocket portal(UUID token) {
        return WebSocket.Json.acceptOrResult(request -> {
            try {

                logger.trace("portal - incoming connection: {}", token);

                UUID user_token = tokenCache.get(token);
                if(user_token == null) {
                    logger.warn("portal - incoming connection: {} not recognized and pair with Person. ", token);
                    return CompletableFuture.completedFuture(F.Either.Left(forbidden("Token not found!")));
                }


                Model_Person person = Model_Person.find.byId(user_token);

                if (sameOriginCheck(request)) {

                    if (tokenCache.containsKey(token)) {

                        Portal portal = this.webSocketService.getInterface(person.id);

                        if (portal == null) {
                            logger.trace("portal - User {} is already connected somewhere else", person.nick_name);
                            portal = new Portal(person.id);
                            this.webSocketService.register(portal); // Intentionally ignore the result of register -> it is null
                        }

                        // Remove Token from Cache
                        tokenCache.remove(token);

                        if (!portal.isRegistered(token)) {

                            return CompletableFuture.completedFuture(F.Either.Right(portal.register(new SinglePortal(token, portal))));

                        } else {
                            logger.info("portal - rejecting connection: {}, already established", token);
                        }
                    } else {
                        logger.info("portal - rejecting connection: {}, token is expired or person not found", token);
                    }
                } else {
                    logger.info("Error! - Origins not Allowed!");
                }

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            return CompletableFuture.completedFuture(F.Either.Left(forbidden()));
        });
    }


    /**
     * Checks that the WebSocket comes from the same origin.  This is necessary to protect
     * against Cross-Site WebSocket Hijacking as WebSocket does not implement Same Origin Policy.
     * <p>
     * See https://tools.ietf.org/html/rfc6455#section-1.3 and
     * http://blog.dewhurstsecurity.com/2013/08/30/security-testing-html5-websockets.html
     */
    private boolean sameOriginCheck(Http.RequestHeader rh) {
        final Optional<String> origin = rh.header("Origin");

        return true;
        /*
        if (! origin.isPresent()) {
            logger.error("originCheck: rejecting request because no Origin header found");
            return false;
        } else if (originMatches(origin.get())) {
            return true;
        } else {
            logger.error("originCheck: rejecting request because Origin header value " + origin + " is not in the same origin");
            return false;
        }
        */
    }

    private boolean originMatches(String origin) {
        try {

            if (list_of_portal_alowed_url_connection == null) {
                list_of_portal_alowed_url_connection = (List<String>) config.getAnyRefList("play.filters.hosts.allowed");
            }

            return list_of_portal_alowed_url_connection.contains(origin);
        }catch (Exception e){
            logger.internalServerError(e);
            return false;
        }
    }

}
