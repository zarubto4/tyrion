package models.login;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) public String id;

    @Column(unique=true)  @Constraints.Email                public String mail;
                          @Constraints.Min(8)  @JsonIgnore  public String password;
    @Column(unique=true)  @Constraints.Min(5)               public String nickName;
                                                            public String firstName;
    @JsonIgnore                                             public String middleName;
                                                            public String lastNAme;
                                                            public String firstTitle;
                                                            public String lastTitle;

                                                            public Date   dateOfBirth;
                                                            private String authToken;

                                                            public boolean emailValidated;

    @Column(length = 64)                                    private byte[] shaPassword;

    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL)     public List<Project>              owningProjects            = new ArrayList<>();
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL)     public List<Post>                 postLiker                 = new ArrayList<>();    // Propojení, které byly uživatelem hodnoceny (jak negativně, tak pozitivně)
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL)     public List<GroupWithPermissions> groupWithPermissions      = new ArrayList<>();


    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL)     public List<BlockoBlock>   blocksAuthor    = new ArrayList<>(); // Propojení, které bločky uživatel vytvořil
    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL)     public List<Post>          personPosts     = new ArrayList<>(); // Propojení, které uživatel napsal
    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL)     public List<LinkedPost>    linkedPosts     = new ArrayList<>(); // Propojení, které uživatel nalinkoval
    @JsonIgnore  @OneToMany(mappedBy="person", cascade = CascadeType.ALL)     public List<LinkedAccount> linkedAccounts;

    @JsonIgnore  @ManyToMany(mappedBy = "ownersOfPermission", cascade = CascadeType.ALL)  @JoinTable(name = "permissionsPerson")  public List<PermissionKey> owningPermissions = new ArrayList<>();




//#### DB FINDER ########################################################################################################
    public static Finder<String,Person> find = new Finder<>(Person.class);
    public static Person findByEmailAddressAndPassword(String emailAddress, String password) { return find.where().eq("mail", emailAddress.toLowerCase()).eq("shaPassword", getSha512(password)).findUnique();}

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

    public void setSha(String value) {
        this.shaPassword = getSha512(value);
    }

    public void setToken(String token){
        authToken = token;
    }

    public String createToken() {

        authToken = UUID.randomUUID().toString();
        update();

        return authToken;
    }

    // If userDB/system make log out
    public void deleteAuthToken() {
        authToken = null;
        update();
    }


}
