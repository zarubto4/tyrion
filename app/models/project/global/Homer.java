package models.project.global;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.SecurityController;
import controllers.WebSocketController_Incoming;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Board;
import models.project.b_program.B_Program_Homer;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Entity
public class Homer extends Model {

/* DATABASE VALUES ----------------------------------------------------------------------------------------------------*/
        @Id         public String id;
                    public String type_of_device;
                    public String  version;

    @JsonIgnore @ManyToOne                   public Project project;
    @JsonProperty                            public String project_id(){ return project == null ? null : project.id; }


    @JsonIgnore  @OneToOne(mappedBy="homer") public B_Program_Homer b_program_homer;
    @JsonProperty                            public boolean online()  {return WebSocketController_Incoming.homer_is_online(id);}

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @JsonProperty
    @ApiModelProperty(required = false, value = "Only if it is online homer")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Board> active_boards(){

        if( !online() ) return null;

        try {
            System.out.println("Volám get HW");
            JsonNode result = WebSocketController_Incoming.get_all_Connected_HW_to_Homer(this);
            System.out.println("zavolal jsem ho a mám ho");

            List<Board> boards = new ArrayList<>();

            if(result.get("status").asText().equals("success")){

                Iterator<JsonNode> iterator =  result.get("hardwareId").elements();
                while (iterator.hasNext()) {
                    JsonNode hardware = iterator.next();


                    try {
                       boards.add(  Board.find.byId(hardware.asText()) );
                    } catch(Exception e){
                        // TODO asi by to chtělo zalogovat popřípadě problém spojenej s tím že příchozí název hardwaru není registrovanej
                        System.out.println("Příchozí jméno hardwaru " + hardware.asText() + " není zaregistrováno do systému!!!! ");
                    }
                }
                return boards;
            }
            return null;
        } catch (Exception e) { return null; }

    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have Project.update_permission = true, you can create M_project on this Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "Its not allowed to create Homer by user. Homer (installed on PC or created in cloud) must build itself in DB - there are private APIs for Homer-Js! User can only connect this homer with own Project";

    @JsonIgnore   public Boolean create_permission()  {  return SecurityController.getPerson().has_permission("Homer_create");  }
    @JsonProperty public Boolean update_permission()  {  return ( Homer.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Homer_update");  }
    @JsonIgnore   public Boolean read_permission()    {  return ( Homer.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Homer_read");    }
    @JsonProperty public Boolean edit_permission()    {  return ( Homer.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Homer_edit");    }
    @JsonProperty public Boolean delete_permission()  {  return SecurityController.getPerson().has_permission("Homer_delete");  }

    public enum permissions{Homer_create, Homer_update, Homer_read, Homer_edit, Homer_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Homer> find = new Finder<>(Homer.class);
}



