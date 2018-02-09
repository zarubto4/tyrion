package controllers;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import models.Model_CompilationServer;
import models.Model_HomerServer;
import models.Model_Person;
import org.ehcache.Cache;
import play.libs.F;
import play.libs.Json;
import play.libs.streams.ActorFlow;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.WebSocket;
import responses.Result_InternalServerError;
import responses.Result_Unauthorized;
import utilities.authentication.Authentication;
import utilities.logger.Logger;
import utilities.swagger.output.Swagger_Websocket_Token;
import websocket.interfaces.WS_Portal;
import websocket.interfaces.WS_Compiler;
import websocket.interfaces.WS_Homer;
import websocket.interfaces.WS_PortalSingle;
import websocket.messages.compilator_with_tyrion.WS_Message_Ping_compilation_server;
import websocket.messages.homer_with_tyrion.WS_Message_Homer_ping;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Controller_WebSocket extends BaseController {

/* LOGGER --------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Controller_WebSocket.class);

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
    public static Cache<String, UUID> tokenCache;

    /**
     * Closes all WebSocket connections
     */
    public static void close() {

        logger.warn("close - closing all WebSockets");

        homers.forEach((id, homer) -> homer.close());
        homers_not_sync.forEach((id, homer) -> homer.close());
        compilers.forEach((id, compiler) -> compiler.close());
        portals.forEach((id, portal) -> portal.close());

        logger.info("close - all WebSockets closed");
    }

    private final ActorSystem actorSystem;
    private final Materializer materializer;

    @Inject
    public Controller_WebSocket(ActorSystem actorSystem, Materializer materializer) {
        this.actorSystem = actorSystem;
        this.materializer = materializer;
    }

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
    @ApiResponses({
            @ApiResponse(code = 200, message = "Token successfully generated",  response = Swagger_Websocket_Token.class),
            @ApiResponse(code = 401, message = "Unauthorized request",          response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",             response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result get_Websocket_token() {
        try {

            String token = UUID.randomUUID().toString();

            tokenCache.put(token, personId());

            Swagger_Websocket_Token swagger_websocket_token = new Swagger_Websocket_Token();
            swagger_websocket_token.websocket_token = token;

            return ok(Json.toJson(swagger_websocket_token));
        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    public WebSocket homer(String token) {
        return WebSocket.Json.acceptOrResult(request -> {
            try {

                logger.info("homer - incoming connection: " + token);

                //Find object (only ID)
                Model_HomerServer homer = Model_HomerServer.find.query().where().eq("connection_identifier", token).select("id").findOne();
                if(homer != null){
                    if (homers.containsKey(homer.id)) {
                        logger.warn("homer - server is already connected, trying to ping previous connection");

                        WS_Message_Homer_ping result = homer.ping();
                        if(!result.status.equals("success")){
                            logger.error("homer - ping failed, removing previous connection");
                            homers.get(homer.id).close();
                        } else {
                            logger.warn("homer - server is already connected, connection is working, cannot connect twice");
                            return CompletableFuture.completedFuture(F.Either.Left(forbidden()));
                        }
                    }

                    logger.info("homer - connection was successful");
                    return CompletableFuture.completedFuture(F.Either.Right(ActorFlow.actorRef(actorRef -> WS_Homer.props(actorRef, homer.id), actorSystem, materializer)));

                } else {
                    logger.warn("homer - server with token: {} is not registered in the database, rejecting", token);
                }

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            return CompletableFuture.completedFuture(F.Either.Left(forbidden()));
        });
    }

    public WebSocket compiler(String token) {
        return WebSocket.Json.acceptOrResult(request -> {
            try {

                logger.info("compiler - incoming connection: {}", token);

                //Find object (only ID)
                Model_CompilationServer compiler = Model_CompilationServer.find.query().where().eq("connection_identifier", token).select("id").findOne();
                if(compiler != null){
                    if (compilers.containsKey(compiler.id)) {
                        logger.warn("compiler - server is already connected, trying to ping previous connection");

                        WS_Message_Ping_compilation_server result = compiler.ping();
                        if(!result.status.equals("success")){
                            logger.error("compiler - ping failed, removing previous connection");
                            compilers.get(compiler.id).close();
                        } else {
                            logger.warn("compiler - server is already connected, connection is working, cannot connect twice");
                            return CompletableFuture.completedFuture(F.Either.Left(forbidden()));
                        }
                    }

                    logger.info("compiler - connection was successful");
                    return CompletableFuture.completedFuture(F.Either.Right(ActorFlow.actorRef(actorRef -> WS_Compiler.props(actorRef, compiler.id), actorSystem, materializer)));

                } else {
                    logger.warn("compiler - server with token: {} is not registered in the database, rejecting", token);
                }

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            return CompletableFuture.completedFuture(F.Either.Left(forbidden()));
        });
    }

    public WebSocket portal(String token) {
        return WebSocket.Json.acceptOrResult(request -> {
            try {
                logger.info("portal - incoming connection: {}", token);

                Model_Person person;

                if (tokenCache.containsKey(token) && (person = Model_Person.getById(tokenCache.get(token))) != null) {

                    WS_Portal portal;

                    if (portals.containsKey(person.id)) {
                        portal = portals.get(person.id);
                    } else {
                        portal = new WS_Portal(person.id);
                    }

                    tokenCache.remove(token);

                    if (!portal.singles.containsKey(UUID.fromString(token))) {
                        return CompletableFuture.completedFuture(F.Either.Right(ActorFlow.actorRef(actorRef -> WS_PortalSingle.props(actorRef, portal, UUID.fromString(token)), actorSystem, materializer)));
                    } else {
                        logger.info("portal - rejecting connection: {}, already established", token);
                    }
                } else {
                    logger.info("portal - rejecting connection: {}, token is expired or person not found", token);
                }
            } catch (Exception e) {
                logger.internalServerError(e);
            }

            return CompletableFuture.completedFuture(F.Either.Left(forbidden()));
        });
    }
}
