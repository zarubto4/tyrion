package utilities.notifications.helps_objects;

import utilities.enums.NotificationElement;
import utilities.swagger.output.Swagger_Notification_Element;

public class Notification_Text {

    public Swagger_Notification_Element element = null;

    public Notification_Text() {
        element = new Swagger_Notification_Element();
        element.type     = NotificationElement.TEXT;
        element.color    = Becki_color.byzance_grey_3.getColor();
    }

    public Notification_Text setText(String message) {
        element.text     = message;
        return this;
    }

    public Notification_Text setColor(Becki_color color) {
        element.color     = color.getColor();
        return this;
    }

    public Notification_Text setBoldText() {
        element.bold     = true;
        return this;
    }

    public Notification_Text setItalicText() {
        element.italic     = true;
        return this;
    }

    public Notification_Text setUnderlineText() {
        element.underline     = true;
        return this;
    }

}
