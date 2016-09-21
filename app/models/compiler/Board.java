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

                                                                            public String ethernet_mac_address;
                                                                            public String wifi_mac_address;

    @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true)   public String personal_description;
                                       @JsonIgnore  @ManyToOne              public TypeOfBoard type_of_board;  // Typ desky
                                       @ApiModelProperty(required = true)   public boolean is_active;
                                                            @JsonIgnore     public Date date_of_create;

                                                   @JsonIgnore @ManyToOne   public Project project;

                                                   @JsonIgnore @ManyToOne   public Version_Object actual_c_program_version;
                                                   @JsonIgnore              public String alternative_program_name;

                                                @JsonIgnore @ManyToOne()    public Cloud_Homer_Server latest_know_server;  // Pouze pokud je připojen přímo na blocko cloud_blocko_server!
                                                @JsonIgnore @ManyToOne()    public Private_Homer_Server private_homer_servers;



    @JsonIgnore  @OneToMany(mappedBy="board", cascade=CascadeType.ALL, fetch = FetchType.EAGER)     public List<B_Pair> b_pair = new ArrayList<>();
    @JsonIgnore  @OneToMany(mappedBy="board", cascade=CascadeType.ALL, fetch = FetchType.EAGER)     public List<C_Program_Update_Plan> c_program_update_plans;


                 @JsonIgnore @OneToOne(fetch = FetchType.EAGER)  public Homer_Instance private_instance;      // Vlastní instance pouze pro HW - V Případě že nebude aktivní instance s Blocko Programem.

/* JSON PROPERTY METHOD ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty  @Transient @ApiModelProperty(required = true) public String type_of_board_id()   { return type_of_board.id; }
    @JsonProperty  @Transient @ApiModelProperty(required = true) public String type_of_board_name() { return type_of_board.name; }

    @JsonProperty  @Transient @ApiModelProperty(required = true) public boolean main_board()        { return type_of_board.connectible_to_internet; }

    @JsonProperty  @Transient @ApiModelProperty(required = true) public String project_id()         { return       project == null ? null : project.id; }
    @JsonProperty  @Transient @ApiModelProperty(required = true) public String project_name()       { return       project == null ? null : project.project_name; }

    @JsonProperty  @Transient @ApiModelProperty(required = true) public Swagger_Board_status status()       {

        // Složený SQL dotaz pro nalezení funkční běžící instance (B_Pair)

        Homer_Instance instance =  Homer_Instance.find.where().disjunction()
                .add( Expr.eq("version_object.b_program_hw_groups.main_board_pair.board.id", id) )
                .add( Expr.eq("version_object.b_program_hw_groups.device_board_pairs.board.id", id) )
                .findUnique();


        Swagger_Board_status board_status = new Swagger_Board_status();


        if(instance == null){

            board_status.where = "nowhere";

        }else  {

            if (instance.cloud_homer_server != null) {
                board_status.where = "cloud";
            }

            if (instance.private_server  != null) {
                board_status.where = "local";
            }

            board_status.b_program_id = instance.version_object.b_program.id;
            board_status.b_program_name = instance.version_object.b_program.name;

            board_status.b_program_version_id = instance.version_object.id;
            board_status.b_program_version_name = instance.version_object.version_name;
        }

        if(alternative_program_name != null ) board_status.actual_program = alternative_program_name;

        if(actual_c_program_version != null){
                    board_status.actual_c_program_id = actual_c_program_version.c_program.id;
                    board_status.actual_c_program_name = actual_c_program_version.c_program.program_name;
                    board_status.actual_c_program_version_id = actual_c_program_version.id;
                    board_status.actual_c_program_version_name = actual_c_program_version.version_name;
        }


        if(!c_program_update_plans.isEmpty()){

            C_Program_Update_Plan plan = C_Program_Update_Plan.find.where().eq("board.id", id).order().asc("actualization_procedure.date_of_create").setMaxRows(1).findUnique();

            board_status.required_c_program_id = plan.c_program_version_for_update.c_program.id;
            board_status.required_c_program_name = plan.c_program_version_for_update.c_program.program_name;

            board_status.required_c_program_version_id = plan.c_program_version_for_update.id;
            board_status.required_c_program_version_name = plan.c_program_version_for_update.version_name;
         }

        return board_status;

    }



    @JsonProperty  @Transient @ApiModelProperty(required = true) public boolean up_to_date(){return (c_program_update_plans == null);}


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String connection_permission_docs    = "read: If user want connect Project with board, he needs two Permission! Project.update_permission == true and also Board.first_connect_permission == true. " +
                                                                                      "- Or user need combination of static/dynamic permission key and Board.first_connect_permission == true";
    @JsonIgnore @Transient public static final String disconnection_permission_docs = "read: If user want remove Board from Project, he needs one single permission Project.update_permission, where hardware is registered. - Or user need static/dynamic permission key";

                                       @JsonIgnore   @Transient public boolean create_permission(){  return   SecurityController.getPerson().has_permission("Board_Create"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()  {  return  (project != null && project.update_permission())|| SecurityController.getPerson().has_permission("Board_edit")  ;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean read_permission()  {  return  (project != null && project.read_permission()  )|| SecurityController.getPerson().has_permission("Board_read")  ;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return  (project != null && project.update_permission())|| SecurityController.getPerson().has_permission("Board_delete");}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission(){  return  (project != null && project.update_permission())|| SecurityController.getPerson().has_permission("Board_update");}

    @ApiModelProperty(required = false, reference = "boolean", value = "It will be visible in Json object, only if value is true. This is an extraordinary value")

    @JsonProperty  @JsonInclude(JsonInclude.Include.NON_NULL) @Transient public Boolean first_connect_permission(){  return   project != null ? null : true;}


    public enum permissions {Board_read, Board_Create, Board_edit, Board_delete, Board_update}


/* ZVLÁŠTNÍ POMOCNÉ METODY ---------------------------------------------------------------------------------------------*/

    @Override
    public void update(){
        super.update();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Board> find = new Finder<>(Board.class);


}
