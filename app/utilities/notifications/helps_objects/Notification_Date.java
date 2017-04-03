package utilities.notifications.helps_objects;

import utilities.enums.Enum_Notification_element_type;
import utilities.swagger.outboundClass.Swagger_Notification_Element;

import java.util.Date;


public class Notification_Date {

    public Swagger_Notification_Element element = null;

    public Notification_Date(){
        element = new Swagger_Notification_Element();
        element.type = Enum_Notification_element_type.date;
    }

    public Notification_Date setDate(Date date){
        element.date  = date;

        return this;
    }
}



