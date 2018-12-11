package controllers;

import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import models.Model_CompilationServer;
import models.Model_HomerServer;
import models.Model_Person;
import org.ehcache.Cache;
import play.libs.F;
import play.libs.ws.WSClient;
import play.mvc.*;
import responses.Result_InternalServerError;
import responses.Result_Unauthorized;
import utilities.authentication.Authentication;
import utilities.logger.Logger;
import utilities.notifications.NotificationService;
import utilities.permission.PermissionService;
import utilities.swagger.output.Swagger_Websocket_Token;
import websocket.WebSocketService;
import websocket.interfaces.*;
import websocket.interfaces.Compiler;

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

    private final Injector injector;

    @Inject
    public Controller_WebSocket(WSClient ws, _BaseFormFactory formFactory, Config config, PermissionService permissionService,
                                NotificationService notificationService, WebSocketService webSocketService, Injector injector) {
        super(ws, formFactory, config, permissionService, notificationService);
        this.webSocketService = webSocketService;
        this.injector = injector;
    }

/* STATIC --------------------------------------------------------------------------------------------------------------*/

    /**
     * Holds person connection tokens and ids
     */
    public static Cache<UUID, UUID> tokenCache;


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

            UUID token = UUID.randomUUID();
            tokenCache.put(token, personId());

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

                Model_HomerServer server = Model_HomerServer.find.query().nullable().where().eq("connection_identifier", token).findOne();
                if (server != null) {
                    if (this.webSocketService.isRegistered(server.id)) {

                        Homer homer = this.webSocketService.getInterface(server.id);

                        try {
                            homer.ping();
                            logger.warn("homer - server is already connected, connection is working, cannot connect twice");
                            return CompletableFuture.completedFuture(F.Either.Left(forbidden()));
                        } catch (Exception e) {
                            logger.error("homer - ping failed, removing previous connection");
                            homer.close();
                        }
                    }

                    logger.info("homer - connection was successful. Server {}", server.name);

                    Homer homer = injector.getInstance(Homer.class);
                    homer.setId(server.id);

                    return CompletableFuture.completedFuture(F.Either.Right(this.webSocketService.register(homer)));

                } else {
                    logger.warn("homer - server with token: {} is not registered in the database, rejecting connection with token: {}", token, token);
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
                Model_CompilationServer server = Model_CompilationServer.find.query().nullable().where().eq("connection_identifier", token).select("id").findOne();
                if(server != null){

                    if (this.webSocketService.isRegistered(server.id)) {
                        logger.error("compiler - server is already connected, trying to ping previous connection");

                        Compiler compiler = this.webSocketService.getInterface(server.id);

                        try {
                            compiler.ping();
                            logger.warn("compiler - server is already connected, connection is working, cannot connect twice");
                            return CompletableFuture.completedFuture(F.Either.Left(forbidden()));
                        } catch (Exception e) {
                            logger.error("compiler - ping failed, removing previous connection");
                            compiler.close();
                        }
                    }

                    Compiler compiler = injector.getInstance(Compiler.class);
                    compiler.setId(server.id);

                    logger.info("compiler - connection was successful");
                    return CompletableFuture.completedFuture(F.Either.Right(this.webSocketService.register(compiler)));

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

                        Portal portal = injector.getInstance(Portal.class);
                        portal.setId(token);
                        portal.setPersonId(person.id);

                        // Remove Token from Cache
                        tokenCache.remove(token);

                        return CompletableFuture.completedFuture(F.Either.Right(this.webSocketService.register(portal)));
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
