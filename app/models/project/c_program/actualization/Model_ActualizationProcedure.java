package models.project.c_program.actualization;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.project.b_program.instnace.Model_HomerInstanceRecord;
import models.project.global.Model_Project;
import utilities.hardware_updater.States.Actual_procedure_State;
import utilities.hardware_updater.States.C_ProgramUpdater_State;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of ActualizationProcedure",
        value = "ActualizationProcedure")
public class Model_ActualizationProcedure extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    static play.Logger.ALogger logger = play.Logger.of("Loggy");

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                                                                @Id public String id; // Vlastní id je přidělováno

    @ApiModelProperty(required = true, value = "Find description on Model Actual_procedure_State")  public Actual_procedure_State state;

                                                    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)  public Model_HomerInstanceRecord homer_instance_record;

    @ApiModelProperty(required = true, value = "Can be empty")
    @OneToMany(mappedBy="actualization_procedure", cascade = CascadeType.ALL)                       public List<Model_CProgramUpdatePlan> updates = new ArrayList<>();

    @ApiModelProperty(required = true, value = "UNIX time in ms",
                                                example = "1466163478925")                          public Date date_of_create;
    @ApiModelProperty(required = true, value = "can be empty, which means that the procedure is not done yet. " +
                                               "UNIX time in ms",
                                                example = "1466163478925")                          public Date date_of_finish;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true ) public Actual_procedure_State state (){
        //update_state();
        return state;
    }


    @JsonProperty @Transient @ApiModelProperty(required = true, readOnly = true) public Integer state_percentage(){ return 41; }        // TODO - připraveno pro becki - nutno dodělat v Tyrionovi http://youtrack.byzance.cz/youtrack/issue/TYRION-346
    @JsonProperty @Transient @ApiModelProperty(required = true, readOnly = true) public String  state_fraction(){ return "21/35"; }     // TODO - http://youtrack.byzance.cz/youtrack/issue/TYRION-347

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_ActualizationProcedure.find.byId(this.id) == null) break;
        }

        super.save();
    }

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

       List<Model_CProgramUpdatePlan> list = Model_CProgramUpdatePlan.find.where()
                                            .eq("actualization_procedure.id",id).where()
                                                .disjunction()
                                                    .add(Expr.eq("state",C_ProgramUpdater_State.homer_server_is_offline       ))
                                                    .add(Expr.eq("state",C_ProgramUpdater_State.instance_inaccessible))
                                                    .add(Expr.isNull("state"))
                                            .findList();

       for(Model_CProgramUpdatePlan plan : list) {
           plan.state = C_ProgramUpdater_State.canceled;
           plan.update();
       }

        state = Actual_procedure_State.canceled;
        this.update();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    class Program_Actualization{
        @ApiModelProperty(required = true, value = "Can be empty")  public String b_program_id;
        @ApiModelProperty(required = true, value = "Can be empty")  public String b_program_version_id;
        @ApiModelProperty(required = true, value = "Can be empty")  public String b_program_name;
        @ApiModelProperty(required = true, value = "Can be empty")  public String b_program_version_name;
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient   public static final String read_permission_docs   = "User can read Actualization_procedure if they have ID of Actualization_procedure";

    @JsonIgnore @Transient   public boolean read_permission()      {  return Model_Project.find.where().eq("b_program.version_objects.actualization_procedures.id",id ).findUnique().read_permission() || Controller_Security.getPerson().has_permission("Actualization_procedure_read"); }

    public enum permissions{Actualization_procedure_read}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_ActualizationProcedure> find = new Model.Finder<>(Model_ActualizationProcedure.class);

}
