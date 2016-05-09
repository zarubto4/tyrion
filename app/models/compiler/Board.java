package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import models.project.b_program.Homer;
import models.project.global.Project;

import javax.persistence.*;


@Entity
public class Board extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)      public String id; // Vlastní id je přidělováno
                         @Column(columnDefinition = "TEXT")      public String personal_description;
                                                @ManyToOne       public TypeOfBoard type_of_board;  // Typ desky
                                                                 public boolean isActive;

    @JsonIgnore @ManyToOne                                       public Project project;
    @JsonIgnore @ManyToOne                                       public Homer homer;


    @JsonProperty  @Transient public String type_of_board_id()   { return type_of_board.id; }
    @JsonProperty  @Transient public String project_id()         { return project.id; }
    @JsonProperty  @Transient public String homer_id()           { return homer.id; }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient public Boolean edit_permission()  {  return  ( Board.find.where().where().eq("projects.ownersOfProject.id", SecurityController.getPerson().id ).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("TypeOfBlock.edit");  }
    @JsonProperty @Transient public Boolean delete_permission(){  return  ( Board.find.where().where().eq("projects.ownersOfProject.id", SecurityController.getPerson().id ).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("TypeOfBlock.delete");}


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, Board> find = new Finder<>(Board.class);


}
