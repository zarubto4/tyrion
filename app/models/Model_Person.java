package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.overflow.LinkedPost;
import models.overflow.Post;
import org.ehcache.Cache;
import org.hibernate.validator.constraints.Email;
import play.data.validation.Constraints;
import utilities.Server;
import utilities.cache.Server_Cache;
import utilities.swagger.outboundClass.Swagger_Person_Short_Detail;

import javax.persistence.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of Person",
        value = "Person")
public class Model_Person extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

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

    @JsonIgnore  @Column(length = 64)                           public byte[] shaPassword;
    @JsonIgnore  @OneToOne(mappedBy = "person")                 public Model_PasswordRecoveryToken passwordRecoveryToken;
    @JsonIgnore  @OneToOne(mappedBy = "person")                 public Model_ChangePropertyToken changePropertyToken;

    @JsonIgnore  @OneToMany(mappedBy="person", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_PaymentDetails>     payment_details      = new ArrayList<>();
    @JsonIgnore  @OneToMany(mappedBy="person", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_ProjectParticipant> projects_participant = new ArrayList<>();

    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Post>                 postLiker                 = new ArrayList<>();    // Propojení, které byly uživatelem hodnoceny (jak negativně, tak pozitivně)
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_SecurityRole>   roles                     = new ArrayList<>();
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_Permission>     person_permissions        = new ArrayList<>();


    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_BlockoBlock>          blocksAuthor         = new ArrayList<>(); // Propojení, které bločky uživatel vytvořil
    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_BlockoBlockVersion>   blockVersionsAuthor  = new ArrayList<>(); // Propojení, které verze bločků uživatel vytvořil
    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_GridWidget>           widgetsAuthor        = new ArrayList<>(); // Propojení, které widgety uživatel vytvořil
    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_GridWidgetVersion>    widgetVersionsAuthor = new ArrayList<>(); // Propojení, které verze widgetů uživatel vytvořil
    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_VersionObject>        version_objects      = new ArrayList<>(); // Propojení, které verze uživatel vytvořil
    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Post>                       personPosts          = new ArrayList<>(); // Propojení, které uživatel napsal
    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<LinkedPost>                 linkedPosts          = new ArrayList<>(); // Propojení, které uživatel nalinkoval
    @JsonIgnore  @OneToMany(mappedBy="person", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_FloatingPersonToken>  floatingPersonTokens = new ArrayList<>(); // Propojení, které uživatel napsal
    @JsonIgnore  @OneToMany(mappedBy="owner",  cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_Invitation>           invitations          = new ArrayList<>(); // Pozvánky, které uživatel rozeslal
    @JsonIgnore  @OneToMany(mappedBy="person", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_Notification>         notifications        = new ArrayList<>();
    @JsonIgnore  @OneToMany(mappedBy="person", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Model_GridTerminal>         grid_terminals       = new ArrayList<>(); // Přihlášený websocket uživatele

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true)
    public String picture_link(){
        if(this.azure_picture_link == null){
            return null;
        }
        return Server.azureLink + azure_picture_link;
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

    @JsonIgnore @Override
    public void save() {

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_Person.find.byId(this.id) == null) break;
        }
        super.save();
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

            return  Model_Permission.find.where().eq("value", permission).eq("roles.persons.id", this.id).findRowCount() +
                    Model_Permission.find.where().eq("value", permission).eq("persons.id", this.id).findRowCount() > 0;
    }

    @JsonIgnore @Transient
    public CloudBlobContainer get_Container(){
        try {
            return Server.blobClient.getContainerReference("pictures");
        }catch (Exception e){
            e.printStackTrace();
            throw new NullPointerException();
        }
    }

    @JsonIgnore @Transient
    public String get_picture_path(){
        return  this.azure_picture_link;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient public boolean create_permission(){  return true;  }
    @JsonIgnore   @Transient public boolean read_permission()  {  return true;  }
    @JsonProperty @Transient public boolean edit_permission()  {  return Controller_Security.getPerson() != null && (Controller_Security.getPerson().id.equals(this.id) || Controller_Security.getPerson().has_permission("Person_edit"));}
    @JsonIgnore   @Transient public boolean activation_permission() {  return Controller_Security.getPerson().has_permission("Person_activation");}
    @JsonIgnore   @Transient public boolean delete_permission()     {  return Controller_Security.getPerson().has_permission("Person_delete");}
    @JsonIgnore   @Transient public boolean admin_permission()     {  return Controller_Security.getPerson().has_permission("Byzance_employee");}

    public enum permissions{ Person_edit, Person_delete, Person_activation, Byzance_employee }



/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model_Person findByEmailAddressAndPassword(String emailAddress, String password) { return find.where().eq("mail", emailAddress.toLowerCase()).eq("shaPassword", getSha512(password)).findUnique();}

    public static Model_Person findByAuthToken(String authToken) {
        if (authToken == null) return null;

        try  {
            Model_FloatingPersonToken token = Model_FloatingPersonToken.find.where().eq("authToken", authToken).findUnique();

            if (token != null && token.isValid()) return get_byAuthToken(authToken);

            return null;
        } catch (Exception e) {
           e.printStackTrace();
           return null;
        }
    }

      public static Model.Finder<String,Model_Person>  find = new Model.Finder<>(Model_Person.class);

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_Person get_byId(String id) {

        Cache<String, Model_Person> cache = Server_Cache.cacheManager.getCache("person_id", String.class, Model_Person.class);

        Model_Person person = cache.get(id);
        if (person == null){

            person = find.byId(id);
            if (person == null) return null;

            cache.put(id, person);
        }

        return person;
    }

    @JsonIgnore
    public static Model_Person get_byAuthToken(String authToken) {

        Cache<String, Model_Person> cache = Server_Cache.cacheManager.getCache("person_token", String.class, Model_Person.class);

        Model_Person person = cache.get(authToken);
        if (person == null){

            person = find.where().eq("floatingPersonTokens.authToken", authToken).findUnique();
            if (person == null) return null;

            cache.put(authToken, person);
        }

        return person;
    }
}
