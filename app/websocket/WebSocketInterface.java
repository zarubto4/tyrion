package websocket;

import akka.NotUsed;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.UUID;
import java.util.function.Consumer;

public interface WebSocketInterface {

    /**
     * Returns the id of the interface.
     * @return UUID id
     */
    UUID getId();

    /**
     * Materializes the interface to the flow.
     * @param materializer for materialization
     * @return flow
     */
    Flow<JsonNode, JsonNode, NotUsed> materialize(Materializer materializer);

    /**
     * Sends a message to the WebSocket interface.
     * No response or confirmation is required.
     * @param message JSON object to send
     */
    void send(ObjectNode message);

    /**
     * Sends a message to the WebSocket interface. Response is required.
     * This operation is blocking and waits until response is received or timeout occurs.
     * @param message object with details such as delay, timeout or tries.
     * @return JSON object response
     */
    ObjectNode sendWithResponse(Request message);

    /**
     * Sends a message to the WebSocket interface. Response is required.
     * This operation is non-blocking. It executes the provided consumer after the response is received.
     * @param message object with details such as delay, timeout or tries.
     * @param consumer function to execute with provided response
     */
    void sendWithResponseAsync(Request message, Consumer<ObjectNode> consumer);

    /**
     * This method receives all messages, that were not responses to waiting requests.
     * @param message received JSON object
     */
    void onMessage(Message message);

    /**
     * Checks whether the interface is online. (is connected)
     * @return true if it is online
     */
    boolean isOnline();

    /**
     * Closes the WebSocket connection.
     */
    void close();

    /**
     * Register the onClose callback.
     * @param onClose consumer
     */
    void onClose(Consumer<Interface> onClose);
}
