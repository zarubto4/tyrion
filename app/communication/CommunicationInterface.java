package communication;

import java.util.UUID;

public interface CommunicationInterface<T> {

    UUID getId();

    boolean isOnline();

    void send(T message);

    T sendWithResponse();
}
