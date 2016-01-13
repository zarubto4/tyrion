package models.permission;

import com.avaje.ebean.Model;
import models.Person;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
@Entity
public class GroupWithPermissions extends Model  {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) public String groupID;
    @Column(unique = true)                                  public String groupName;
                                                            public String description;

    @ManyToMany(mappedBy = "groupWithPermissions", cascade = CascadeType.ALL) @JoinTable(name = "permissionGroupMembers")     public List<Person> members = new ArrayList<>();
    @ManyToMany(mappedBy = "groupWithPermissions", cascade = CascadeType.ALL) @JoinTable(name = "permissionGroupPermissions") public List<PermissionKey> permission = new ArrayList<>();


 //#### DB FINDER ########################################################################################################
    public static Finder<String, GroupWithPermissions> find = new Finder<>(GroupWithPermissions.class);

}
