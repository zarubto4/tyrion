package models.permission;


import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import models.login.Person;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class PermissionKey extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)         public String id;
    public String key;
    public String comment; // Doplňující a vysvětlující text

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL)  public List<GroupWithPermissions> groupWithPermissions = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL)  public List<Person> ownersOfPermission = new ArrayList<>();

 //#### DB FINDER ########################################################################################################
    public static Finder<String, PermissionKey> find = new Finder<>(PermissionKey.class);

    public PermissionKey(String key, String comment) {
        this.key = key;
        this.comment = comment;
        this.save();
    }

}
