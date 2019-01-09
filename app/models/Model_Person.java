package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import controllers._BaseController;
import exceptions.NotFoundException;
import io.intercom.api.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import org.hibernate.validator.constraints.Email;
import org.mindrot.jbcrypt.BCrypt;
import utilities.Server;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.EntityType;
import utilities.enums.NotificationAction;
import utilities.enums.NotificationImportance;
import utilities.enums.NotificationLevel;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.notifications.helps_objects.Becki_color;
import utilities.notifications.helps_objects.Notification_Button;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.permission.Action;
import utilities.permission.JsonPermission;
import utilities.permission.Permissible;
import utilities.permission.WithPermission;

import javax.persistence.*;
import java.util.*;

@Entity
@ApiModel("Person")
@Table(name = "Person")
public class Model_Person extends BaseModel implements Permissible {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Person.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Column(unique=true) @Email
    public String email;

    @Column(unique=true)
    public String nick_name;

    public String first_name;
    public String last_name;
    public String country;
    public String gender;

    @JsonIgnore public String portal_config; // Prostor kde si vývojáři Becki mohou poznamenávat nějaké detaily o uživately

    @JsonIgnore public boolean validated;
    @JsonIgnore public boolean frozen;
    @JsonIgnore public String password;
    @JsonIgnore public String alternative_picture_link;   // alternativa k prolinkování obrázku - není na azure!

    @JsonIgnore public String facebook_oauth_id;
    @JsonIgnore public String github_oauth_id;

    // ##### RELATIONS #####

    @JsonIgnore @OneToOne   public Model_Blob picture;

    @JsonIgnore @OneToOne(mappedBy = "person") public Model_PasswordRecoveryToken passwordRecoveryToken;
    @JsonIgnore @OneToOne(mappedBy = "person") public Model_ChangePropertyToken   changePropertyToken;

    @JsonIgnore @ManyToMany(mappedBy = "persons", fetch = FetchType.LAZY) public List<Model_Project> projects = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "persons", fetch = FetchType.LAZY) public List<Model_Role> roles = new ArrayList<>();
    @JsonIgnore @ManyToMany(fetch = FetchType.LAZY) public List<Model_Permission> permissions = new ArrayList<>();

    @JsonIgnore @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_Employee>              employees            = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_AuthorizationToken>    authorization_tokens = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy = "owner",  cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_Invitation>            invitations          = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_Notification>          notifications        = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_GridTerminal>          grid_terminals       = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public String cache_picture_link;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true)
    public String picture_link() {
        try {
                if (this.alternative_picture_link != null && alternative_picture_link.contains("http")) {
                cache_picture_link = alternative_picture_link;  // Its probably link from GitHub or profile picture from facebook
            }
            else if (picture != null) {
                cache_picture_link = picture.link;
            }
            return cache_picture_link;

        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty()
    @WithPermission(Action.ACTIVATE)
    public boolean byzance_admin() {
        return true;
    }

/* JSON IGNORE VALUES --------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public List<Model_Project> get_user_access_projects() {


        // Chache Add Projects
        if (idCache().gets(Model_Project.class) == null) {
            // Získání seznamu
            idCache().add(Model_Project.class, Model_Project.find.query().where().eq("persons.id", id).select("id").findSingleAttributeList());
        }

        List<Model_Project> projects = new ArrayList<>();

        for (UUID project_id : idCache().gets(Model_Project.class) ) {
            try {
                Model_Project project = Model_Project.find.byId(project_id);
                projects.add(project);
            } catch (NotFoundException e){
                // Nothing
            }
        }

        return projects;
    }

    public String full_name() {
        return this.first_name + " " + this.last_name;
    }

/* Security Tools @ JSON IGNORE -----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void setPassword(String password) {
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    @JsonIgnore
    public boolean checkPassword(String password) {
        return BCrypt.checkpw(password, this.password);
    }

    @JsonIgnore @Transient
    public String get_path() {
        return "pictures_persons" + "/" + this.id;
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    @Override
    public void save() {


        super.save();

        try {
            // Create
            io.intercom.api.User user = new io.intercom.api.User()
                    .setEmail(email)
                    .setName(first_name + " " + last_name)
                    .addCustomAttribute(io.intercom.api.CustomAttribute.newStringAttribute("alias", nick_name))
                    // .addCustomAttribute(io.intercom.api.CustomAttribute.newBooleanAttribute("browncoat", true))
                    .setUserId(id.toString());
            User.create(user);
        } catch (Exception e) {

        }
    }

    @Override
    public void update() {

        try {

            User user = User.find(this.id.toString());
            user.setName( first_name + " " + last_name)
                    .addCustomAttribute(io.intercom.api.CustomAttribute.newStringAttribute("alias", nick_name));
            User.update(user);

        } catch (io.intercom.api.NotFoundException e) {

            io.intercom.api.User user = new io.intercom.api.User()
                    .setEmail(email)
                    .setName(first_name + " " + last_name)
                    .addCustomAttribute(io.intercom.api.CustomAttribute.newStringAttribute("alias", nick_name))
                    // .addCustomAttribute(io.intercom.api.CustomAttribute.newBooleanAttribute("browncoat", true))
                    .setUserId(id.toString());
            User.create(user);

        }

        super.update();
    }

    @Override
    public boolean delete() {
        super.delete();
        return true;
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @Transient @JsonIgnore
    public void notification_error(String text) {

        new Model_Notification()
                .setImportance(NotificationImportance.NORMAL)
                .setLevel(NotificationLevel.ERROR)
                .setText(new Notification_Text().setText(text))
                .setButton( new Notification_Button().setAction(NotificationAction.CONFIRM_NOTIFICATION).setPayload("null").setColor(Becki_color.byzance_blue).setText("OK")  )
                .send(this);
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.PERSON;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE, Action.ACTIVATE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @InjectCache(value = UUID.class, duration = 3600, maxElements = 200, name = "Model_Person_Token")
    public static Cache<UUID, UUID> token_cache;

    public static Model_Person getByEmail(String email) {
        return find.query().where().eq("email", email).findOne();
    }

    public static Model_Person getByAuthToken(UUID token) throws NotFoundException {

        UUID id = token_cache.get(token);
        if (id == null) {

            Model_Person person = find.query().where().eq("authorization_tokens.token", token).findOne();

            // cache.put(person.id, person);
            token_cache.put(token, person.id);

            return person;

        } else {
            return find.byId(id);
        }
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_Person.class)
    public static CacheFinder<Model_Person> find = new CacheFinder<>(Model_Person.class);
}