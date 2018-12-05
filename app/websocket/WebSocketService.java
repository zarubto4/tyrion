package websocket;

import akka.stream.javadsl.Flow;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import play.libs.Json;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Singleton
public class WebSocketService {

    private final Injector injector;

    /**
     * Contains all WebSocket connections.
     */
    private Map<UUID, WebSocketInterface> interfaces = new HashMap<>();

    @Inject
    public WebSocketService(Injector injector) {
        this.injector = injector;
    }

    public Flow<JsonNode, JsonNode, ?> register(WebSocketInterface iface) {

        this.interfaces.put(iface.getId(), iface);

        iface.onClose(i -> this.interfaces.remove(i.getId()));

        return iface.materialize(this);
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
