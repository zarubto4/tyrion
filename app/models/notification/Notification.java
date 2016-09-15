package models.notification;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.person.Person;
import play.libs.Json;
import utilities.Server;
import utilities.enums.Notification_Type;
import utilities.enums.Notification_level;
import utilities.swagger.outboundClass.Swagger_Notification_Element;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Notification extends Model {

    static play.Logger.ALogger logger = play.Logger.of("Notification");

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true) public String id;

    @Enumerated(EnumType.STRING)       @ApiModelProperty(required = true) public Notification_level notification_level;     // Důležitost (podbarvení zprávy)

    @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true) private String content_string;                           // Obsah v podobě Json.toString().
                                       @ApiModelProperty(required = true) public boolean confirmation_required;
                                                           @JsonIgnore    public boolean confirmed;
                                       @ApiModelProperty(required = true) public boolean was_read;

    @ApiModelProperty(required = true,
            dataType = "integer", readOnly = true,
            value = "UNIX time in milis - Date: number of miliseconds elapsed since  Thursday, 1 January 1970",
            example = "1466163478925")                                    public Date   created;
    @JsonIgnore @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)  private Person person;



/* BODY NOTIFICATION SEGMENTS ------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    List<Swagger_Notification_Element> array = new ArrayList<>();

    @JsonIgnore
    public  Notification(Notification_level level, Person person){
        this.notification_level = level;
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

        Swagger_Notification_Element element = new Swagger_Notification_Element();
        element.type     = Notification_Type.text;
        element.value    = message;

        array.add(element);
        return this;
    }

    @JsonIgnore @Transient
    public Notification setBoldText(String message){

        Swagger_Notification_Element element = new Swagger_Notification_Element();
        element.type     = Notification_Type.bold_text;
        element.value    = message;

        array.add(element);
        return this;
    }

    @JsonIgnore @Transient
    public Notification setObject(Class object , String id , String label){

        Swagger_Notification_Element element = new Swagger_Notification_Element();
        element.type     = Notification_Type.object;
        element.value    = object.getSimpleName().replaceAll("Swagger_","");
        element.id       = id;
        element.label    = label;

        array.add(element);
        return this;
    }

    @JsonIgnore @Transient
    public Notification setLink_ToTyrion(String label, String url){

        Swagger_Notification_Element element = new Swagger_Notification_Element();
        element.type     = Notification_Type.confirmation;
        element.url = url;
        element.label = label;

        array.add(element);
        return this;
    }

    @JsonIgnore @Transient
    public Notification required(){
        confirmation_required = true;

        Swagger_Notification_Element element = new Swagger_Notification_Element();
        element.type     = Notification_Type.confirmation;
        element.required = true;
        element.get_url     = Server.tyrion_serverAddress + "/notification/confirm/" + this.id;

        array.add(element);
        return this;
    }

    @JsonIgnore @Transient
    public Notification save_object(){
        content_string = Json.toJson(array).toString();
        super.save();
        return this;
    }




 /* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @JsonProperty
    @ApiModelProperty(required = true)
    public List<Swagger_Notification_Element> notification_body(){
        try {
                if(array == null || array.size() < 1) array = new ObjectMapper().readValue(content_string, new TypeReference<List<Swagger_Notification_Element>>() {});
                return array;

        }catch (Exception e){
            logger.error("Parsing notification error", e);
            return new ArrayList<Swagger_Notification_Element>();   // Vracím prázdný list - ale reportuji chybu
        }

    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void set_read(){
            was_read = true;
            this.update();
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

   @JsonIgnore @Transient public boolean delete_permission(){  return person.id.equals(SecurityController.getPerson().id);}


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Notification> find = new Finder<>(Notification.class);

}
