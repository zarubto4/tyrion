package utilities.notifications.helps_objects;

import utilities.enums.NotificationElement;
import utilities.swagger.output.Swagger_Notification_Element;

public class Notification_Link {

    public Swagger_Notification_Element element = null;

    public Notification_Link() {
        element = new Swagger_Notification_Element();
        element.type = NotificationElement.LINK;
    }

    public Notification_Link setUrl(String text, String url) {
        element.url  = url;
        element.text = text;

        return this;
    }

}
