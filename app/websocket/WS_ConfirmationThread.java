package websocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import utilities.errors.ErrorCode;
import utilities.logger.Logger;
import websocket.messages.compilator_with_tyrion.WS_Message_Make_compilation;

import javax.xml.crypto.Data;
import java.util.Date;
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
    private boolean resolved;

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

            logger.trace("Sending Thread Start:: {}:{} Planed delay: {}", new Date().getMinutes(), new Date().getSeconds(), this.delay);
            sleep(this.delay);
            logger.trace("Sending Thread After delay:: {}:{} Planed delay: {}", new Date().getMinutes(), new Date().getSeconds(), this.delay);

            while (!resolved && this.retries >= 0) {

                logger.trace("get - sending message with response, message_id: {}, message_type: {}, retries: {}, timeout: {} ", id, message.get("message_type").asText(), retries, timeout);

                // Sender is not set -so message was add to buffer for a second response
                if(sender == null) {
                    logger.trace("Sending Thread After delay:: {} time", this.delay);
                    return time_out_exception_error_response();
                }

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
            return null; // TODO maybe some error?
        }
    }

    public void setSender(WS_Interface sender) {
        this.sender = sender;
    }

    public void stop() {
        this.resolved = true;
    }

    public ObjectNode time_out_exception_error_response() {

        logger.error("time_out_exception_error_response:: message_id: {}, message_type: {}, retries: {}, timeout: {}  ", id, message.get("message_type").asText(), retries, timeout);

        ObjectNode request = Json.newObject();
        message.put("message_type", message.get("message_type").asText());
        message.put("status", ErrorCode.WEBSOCKET_TIME_OUT_EXCEPTION.error_message());
        message.put("error_code", ErrorCode.WEBSOCKET_TIME_OUT_EXCEPTION.error_code());
        message.put("message_id",message.get("message_id").asText());
        message.put("message_channel",message.get("message_channel").asText());
        message.put("websocket_identificator",message.get("websocket_identificator").asText());

        this.sender.removeMessage(this.id);

        return request;
    }
}
