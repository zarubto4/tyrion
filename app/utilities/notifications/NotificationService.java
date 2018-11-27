package utilities.notifications;

import com.google.inject.Singleton;
import models.Model_Notification;
import models.Model_Person;
import utilities.enums.NotificationImportance;
import websocket.interfaces.Portal;

import java.util.*;

@Singleton
public class NotificationService {

    private final Map<UUID, List<Portal>> subscriptions = new HashMap<>();

    public void send(Model_Person receiver, Model_Notification notification) {
        this.send(Collections.singletonList(receiver), notification);
    }

    public void send(List<Model_Person> receivers, Model_Notification notification) {
        receivers.forEach(receiver -> {

            if (notification.notification_importance != NotificationImportance.LOW) {
                notification.
            }

            if (this.subscriptions.containsKey(receiver.id)) {
                this.subscriptions.get(receiver.id).forEach(portal -> portal.send());
            }
        });

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
