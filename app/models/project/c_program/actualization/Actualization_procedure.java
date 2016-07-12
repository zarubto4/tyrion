package models.project.c_program.actualization;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Version_Object;
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


             @JsonIgnore   @Enumerated(EnumType.STRING)     public Actual_procedure_State state;


                           @JsonIgnore     @ManyToOne()     public Project project;
                           @JsonIgnore     @ManyToOne()     public Version_Object b_program_version_procedure;


    @OneToMany(mappedBy="actualization_procedure", cascade = CascadeType.ALL, fetch = FetchType.LAZY)   public List<C_Program_Update_Plan> updates = new ArrayList<>();


    @ApiModelProperty(required = true, value = "Format:")                                                                         public Date date_of_create;
    @ApiModelProperty(required = true, value = "can be empty, which means that the procedure is not done yet" +
                                               "Format: ")  public Date date_of_finish;


/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true ) public Program_Actualization b_program_actualization(){

        if(b_program_version_procedure != null ) {
            Program_Actualization program_actualization = new  Program_Actualization();
            program_actualization.b_program_id = b_program_version_procedure.b_program.id;
            program_actualization.b_program_name = b_program_version_procedure.b_program.name;
            program_actualization.b_program_version_id = b_program_version_procedure.id;
            program_actualization.b_program_version_name = b_program_version_procedure.version_name;

            return program_actualization;
        }

        return null;
    }

    @JsonProperty @Transient @ApiModelProperty(required = true ) public Actual_procedure_State state (){
        update_state();
        return state;
    }



/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

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
    @JsonIgnore @Transient   public Boolean read_permission()      {  return project.read_permission() || SecurityController.getPerson().has_permission("Actualization_procedure_read"); }

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



/* DESCRIPTION - DOCUMENTATION ---------------------------------------------------------------------------------------------------------*/
    @JsonIgnore @Transient public final static String state_documentation = "States of update plan for each board is: \n\n"
            + C_ProgramUpdater_State.canceled         + " State where the procedure is canceled by system or board owner" + "\n"
            + C_ProgramUpdater_State.complete         + " State where procedure was absolutely successful" + "\n"
            + C_ProgramUpdater_State.overwritten      + " State where procedure was overwritten by newer versions" + "\n"
            + C_ProgramUpdater_State.in_progress      + " State where system is installing new firmware to board. Its not possible terminate this procedure in this time" + "\n"
            + C_ProgramUpdater_State.instance_inaccessible + " State where instance in Homer wasn't accessible while update procedure" + "\n"
            + C_ProgramUpdater_State.homer_server_is_offline + " State where server where board is connected wasn't accessible while update procedure" + "\n"
            + C_ProgramUpdater_State.waiting_for_device + " State where board is not connected to Homer Server and Main Center is waiting for that" + "\n"
            + C_ProgramUpdater_State.waiting_for_device + " State where shit happens - Server don't know what happens - Automatically reported to BackEnd development team" + "\n"
            ;
}
