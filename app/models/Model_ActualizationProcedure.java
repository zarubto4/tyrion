package models;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.*;
import utilities.loggy.Loggy;
import utilities.notifications.helps_objects.Notification_Text;

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

                                                    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)  public Model_HomerInstanceRecord homer_instance_record; // For updates under instance snapshot records
                                                   // @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)  public Model_HomerInstance homer_instance;              // For updates under virtual instance

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

    @JsonProperty @Transient @ApiModelProperty(required = true, readOnly = true) public String state_fraction(){

        try {

            int all = Model_CProgramUpdatePlan.find.where()
                    .eq("actualization_procedure.id", id)
                    .findRowCount();

            int complete = Model_CProgramUpdatePlan.find.where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", Enum_CProgram_updater_state.complete)
                    .findRowCount();

            return complete + "/" + all;

        }catch (Exception e){
            Loggy.internalServerError("Model_ActualizationProcedure:: state_fraction", e);
            return null;
        }

    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        date_of_create = new Date();

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_ActualizationProcedure.find.byId(this.id) == null) break;
        }

        this.state = Enum_Update_group_procedure_state.not_start_yet;
        super.save();
    }


    @JsonIgnore @Transient
    public void update_state(){
        try {

            System.out.println(".............. Byl zavolán Model_ActualizationProcedure :::-->>>>>>>> update_state");

            logger.debug("Actualization procedure - update state");

            // Metoda je vyvolána, pokud chceme synchronizovat Aktualizační proceduru a nějakým způsobem jí označit
            // Třeba kolik procent už je vykonáno

            int all = Model_CProgramUpdatePlan.find.where()
                    .eq("actualization_procedure.id", id)
                    .findRowCount();

            int complete = Model_CProgramUpdatePlan.find.where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", Enum_CProgram_updater_state.complete)
                    .findRowCount();

            if (complete == all) {

                System.out.println("Actualization procedure  ---  Mám Všechno successful_complete hotové ");

                date_of_finish = new Date();
                state = Enum_Update_group_procedure_state.successful_complete;

                this.notification_update_procedure_complete();

                this.update();
                return;
            }

            int canceled = Model_CProgramUpdatePlan.find.where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", Enum_CProgram_updater_state.canceled)
                    .findRowCount();


            int override = Model_CProgramUpdatePlan.find.where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", Enum_CProgram_updater_state.overwritten)
                    .findRowCount();


            if ((complete + canceled + override) == all) {
                date_of_finish = new Date();
                state = Enum_Update_group_procedure_state.complete;

                System.out.println("Actualization procedure  ---  Mám Všechno complete hotové ");

                this.notification_update_procedure_complete();

                this.update();
                return;
            }

            int in_progress = Model_CProgramUpdatePlan.find.where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", Enum_CProgram_updater_state.in_progress)
                    .findRowCount();

            if (in_progress != 0) {
                state = Enum_Update_group_procedure_state.in_progress;

                notification_update_procedure_progress();

                this.update();
            }

            int critical_error = Model_CProgramUpdatePlan.find.where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", Enum_CProgram_updater_state.critical_error)
                    .findRowCount();

            int not_updated = Model_CProgramUpdatePlan.find.where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", Enum_CProgram_updater_state.not_updated)
                    .findRowCount();

            if (((critical_error + override + canceled + complete + not_updated) * 1.0 / all) == 1.0) {
                date_of_finish = new Date();
                state = Enum_Update_group_procedure_state.complete_with_error;
                this.update();
                return;
            }

        }catch (Exception e){
            Loggy.internalServerError("Model_ActualizationProcedure:: update_state", e);
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

    @JsonIgnore @Transient
    public String get_project_id(){

        if(type_of_update == Enum_Update_type_of_update.MANUALLY_BY_USER_BLOCKO_GROUP || type_of_update == Enum_Update_type_of_update.MANUALLY_BY_USER_BLOCKO_GROUP_ON_TIME ) {
            return Model_Project.find.where().eq("b_programs.instance.instance_history.procedures.id", id).select("id").findUnique().id;
        }

        if(type_of_update == Enum_Update_type_of_update.MANUALLY_BY_USER_INDIVIDUAL){
            return Model_Project.find.where().eq("boards.c_program_update_plans.actualization_procedure.id", id).findUnique().id;
        }

        return null;
    }


/* SERVER WEBSOCKET ----------------------------------------------------------------------------------------------------*/


/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    class Program_Actualization{
        @ApiModelProperty(required = true, value = "Can be empty")  public String b_program_id;
        @ApiModelProperty(required = true, value = "Can be empty")  public String b_program_version_id;
        @ApiModelProperty(required = true, value = "Can be empty")  public String b_program_name;
        @ApiModelProperty(required = true, value = "Can be empty")  public String b_program_version_name;
    }


/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void notification_update_procedure_start(){
        try {

            System.out.println(".............. Byl zavolán obejkt notification_update_procedure_start");

            Model_Notification notification = new Model_Notification();

            notification.setImportance(Enum_Notification_importance.low)
                        .setLevel(Enum_Notification_level.info);

            if(type_of_update == Enum_Update_type_of_update.MANUALLY_BY_USER_INDIVIDUAL){
                notification.setText(new Notification_Text().setText("Your manual update "))
                .setObject(this);

                if(updates.size() == 1){

                    notification.setText(new Notification_Text().setText(" for Board "))
                                .setObject(updates.get(0).board)
                                .setText( new Notification_Text().setText(" from Code Editor with Program "))
                                .setObject(updates.get(0).c_program_version_for_update.c_program)
                                .setText( new Notification_Text().setText(" version "))
                                .setObject(updates.get(0).c_program_version_for_update)
                                .setText( new Notification_Text().setText("."));

                }
                else{
                    notification.setText(new Notification_Text().setText(" for " + updates.size()  + " devices from Code Editor"));
                }

                notification.setText(new Notification_Text().setText(" just begun. We will keep you informed about progress."));

            }else if(type_of_update == Enum_Update_type_of_update.MANUALLY_BY_USER_BLOCKO_GROUP || type_of_update == Enum_Update_type_of_update.MANUALLY_BY_USER_BLOCKO_GROUP_ON_TIME ){

                notification.setText(new Notification_Text().setText("Update under Instance "))
                .setObject(homer_instance_record.actual_running_instance);

                if(updates.size() == 1){

                    notification.setText(new Notification_Text().setText(" with one device "));
                    notification.setObject(updates.get(0).board);

                }
                else{
                    notification.setText(new Notification_Text().setText(" with " + updates.size()  + " devices from Blocko Snapshot "));
                }

                notification.setText(new Notification_Text().setText(" just begun. We will keep you informed about progress."));

            }else {

                logger.error("Model_ActualizationProcedure:: Update procedure has not set type_of_update");

            }


            System.out.println(" Notification Update started ");

            notification.send_under_project(get_project_id());

        }catch (Exception e){
            Loggy.internalServerError("Model_ActualizationProcedure:: notification_update_procedure_start", e);
        }
    }

    @JsonIgnore @Transient
    public void notification_update_procedure_progress(){
        try {

            System.out.println(".............. Byl zavolán obejkt notification_update_procedure_progress");

            if(state_fraction().contains("0/")){
                System.out.println(".............. Byl zavolán NEVHODNE!!!! notification_update_procedure_progress");
                return;
            }

            if(state == Enum_Update_group_procedure_state.complete || state == Enum_Update_group_procedure_state.successful_complete  || state == Enum_Update_group_procedure_state.complete_with_error ){
                System.out.println(".............. Byl zavolán NEVHODNE!!!! notification_update_procedure_progress state is complete");
                return;
            }

            Model_Notification notification = new Model_Notification();

            notification.setId(UUID.randomUUID().toString())
                        .setImportance( Enum_Notification_importance.low)
                        .setLevel( Enum_Notification_level.info);

            notification.setText(new Notification_Text().setText("Update of Procedure "))
                        .setObject(this)
                        .setText( new Notification_Text().setText(" is done from " + state_fraction() + " ." ))
                        .send_under_project(get_project_id());

        }catch (Exception e){
            Loggy.internalServerError("Model_ActualizationProcedure:: notification_update_procedure_progress", e);
        }
    }

    @JsonIgnore @Transient
    public void notification_update_procedure_final_report(){
        try {

            System.out.println(".............. Byl zavolán obejkt notification_update_procedure_final_report");

            Model_Notification notification =  new Model_Notification();

            notification.setImportance( Enum_Notification_importance.low )
                        .setLevel( Enum_Notification_level.success );


            int successfully_updated = Model_CProgramUpdatePlan.find.where()
                    .eq("actualization_procedure.id", id).where()
                    .eq("state", Enum_CProgram_updater_state.complete)
                    .findRowCount();

            int waiting_for_device = Model_CProgramUpdatePlan.find.where()
                    .eq("actualization_procedure.id", id).where()
                    .disjunction()
                        .eq("state", Enum_CProgram_updater_state.waiting_for_device)
                        .eq("state", Enum_CProgram_updater_state.homer_server_is_offline)
                        .eq("state", Enum_CProgram_updater_state.instance_inaccessible)
                    .endJunction()
                    .findRowCount();

            int error_device = Model_CProgramUpdatePlan.find.where()
                    .eq("actualization_procedure.id", id).where()
                    .disjunction()
                        .eq("state", Enum_CProgram_updater_state.critical_error)
                        .eq("state", Enum_CProgram_updater_state.not_updated)
                    .endJunction()
                    .findRowCount();

            notification.setText(new Notification_Text().setText("Update Procedure "))
                    .setObject(this)
                    .setText(new Notification_Text().setText(" is complete. \n"))
                    .setText(new Notification_Text().setText("Number of Total devices for update: " + updates.size() + ". "))
                    .setText(new Notification_Text().setText("Successfully updated: " + successfully_updated + ". " ));

            if(waiting_for_device != 0){
                notification.setText(new Notification_Text().setText("Unavailable \"offline\" devices: "  + waiting_for_device + ". " ));
            }

            if(error_device != 0){
                notification.setText(new Notification_Text().setText("Unsuccessful updates "  + error_device + ". " ));
            }
            
            notification.send_under_project(get_project_id());

        }catch (Exception e){
            Loggy.internalServerError("Model_ActualizationProcedure:: notification_update_procedure_final_report", e);
        }
    }

    @JsonIgnore @Transient
    public void notification_update_procedure_complete(){
        try {

            System.out.println(".............. Byl zavolán obejkt notification_update_procedure_complete");

            Model_Notification notification =  new Model_Notification();

            notification.setImportance( Enum_Notification_importance.normal )
                        .setLevel( Enum_Notification_level.success );

            // Individual update
            if(type_of_update == Enum_Update_type_of_update.MANUALLY_BY_USER_INDIVIDUAL){

                if(state == Enum_Update_group_procedure_state.successful_complete) {
                    notification.setText(new Notification_Text().setText("Update Procedure "))
                            .setObject(this)
                            .setText(new Notification_Text().setText(" is complete."));
                } else{
                    notification.setText(new Notification_Text().setText("Update Procedure "))
                            .setObject(this)
                            .setText(new Notification_Text().setText(" is set as complete but something is wrong. Check Update procedure details."));
                }

                notification.send_under_project(get_project_id());
                return;
            }

            if(state == Enum_Update_group_procedure_state.successful_complete) {

                notification.setText( new Notification_Text().setText("Update Procedure "))
                        .setObject(this)
                        .setText(new Notification_Text().setText(" started at: "))
                        .setDate(date_of_create)
                        .setText(new Notification_Text().setText(" is done with no errors or other issues."));

                notification.send_under_project(get_project_id());
                return;
            }

            if(state == Enum_Update_group_procedure_state.complete) {

                notification.setText( new Notification_Text().setText("Update Procedure "))
                            .setObject(this)
                            .setText(new Notification_Text().setText(" started at: "))
                            .setDate(date_of_create)
                            .setText(new Notification_Text().setText(" is done but something is not right. For more information, please visit the update procedure details."));

                notification.send_under_project(get_project_id());
                return;

            }

        }catch (Exception e){
            Loggy.internalServerError("Model_ActualizationProcedure:: notification_update_procedure_complete", e);
        }
    }


/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient   public static final String read_permission_docs   = "User can read Actualization_procedure if they have ID of Actualization_procedure";

    @JsonIgnore @Transient   public boolean read_permission()      {  return Model_Project.find.where().eq("b_programs.instance.instance_history.procedures.id",id ).findUnique().read_permission() || Controller_Security.get_person().has_permission("Actualization_procedure_read"); }

    public enum permissions{Actualization_procedure_read}




/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_ActualizationProcedure> find = new Model.Finder<>(Model_ActualizationProcedure.class);

}
