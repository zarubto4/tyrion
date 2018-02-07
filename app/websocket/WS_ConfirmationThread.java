package websocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import utilities.errors.ErrorCode;
import utilities.logger.Logger;

import java.util.UUID;
import java.util.function.Supplier;

import static java.lang.Thread.sleep;

public class WS_ConfirmationThread implements Supplier<ObjectNode> {

/* LOGGER --------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(WS_ConfirmationThread.class);

    private WS_Interface sender;
    private ObjectNode message;
    private UUID id;
    private int delay;
    private int timeout;
    private int retries;

    public WS_ConfirmationThread(ObjectNode message, int delay, int timeout, int retries) {
        this.message = message;
        this.delay = delay;
        this.timeout = timeout;
        this.retries = retries;
        this.id = UUID.fromString(message.get("message_id").asText());
    }

    @Override
    public ObjectNode get() {
        try {

            sleep(this.delay);

            while (this.retries >= 0) {

                logger.trace("get - sending message with response, message_id: {}, message_type: {}, retries: {}, timeout: {} ", id, message.get("message_type").asText(), retries, timeout);

                this.sender.send(this.message);

                --this.retries;

                sleep(this.timeout);
            }

            logger.warn("get - timeout, responding with error, id: {}", id);
            return time_out_exception_error_response();

        } catch (Exception e) {
            logger.internalServerError(e);
            return null; // TODO maybe some error?
        }
    }

    public void setSender(WS_Interface sender) {
        this.sender = sender;
    }

    public ObjectNode time_out_exception_error_response() {


        ObjectNode request = Json.newObject();
        message.put("error_message", ErrorCode.WEBSOCKET_TIME_OUT_EXCEPTION.error_message());
        message.put("error_code", ErrorCode.WEBSOCKET_TIME_OUT_EXCEPTION.error_code());

        this.sender.removeMessage(this.id);

        return request;
    }
}
