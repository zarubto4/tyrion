package utilities.notifications.helps_objects;

import utilities.enums.Enum_Notification_type;
import utilities.swagger.outboundClass.Swagger_Notification_Element;

public class Notification_Link {

    public Swagger_Notification_Element element = null;

    public Notification_Link(){
        element = new Swagger_Notification_Element();
        element.type = Enum_Notification_type.link;
    }

    public Notification_Link setUrl(String text, String url){
        element.url  = url;
        element.text = text;

        return this;
    }

}
