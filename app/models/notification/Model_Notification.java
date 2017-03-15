package models.notification;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Model_Board;
import models.compiler.Model_VersionObject;
import models.person.Model_Person;
import models.project.b_program.Model_BProgram;
import models.project.c_program.Model_CProgram;
import models.project.global.Model_Project;
import play.libs.Json;
import utilities.enums.*;
import utilities.notifications.Notification_Handler;
import utilities.swagger.outboundClass.Swagger_Notification_Button;
import utilities.swagger.outboundClass.Swagger_Notification_Element;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of Notification",
        value = "Notification")
public class Model_Notification extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    static play.Logger.ALogger logger = play.Logger.of("Notification");

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                             @Id @ApiModelProperty(required = true) public String id;

    @Enumerated(EnumType.STRING) @ApiModelProperty(required = true) public Notification_level notification_level;   // Typ zprávy
    @Enumerated(EnumType.STRING) @ApiModelProperty(required = true) public Notification_importance notification_importance; // Důležitost (podbarvení zprávy)

                                @Column(columnDefinition = "TEXT")  private String content_string;  // Obsah v podobě Json.toString().
                                @Column(columnDefinition = "TEXT")  private String buttons_string;

                                @ApiModelProperty(required = true)  public boolean confirmation_required;
                                @ApiModelProperty(required = true)  public boolean confirmed;
                                @ApiModelProperty(required = true)  public boolean was_read;

    @ApiModelProperty(required = true,
            dataType = "integer", readOnly = true,
            value = "UNIX time in ms",
            example = "1466163478925")                              public Date   created;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY) public Model_Person person;

/* BODY NOTIFICATION SEGMENTS ------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    List<Swagger_Notification_Element> array = new ArrayList<>();

    @JsonIgnore @Transient
    List<Swagger_Notification_Button> buttons = new ArrayList<>();

    @JsonIgnore @Transient
    public Notification_state state;

    @JsonIgnore @Transient
    public List<Model_Person> receivers = new ArrayList<>();

    @JsonIgnore
    public Model_Notification(Notification_importance importance, Notification_level level, Model_Person person){
        this.notification_level = level;
        this.notification_importance = importance;
        this.person = person;
        created = new Date();
    }

    @JsonIgnore
    public Model_Notification(Notification_importance importance, Notification_level level){
        this.notification_level = level;
        this.notification_importance = importance;
        this.state = Notification_state.created;
        this.created = new Date();
    }

    //---------------------------------------------------------------------------------------------------------------------

    @JsonIgnore @Transient
    public Model_Notification setText(String message){

        Swagger_Notification_Element element = new Swagger_Notification_Element();
        element.type     = Notification_type.text;
        element.text     = message;
        element.color    = "black";

        array.add(element);
        return this;
    }

    public Model_Notification setText(String message, String color, boolean bold, boolean italic, boolean underline){

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
    public Model_Notification setBoldText(String message){

        Swagger_Notification_Element element = new Swagger_Notification_Element();
        element.type     = Notification_type.text;
        element.text     = message;
        element.bold     = true;

        array.add(element);
        return this;
    }

    @JsonIgnore @Transient
    public Model_Notification setObject(Class object , String id , String text, String project_id){

        Swagger_Notification_Element element = new Swagger_Notification_Element();
        element.type     = Notification_type.object;
        element.name    = object.getSimpleName().replaceAll("Swagger_","").replaceAll("Model_","");
        element.id       = id;
        element.text    = text;
        element.project_id = project_id;
        element.color      = "black";

        array.add(element);
        return this;
    }

    @JsonIgnore @Transient
    public Model_Notification setObject(Class object , String id , String text, String project_id, String color, boolean button, boolean bold, boolean italic, boolean underline){

        Swagger_Notification_Element element = new Swagger_Notification_Element();
        element.type       = Notification_type.object;
        element.name       = object.getSimpleName().replaceAll("Swagger_","").replaceAll("Model_","");
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
    public Model_Notification setObject(Object object){

        Swagger_Notification_Element element = new Swagger_Notification_Element();
        element.type       = Notification_type.object;
        element.color      = "black";

        String class_name = object.getClass().getSimpleName().replaceAll("Swagger_","").replaceAll("Model_","");

        switch (class_name){
            case "Person" : {
                element.name = class_name;
                element.text = ((Model_Person)object).full_name;
                element.id = ((Model_Person)object).id;
                break;
            }
            case "Project" : {
                element.name = class_name;
                element.text = ((Model_Project)object).name;
                element.id = ((Model_Project)object).id;
                break;
            }
            case "Board" : {
                Model_Board board = (Model_Board)object;
                element.name = class_name;
                element.id = board.id;
                element.text = board.personal_description;
                element.project_id = board.project != null ? board.project.id : null;
                break;
            }
            case "CProgram" : {
                Model_CProgram cProgram = (Model_CProgram)object;
                element.name = class_name;
                element.id = cProgram.id;
                element.text = cProgram.name;
                element.project_id = cProgram.project != null ? cProgram.project.id : null;
                break;
            }
            case "BProgram" : {
                Model_BProgram bProgram = (Model_BProgram)object;
                element.name = class_name;
                element.id = bProgram.id;
                element.text = bProgram.name;
                element.project_id = bProgram.project != null ? bProgram.project.id : null;
                break;
            }
            case "VersionObject" : {

                Model_VersionObject versionObject = (Model_VersionObject)object;

                element.id = versionObject.id;

                if (versionObject.c_program != null){

                    element.name = "C_Program_Version";
                    element.text = versionObject.version_name;
                    element.program_id = versionObject.c_program.id;
                    element.project_id = versionObject.c_program.project != null ? versionObject.c_program.project.id : null;

                } else if (versionObject.b_program != null){

                    element.name = "B_Program_Version";
                    element.text = versionObject.version_name;
                    element.program_id = versionObject.b_program.id;
                    element.project_id = versionObject.b_program.project != null ? versionObject.b_program.project.id : null;
                }

                break;
            }
        }

        array.add(element);
        return this;
    }

    @JsonIgnore @Transient
    public Model_Notification setLink(String text, String url, String color, boolean button, boolean bold, boolean italic, boolean underline){

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
    public Model_Notification setLink(String text, String url){

        Swagger_Notification_Element element = new Swagger_Notification_Element();
        element.type = Notification_type.link;
        element.url  = url;
        element.text = text;

        array.add(element);
        return this;
    }

    @JsonIgnore @Transient
    public Model_Notification setButton(Notification_action action, String payload, String color, String text, boolean bold, boolean italic, boolean underline){

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

 /* JSON PROPERTY VALUES -----------------------------------------------------------------------------------------------*/

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

            if((buttons == null || buttons.size() < 1) && buttons_string != null ){
                buttons = new ObjectMapper().readValue(buttons_string, new TypeReference<List<Swagger_Notification_Button>>() {});
            }
            return buttons;

        }catch (Exception e){
            logger.error("Parsing notification buttons error", e);
            return new ArrayList<Swagger_Notification_Button>();   // Vracím prázdný list - ale reportuji chybu
        }

    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

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

    @Override
    public void delete(){
        try {
            this.state = Notification_state.deleted;
            this.send();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.delete();
    }

    @JsonIgnore @Transient
    public Model_Notification save_object(){

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_Notification.find.byId(this.id) == null) break;
        }

        // Uložím notifikaci a její obsah převedu do Stringu
        content_string = Json.toJson(array).toString();
        buttons_string = Json.toJson(buttons).toString();
        super.save();
        return this;
    }

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
        this.state = Notification_state.updated;
        this.send();
    }

    @JsonIgnore @Transient
    public void send(List<Model_Person> receivers){
        this.receivers = new ArrayList<>();
        this.receivers = receivers;
        Notification_Handler.add_to_queue(this);
    }

    @JsonIgnore @Transient
    public void send(Model_Person person){
        this.receivers = new ArrayList<>();
        this.receivers.add(person);
        Notification_Handler.add_to_queue(this);
    }

    // Pro opětovné odeslání, když už notifikace obsahuje person
    @JsonIgnore @Transient
    public void send(){
        try {
            this.receivers = new ArrayList<>();
            this.receivers.add(this.person);
            Notification_Handler.add_to_queue(this);
        }catch (NullPointerException npe){
            logger.error("Method probably misused, use this method only when you resend notifications. If notification contains person.");
        }

    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public boolean delete_permission(){return this.person.id.equals(Controller_Security.getPerson().id) || Controller_Security.getPerson().has_permission("Notification_delete") ;}
    @JsonIgnore @Transient public boolean confirm_permission(){return this.person.id.equals(Controller_Security.getPerson().id) || Controller_Security.getPerson().has_permission("Notification_confirm") ;}


/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<String,Model_Notification> find = new Finder<>(Model_Notification.class);

}
