package models.person;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.blocko.BlockoBlock;
import models.notification.Notification;
import models.overflow.LinkedPost;
import models.overflow.Post;
import models.project.global.Project;
import models.project.global.financial.Payment_Details;
import models.project.m_program.M_Project;
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

    @ApiModelProperty(required = true) @Column(unique=true)     public String mail;
    @ApiModelProperty(required = true) @Column(unique=true)     public String nick_name;
    @ApiModelProperty(required = true)                          public String full_name;


                                                @JsonIgnore     public boolean mailValidated;

    @JsonIgnore  @Column(length = 64)                           public byte[] shaPassword;
    @JsonIgnore  @OneToOne(mappedBy = "person")                 public PasswordRecoveryToken passwordRecoveryToken;

    @JsonIgnore  @OneToMany(mappedBy="person", cascade = CascadeType.ALL) public List<Payment_Details>     payment_details = new ArrayList<>();

    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL)     public List<Project>              owningProjects            = new ArrayList<>();
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL)     public List<Post>                 postLiker                 = new ArrayList<>();    // Propojení, které byly uživatelem hodnoceny (jak negativně, tak pozitivně)
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL)     public List<SecurityRole>         roles                     = new ArrayList<>();
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL)     public List<PersonPermission>     person_permissions = new ArrayList<>();


    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL)     public List<BlockoBlock>          blocksAuthor         = new ArrayList<>(); // Propojení, které bločky uživatel vytvořil
    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL)     public List<Post>                 personPosts          = new ArrayList<>(); // Propojení, které uživatel napsal
    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL)     public List<LinkedPost>           linkedPosts          = new ArrayList<>(); // Propojení, které uživatel nalinkoval
    @JsonIgnore  @OneToMany(mappedBy="person", cascade = CascadeType.ALL)     public List<FloatingPersonToken>  floatingPersonTokens = new ArrayList<>(); // Propojení, které uživatel napsal
    @JsonIgnore  @OneToMany(mappedBy="owner",  cascade = CascadeType.ALL)     public List<Invitation>           invitations          = new ArrayList<>(); // Pozvánky, které uživatel rozeslal
    @JsonIgnore  @OneToMany(mappedBy="person", cascade = CascadeType.ALL)     public List<Notification>         notifications        = new ArrayList<>();


   // @JsonIgnore @OneToMany(mappedBy="product", cascade = CascadeType.ALL) public List<Product> products  = new ArrayList<>();


/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/



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
    public void setToken(String token, String user_agent){
        FloatingPersonToken floatingPersonToken = new FloatingPersonToken();
        floatingPersonToken.set_basic_values(token);
        floatingPersonToken.person = this;
        floatingPersonToken.user_agent = user_agent;
        floatingPersonToken.save();
        this.floatingPersonTokens.add(floatingPersonToken);
    }

    @JsonIgnore @Transient
    public boolean has_permission(String permission){
        return Permission.check_permission(permission);
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient public boolean create_permission(){  return true;  }
    @JsonIgnore   @Transient public boolean read_permission()  {  return true;  }
    @JsonProperty @Transient public boolean edit_permission()  {   return SecurityController.getPerson() != null && ((M_Project.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Person_edit"));}
    @JsonProperty @Transient public boolean delete_permission(){return SecurityController.getPerson() != null && ((M_Project.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Person_delete"));}

    public enum permissions{ Person_edit, Person_delete }



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
