package models.compiler;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.project.b_program.B_Pair;
import models.project.b_program.Homer_Instance;
import models.project.b_program.servers.Cloud_Homer_Server;
import models.project.b_program.servers.Private_Homer_Server;
import models.project.c_program.actualization.C_Program_Update_Plan;
import models.project.global.Project;
import utilities.swagger.outboundClass.Swagger_Board_status;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
public class Board extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    static play.Logger.ALogger logger = play.Logger.of("Loggy");

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id                                @ApiModelProperty(required = true)   public String id; // Vlastní id je přidělováno
    @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true)   public String personal_description;
                                       @JsonIgnore  @ManyToOne              public TypeOfBoard type_of_board;  // Typ desky
                                       @ApiModelProperty(required = true)   public boolean isActive;
                                               @JsonIgnore  public Date date_of_create;

                                    @JsonIgnore @ManyToOne  public Project project;

                                    @JsonIgnore @ManyToOne  public Version_Object actual_c_program_version;
                                    @JsonIgnore             public String alternative_program_name;

                                  @JsonIgnore @ManyToOne()  public Cloud_Homer_Server latest_know_server;  // Pouze pokud je připojen přímo na blocko cloud_blocko_server!
                                  @JsonIgnore @ManyToOne()  public Private_Homer_Server private_homer_servers;

    @JsonIgnore  @OneToMany(mappedBy="board",cascade=CascadeType.ALL, fetch = FetchType.EAGER)            public List<B_Pair> b_pair = new ArrayList<>();
    @JsonIgnore  @OneToMany(mappedBy="board", cascade=CascadeType.ALL, fetch = FetchType.EAGER) public List<C_Program_Update_Plan> c_program_update_plans;

                                                                 // Vlastní instance pouze pro HW - V Případě že nebude aktivní instance s Blocko Programem.
                 @JsonIgnore @OneToOne(fetch = FetchType.EAGER)  public Homer_Instance private_instance;

/* JSON PROPERTY METHOD ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty  @Transient @ApiModelProperty(required = true) public String type_of_board_id()   { return type_of_board == null ? null : type_of_board.id; }
    @JsonProperty  @Transient @ApiModelProperty(required = true) public String project_id()         { return       project == null ? null : project.id; }


    @JsonProperty  @Transient @ApiModelProperty(required = true) public Swagger_Board_status status()       {

        // Složený SQL dotaz pro nalezení funkční běžící instance (B_Pair)
        B_Pair b_pair_main = B_Pair.find.where().disjunction()
                    .add( Expr.isNotNull("padavan_board_pair.homer_instance"))
                    .add( Expr.isNotNull("yoda_board_pair.homer_instance"))
                .where().eq("board.id", id).findUnique();


        Swagger_Board_status board_status = new Swagger_Board_status();


        if(b_pair_main == null){
            board_status.where = "nowhere";
        }

        if(b_pair_main != null) {

            // Určím nadřazenou verzi (Yoda má  yoda_board_pair ostatní padavan_board_pair )
            Version_Object version_object;
            if (b_pair_main.padavan_board_pair != null) version_object = b_pair_main.padavan_board_pair;
            else version_object = b_pair_main.yoda_board_pair;

            if (version_object.homer_instance.cloud_homer_server != null) {
                board_status.where = "cloud";
            }



            if (version_object.homer_instance.private_server != null) {
                board_status.where = "local";
            }

            board_status.b_program_id = version_object.b_program.id;
            board_status.b_program_version_id = version_object.id;
        }

        if(alternative_program_name != null ) board_status.actual_program = alternative_program_name;

        if(actual_c_program_version != null){
                    board_status.actual_c_program_id = actual_c_program_version.c_program.id;
                    board_status.actual_c_program_version_id = actual_c_program_version.id;
        }


        if(!c_program_update_plans.isEmpty()){

            C_Program_Update_Plan plan = C_Program_Update_Plan.find.where().eq("board.id", id).order().asc("actualization_procedure.date_of_create").setMaxRows(1).findUnique();

            board_status.required_c_program_id = plan.c_program_version_for_update.c_program.id;
            board_status.required_c_program_version_id = plan.c_program_version_for_update.id;
         }


        return board_status;

    }



    @JsonProperty  @Transient @ApiModelProperty(required = true) public boolean up_to_date(){return (c_program_update_plans == null);}


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


/* ZVLÁŠTNÍ POMOCNÉ METODY ---------------------------------------------------------------------------------------------*/

    @Override
    public void update(){
        super.update();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Board> find = new Finder<>(Board.class);


}
