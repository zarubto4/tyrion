package models.person;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.blocko.BlockoBlock;
import models.compiler.FileRecord;
import models.compiler.Version_Object;
import models.grid.GridWidget;
import models.notification.Notification;
import models.overflow.LinkedPost;
import models.overflow.Post;
import models.project.global.Project;
import models.project.global.financial.Payment_Details;
import org.hibernate.validator.constraints.Email;
import play.data.validation.Constraints;
import utilities.Server;
import utilities.permission.Permission;

import javax.persistence.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Person extends Model {

    /* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @ApiModelProperty(required = true)                          public String id;

    @ApiModelProperty(required = true) @Column(unique=true) @Email @Constraints.Email public String mail;
    @ApiModelProperty(required = true) @Column(unique=true)     public String nick_name;
    @ApiModelProperty(required = true)                          public String full_name;

                                                    @JsonIgnore public String azure_picture_link;
                                        @JsonIgnore @OneToOne   public FileRecord picture;

                                                 @JsonIgnore    public boolean freeze_account; // Zmražený účet - Účty totiž nechceme mazat!
                                                 @JsonIgnore    public boolean mailValidated;

    @JsonIgnore  @Column(length = 64)                           public byte[] shaPassword;
    @JsonIgnore  @OneToOne(mappedBy = "person")                 public PasswordRecoveryToken passwordRecoveryToken;

    @JsonIgnore  @OneToMany(mappedBy="person", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Payment_Details>  payment_details = new ArrayList<>();

    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Project>              owningProjects            = new ArrayList<>();
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Post>                 postLiker                 = new ArrayList<>();    // Propojení, které byly uživatelem hodnoceny (jak negativně, tak pozitivně)
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<SecurityRole>         roles                     = new ArrayList<>();
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<PersonPermission>     person_permissions        = new ArrayList<>();


    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<BlockoBlock>          blocksAuthor         = new ArrayList<>(); // Propojení, které bločky uživatel vytvořil
    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<GridWidget>           widgetsAuthor        = new ArrayList<>(); // Propojení, které widgety uživatel vytvořil
    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Version_Object>       version_objects      = new ArrayList<>(); // Propojení, které verze uživatel vytvořil
    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Post>                 personPosts          = new ArrayList<>(); // Propojení, které uživatel napsal
    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<LinkedPost>           linkedPosts          = new ArrayList<>(); // Propojení, které uživatel nalinkoval
    @JsonIgnore  @OneToMany(mappedBy="person", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<FloatingPersonToken>  floatingPersonTokens = new ArrayList<>(); // Propojení, které uživatel napsal
    @JsonIgnore  @OneToMany(mappedBy="owner",  cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Invitation>           invitations          = new ArrayList<>(); // Pozvánky, které uživatel rozeslal
    @JsonIgnore  @OneToMany(mappedBy="person", cascade = CascadeType.ALL, fetch = FetchType.LAZY)     public List<Notification>         notifications        = new ArrayList<>();


/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true)
    public String picture_link(){
        if(this.azure_picture_link == null){
            return null;
        }
        return Server.azureLink + azure_picture_link;
    }


/* Security Tools @ JsonIgnore -----------------------------------------------------------------------------------------*/

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
        return Permission.check_permission(permission);
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

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient public boolean create_permission(){  return true;  }
    @JsonIgnore   @Transient public boolean read_permission()  {  return true;  }
    @JsonProperty @Transient public boolean edit_permission()  {  return SecurityController.getPerson() != null && SecurityController.getPerson().id.equals(this.id) || SecurityController.getPerson().has_permission("Person_edit");}
    @JsonIgnore   @Transient public boolean activation_permission() {  return SecurityController.getPerson().has_permission("Person_activation");}
    @JsonIgnore   @Transient public boolean delete_permission()     {  return SecurityController.getPerson().has_permission("Person_delete");}

    public enum permissions{ Person_edit, Person_delete, Person_activation }



/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Person findByEmailAddressAndPassword(String emailAddress, String password) { return find.where().eq("mail", emailAddress.toLowerCase()).eq("shaPassword", getSha512(password)).findUnique();}

    public static Person findByAuthToken(String authToken) {
        if (authToken == null) { return null; }
        try  {
            return find.where().eq("floatingPersonTokens.authToken", authToken).findUnique(); }
        catch (Exception e) {
           e.printStackTrace();
           return null;
        }
    }

      public static Model.Finder<String,Person>  find = new Model.Finder<>(Person.class);
}
