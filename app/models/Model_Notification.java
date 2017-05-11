package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.libs.Json;
import utilities.enums.*;
import utilities.logger.Class_Logger;
import utilities.notifications.NotificationHandler;
import utilities.notifications.helps_objects.Notification_Button;
import utilities.notifications.helps_objects.Notification_Date;
import utilities.notifications.helps_objects.Notification_Link;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.swagger.outboundClass.Swagger_Notification_Button;
import utilities.swagger.outboundClass.Swagger_Notification_Element;
import web_socket.services.WS_Becki_Website;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "Notification", description = "Model of Notification" )
public class Model_Notification extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_Notification.class);


/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

             @Id   @JsonProperty @ApiModelProperty(required = true) public String id;

    @Enumerated(EnumType.STRING) @ApiModelProperty(required = true) public Enum_Notification_level notification_level;   // Typ zprávy
    @Enumerated(EnumType.STRING) @ApiModelProperty(required = true) public Enum_Notification_importance notification_importance; // Důležitost (podbarvení zprávy)
    @Transient                   @ApiModelProperty(required = true) public Enum_Notification_type notification_type; // Typ zprávy pro long pool - chain message.
    @Enumerated(EnumType.STRING) @ApiModelProperty(required = true) public Enum_Notification_state state; // Machinace s notifikací na straně Becki

                                @Column(columnDefinition = "TEXT")  private String content_string;  // Obsah v podobě Json.toString().
                                @Column(columnDefinition = "TEXT")  private String buttons_string;

                                @ApiModelProperty(required = true)  public boolean confirmation_required;
                                @ApiModelProperty(required = true)  public boolean confirmed;
                                @ApiModelProperty(required = true)  public boolean was_read;


    @ApiModelProperty(required = true, dataType = "integer",
            readOnly = true, value = "UNIX time in ms")             public Date   created;

    @JsonIgnore @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY) public Model_Person person;


/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, example = "notification")            @JsonProperty public static final String messageType = "notification";
    @ApiModelProperty(required = true, example =  WS_Becki_Website.CHANNEL) @JsonProperty public static final String messageChannel = WS_Becki_Website.CHANNEL;

    @JsonProperty @ApiModelProperty(required = true) public String messageType(){ return messageType;}
    @JsonProperty @ApiModelProperty(required = true) public String messageChannel(){ return messageChannel;}


    @JsonProperty @ApiModelProperty(required = true)
    public List<Swagger_Notification_Element> notification_body(){
        try {
            if(array == null || array.size() < 1) array = new ObjectMapper().readValue(content_string, new TypeReference<List<Swagger_Notification_Element>>() {});
            return array;

        }catch (Exception e){
            terminal_logger.error("notification_body:: Parsing notification body error", e);
            return new ArrayList<Swagger_Notification_Element>();   // Vracím prázdný list - ale reportuji chybu
        }
    }

    @JsonProperty @ApiModelProperty(required = false)
    public List<Swagger_Notification_Button> buttons(){
        try {

            if((buttons == null || buttons.size() < 1) && buttons_string != null ){
                buttons = new ObjectMapper().readValue(buttons_string, new TypeReference<List<Swagger_Notification_Button>>() {});
            }
            return buttons;

        }catch (Exception e){
            terminal_logger.error("buttons:: Parsing notification buttons error", e);
            return new ArrayList<Swagger_Notification_Button>();   // Vracím prázdný list - ale reportuji chybu
        }

    }

/* BODY NOTIFICATION SEGMENTS ------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient List<Swagger_Notification_Element> array = new ArrayList<>();
    @JsonIgnore @Transient List<Swagger_Notification_Button> buttons = new ArrayList<>();


    @JsonIgnore
    public Model_Notification(){
        this.state = Enum_Notification_state.created;
        this.notification_type = Enum_Notification_type.INDIVIDUAL;
        this.created = new Date();
    }

    @JsonIgnore  @Transient
    public Model_Notification setId(String id){
        this.id = id;
        return this;
    }

    @JsonIgnore  @Transient
    public Model_Notification setImportance(Enum_Notification_importance importance){
        this.notification_importance = importance;
        return this;
    }

    @JsonIgnore  @Transient
    public Model_Notification setLevel(Enum_Notification_level level){
        this.notification_level = level;
        return this;
    }

    @JsonIgnore  @Transient
    public Model_Notification setState(Enum_Notification_state state){
        this.state = state;
        return this;
    }

    //---------------------------------------------------------------------------------------------------------------------

    @JsonIgnore @Transient
    public Model_Notification setText(Notification_Text text){
        array.add(text.element);
        return this;
    }

    @JsonIgnore @Transient
    public Model_Notification setChainType(Enum_Notification_type notification_type){
        this.notification_type = notification_type;
        return this;
    }

    @JsonIgnore @Transient
    public Model_Notification setDate(Date date){

        Notification_Date element_date = new Notification_Date();
        element_date.setDate(date);

        array.add(element_date.element);
        return this;
    }

    @JsonIgnore @Transient
    public Model_Notification setObject(Object object){

        Swagger_Notification_Element element = new Swagger_Notification_Element();
        element.type       = Enum_Notification_element_type.object;
        element.color      = "black";

        String class_name = object.getClass().getSimpleName().replaceAll("Swagger_","").replaceAll("Model_","");

        switch (class_name){
            case "Person" : {
                element.name = class_name;
                element.text = ((Model_Person)object).full_name;
                element.id = ((Model_Person)object).id;
                break;
            }
            case "Product" : {
                element.name = class_name;
                element.text = ((Model_Product)object).name;
                element.id = ((Model_Product)object).id;
                break;
            }
            case "Invoice" : {
                element.name = class_name;
                element.text = ((Model_Invoice)object).invoice_number;
                element.id = ((Model_Invoice)object).id;
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

            case "HomerInstance" : {
                Model_HomerInstance homerInstance = (Model_HomerInstance) object;
                element.name = class_name;
                element.id = homerInstance.blocko_instance_name;
                if(homerInstance.project != null) element.project_id = homerInstance.project.id;
                else element.project_id = homerInstance.b_program.project_id();

                break;
            }

            case "ActualizationProcedure" : {
                Model_ActualizationProcedure actualizationProcedure = (Model_ActualizationProcedure) object;
                element.name = class_name;
                element.id = actualizationProcedure.id;
                element.project_id = actualizationProcedure.get_project_id();
                break;
            }

            default:{
                terminal_logger.internalServerError("setObject:", new Exception("Notification Unsupported Object: " + class_name));
            }
        }

        array.add(element);
        return this;
    }


    @JsonIgnore @Transient
    public Model_Notification setLink(Notification_Link link){
        array.add(link.element);
        return this;
    }

    @JsonIgnore @Transient
    public Model_Notification setButton(Notification_Button button){
        this.confirmation_required = true;
        buttons.add(button.element);
        return this;
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @Override
    public void save(){
        // Notifikace je automaticky uložena pomocí save_object()
        terminal_logger.info("Notifikace je automaticky uložena pomocí save_object()");
        try {
            throw new Exception("Not supported! Notifications are saved automatically using save_object()");
        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }

    @Override
    public void delete(){
        try {
            this.state = Enum_Notification_state.deleted;
            this.send();
        } catch (Exception e) {
            terminal_logger.internalServerError(e);
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

        try {

            // If the notification is about project invitation, id of the notification is saved to model invitation
            if ((!this.buttons().isEmpty()) && (this.buttons().get(0).action == Enum_Notification_action.accept_project_invitation)) {

                Model_Invitation invitation = Model_Invitation.find.byId(this.buttons().get(0).payload);
                invitation.notification_id = this.id;
                invitation.update();
            }
        } catch (Exception e) {
            terminal_logger.internalServerError("save_object:", e);
        }

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
        this.state = Enum_Notification_state.updated;
        this.send();
    }



    @JsonIgnore @Transient public List<String> list_of_ids_receivers = new ArrayList<>(); // List ofon_ids Pers

    @JsonIgnore @Transient
    public void send(List<Model_Person> receivers){
        for(Model_Person person : receivers) this.list_of_ids_receivers.add(person.id);
        NotificationHandler.addToQueue(this);
    }


    @JsonIgnore @Transient
    public void send_under_project(String project_id){
        this.list_of_ids_receivers.addAll( Model_Project.get_project_becki_person_ids_list(project_id)); // Přidám z Cashe všechny ID osob, které odebírjí konkrétní projekt
        NotificationHandler.addToQueue(this);
    }

    @JsonIgnore @Transient
    public void send(Model_Person person){
        this.list_of_ids_receivers.add(person.id);
        NotificationHandler.addToQueue(this);
    }

    // Pro opětovné odeslání, když už notifikace obsahuje person
    @JsonIgnore @Transient
    public void send(){
        try {
            NotificationHandler.addToQueue(this);
        }catch (NullPointerException npe){
            terminal_logger.error("Method probably misused, use this method only when you resend notifications. If notification contains person.");
        }

    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public boolean delete_permission(){return this.person.id.equals(Controller_Security.get_person().id) || Controller_Security.get_person().has_permission("Notification_delete") ;}
    @JsonIgnore @Transient public boolean confirm_permission(){return this.person.id.equals(Controller_Security.get_person().id) || Controller_Security.get_person().has_permission("Notification_confirm") ;}


/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<String,Model_Notification> find = new Finder<>(Model_Notification.class);

}
