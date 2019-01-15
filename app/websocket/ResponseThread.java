package websocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.RequestTimeoutException;
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

            if (this.delay > 0) {
                sleep(this.delay);
            }

            while (!resolved && this.retries > 0) {

                logger.trace("get - sending message with response, message_id: {}, message_type: {}, retries: {}, timeout: {} ", id, message.get("message_type").asText(), retries, timeout);

                this.sender.send(this.message);

                --this.retries;

                sleep(this.timeout);
            }

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }

        if (resolved) {
            return null;
        }

        logger.warn("get - timeout, responding with error, id: {}", id);
        throw new RequestTimeoutException();
    }

    public void setSender(Interface sender) {
        this.sender = sender;
    }

    public void stop() {
        this.resolved = true;
    }
}
