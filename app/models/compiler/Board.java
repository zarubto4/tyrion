package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.project.b_program.B_Pair;
import models.project.b_program.Homer;
import models.project.global.Project;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
public class Board extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    static play.Logger.ALogger logger = play.Logger.of("Loggy");

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id                                                          public String id; // Vlastní id je přidělováno
                         @Column(columnDefinition = "TEXT")      public String personal_description;
                                    @JsonIgnore  @ManyToOne      public TypeOfBoard type_of_board;  // Typ desky
                                                                 public boolean isActive;
                                               @JsonIgnore       public Date date_of_create;

                                    @JsonIgnore @ManyToOne       public Project project;
                                    @JsonIgnore @ManyToOne       public Homer homer;
                                                @ManyToOne       public Version_Object actual_c_program_version;


    @JsonIgnore  @OneToMany(mappedBy="board",cascade=CascadeType.ALL)                public List<B_Pair> b_pair = new ArrayList<>();
    @JsonIgnore  @OneToMany(mappedBy="board_for_update",cascade=CascadeType.ALL)     public List<C_Program_Update_Plan> c_program_update_plans = new ArrayList<>();
/* JSON PROPERTY METHOD ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty  @Transient public String type_of_board_id()   { return type_of_board == null ? null : type_of_board.id; }
    @JsonProperty  @Transient public String project_id()         { return       project == null ? null : project.id; }


    @JsonProperty  @Transient public Board_status status()       {

        // Složený SQL dotaz pro nalezení funkční běžící instance (B_Pair)
        B_Pair b_pairs = B_Pair.find.where().or(
                com.avaje.ebean.Expr.or(
                        com.avaje.ebean.Expr.isNotNull("b_program_version.b_program_homer"),
                        com.avaje.ebean.Expr.isNotNull("b_program_version.b_program_cloud")
                ),
                com.avaje.ebean.Expr.or(
                        com.avaje.ebean.Expr.isNotNull("version_master_board.b_program_homer"),
                        com.avaje.ebean.Expr.isNotNull("version_master_board.b_program_cloud")
                )
        ).where().eq("board.id", id).findUnique();

        if(b_pairs == null){
            Board_status board_status = new Board_status();
            board_status.where = "nowhere";
            return  board_status;
        }


            // Určím nadřazenou verzi (Yoda má  version_master_board ostatní b_program_version )
            Version_Object version_object;
            if(b_pairs.b_program_version != null) version_object = b_pairs.b_program_version;
            else version_object = b_pairs.version_master_board;

            if(version_object.b_program_cloud != null) {

                System.out.println("Jakou mám mít verzi C_Programu?");


                System.out.print("C_program verze: ");


                Board_status board_status = new Board_status();
                board_status.where = "cloud";
                board_status.b_program_id = version_object.b_program.id;
                board_status.b_program_version_id = version_object.id;
                if(actual_c_program_version != null){
                    board_status.actual_c_program_id = actual_c_program_version.c_program.id;
                    board_status.actual_c_program_version_id = actual_c_program_version.id;
                }else{
                    board_status.required_c_program_id = b_pairs.c_program_id();
                    board_status.required_c_program_version_id = b_pairs.c_program_version_id();
                }

                return board_status;
            }

            if(version_object.b_program_homer != null) {
                Board_status board_status = new Board_status();
                board_status.where = "local";
                board_status.b_program_id = version_object.b_program.id;
                board_status.b_program_version_id = version_object.id;
                return board_status;
            }


        // V případě že Deska nikde není (není Yoda ani Padavan) - dotáži se některých serverů, zda tam deska není připojená



        return null;
    }

    class Board_status{
        public String where;
        public String b_program_id;
        public String b_program_version_id;

        public String actual_c_program_id;
        public String actual_c_program_version_id;

        public String required_c_program_id;
        public String required_c_program_version_id;
    }


    @JsonProperty  @Transient public boolean up_to_date(){return  true;}


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String connection_permission_docs    = "read: If user want connect Project with board, he needs two Permission! Project.update_permission == true and also Board.first_connect_permission == true. " +
                                                                                      "- Or user need combination of static/dynamic permission key and Board.first_connect_permission == true";
    @JsonIgnore @Transient public static final String disconnection_permission_docs = "read: If user want remove Board from Project, he needs one single permission Project.update_permission, where hardware is registered. - Or user need static/dynamic permission key";

                                       @JsonIgnore   @Transient public Boolean create_permission(){  return   SecurityController.getPerson().has_permission("Board_Create"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public Boolean edit_permission()  {  return  (project != null && project.update_permission())|| SecurityController.getPerson().has_permission("Board_edit")  ;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public Boolean read_permission()  {  return  (project != null && project.read_permission()  )|| SecurityController.getPerson().has_permission("Board_read")  ;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public Boolean delete_permission(){  return  (project != null && project.update_permission())|| SecurityController.getPerson().has_permission("Board_delete");}
    @JsonProperty @Transient @ApiModelProperty(required = true) public Boolean update_permission(){  return  (project != null && project.update_permission())|| SecurityController.getPerson().has_permission("Board_update");}

    @ApiModelProperty(required = false, value = "It will be visible in Json object, only if value is true. This is an extraordinary value")
    @JsonProperty  @JsonInclude(JsonInclude.Include.NON_NULL) @Transient public Boolean first_connect_permission(){  return   project != null ? null : true;}


    public enum permissions{Board_read, Board_Create, Board_edit, Board_delete, Board_update}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, Board> find = new Finder<>(Board.class);


}
