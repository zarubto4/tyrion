package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers.SecurityController;
import models.blocko.BlockoBlock;
import models.blocko.Project;
import models.overflow.LinkedPost;
import models.overflow.Post;
import models.permission.GroupWithPermissions;
import models.permission.PermissionKey;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
public class Person extends Model{

    //#### DB VALUE ########################################################################################################
    @Id  @Column(unique=true)  @Constraints.Email           public String mail;
    @Constraints.Required @Constraints.Min(8)  @JsonIgnore  public String password;
                                                            public String firstName;
    @JsonIgnore                                             public String middleName;
                                                            public String lastNAme;
                                                            public String firstTitle;
                                                            public String lastTitle;

                                                            public Date   dateOfBirth;
                                                            private String authToken;
    @Column(length = 64, nullable = false)                  private byte[] shaPassword;

    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL)     public List<Project>              owningProjects            = new ArrayList<>();
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL)     public List<Post>                 postLiker                 = new ArrayList<>();    // Propojení, které byly uživatelem hodnoceny (jak negativně, tak pozitivně)
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL)     public List<GroupWithPermissions> groupWithPermissions      = new ArrayList<>();

    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL)     public List<BlockoBlock>  blocksAuthor    = new ArrayList<>(); // Propojení, které bločky uživatel vytvořil
    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL)     public List<Post>         personPosts     = new ArrayList<>(); // Propojení, které uživatel napsal
    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL)     public List<LinkedPost>   linkedPosts     = new ArrayList<>(); // Propojení, které uživatel nalinkoval

    @JsonIgnore  @ManyToMany(mappedBy = "ownersOfPermission", cascade = CascadeType.ALL)  @JoinTable(name = "permissionsPerson")  public List<PermissionKey> owningPermissions = new ArrayList<>();




//#### DB FINDER ########################################################################################################
    public static Finder<String,Person> find = new Finder<>(Person.class);

    public static Person findByEmailAddressAndPassword(String emailAddress, String password) { return find.where().eq("mail", emailAddress.toLowerCase()).eq("shaPassword", getSha512(password)).findUnique();}

//#### PERMISSIONS #####################################################################################################

    public static boolean containsPermission(String id, String key){
        return (Person.find.where() .eq("personID", id)
                .in("permission", PermissionKey.find.where().eq("key", key).findUnique())
                .findList().size() > 0);
    }
    public static boolean containsPermission(String key){
        return (Person.find.where() .eq("mail", SecurityController.getPerson().mail)
                .in("permission", PermissionKey.find.where().eq("key", key).findUnique())
                .findList().size() > 0);

    }

//#### SECURITY LOGIN ##################################################################################################
    public static Person findByAuthToken(String authToken) {
        if (authToken == null) { return null; }
        try  { return find.where().eq("authToken", authToken).findUnique(); }
        catch (Exception e) { return null; }
    }


    public static byte[] getSha512(String value) {
        try {
            return MessageDigest.getInstance("SHA-512").digest(value.getBytes("UTF-8"));
        }
        catch (Exception e) {throw new RuntimeException(e); }
    }

    public void setSha() {
        this.shaPassword = getSha512(password);
    }

    public String createToken() {

        authToken = UUID.randomUUID().toString();
        save();

        return authToken;
    }

    // If user/system make log out
    public void deleteAuthToken() {
        authToken = null;
        save();
    }


}
