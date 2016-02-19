package models.persons;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.blocko.BlockoBlock;
import models.overflow.LinkedPost;
import models.overflow.Post;
import models.project.global.Project;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
public class Person extends Model implements Subject {

    //#### DB VALUE ########################################################################################################
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;

                   @Column(unique=true)  @Constraints.Email     public String mail;
                   @Column(unique=true)  @Constraints.Min(5)    public String nickName;
                                                                public String firstName;
                                                                public String middleName;
                                                                public String lastName;
                                                                public String firstTitle;
                                                                public String lastTitle;
                                                                public Date   dateOfBirth;
                                                @JsonIgnore     private String authToken;
                                                @JsonIgnore     public boolean emailValidated;
                                       @Column(length = 64)     private byte[] shaPassword;

    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL)     public List<Project>              owningProjects            = new ArrayList<>();
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL)     public List<Post>                 postLiker                 = new ArrayList<>();    // Propojení, které byly uživatelem hodnoceny (jak negativně, tak pozitivně)
   @JsonProperty @ManyToMany(cascade = CascadeType.ALL)     public List<SecurityRole>         roles                     = new ArrayList<>();
   @JsonProperty @ManyToMany(cascade = CascadeType.ALL)     public List<PersonPermission>     permissions               = new ArrayList<>();

    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL)     public List<BlockoBlock>   blocksAuthor   = new ArrayList<>(); // Propojení, které bločky uživatel vytvořil
    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL)     public List<Post>          personPosts    = new ArrayList<>(); // Propojení, které uživatel napsal
    @JsonIgnore  @OneToMany(mappedBy="author", cascade = CascadeType.ALL)     public List<LinkedPost>    linkedPosts    = new ArrayList<>(); // Propojení, které uživatel nalinkoval
    @JsonIgnore  @OneToMany(mappedBy="person", cascade = CascadeType.ALL)     public List<LinkedAccount> linkedAccounts = new ArrayList<>();


//#### DeadBolt ########################################################################################################

    @Override   @JsonIgnore
    public List<? extends Role> getRoles() { return roles; }

    @Override   @JsonIgnore
    public List<? extends Permission> getPermissions() { return permissions;}

    @Override   @JsonIgnore
    public String getIdentifier() { return nickName; }

//#### FINDER ########################################################################################################

    public static Person findByEmailAddressAndPassword(String emailAddress, String password) { return find.where().eq("mail", emailAddress.toLowerCase()).eq("shaPassword", getSha512(password)).findUnique();}

    public static Person findByAuthToken(String authToken) {
        if (authToken == null) { return null; }
        try  { return find.where().eq("authToken", authToken).findUnique(); }
        catch (Exception e) { return null; }
    }

//#### SECURITY LOGIN ##################################################################################################

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

    public String createToken() throws Exception{

        while(true){ // I need Unique Value
            authToken = UUID.randomUUID().toString();
            if (LinkedAccount.find.where().eq("authToken",authToken).findUnique() == null) break;
        }

        update();
        return authToken;
    }

    // If userDB/system make log out
    public void deleteAuthToken() {
        authToken = null;
        update();
    }

    // Finder **********************************************************************************************************
    public static Finder<String,Person> find = new Finder<>(Person.class);
}
