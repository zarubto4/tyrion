package websocket;

import akka.NotUsed;
import akka.stream.javadsl.Flow;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public interface WebSocketInterface {

    /**
     * The default timeout for the {@link #ask(Request)} method.
     */
    long TIMEOUT = 10000;

    /**
     * Returns the id of the interface.
     * @return UUID id
     */
    UUID getId();

    /**
     * Sets the id of the interface.
     */
    void setId(UUID id);

    /**
     * Materializes the interface to the flow.
     * @return flow
     */
    Flow<JsonNode, JsonNode, NotUsed> jsonFlow();

    /**
     * Materializes the interface to the flow.
     * @return flow
     */
    Flow<akka.http.javadsl.model.ws.Message, akka.http.javadsl.model.ws.Message, NotUsed> textFlow();

    /**
     * Sends a message through the WebSocket.
     * No response or confirmation is required.
     * @param message to send
     */
    void tell(ObjectNode message);

    /**
     * Sends a request message through the WebSocket and waits for the response.
     * Returned stage is completed normally if the response is received,
     * otherwise it is completed exceptionally, with {@link java.util.concurrent.TimeoutException}
     * after {@link #TIMEOUT} millis.
     * @param request to perform
     * @return {@link CompletionStage}
     */
    CompletionStage<Message> ask(Request request);

    /**
     * Enables to set custom timeout value.
     * See {@link #ask(Request)} for details.
     * @param request to perform
     * @param timeout in millis after which the stage will complete exceptionally
     * @return {@link CompletionStage}
     */
    CompletionStage<Message> ask(Request request, long timeout);

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
     * Ping the interface.
     * @return number of milliseconds till the response was received.
     */
    CompletionStage<Long> ping();

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
