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
    @JsonProperty                            public String project_id(){ return project.id; }


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

    @JsonProperty public Boolean read_permission()  {  return ( Project.find.where().eq("m_project.project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Homer.read"); }
    @JsonProperty public Boolean edit_permission()  {  return ( Project.find.where().eq("m_project.project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Homer.edit"); }
    @JsonProperty public Boolean delete_permisison(){  return ( Project.find.where().eq("m_project.project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Homer.delete"); }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Homer> find = new Finder<>(Homer.class);
}



