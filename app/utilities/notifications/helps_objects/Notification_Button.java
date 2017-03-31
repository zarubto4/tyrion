package utilities.notifications.helps_objects;

import utilities.enums.Enum_Notification_action;
import utilities.swagger.outboundClass.Swagger_Notification_Button;

public class Notification_Button {


    public Swagger_Notification_Button element = null;

    public Notification_Button(){
        element          =  new Swagger_Notification_Button();
        element.color    =  Becki_color.byzance_blue.getColor(); // Default color
    }

    public Notification_Button setAction(Enum_Notification_action action){
        element.action     = action;
        return this;
    }


    public Notification_Button setPayload(String payload){
        element.payload     = payload;
        return this;
    }

    public Notification_Button setColor(Becki_color color){
        element.color     = color.getColor();
        return this;
    }

    public Notification_Button setText(String text){
        element.text     = text;
        return this;
    }

    public Notification_Button setBold(){
        element.bold     = true;
        return this;
    }

    public Notification_Button setItalic(){
        element.italic     = true;
        return this;
    }

    public Notification_Button setUnderLine(){
        element.underline     = true;
        return this;
    }


    //(Enum_Notification_action action, String payload, String color, String text, boolean bold, boolean italic, boolean underline){

}
