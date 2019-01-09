package websocket;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.ws.WebSocketRequest;
import akka.http.javadsl.model.ws.WebSocketUpgradeResponse;
import akka.japi.Pair;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import play.inject.ApplicationLifecycle;
import play.libs.Json;
import utilities.logger.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Singleton
public class WebSocketService {

    private static final Logger logger = new Logger(WebSocketService.class);

    private final Injector injector;
    private final ActorSystem actorSystem;
    private final Materializer materializer;

    /**
     * Contains all WebSocket connections.
     */
    private Map<UUID, WebSocketInterface> interfaces = new HashMap<>();

    @Inject
    public WebSocketService(Injector injector, ApplicationLifecycle applicationLifecycle, ActorSystem actorSystem, Materializer materializer) {
        this.injector = injector;
        this.actorSystem = actorSystem;
        this.materializer = materializer;

        applicationLifecycle.addStopHook(() -> {
            this.close();
            return CompletableFuture.completedFuture(null);
        });
    }

    public CompletionStage<Void> create(Class<? extends WebSocketInterface> cls, UUID id, String url) {

        logger.info("create - connecting to: {}", url);

        WebSocketInterface webSocketInterface = this.injector.getInstance(cls);
        webSocketInterface.setId(id);

        Http http = Http.get(actorSystem);

        Pair<CompletionStage<WebSocketUpgradeResponse>, NotUsed> pair = http.singleWebSocketRequest(
                WebSocketRequest.create(url),
                webSocketInterface.textFlow(),
                this.materializer
        );

        return pair.first().thenAccept(upgrade -> {
            if (upgrade.response().status().equals(StatusCodes.SWITCHING_PROTOCOLS)) {
                logger.info("create - successful connection to: {}", url);
                this.interfaces.put(webSocketInterface.getId(), webSocketInterface);
                webSocketInterface.onClose(i -> this.interfaces.remove(i.getId()));
            } else {
                int status = upgrade.response().status().intValue();
                logger.warn("create - request failed with status: {}", status);
                throw new RuntimeException("Unable to connect to: " + url + " response status: " + status);
            }
        });
    }

    public Flow<JsonNode, JsonNode, ?> register(WebSocketInterface iface) {

        this.interfaces.put(iface.getId(), iface);

        iface.onClose(i -> this.interfaces.remove(i.getId()));

        return iface.jsonFlow();
    }

    public boolean isRegistered(UUID id) {
        return this.interfaces.containsKey(id);
    }

    public Long countOf(Predicate<WebSocketInterface> predicate) {
        return this.interfaces.values().stream().filter(predicate).count();
    }

    public List<UUID> getIdsOf(Predicate<WebSocketInterface> predicate) {
        return this.interfaces.entrySet().stream().filter(entry -> predicate.test(entry.getValue())).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public <T extends Interface> T getInterface(UUID id) {
        return (T) this.interfaces.get(id);
    }

    public void test() {
        this.interfaces.forEach((key, value) -> {
            value.send(Json.newObject().put("test", "sadasdasdas").put("message_id", UUID.randomUUID().toString()));
        });
    }

    /**
     * Closes given WebSocket connection.
     */
    public void close(UUID id) {
        if (this.interfaces.containsKey(id)) {
            this.interfaces.get(id).close();
        }
    }

    /**
     * Closes all WebSocket connections in this service.
     */
    public void close() {
        new ArrayList<>(this.interfaces.keySet()).forEach(key -> this.interfaces.get(key).close()); // Prevents ConcurrentModificationException
    }
}
