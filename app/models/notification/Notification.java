package models.notification;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.NotificationController;
import io.swagger.annotations.ApiModelProperty;
import models.person.Person;
import play.libs.Json;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Notification extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true) public String id;

    @Enumerated(EnumType.STRING)       @ApiModelProperty(required = true) public NotificationController.Notification_level level;     // Důležitost (podbarvení zprávy)

    @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true) private String content_string;                           // Obsah v podobě Json.toString().
                                       @ApiModelProperty(required = true) public boolean confirmation_required;
                                                           @JsonIgnore    public boolean confirmed;
                                       @ApiModelProperty(required = true) public boolean was_read;


    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp", example = "1458315085") private Date created;
    @JsonIgnore @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)  private Person person;



/* BODY NOTIFICATION SEGMENTS ------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    ArrayNode array = new ObjectMapper().createArrayNode();

    @JsonIgnore
    public  Notification(NotificationController.Notification_level level, Person person){
        this.level = level;
        this.person = person;
        created = new Date();
    }

 //---------------------------------------------------------------------------------------------------------------------

    public enum Notification_type {

        SINGLE_STRING_MESSAGE,
        BOARD_UPDATE, // Typ zprávy při updatu desky
        PROJECT_INVITE, // Typ zprávy, když uživatel pozve jiného do projektu
    }

 //---------------------------------------------------------------------------------------------------------------------

    @JsonIgnore @Transient
    public Notification setText(String message){

        ObjectNode o = Json.newObject();
             o.put("type", "text");
             o.put("value", message);

        array.add(o);
        return this;
    }

    @JsonIgnore @Transient
    public Notification setBoldText(String message){

        ObjectNode o = Json.newObject();
        o.put("type", "bold_text");
        o.put("value", message);

        array.add(o);
        return this;
    }

    @JsonIgnore @Transient
    public Notification setObject(Class object , String id , String label){

        ObjectNode o = Json.newObject();
        o.put("type", "object");
        o.put("value", object.getSimpleName().replaceAll("Swagger_","") );
        o.put("id", id);
        o.put("label",  label );

        array.add(o);
        return this;
    }

    @JsonIgnore @Transient
    public Notification setLink_ToTyrion(String label, String url){

        ObjectNode o = Json.newObject();
        o.put("type", "link");
        o.put("url",url);
        o.put("label", label);

        array.add(o);
        return this;
    }

    @JsonIgnore @Transient
    public Notification required(String url){
        confirmation_required = true;

        ObjectNode o = Json.newObject();
        o.put("type", "confirmation");
        o.put("required", true);
        o.put("get_url", url);

        array.add(o);
        return this;
    }

    @JsonIgnore @Transient
    public Notification save_object(){
        content_string = array.toString();
        super.save();
        return this;
    }




 /* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @JsonProperty(value = "Its not possible document that throw swagger. Please visit documentation.byzance.cz")
    @ApiModelProperty(required = true)
    public JsonNode notification_body(){
        if(array.size() < 1) return Json.parse(content_string);
        else return array;
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void set_read(){
            was_read = true;
            this.update();
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Notification> find = new Finder<>(Notification.class);
}
