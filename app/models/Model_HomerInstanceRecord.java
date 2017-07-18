package models;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.Form;
import play.i18n.Lang;
import play.libs.Json;
import utilities.cache.helps_objects.TyrionCachedList;
import utilities.enums.*;
import utilities.logger.Class_Logger;
import utilities.swagger.outboundClass.Swagger_ActualizationProcedure_Short_Detail;
import web_socket.message_objects.homer_instance_with_tyrion.WS_Message_Instance_device_set_snap;
import web_socket.message_objects.homer_instance_with_tyrion.WS_Message_Instance_status;
import web_socket.message_objects.homer_with_tyrion.WS_Message_Homer_Instance_add;
import web_socket.message_objects.homer_instance_with_tyrion.WS_Message_Instance_upload_blocko_program;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Entity
@ApiModel( value = "HomerInstanceRecord", description = "Model of HomerInstanceRecord")
public class Model_HomerInstanceRecord extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_HomerInstanceRecord.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Id   public String id;

    @JsonIgnore @ManyToOne(cascade = CascadeType.ALL,  fetch = FetchType.LAZY)     public Model_HomerInstance main_instance_history;

    @ApiModelProperty(required = false, readOnly = true, value = "can be null")   public Date date_of_created;
    @ApiModelProperty(required = false, readOnly = true, value = "can be null")   public Date running_from;
    @ApiModelProperty(required = false, readOnly = true, value = "can be null")   public Date running_to;
    @ApiModelProperty(required = false, readOnly = true, value = "can be null")   public Date planed_when;

    @JsonIgnore @ManyToOne(cascade=CascadeType.ALL, fetch = FetchType.LAZY)       public Model_VersionObject version_object;
    @JsonIgnore @OneToOne(cascade=CascadeType.ALL)                                public Model_HomerInstance actual_running_instance;      // Aktuálně běžící instnace na Serveru (Pokud není null má běžet- má běžet na serveru)
    @JsonIgnore @OneToMany(mappedBy="homer_instance_record", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_ActualizationProcedure> procedures = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @TyrionCachedList private String cache_main_instance_history_id;
    @JsonIgnore @Transient @TyrionCachedList private String cache_version_object_id;
    @JsonIgnore @Transient @TyrionCachedList private List<String> cache_procedures_ids = new ArrayList<>();


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @Transient @JsonProperty  public  String b_program_version_id()          {  return cache_version_object_id != null ? cache_version_object_id : get_b_program_version().id;}
    @Transient @JsonProperty  public  String b_program_version_name()        {  return get_b_program_version().version_name;}
    @Transient @JsonProperty  public  String b_program_version_description() {  return get_b_program_version().version_description;}
    @Transient @JsonProperty  public  String b_program_id()                  {  return get_b_program_version().get_b_program().id;}
    @Transient @JsonProperty  public  String b_program_name()                {  return get_b_program_version().get_b_program().name;}
    @Transient @JsonProperty  public  String b_program_description()         {  return get_b_program_version().get_b_program().description;}
    @Transient @JsonProperty  public  String instance_record_id()            {  return this.id;}

    @Transient @JsonProperty  public List<Model_BProgramHwGroup> hardware_group()               {  return get_b_program_version().b_program_hw_groups;}
    @Transient @JsonProperty  public List<Model_MProjectProgramSnapShot> m_project_snapshot()    {  return get_b_program_version().b_program_version_snapshots;}
    @Transient @JsonProperty  public Enum_Homer_instance_record_status status()    {

        if(planed_when.getTime() > new Date().getTime()) return Enum_Homer_instance_record_status.FUTURE;
        if(actual_running_instance != null) return Enum_Homer_instance_record_status.NOW;
        else return Enum_Homer_instance_record_status.HISTORY;

    }

    @Transient  @JsonProperty public List<Swagger_ActualizationProcedure_Short_Detail> procedure_short_details() {

        List<Swagger_ActualizationProcedure_Short_Detail> procedures_short_details = new ArrayList<>();

        for (Model_ActualizationProcedure procedure : get_actualization_procedures() ) {
            procedures_short_details.add(procedure.short_detail());
        }

        return procedures_short_details;
    }


/* JSON IGNORE  ----------------------------------------------------------------------------------------------------*/


    @JsonIgnore @TyrionCachedList
    public Model_VersionObject get_b_program_version(){

        if(cache_version_object_id == null){
            Model_VersionObject version = Model_VersionObject.find.where().eq("instance_record.id", id).select("id").findUnique();
            cache_version_object_id = version.id;
        }

        return Model_VersionObject.get_byId(cache_version_object_id);

    }

    @JsonIgnore @Transient @TyrionCachedList public List<Model_ActualizationProcedure> get_actualization_procedures() {
        try{

            if(cache_procedures_ids.isEmpty()){

                List<Model_ActualizationProcedure> procedures = Model_ActualizationProcedure.find.where().eq("homer_instance_record.id", id).select("id").findList();

                // Získání seznamu
                for (Model_ActualizationProcedure procedure : procedures) {
                    cache_procedures_ids.add(procedure.id.toString());
                }

            }

            List<Model_ActualizationProcedure> procedures  = new ArrayList<>();

            for(String procedure_id : cache_procedures_ids){
                procedures.add(Model_ActualizationProcedure.get_byId(procedure_id));
            }

            return procedures;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new ArrayList<Model_ActualizationProcedure>();
        }
    }


    @JsonIgnore
    public Model_Product getProduct(){
        return this.actual_running_instance.get_project().get_product();

    }


    @JsonIgnore @Transient public List<String>get_boards_required_by_record(){

        List<String> board_ids = new ArrayList<>();

        // Contains All Hardware for Update
        for(Model_BProgramHwGroup group : get_b_program_version().b_program_hw_groups) {
            board_ids.add(group.main_board_pair.id.toString());
            for(Model_BPair model_bPair : group.device_board_pairs){
                board_ids.add(model_bPair.id.toString());
            }
        }

        return board_ids;
    }


/* INSTANCE WEBSOCKET CONTROLLING ON HOMER SERVER-----------------------------------------------------------------------*/


    /**
     * Nasazení Record Instance do Cloudu - To jest nastavení Record jako výchozího stavu,
     * nasazení blocko programu, snchronizace hardaru a verzí atd..
     */
    public void set_record_into_cloud() {
        new Thread(() -> {

            // Step 1
            terminal_logger.debug("Set Instance Record into Cloud ");
            if(this.actual_running_instance == null){
                terminal_logger.debug("Actual_running_instance its  null - Its required set this intance record as actual running into database");
                this.change_record_as_main();
            }

            if(!main_instance_history.server_is_online()) {
                terminal_logger.debug("Server je offline  - Its not possible to continue");
                return;
            }

            WS_Message_Instance_status status = actual_running_instance.get_instance_status();

            WS_Message_Instance_status.InstanceStatus instanceStatus = status.get_status(actual_running_instance.id);

            if(instanceStatus.error_code != null ){
                terminal_logger.warn("Instance " + actual_running_instance.id + " Instance not set in Homer Server ");
            }

            // Instance status
            if(!instanceStatus.status){
                // Vytvořím Instanci
                WS_Message_Homer_Instance_add result_instance   = actual_running_instance.cloud_homer_server.add_instance(actual_running_instance);
                if(!result_instance.status.equals("success")){
                    terminal_logger.internalServerError(new Exception("Failed to add Instance. ErrorCode: " + result_instance.error_code + ". Error: " + result_instance.error));
                }
            }

            // Step 2
            WS_Message_Instance_upload_blocko_program result_step_2 = this.upload_blocko_program();
            if(!result_step_2.status.equals("success")){
                terminal_logger.warn("Instance " + actual_running_instance.id + " Krok dva nevyšel Error :: Error Code:: " + result_step_2.error_code);
            }

            // Step 3
            WS_Message_Instance_device_set_snap result_step_3 =  this.update_device_summary_collection();
            if(!result_step_3.status.equals("success")){
                terminal_logger.warn("Instance " + actual_running_instance.id + " Krok dva nevyšel Error :: Error Code:: " + result_step_3.error_code);
            }

            // Step 4
            this.create_actualization_hardware_request();


        }).start();

    }

    //Add Record to cloud Step 1
    private void change_record_as_main() {

        try {

            terminal_logger.debug("upload_record: thread is running under record ID:: {} ", id);

            Model_HomerInstance instance = main_instance_history;

            if( instance.actual_instance != null) {

                terminal_logger.debug("upload_record Actual Instnace != null -> InstanceRecord ID: {}", instance.actual_instance.id);

                terminal_logger.debug("upload_record: Record overwriting previous instance record:: " + instance.actual_instance.id);

                Model_HomerInstanceRecord previous_version = instance.actual_instance;

                previous_version.running_to = new Date();
                previous_version.actual_running_instance = null;

                previous_version.update();

            }else {
                terminal_logger.debug("upload_record: First uploading of instance");
            }

            // Synchronize Grid App settings from last config
            // Try Find Latest runing Record with latest configuration - it can be instance.actual_instance from previous step or
            // record that was turned off and on again.
            Model_HomerInstanceRecord latest_version = Model_HomerInstanceRecord.find.where().eq("main_instance_history.id", main_instance_history.id).isNotNull("running_to").orderBy().desc("running_to").setMaxRows(1).findUnique();

            if(latest_version != null){

                for(Model_MProjectProgramSnapShot snap_shot : latest_version.get_b_program_version().b_program_version_snapshots){

                    // Programy
                    for(Model_MProgramInstanceParameter old_parameter : snap_shot.m_program_snapshots()){

                        Model_MProgramInstanceParameter new_parameter = Model_MProgramInstanceParameter.find.where()
                                .eq("m_project_program_snapshot.instance_versions.instance_record.id", id)
                                .eq("m_program_version.id", old_parameter.m_program_version.id)
                                .findUnique();

                        if(new_parameter == null){
                            continue;
                        }

                        new_parameter.connection_token = old_parameter.connection_token;
                        new_parameter.snapshot_settings = old_parameter.snapshot_settings;
                        new_parameter.update();

                    }
                }
            }

            instance.refresh();
            instance.actual_instance = this;
            this.actual_running_instance = instance;
            update();
            refresh();

            instance.update();
            instance.refresh();


        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }


    }

    //Add Record to cloud Step 2
    @JsonIgnore @Transient
    private WS_Message_Instance_upload_blocko_program upload_blocko_program(){
        try {

            Model_FileRecord fileRecord = Model_FileRecord.find.where().eq("version_object.id", get_b_program_version().id).eq("file_name", "program.js").findUnique();

            JsonNode node =  main_instance_history.cloud_homer_server.sender().write_with_confirmation(new WS_Message_Instance_upload_blocko_program().make_request(main_instance_history, get_b_program_version()), 1000 * 3, 0, 2);

            final Form<WS_Message_Instance_upload_blocko_program> form = Form.form(WS_Message_Instance_upload_blocko_program.class).bind(node);
            if(form.hasErrors()) throw new Exception("WS_Message_Instance_upload_blocko_program: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());

            return form.get();

        }catch (InterruptedException|TimeoutException e){
            return  new WS_Message_Instance_upload_blocko_program();
        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return  new WS_Message_Instance_upload_blocko_program();
        }
    }

    //Add Record to cloud Step 3
    @JsonIgnore @Transient
    private WS_Message_Instance_device_set_snap update_device_summary_collection(){
        try {

            // Seznam - který by na instanci měl běžet!
            List<String> hardware_ids_required_by_instance = actual_running_instance.get_boards_id_required_by_record();

            // Přidat nový otisk hardwaru
            if(!hardware_ids_required_by_instance.isEmpty()){
                return actual_running_instance.set_device_to_instance(hardware_ids_required_by_instance);
            }else {
                WS_Message_Instance_device_set_snap result = new WS_Message_Instance_device_set_snap();
                result.status = "success";
                return result;
            }


        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new WS_Message_Instance_device_set_snap();
        }
    }

    //Add Record to cloud Step 4
    @JsonIgnore @Transient
    private void create_actualization_hardware_request() {
        try {

            terminal_logger.debug("create_actualization_request:: byl zavolán na Instance Record:: {}" , id);

            Model_ActualizationProcedure actualization_procedure = new Model_ActualizationProcedure();
            actualization_procedure.project_id = actual_running_instance.get_project_id();
            actualization_procedure.date_of_create = new Date();

            if(running_from != null) actualization_procedure.date_of_planing = running_from;

            actualization_procedure.homer_instance_record = this;
            actualization_procedure.type_of_update = Enum_Update_type_of_update.MANUALLY_BY_USER_BLOCKO_GROUP;
            //actualization_procedure.save();


            // List Of updates
            List<Model_CProgramUpdatePlan> updates = new ArrayList<>();


            // List Of hardware for updates
            List<Model_BPair> model_bPairs = new ArrayList<>();


            // Contains All Hardware for Update
            for(Model_BProgramHwGroup group : get_b_program_version().b_program_hw_groups) {
                model_bPairs.add(group.main_board_pair);
                model_bPairs.addAll(group.device_board_pairs);
            }

            actualization_procedure.refresh();

            // Projedu seznam HW - podle skupin instancí jak jsou poskládané podle Yody
            for(Model_BPair model_bPair : model_bPairs) {

                List<Model_CProgramUpdatePlan> old_plans_main_board = Model_CProgramUpdatePlan.find.where()
                        .eq("firmware_type", Enum_Firmware_type.FIRMWARE.name())
                        .eq("board.id", model_bPair.board.id).where()
                        .disjunction()
                        .add(Expr.eq("state", Enum_CProgram_updater_state.not_start_yet))
                        .add(Expr.eq("state", Enum_CProgram_updater_state.waiting_for_device))
                        .add(Expr.eq("state", Enum_CProgram_updater_state.instance_inaccessible))
                        .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_is_offline))
                        .add(Expr.isNull("state"))
                        .endJunction()
                        .findList();

                terminal_logger.debug("create_actualization_request:: The number still valid update plans for Main Board Id:: {}that must be override:: {}",  model_bPair.board.id , old_plans_main_board.size());

                for (Model_CProgramUpdatePlan old_plan : old_plans_main_board) {
                    terminal_logger.debug("create_actualization_request:: Old plan for override under B_Program in Cloud: {} ", old_plan.id);
                    old_plan.state = Enum_CProgram_updater_state.overwritten;
                    old_plan.date_of_finish = new Date();
                    old_plan.update();
                }

                Model_CProgramUpdatePlan plan_master_board = new Model_CProgramUpdatePlan();
                plan_master_board.board = model_bPair.board;
                plan_master_board.firmware_type = Enum_Firmware_type.FIRMWARE;
                plan_master_board.c_program_version_for_update = model_bPair.c_program_version;
                updates.add(plan_master_board);

            }



            if(updates.isEmpty()){
                terminal_logger.debug("create_actualization_request:: nothing for update");
                return;
            }

            actualization_procedure.updates.addAll(updates);
            actualization_procedure.save();


            for(Model_CProgramUpdatePlan plan_update : actualization_procedure.updates){
                if(plan_update.state != Enum_CProgram_updater_state.complete){
                    actualization_procedure.execute_update_procedure();
                    return;
                }
            }

            actualization_procedure.state = Enum_Update_group_procedure_state.successful_complete;
            actualization_procedure.date_of_finish = actualization_procedure.date_of_create;
            actualization_procedure.update();

        }catch (Exception e){
            terminal_logger.internalServerError("create_actualization_request:", e);
        }
    }




/* JSON Override  Method -----------------------------------------------------------------------------------------*/

    @Override
    public void save(){

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_HomerInstanceRecord.find.byId(this.id) == null) break;
        }
        super.save();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_HomerInstanceRecord get_byId(String id) {

        terminal_logger.warn("CACHE is not implemented - TODO");
        return find.byId(id);

    }

    /* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_HomerInstanceRecord> find = new Finder<>(Model_HomerInstanceRecord.class);

}
