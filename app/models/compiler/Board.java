package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.project.global.Project;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class Board extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)      public String id; // Vlastní id je přidělováno
                         @Column(columnDefinition = "TEXT")      public String personal_description;
                                                @ManyToOne       public TypeOfBoard type_of_board;  // Typ desky
                                                                 public boolean isActive;

    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL)     public List<Project> projects = new ArrayList<>();

    @ApiModelProperty(readOnly =true)
    @JsonProperty  @Transient public String type_of_board_id()   { return type_of_board.id; }


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonProperty public Boolean edit_permission()  {  return  ( Board.find.where().where().eq("projects.ownersOfProject.id", SecurityController.getPerson().id ).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("TypeOfBlock.edit");  }
    @JsonProperty public Boolean delete_permission(){  return  ( Board.find.where().where().eq("projects.ownersOfProject.id", SecurityController.getPerson().id ).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("TypeOfBlock.delete");}


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, Board> find = new Finder<>(Board.class);


}
