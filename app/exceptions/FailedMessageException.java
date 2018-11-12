package exceptions;

import websocket.Message;

public class FailedMessageException extends BaseException {

    private Message message;

    public FailedMessageException(Message message) {
        this.message = message;
    }

    public Message getFailedMessage() {
        return this.message;
    }
}
