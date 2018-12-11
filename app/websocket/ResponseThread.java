package websocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import utilities.errors.ErrorCode;
import utilities.logger.Logger;

import java.util.UUID;
import java.util.function.Supplier;

import static java.lang.Thread.sleep;

public class ResponseThread implements Supplier<Message> {

/* LOGGER --------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(ResponseThread.class);

    private Interface sender;
    private ObjectNode message;
    private UUID id;
    private int delay;
    private int timeout;
    private int retries;
    private boolean resolved;

    public ResponseThread(ObjectNode message, int delay, int timeout, int retries) {
        this.message = message;
        this.delay = delay;
        this.timeout = timeout;
        this.retries = retries;
        this.id = UUID.fromString(message.get(Message.ID).asText());
    }

    @Override
    public Message get() {
        try {
            sleep(this.delay);
            while (!resolved && this.retries > 0) {

                logger.trace("get - sending message with response, message_id: {}, message_type: {}, retries: {}, timeout: {} ", id, message.get("message_type").asText(), retries, timeout);

                this.sender.send(this.message);

                --this.retries;

                sleep(this.timeout);
            }

            if (resolved) {
                return null;
            }

            logger.warn("get - timeout, responding with error, id: {}", id);
            return time_out_exception_error_response();

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    public void setSender(Interface sender) {
        this.sender = sender;
    }

    public void stop() {
        this.resolved = true;
    }

    public Message time_out_exception_error_response() {

        logger.error("time_out_exception_error_response:: message_id: {}, message_type: {}, retries: {}, timeout: {}  ", id, message.get("message_type").asText(), retries, timeout);

        ObjectNode request = Json.newObject();
        message.put("message_type", message.get("message_type").asText());
        message.put("status", ErrorCode.WEBSOCKET_TIME_OUT_EXCEPTION.error_message());
        message.put("error_code", ErrorCode.WEBSOCKET_TIME_OUT_EXCEPTION.error_code());
        message.put("message_id",message.get("message_id").asText());
        message.put("message_channel",message.get("message_channel").asText());

        this.sender.removeMessage(this.id);

        return new Message(request, null);
    }
}
