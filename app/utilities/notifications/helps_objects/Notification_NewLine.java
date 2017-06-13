package utilities.notifications.helps_objects;


import utilities.enums.Enum_Notification_element_type;
import utilities.swagger.outboundClass.Swagger_Notification_Element;

public class Notification_NewLine {

    public Swagger_Notification_Element element = null;

    public Notification_NewLine(){
        element = new Swagger_Notification_Element();
        element.type = Enum_Notification_element_type.newLine;
    }
}
