package models.project.b_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import controllers.WebSocketController_Incoming;
import models.compiler.Board;
import models.project.global.Project;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class Homer extends Model {

/* DATABASE VALUES ----------------------------------------------------------------------------------------------------*/
        @Id         public String id;
                    public String type_of_device;
                    public String  version;

    @JsonIgnore @ManyToOne                   public Project project;
    @JsonProperty @Transient                 public String project_id(){ return project == null ? null : project.id; }

    @JsonIgnore @OneToMany(mappedBy="homer", cascade = CascadeType.ALL) public List<Board>  boards  = new ArrayList<>();
    @JsonProperty  @Transient                                           public List<String> boards_id()   { return boards.stream().map(m -> m.id).collect(Collectors.toList());}


    @JsonIgnore  @OneToOne(mappedBy="homer") public B_Program_Homer b_program_homer;
    @JsonProperty                            public boolean online()  {return WebSocketController_Incoming.homer_online_state(id);}

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have Project.update_permission = true, you can create M_project on this Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "Its not allowed to create Homer by user. Homer (installed on PC or created in cloud) must build itself in DB - there are private APIs for Homer-Js! User can only connect this homer with own Project";

    @JsonIgnore   @Transient public Boolean create_permission()  {  return SecurityController.getPerson().has_permission("Homer_create");  }
    @JsonProperty @Transient public Boolean update_permission()  {  return ( Homer.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Homer_update");  }
    @JsonIgnore   @Transient public Boolean read_permission()    {  return ( Homer.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Homer_read");    }
    @JsonProperty @Transient public Boolean edit_permission()    {  return ( Homer.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Homer_edit");    }
    @JsonProperty @Transient public Boolean delete_permission()  {  return SecurityController.getPerson().has_permission("Homer_delete");  }

    public enum permissions{Homer_create, Homer_update, Homer_read, Homer_edit, Homer_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Homer> find = new Finder<>(Homer.class);
}



