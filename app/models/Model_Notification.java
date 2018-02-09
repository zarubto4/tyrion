package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.libs.Json;
import utilities.enums.*;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.notifications.NotificationHandler;
import utilities.notifications.helps_objects.*;
import utilities.swagger.output.Swagger_Notification_Button;
import utilities.swagger.output.Swagger_Notification_Element;
import websocket.interfaces.WS_Portal;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "Notification", description = "Model of Notification" )
@Table(name="Notification")
public class Model_Notification extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Notification.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

          @Enumerated(EnumType.STRING) public NotificationLevel notification_level;   // Typ zprávy
          @Enumerated(EnumType.STRING) public NotificationImportance notification_importance; // Důležitost (podbarvení zprávy)
          @Transient                   public NotificationType notification_type; // Typ zprávy pro long pool - chain message.
          @Enumerated(EnumType.STRING) public NotificationState state; // Machinace s notifikací na straně Becki

    @Column(columnDefinition = "TEXT") private String content_string;  // Obsah v podobě Json.toString().
    @Column(columnDefinition = "TEXT") private String buttons_string;

                                       public boolean confirmation_required;
                                       public boolean confirmed;
                                       public boolean was_read;
                                

    @JsonIgnore @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY) public Model_Person person;

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, example = "notification")            @JsonProperty public static final String messageType = "notification";              //TODO DB na message_type
    @ApiModelProperty(required = true, example = WS_Portal.CHANNEL) @JsonProperty public static final String messageChannel = WS_Portal.CHANNEL; //TODO DB na message_channel

    @JsonProperty @ApiModelProperty(required = true) public String message_type() { return messageType;}
    @JsonProperty @ApiModelProperty(required = true) public String message_channel() { return messageChannel;}

    @JsonProperty @ApiModelProperty(required = true)
    public List<Swagger_Notification_Element> notification_body() {
        try {
            if (array == null || array.size() < 1) array = new ObjectMapper().readValue(content_string, new TypeReference<List<Swagger_Notification_Element>>() {});
            return array;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();   // Vracím prázdný list - ale reportuji chybu
        }
    }

    @JsonProperty @ApiModelProperty(required = false)
    public List<Swagger_Notification_Button> buttons() {
        try {

            if ((buttons == null || buttons.size() < 1) && buttons_string != null ) {
                buttons = new ObjectMapper().readValue(buttons_string, new TypeReference<List<Swagger_Notification_Button>>() {});
            }
            return buttons;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();   // Vracím prázdný list - ale reportuji chybu
        }

    }

/* BODY NOTIFICATION SEGMENTS ------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient List<Swagger_Notification_Element> array = new ArrayList<>();
    @JsonIgnore @Transient List<Swagger_Notification_Button> buttons = new ArrayList<>();

    @JsonIgnore
    public Model_Notification() {
        this.state = NotificationState.CREATED;
        this.notification_type = NotificationType.INDIVIDUAL;
        this.created = new Date();
    }

    @JsonIgnore
    public Model_Notification setNotificationId(UUID id) {
        this.id = id;
        return this;
    }

    @JsonIgnore
    public Model_Notification setImportance(NotificationImportance importance) {
        this.notification_importance = importance;
        return this;
    }

    @JsonIgnore
    public Model_Notification setLevel(NotificationLevel level) {
        this.notification_level = level;
        return this;
    }

    @JsonIgnore
    public Model_Notification setState(NotificationState state) {
        this.state = state;
        return this;
    }

    //---------------------------------------------------------------------------------------------------------------------

    @JsonIgnore
    public Model_Notification setText(Notification_Text text) {
        array.add(text.element);
        return this;
    }

    @JsonIgnore
    public Model_Notification setNewLine() {
        array.add( new Notification_NewLine().element );
        return this;
    }

    @JsonIgnore
    public Model_Notification setChainType(NotificationType notification_type) {
        this.notification_type = notification_type;
        return this;
    }

    @JsonIgnore
    public Model_Notification setDate(Date date) {

        Notification_Date element_date = new Notification_Date();
        element_date.setDate(date);

        array.add(element_date.element);
        return this;
    }

    @JsonIgnore
    public Model_Notification setObject(Object object) {

        Swagger_Notification_Element element = new Swagger_Notification_Element();
        element.type       = NotificationElement.OBJECT;
        element.color      = "black";

        String class_name = object.getClass().getSimpleName().replaceAll("Swagger_","").replaceAll("Model_","");

        switch (class_name) {
            case "Person" : {
                element.name = class_name;
                element.text = ((Model_Person)object).full_name();
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
            case "Hardware" : {
                Model_Hardware hardware = (Model_Hardware)object;
                element.name = class_name;
                element.id = hardware.id;
                element.text = hardware.name;
                element.project_id = hardware.project_id();
                break;
            }
            case "CProgram" : {
                Model_CProgram cProgram = (Model_CProgram)object;
                element.name = class_name;
                element.id = cProgram.id;
                element.text = cProgram.name;
                element.project_id = cProgram.project_id();
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
            case "Version" : {

                Model_Version version = (Model_Version)object;

                element.id = version.id;

                if (version.get_c_program() != null) {

                    element.name = "C_Program_Version";
                    element.text = version.name;
                    element.program_id = version.get_c_program().id;
                    element.project_id = version.get_c_program().project_id();

                } else if (version.get_b_program() != null) {

                    element.name = "B_Program_Version";
                    element.text = version.name;
                    element.program_id = version.get_b_program().id;
                    element.project_id = version.get_b_program().project != null ? version.get_b_program().project.id : null;
                }

                break;
            }

            case "Instance" : {
                Model_Instance homerInstance = (Model_Instance) object;
                element.name = class_name;
                element.id = homerInstance.id;
                element.project_id = homerInstance.project_id();

                break;
            }

            case "ActualizationProcedure" : {
                Model_UpdateProcedure actualizationProcedure = (Model_UpdateProcedure) object;
                element.name = class_name;
                element.text = actualizationProcedure.id.toString().substring(0,12);
                element.id = actualizationProcedure.id;
                element.project_id = actualizationProcedure.get_project_id();
                break;
            }

            default:{
                logger.internalServerError(new Exception("Notification Unsupported Object: " + class_name));
            }
        }

        array.add(element);
        return this;
    }

    @JsonIgnore
    public Model_Notification setLink(Notification_Link link) {
        array.add(link.element);
        return this;
    }

    @JsonIgnore
    public Model_Notification setButton(Notification_Button button) {
        this.confirmation_required = true;
        buttons.add(button.element);
        return this;
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @Override
    public void save() {
        // Notifikace je automaticky uložena pomocí save_object()
        logger.info("Notifikace je automaticky uložena pomocí save_object()");
        try {
            throw new Exception("Not supported! Notifications are saved automatically using save_object()");
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @Override
    public boolean delete() {
        try {
            this.state = NotificationState.DELETED;
            this.send();
        } catch (Exception e) {
            logger.internalServerError(e);
        }
        return super.delete();
    }

    @JsonIgnore
    public Model_Notification save_object() {

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID(); // TODO rewrite
            if (Model_Notification.find.byId(this.id) == null) break;
        }

        // Uložím notifikaci a její obsah převedu do Stringu
        content_string = Json.toJson(array).toString();
        buttons_string = Json.toJson(buttons).toString();
        super.save();

        try {

            // If the notification is about project invitation, id of the notification is saved to model invitation
            if ((!this.buttons().isEmpty()) && (this.buttons().get(0).action == NotificationAction.ACCEPT_PROJECT_INVITATION)) {

                Model_Invitation invitation = Model_Invitation.getById(this.buttons().get(0).payload);
                invitation.notification_id = this.id;
                invitation.update();
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }

        return this;
    }

    @JsonIgnore
    public void set_read() {
        this.was_read = true;
        this.update();
    }

    @JsonIgnore
    public void confirm() {
        this.confirmed = true;
        this.was_read = true;
        this.update();
        this.state = NotificationState.UPDATED;
        this.send();
    }

    @JsonIgnore @Transient public List<UUID> list_of_ids_receivers = new ArrayList<>();

    @JsonIgnore
    public void send(List<Model_Person> receivers) {
        for (Model_Person person : receivers) this.list_of_ids_receivers.add(person.id);
        NotificationHandler.addToQueue(this);
    }

    @JsonIgnore
    public void send_under_project(UUID project_id) {

        if (project_id == null) {
            return;
        }

        this.list_of_ids_receivers.addAll(Model_Project.get_project_becki_person_ids_list(project_id)); // Přidám z Cashe všechny ID osob, které odebírjí konkrétní projekt
        NotificationHandler.addToQueue(this);
    }

    @JsonIgnore
    public void send(Model_Person person) {
        this.list_of_ids_receivers.add(person.id);
        NotificationHandler.addToQueue(this);
    }

    // Pro opětovné odeslání, když už notifikace obsahuje person
    @JsonIgnore
    public void send() {
        try {
            NotificationHandler.addToQueue(this);
        } catch (NullPointerException npe) {
            logger.internalServerError(new Exception("Method probably misused, use this method only when you resend notifications. If notification contains person."));
        }

    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore public boolean delete_permission() {return this.person.id.equals(BaseController.personId()) || BaseController.person().has_permission("Notification_delete") ;}
    @JsonIgnore public boolean confirm_permission() {return this.person.id.equals(BaseController.personId()) || BaseController.person().has_permission("Notification_confirm") ;}



/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Notification getById(String id) {
        return getById(UUID.fromString(id));
    }

    public static Model_Notification getById(UUID id) {
        logger.warn("CACHE is not implemented - TODO");
        return find.byId(id);
    }


/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID,Model_Notification> find = new Finder<>(Model_Notification.class);
}