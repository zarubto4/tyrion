package models;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Update_group_procedure_state;
import utilities.enums.Enum_CProgram_updater_state;
import utilities.enums.Enum_Update_type_of_update;

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

    @ApiModelProperty(required = true, value = "Find description on Model Actual_procedure_State")  public Enum_Update_group_procedure_state state;

                                                    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)  public Model_HomerInstanceRecord homer_instance_record;

    @ApiModelProperty(required = true, value = "Can be empty")
    @OneToMany(mappedBy="actualization_procedure", cascade = CascadeType.ALL) @OrderBy("date_of_create DESC") public List<Model_CProgramUpdatePlan> updates = new ArrayList<>();

    @ApiModelProperty(required = true, value = "UNIX time in ms")  public Date date_of_create;
    @ApiModelProperty(required = true, value = "UNIX time in ms")  public Date date_of_planing;
    @ApiModelProperty(required = true, value = "UNIX time in ms")  public Date date_of_finish;

    @Enumerated(EnumType.STRING)  @ApiModelProperty(required = true)  public Enum_Update_type_of_update type_of_update;   // Typ updatu pro případné rozhodování úrovně notifikací směrem k uživatelovi

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true ) public Enum_Update_group_procedure_state state (){
        return state;
    }

    @JsonProperty @Transient @ApiModelProperty(required = true, readOnly = true) public String  state_fraction(){

        int all = Model_CProgramUpdatePlan.find.where()
                .eq("actualization_procedure.id",id)
                .findRowCount();

        int complete = Model_CProgramUpdatePlan.find.where()
                .eq("actualization_procedure.id",id).where()
                .eq("state", Enum_CProgram_updater_state.complete)
                .findRowCount();

        return all + "/" + complete;

    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        date_of_create = new Date();

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_ActualizationProcedure.find.byId(this.id) == null) break;
        }

        super.save();
    }


    @JsonIgnore @Transient
    public void update_state(){

        System.out.println("Actualization procedure - update state");

        // Metoda je vyvolána, pokud chceme synchronizovat Aktualizační proceduru a nějakým způsobem jí označit
        // Třeba kolik procent už je vykonáno

        int all = Model_CProgramUpdatePlan.find.where()
                .eq("actualization_procedure.id",id)
                .findRowCount();

        int complete = Model_CProgramUpdatePlan.find.where()
                .eq("actualization_procedure.id",id).where()
                .eq("state", Enum_CProgram_updater_state.complete)
                .findRowCount();

        if( ( ( complete ) * 1.0 / all ) == 1.0 ){
            date_of_finish = new Date();
            state = Enum_Update_group_procedure_state.successful_complete;
            this.update();
            return;
        }

        int canceled = Model_CProgramUpdatePlan.find.where()
                .eq("actualization_procedure.id",id).where()
                .eq("state", Enum_CProgram_updater_state.canceled)
                .findRowCount();


        int override = Model_CProgramUpdatePlan.find.where()
                .eq("actualization_procedure.id",id).where()
                .eq("state", Enum_CProgram_updater_state.overwritten)
                .findRowCount();

        if( ( ( complete + canceled + override) * 1.0 / all ) == 1.0 ){
            date_of_finish = new Date();
            state = Enum_Update_group_procedure_state.complete;
            this.update();
            return;
        }

        int in_progress = Model_CProgramUpdatePlan.find.where()
                .eq("actualization_procedure.id",id).where()
                .eq("state", Enum_CProgram_updater_state.in_progress)
                .findRowCount();

        if(in_progress != 0){
            state = Enum_Update_group_procedure_state.in_progress;
            this.update();
        }

        int critical_error = Model_CProgramUpdatePlan.find.where()
                .eq("actualization_procedure.id",id).where()
                .eq("state", Enum_CProgram_updater_state.critical_error)
                .findRowCount();

        int not_updated = Model_CProgramUpdatePlan.find.where()
                .eq("actualization_procedure.id",id).where()
                .eq("state", Enum_CProgram_updater_state.not_updated)
                .findRowCount();

        if( ( (critical_error + override + canceled + complete  + not_updated) * 1.0 / all ) == 1.0 ){
            date_of_finish = new Date();
            state = Enum_Update_group_procedure_state.complete_with_error;
            this.update();
            return;
        }
    }


    @JsonIgnore @Transient
    public void cancel_procedure(){

       List<Model_CProgramUpdatePlan> list = Model_CProgramUpdatePlan.find.where()
                                            .eq("actualization_procedure.id",id).where()
                                                .disjunction()
                                                    .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_is_offline       ))
                                                    .add(Expr.eq("state", Enum_CProgram_updater_state.instance_inaccessible))
                                                    .add(Expr.isNull("state"))
                                            .findList();

       for(Model_CProgramUpdatePlan plan : list) {
           plan.state = Enum_CProgram_updater_state.canceled;
           plan.update();
       }

        state = Enum_Update_group_procedure_state.canceled;
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

    @JsonIgnore @Transient   public boolean read_permission()      {  return Model_Project.find.where().eq("b_programs.instance.instance_history.procedures.id",id ).findUnique().read_permission() || Controller_Security.getPerson().has_permission("Actualization_procedure_read"); }

    public enum permissions{Actualization_procedure_read}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_ActualizationProcedure> find = new Model.Finder<>(Model_ActualizationProcedure.class);

}
