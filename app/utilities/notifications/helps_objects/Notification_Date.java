package utilities.notifications.helps_objects;

import utilities.enums.NotificationElement;
import utilities.swagger.output.Swagger_Notification_Element;

import java.util.Date;


public class Notification_Date {

    public Swagger_Notification_Element element = null;

    public Notification_Date() {
        element = new Swagger_Notification_Element();
        element.type = NotificationElement.DATE;
    }

    public Notification_Date setDate(Date date) {
        element.date  = date;

        return this;
    }
}



