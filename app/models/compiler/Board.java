package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import models.project.b_program.B_Pair;
import models.project.b_program.Homer;
import models.project.global.Project;

import javax.persistence.*;


@Entity
public class Board extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id                                                          public String id; // Vlastní id je přidělováno
                         @Column(columnDefinition = "TEXT")      public String personal_description;
                                    @JsonIgnore  @ManyToOne      public TypeOfBoard type_of_board;  // Typ desky
                                                                 public boolean isActive;

                                    @JsonIgnore @ManyToOne       public Project project;
                                    @JsonIgnore @ManyToOne       public Homer homer;

        @JsonIgnore @OneToOne(mappedBy="board",cascade=CascadeType.ALL)      public B_Pair b_pair;


    @JsonProperty  @Transient public String type_of_board_id()   { return type_of_board == null ? null : type_of_board.id; }
    @JsonProperty  @Transient public String project_id()         { return       project == null ? null : project.id; }
    @JsonProperty  @Transient public String homer_id()           { return         homer == null ? null : homer.id; }


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public Boolean create_permission(){  return  ( Board.find.where().where().eq("project.ownersOfProject.id", SecurityController.getPerson().id ).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Board_Create"); }
    @JsonIgnore @Transient public Boolean edit_permission()  {  return  ( Board.find.where().where().eq("project.ownersOfProject.id", SecurityController.getPerson().id ).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Board_edit"); }
    @JsonIgnore @Transient public Boolean delete_permission(){  return  ( Board.find.where().where().eq("project.ownersOfProject.id", SecurityController.getPerson().id ).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Board_delete");}

    public enum permissions{Board_edit, Board_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, Board> find = new Finder<>(Board.class);


}
