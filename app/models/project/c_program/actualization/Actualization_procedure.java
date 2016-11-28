package models.project.c_program.actualization;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.project.b_program.instnace.Homer_Instance_Record;
import models.project.global.Project;
import utilities.hardware_updater.States.Actual_procedure_State;
import utilities.hardware_updater.States.C_ProgramUpdater_State;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Actualization_procedure extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id; // Vlastní id je přidělováno


    @ApiModelProperty(required = true, value = "Find description on Model Actual_procedure_State")   public Actual_procedure_State state;


    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)   public Homer_Instance_Record homer_instance_record;

    @ApiModelProperty(required = true, value = "Can be empty")  @OneToMany(mappedBy="actualization_procedure", cascade = CascadeType.ALL)   public List<C_Program_Update_Plan> updates = new ArrayList<>();

    @ApiModelProperty(required = true, value = "UNIX time in milis - Date: number of miliseconds elapsed since  Thursday, 1 January 1970",
                                                example = "1466163478925")   public Date date_of_create;
    @ApiModelProperty(required = true, value = "can be empty, which means that the procedure is not done yet. " +
                                               "UNIX time in milis - Date: number of miliseconds elapsed since  Thursday, 1 January 1970",
                                                example = "1466163478925")   public Date date_of_finish;


/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/



    @JsonProperty @Transient @ApiModelProperty(required = true ) public Actual_procedure_State state (){
        //update_state();
        return state;
    }


    @JsonProperty @Transient @ApiModelProperty(required = true, readOnly = true) public Integer state_percentage(){ return 41; }        // TODO - připraveno pro becki - nutno dodělat v Tyrionovi http://youtrack.byzance.cz/youtrack/issue/TYRION-346
    @JsonProperty @Transient @ApiModelProperty(required = true, readOnly = true) public String  state_fraction(){ return "21/35"; }     // TODO - http://youtrack.byzance.cz/youtrack/issue/TYRION-347



/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    /**
    @JsonIgnore @Transient
    public void update_state(){

        System.out.println("Actualization procedure - update state");

        this.refresh();

        // Metoda je vyvolána, pokud chceme synchronizovat Aktualizační proceduru a nějakým způsobem jí označit
        // Třeba kolik procent už je vykonáno

        int all = C_Program_Update_Plan.find.where()
                .eq("actualization_procedure.id",id)
                .findRowCount();

        int complete = C_Program_Update_Plan.find.where()
                .eq("actualization_procedure.id",id).where()
                .eq("state",C_ProgramUpdater_State.complete)
                .findRowCount();

        if( ( ( complete ) * 1.0 / all ) == 1.0 ){
            date_of_finish = new Date();
            state = Actual_procedure_State.successful_complete;
            this.update();
            return;
        }

        int canceled = C_Program_Update_Plan.find.where()
                .eq("actualization_procedure.id",id).where()
                .eq("state",C_ProgramUpdater_State.canceled)
                .findRowCount();

        int override = C_Program_Update_Plan.find.where()
                .eq("actualization_procedure.id",id).where()
                .eq("state",C_ProgramUpdater_State.overwritten)
                .findRowCount();

        if( ( ( complete + canceled + override ) * 1.0 / all ) == 1.0 ){
            date_of_finish = new Date();
            state = Actual_procedure_State.complete;
            this.update();
            return;
        }

        int critical_error = C_Program_Update_Plan.find.where()
                .eq("actualization_procedure.id",id).where()
                .eq("state",C_ProgramUpdater_State.critical_error)
                .findRowCount();

        if( ( (critical_error +override + canceled + complete ) * 1.0 / all ) == 1.0 ){
            date_of_finish = new Date();
            state = Actual_procedure_State.complete_with_error;
            this.update();
            return;
        }
    }
     */

    @JsonIgnore @Transient
    public void cancel_procedure(){

       List<C_Program_Update_Plan> list = C_Program_Update_Plan.find.where()
                                            .eq("actualization_procedure.id",id).where()
                                                .disjunction()
                                                    .add(Expr.eq("state",C_ProgramUpdater_State.homer_server_is_offline       ))
                                                    .add(Expr.eq("state",C_ProgramUpdater_State.instance_inaccessible))
                                                    .add(Expr.isNull("state"))
                                            .findList();

       for(C_Program_Update_Plan plan : list) {
           plan.state = C_ProgramUpdater_State.canceled;
           plan.update();
       }

        state = Actual_procedure_State.canceled;
        this.update();
    }





/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient   public static final String read_permission_docs   = "User can read Actualization_procedure if they have ID of Actualization_procedure";

    @JsonIgnore @Transient   public boolean read_permission()      {  return Project.find.where().eq("b_program.version_objects.actualization_procedures.id",id ).findUnique().read_permission() || SecurityController.getPerson().has_permission("Actualization_procedure_read"); }

    public enum permissions{Actualization_procedure_read}



/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Actualization_procedure> find = new Model.Finder<>(Actualization_procedure.class);


/* POMOCNÉ TŘÍDY -------------------------------------------------------------------------------------------------------*/

    class Program_Actualization{
        @ApiModelProperty(required = true, value = "Can be empty")  public String b_program_id;
        @ApiModelProperty(required = true, value = "Can be empty")  public String b_program_version_id;
        @ApiModelProperty(required = true, value = "Can be empty")  public String b_program_name;
        @ApiModelProperty(required = true, value = "Can be empty")  public String b_program_version_name;
    }

}
