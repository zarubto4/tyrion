package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import org.hibernate.validator.constraints.Email;
import org.springframework.cache.annotation.Cacheable;
import play.data.validation.Constraints;
import utilities.Server;
import utilities.enums.Enum_Notification_action;
import utilities.enums.Enum_Notification_importance;
import utilities.enums.Enum_Notification_level;
import utilities.logger.Class_Logger;
import utilities.notifications.helps_objects.Becki_color;
import utilities.notifications.helps_objects.Notification_Button;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.swagger.outboundClass.Swagger_Person_Short_Detail;
import utilities.swagger.outboundClass.Swagger_Project_Short_Detail;

import javax.persistence.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "Person", description = "Model of Person")
public class Model_Person extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_Person.class);


/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @ApiModelProperty(required = true)                      public String id;
    @ApiModelProperty(required = true) @Column(unique=true)
    @Email @Constraints.Email                                   public String mail;
    @ApiModelProperty(required = true) @Column(unique=true)     public String nick_name;
    @ApiModelProperty(required = true)                          public String full_name;
    @ApiModelProperty(required = true)                          public String country;
    @ApiModelProperty(required = true)                          public String gender;

                                                    @JsonIgnore public String azure_picture_link;
                                        @JsonIgnore @OneToOne   public Model_FileRecord picture;

                                                 @JsonIgnore    public boolean freeze_account; // Zmražený účet - Účty totiž nechceme mazat!
                                                 @JsonIgnore    public boolean mailValidated;

                                                 @JsonIgnore    public String facebook_oauth_id;
                                                 @JsonIgnore    public String github_oauth_id;

    @JsonIgnore  @Column(length = 64)                           public byte[] shaPassword;
    @JsonIgnore  @OneToOne(mappedBy = "person")                 public Model_PasswordRecoveryToken passwordRecoveryToken;
    @JsonIgnore  @OneToOne(mappedBy = "person")                 public Model_ChangePropertyToken   changePropertyToken;

    @JsonIgnore   @OneToOne(mappedBy="person", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public Model_Customer customer;
    @JsonIgnore  @OneToMany(mappedBy="person", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_Employee>           employees            = new ArrayList<>();
    @JsonIgnore  @OneToMany(mappedBy="person", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_ProjectParticipant> projects_participant = new ArrayList<>();

    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_SecurityRole>   roles                     = new ArrayList<>();
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_Permission>     person_permissions        = new ArrayList<>();


    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_BlockoBlock>          blocksAuthor         = new ArrayList<>(); // Propojení, které bločky uživatel vytvořil
    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_BlockoBlockVersion>   blockVersionsAuthor  = new ArrayList<>(); // Propojení, které verze bločků uživatel vytvořil
    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_GridWidget>           widgetsAuthor        = new ArrayList<>(); // Propojení, které widgety uživatel vytvořil
    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_GridWidgetVersion>    widgetVersionsAuthor = new ArrayList<>(); // Propojení, které verze widgetů uživatel vytvořil
    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_VersionObject>        version_objects      = new ArrayList<>(); // Propojení, které verze uživatel vytvořil
    @JsonIgnore  @OneToMany(mappedBy="person", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_FloatingPersonToken>  floatingPersonTokens = new ArrayList<>(); // Propojení, které uživatel napsal
    @JsonIgnore  @OneToMany(mappedBy="owner",  cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_Invitation>           invitations          = new ArrayList<>(); // Pozvánky, které uživatel rozeslal
    @JsonIgnore  @OneToMany(mappedBy="person", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_Notification>         notifications        = new ArrayList<>();
    @JsonIgnore  @OneToMany(mappedBy="person", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_GridTerminal>         grid_terminals       = new ArrayList<>(); // Přihlášený websocket uživatele

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public List<String> project_ids = new ArrayList<>();
    @JsonIgnore @Transient public HashMap<String, Boolean> permissions_keys = new HashMap<>();

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true)
    public String picture_link(){
        if(this.azure_picture_link == null){
            return null;
        }

        if(azure_picture_link.contains("http")) return azure_picture_link;  // Its probably link from GitHub or profile picture from facebook
        return Server.azure_blob_Link + azure_picture_link;
    }

/* JSON IGNORE VALUES --------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public List<Swagger_Project_Short_Detail> get_user_access_projects(){

        // Chache Add Projects
        if(project_ids.isEmpty()) {

            // Získání seznamu
            List<Model_Project> projects = Model_Project.find.where().eq("participants.person.id", id).order().asc("name").select("id").findList();
            for (Model_Project project : projects) {
                project_ids.add(project.id);
            }
        }

        List<Swagger_Project_Short_Detail> projects = new ArrayList<>();

        for(String project_id : project_ids){
            projects.add(Model_Project.get_byId(project_id).project_short_detail());
        }

        return projects;
    }


/* Security Tools @ JSON IGNORE -----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Swagger_Person_Short_Detail get_short_person() {

        Swagger_Person_Short_Detail help = new Swagger_Person_Short_Detail();
        help.id = this.id;
        help.nick_name = this.nick_name;
        help.mail = this.mail;

        return help;
    }

    @JsonIgnore @Transient
    public static byte[] getSha512(String value) {
        try {
            return MessageDigest.getInstance("SHA-512").digest(value.getBytes("UTF-8"));
        }
        catch (Exception e) { throw new RuntimeException(e);}
    }

    @JsonIgnore
    public void setSha(String value) {
        setShaPassword( getSha512(value) );
    }

    // Z důvodu Cashování Play na SETTER a GETTER byla zvolena tato "zbytečná metoda" - slouží jen pro Definování HASH hesla ( New, Recovery)
    @JsonIgnore
    public void setShaPassword(byte[] shaPassword) {
        this.shaPassword = shaPassword;
    }

    @JsonIgnore @Transient
    public boolean has_permission(String permission){
        try {

            return Model_Permission.find.where().eq("value", permission).eq("roles.persons.id", this.id).findRowCount() +
                    Model_Permission.find.where().eq("value", permission).eq("persons.id", this.id).findRowCount() > 0;
        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return false;
        }
    }

    @JsonIgnore @Transient
    public CloudBlobContainer get_Container(){
        try {
            return Server.blobClient.getContainerReference("pictures");
        }catch (Exception e){
            terminal_logger.internalServerError("get_Container:", e);
            throw new NullPointerException();
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save: Creating new Object");

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (find.byId(this.id) == null) break;
        }

        super.save();
    }

    @JsonIgnore @Override
    public void update() {

        terminal_logger.debug("update: ID = {}",  this.id);

        super.update();
    }

    @JsonIgnore @Override
    public void delete() {

        terminal_logger.debug("delete: ID = {}", this.id);

        this.customer.delete();

        super.delete();
    }


/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void notification_error(String text){

        new Model_Notification()
                .setImportance(Enum_Notification_importance.normal)
                .setLevel(Enum_Notification_level.error)
                .setText(new Notification_Text().setText(text))
                .setButton( new Notification_Button().setAction(Enum_Notification_action.confirm_notification).setPayload("null").setColor(Becki_color.byzance_blue).setText("OK")  )
                .send(this);
    }
/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient public boolean create_permission()     {  return true;  }
    @JsonIgnore   @Transient public boolean read_permission()       {  return true;  }
    @JsonProperty @Transient public boolean edit_permission()       {  return Controller_Security.get_person() != null && (Controller_Security.get_person_id().equals(this.id) || Controller_Security.get_person().permissions_keys.containsKey("Person_edit"));}
    @JsonIgnore   @Transient public boolean activation_permission() {  return Controller_Security.get_person().permissions_keys.containsKey("Person_activation");}
    @JsonIgnore   @Transient public boolean delete_permission()     {  return Controller_Security.get_person().permissions_keys.containsKey("Person_delete");}
    @JsonIgnore   @Transient public boolean admin_permission()      {  return Controller_Security.get_person().permissions_keys.containsKey("Byzance_employee");}

    public enum permissions{ Person_edit, Person_delete, Person_activation, Byzance_employee }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE        = Model_Person.class.getSimpleName();
    public static final String CACHE_TOKEN  = Model_Person.class.getSimpleName() + "_TOKEN";

    public static  Cache<String, Model_Person> cache = null; // < Person_id, Person>
    public static  Cache<String, String> token_cache = null; // < Token_Key, Person_is>

    @JsonIgnore
    public static Model_Person get_byId(String id) {

        Model_Person person = cache.get(id);
        if (person == null){

            person = Model_Person.find.byId(id);
            if (person == null) return null;

            for(Model_Permission permission : person.person_permissions){
                person.permissions_keys.put(permission.value, true);
            }

            cache.put(id, person);
        }

        return person;
    }

    @JsonIgnore
    public static Model_Person get_byAuthToken(String authToken) {

        String person_id = token_cache.get(authToken);
        if (person_id == null){

            Model_Person person = find.where().eq("floatingPersonTokens.authToken", authToken).findUnique();
            if (person == null){
                terminal_logger.warn( "get_byAuthToken :: This object authToken:: " + authToken + " wasn't found. ");
                return null;
            }

            cache.put(person.id, person);
            token_cache.put(authToken, person.id);

            return person;

        }else {
            return get_byId(person_id);
        }
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model_Person findByEmailAddressAndPassword(String emailAddress, String password) { return find.where().eq("mail", emailAddress.toLowerCase()).eq("shaPassword", getSha512(password)).findUnique();}

    public static Model.Finder<String,Model_Person>  find = new Model.Finder<>(Model_Person.class);
}