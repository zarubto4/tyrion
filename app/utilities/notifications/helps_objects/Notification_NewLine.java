package utilities.notifications.helps_objects;


import utilities.enums.NotificationElement;
import utilities.swagger.output.Swagger_Notification_Element;

public class Notification_NewLine {

    public Swagger_Notification_Element element = null;

    public Notification_NewLine() {
        element = new Swagger_Notification_Element();
        element.type = NotificationElement.NEW_LINE;
    }
}
