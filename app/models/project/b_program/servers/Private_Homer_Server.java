package models.project.b_program.servers;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import models.project.b_program.Homer_Instance;
import models.project.global.Project;

import javax.persistence.*;

@Entity
public class Private_Homer_Server extends Model {

/* DATABASE VALUES ----------------------------------------------------------------------------------------------------*/
        @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
                                                                 public String mac_address;

                                                                 public String type_of_device;
                                                                 public String  version;

    @JsonIgnore @ManyToOne                   public Project project;
    @JsonProperty @Transient                 public String project_id(){ return project == null ? null : project.id; }

    @JsonIgnore  @OneToOne @JoinColumn(name="private_server_id")  public Homer_Instance b_program_homer;

    @JsonProperty                            public boolean online()  {return false;}

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have Project.update_permission = true, you can create Private_Homer_Server on this Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "Its not allowed to create Homer by user. Homer (installed on PC or created in cloud) must build itself in DB - there are private APIs for Homer-Js! User can only connect this homer with own Project in Homer program (there is Login and buttons for connect to project";

    @JsonIgnore   @Transient public Boolean create_permission()  {  return (project != null ? project.update_permission() : false ) || SecurityController.getPerson().has_permission("Homer_create");  }
    @JsonProperty @Transient public Boolean update_permission()  {  return (project != null ? project.update_permission() : false ) || SecurityController.getPerson().has_permission("Homer_update");  }
    @JsonIgnore   @Transient public Boolean read_permission()    {  return (project != null ? project.read_permission()   : false ) || SecurityController.getPerson().has_permission("Homer_read");    }
    @JsonProperty @Transient public Boolean edit_permission()    {  return (project != null ? project.update_permission() : false ) || SecurityController.getPerson().has_permission("Homer_edit");    }
    @JsonProperty @Transient public Boolean delete_permission()  {  return (project != null ? project.update_permission() : false ) || SecurityController.getPerson().has_permission("Homer_delete");  }

    public enum permissions{Homer_create, Homer_update, Homer_read, Homer_edit, Homer_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Private_Homer_Server> find = new Finder<>(Private_Homer_Server.class);
}



