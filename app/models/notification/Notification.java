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
import utilities.enums.Notification_action;
import utilities.enums.Notification_type;
import utilities.enums.Notification_importance;
import utilities.enums.Notification_level;
import utilities.notifications.Notification_Handler;
import utilities.swagger.outboundClass.Swagger_Notification_Button;
import utilities.swagger.outboundClass.Swagger_Notification_Element;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Entity
public class Notification extends Model {

    static play.Logger.ALogger logger = play.Logger.of("Notification");

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true) public String id;

    @Enumerated(EnumType.STRING)       @ApiModelProperty(required = true) public Notification_level notification_level;     // Důležitost (podbarvení zprávy)     // Typ zprávy
    @Enumerated(EnumType.STRING)       @ApiModelProperty(required = true) public Notification_importance notification_importance; // Důležitost (podbarvení zprávy)

    @Column(columnDefinition = "TEXT")  private String content_string;                           // Obsah v podobě Json.toString().
    @Column(columnDefinition = "TEXT")  private String buttons_string;

    @ApiModelProperty(required = true)  public boolean confirmation_required;
    @ApiModelProperty(required = true)  public boolean confirmed;
    @ApiModelProperty(required = true)  public boolean was_read;

    @ApiModelProperty(required = true,
            dataType = "integer", readOnly = true,
            value = "UNIX time in milis - Date: number of miliseconds elapsed since  Thursday, 1 January 1970",
            example = "1466163478925")                                    public Date   created;
    @JsonIgnore @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)  public Person person;



/* BODY NOTIFICATION SEGMENTS ------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    List<Swagger_Notification_Element> array = new ArrayList<>();

    @JsonIgnore @Transient
    List<Swagger_Notification_Button> buttons = new ArrayList<>();

    @JsonIgnore @Transient
    public List<Person> receivers = new ArrayList<>();

    @JsonIgnore
    public  Notification(Notification_importance importance, Notification_level level, Person person){
        this.notification_level = level;
        this.notification_importance = importance;
        this.person = person;
        created = new Date();
    }

    @JsonIgnore
    public  Notification(Notification_importance importance, Notification_level level){
        this.notification_level = level;
        this.notification_importance = importance;
        this.created = new Date();
    }

    //---------------------------------------------------------------------------------------------------------------------

    //---------------------------------------------------------------------------------------------------------------------

    @JsonIgnore @Transient
    public Notification setText(String message){

        Swagger_Notification_Element element = new Swagger_Notification_Element();
        element.type     = Notification_type.text;
        element.text     = message;
        element.color    = "black";

        array.add(element);
        return this;
    }

    public Notification setText(String message, String color, boolean bold, boolean italic, boolean underline){

        Swagger_Notification_Element element = new Swagger_Notification_Element();
        element.type     = Notification_type.text;
        element.text     = message;
        element.color    = color;
        element.bold     = bold;
        element.italic   = italic;
        element.underline= underline;

        array.add(element);
        return this;
    }

    @JsonIgnore @Transient
    public Notification setBoldText(String message){

        Swagger_Notification_Element element = new Swagger_Notification_Element();
        element.type     = Notification_type.text;
        element.text     = message;
        element.bold     = true;

        array.add(element);
        return this;
    }

    @JsonIgnore @Transient
    public Notification setObject(Class object , String id , String text, String project_id){

        Swagger_Notification_Element element = new Swagger_Notification_Element();
        element.type     = Notification_type.object;
        element.name    = object.getSimpleName().replaceAll("Swagger_","");
        element.id       = id;
        element.text    = text;
        element.project_id = project_id;
        element.color      = "black";

        array.add(element);
        return this;
    }

    @JsonIgnore @Transient
    public Notification setObject(Class object , String id , String text, String project_id, String color, boolean button, boolean bold, boolean italic, boolean underline){

        Swagger_Notification_Element element = new Swagger_Notification_Element();
        element.type       = Notification_type.object;
        element.name       = object.getSimpleName().replaceAll("Swagger_","");
        element.id         = id;
        element.text       = text;
        element.project_id = project_id;
        element.color      = color;
        element.button     = button;
        element.bold       = bold;
        element.italic     = italic;
        element.underline  = underline;

        array.add(element);
        return this;
    }

    @JsonIgnore @Transient
    public Notification setLink(String text, String url, String color, boolean button, boolean bold, boolean italic, boolean underline){

        Swagger_Notification_Element element = new Swagger_Notification_Element();
        element.type = Notification_type.link;
        element.url  = url;
        element.text = text;
        element.color     = color;
        element.button    = button;
        element.bold      = bold;
        element.italic    = italic;
        element.underline = underline;

        array.add(element);
        return this;
    }

    @JsonIgnore @Transient
    public Notification setLink(String text, String url){

        Swagger_Notification_Element element = new Swagger_Notification_Element();
        element.type = Notification_type.link;
        element.url  = url;
        element.text = text;

        array.add(element);
        return this;
    }

    @JsonIgnore @Transient
    public Notification setButton(Notification_action action, String payload, String color, String text, boolean bold, boolean italic, boolean underline){

        this.confirmation_required = true;

        Swagger_Notification_Button button = new Swagger_Notification_Button();
        button.action    = action;
        button.payload   = payload;
        button.text      = text;
        button.color     = color;
        button.bold      = bold;
        button.italic    = italic;
        button.underline = underline;

        buttons.add(button);
        return this;
    }

    @Override
    public void save(){
        // Notifikace je automaticky uložena pomocí save_object()
        logger.info("Notifikace je automaticky uložena pomocí save_object()");
        try {
            throw new Exception("Not supported! Notifications are saved automatically using save_object()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JsonIgnore @Transient
    public Notification save_object(){

        // Uložím notifikaci a její obsah převedu do Stringu
        content_string = Json.toJson(array).toString();
        buttons_string = Json.toJson(buttons).toString();
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
            logger.error("Parsing notification body error", e);
            return new ArrayList<Swagger_Notification_Element>();   // Vracím prázdný list - ale reportuji chybu
        }

    }

    @JsonProperty
    @ApiModelProperty(required = true)
    public List<Swagger_Notification_Button> buttons(){
        try {
            if(buttons == null || buttons.size() < 1) buttons = new ObjectMapper().readValue(buttons_string, new TypeReference<List<Swagger_Notification_Button>>() {});
            return buttons;

        }catch (Exception e){
            logger.error("Parsing notification buttons error", e);
            return new ArrayList<Swagger_Notification_Button>();   // Vracím prázdný list - ale reportuji chybu
        }

    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void set_read(){
        this.was_read = true;
        this.update();
    }

    @JsonIgnore @Transient
    public void confirm(){
        this.confirmed = true;
        this.was_read = true;
        this.update();
    }

    @JsonIgnore @Transient
    public void send(List<Person> receivers){
        this.receivers = new ArrayList<>();
        this.receivers = receivers;
        Notification_Handler.add_to_queue(this);
    }

    @JsonIgnore @Transient
    public void send(Person person){
        this.receivers = new ArrayList<>();
        this.receivers.add(person);
        Notification_Handler.add_to_queue(this);
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public boolean delete_permission(){return this.person.id.equals(SecurityController.getPerson().id) || SecurityController.getPerson().has_permission("Notification_delete") ;}
    @JsonIgnore @Transient public boolean confirm_permission(){return this.person.id.equals(SecurityController.getPerson().id) || SecurityController.getPerson().has_permission("Notification_confirm") ;}


/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<String,Notification> find = new Finder<>(Notification.class);

}
