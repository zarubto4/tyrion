package utilities.notifications;

import com.google.inject.Singleton;
import models.Model_Notification;
import websocket.interfaces.Portal;

import java.util.*;

@Singleton
public class NotificationService {

    private final Map<UUID, List<Portal>> subscriptions = new HashMap<>();

    public void send(Model_Notification notification) {
        if (this.subscriptions.containsKey(UUID.randomUUID())) {
            // TODO
        }
    }

    public void subscribe(Portal portal) {
        if (!this.subscriptions.containsKey(portal.getPersonId())) {
            this.subscriptions.put(portal.getPersonId(), new ArrayList<>());
        }

        this.subscriptions.get(portal.getPersonId()).add(portal);
    }

    public void unsubscribe(Portal portal) {
        if (this.subscriptions.containsKey(portal.getPersonId())) {
            this.subscriptions.get(portal.getPersonId()).remove(portal);

            if (this.subscriptions.get(portal.getPersonId()).isEmpty()) {
                this.subscriptions.remove(portal.getPersonId());
            }
        }
    }
}
