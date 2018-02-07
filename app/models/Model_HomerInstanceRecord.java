package models;

public class Model_HomerInstanceRecord {
/* TODO to remove
     @JsonIgnore @OneToMany(mappedBy="homer_instance_record", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_ActualizationProcedure> procedures = new ArrayList<>();



    @JsonProperty  public  String b_program_version_id()          {  return cache_version_object_id != null ? cache_version_object_id : get_b_program_version().id;}
    @JsonProperty  public  String b_program_version_name()        {  return get_b_program_version().version_name;}
    @JsonProperty  public  String b_program_version_description() {  return get_b_program_version().version_description;}
    @JsonProperty  public  String b_program_id()                  {  return get_b_program_version().get_b_program().id;}
    @JsonProperty  public  String b_program_name()                {  return get_b_program_version().get_b_program().name;}
    @JsonProperty  public  String b_program_description()         {  return get_b_program_version().get_b_program().description;}
    @JsonProperty  public  String instance_record_id()            {  return this.id;}

    @JsonProperty  public List<Model_BProgramHwGroup> hardware_group()               {  return get_b_program_version().b_program_hw_groups;}
    @JsonProperty  public List<Model_MProjectProgramSnapShot> m_project_snapshot()    {  return get_b_program_version().b_program_version_snapshots;}
    @JsonProperty  public Enum_Homer_instance_record_status status()    {

        if (planed_when.getTime() > new Date().getTime()) return Enum_Homer_instance_record_status.FUTURE;
        if (actual_running_instance != null) return Enum_Homer_instance_record_status.NOW;
        else return Enum_Homer_instance_record_status.HISTORY;
    }

    @JsonProperty
    public List<Swagger_ActualizationProcedure_Short_Detail> procedure_short_details() {

        List<Swagger_ActualizationProcedure_Short_Detail> procedures_short_details = new ArrayList<>();

        for (Model_ActualizationProcedure procedure : get_actualization_procedures() ) {
            procedures_short_details.add(procedure.short_detail());
        }

        return procedures_short_details;
    }

    @JsonIgnore
    public List<Model_ActualizationProcedure> get_actualization_procedures() {
        try {

            if (cache_procedures_ids.isEmpty()) {

                List<Model_ActualizationProcedure> procedures = Model_ActualizationProcedure.find.query().where().eq("homer_instance_record.id", id).select("id").findList();

                // Získání seznamu
                for (Model_ActualizationProcedure procedure : procedures) {
                    cache_procedures_ids.add(procedure.id.toString());
                }
            }

            List<Model_ActualizationProcedure> procedures  = new ArrayList<>();

            for (String procedure_id : cache_procedures_ids) {
                procedures.add(Model_ActualizationProcedure.getById(procedure_id));
            }

            return procedures;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<Model_ActualizationProcedure>();
        }
    }

    //Add Record to cloud Step 4
    @JsonIgnore @Transient
    private void create_actualization_hardware_request() {
        try {

            logger.debug("create_actualization_request: Instance Record = {}" , id);

            Model_ActualizationProcedure actualization_procedure = new Model_ActualizationProcedure();
            actualization_procedure.project_id = actual_running_instance.get_project_id();
            actualization_procedure.created = new Date();

            if (running_from != null) actualization_procedure.date_of_planing = running_from;

            actualization_procedure.homer_instance_record = this;
            actualization_procedure.type_of_update = UpdateType.MANUALLY_BY_USER_BLOCKO_GROUP;
            //actualization_procedure.save();

            // List Of updates
            List<Model_CProgramUpdatePlan> updates = new ArrayList<>();

            // List Of hardware for updates
            List<Model_BPair> model_bPairs = new ArrayList<>();

            // Contains All Hardware for Update
            for (Model_BProgramHwGroup group : get_b_program_version().b_program_hw_groups) {
                model_bPairs.add(group.main_board_pair);
                model_bPairs.addAll(group.device_board_pairs);
            }

            //actualization_procedure.refresh();

            // Projedu seznam HW - podle skupin instancí jak jsou poskládané podle Yody
            for (Model_BPair model_bPair : model_bPairs) {

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

                logger.debug("create_actualization_request: The number still valid update plans for Main Board Id:: {}that must be override:: {}",  model_bPair.board.id , old_plans_main_board.size());

                for (Model_CProgramUpdatePlan old_plan : old_plans_main_board) {
                    logger.debug("create_actualization_request: Old plan for override under B_Program in Cloud: {} ", old_plan.id);
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

            if (updates.isEmpty()) {
                logger.debug("create_actualization_request: nothing for update");
                return;
            }

            actualization_procedure.updates.addAll(updates);
            actualization_procedure.save();

            for (Model_CProgramUpdatePlan plan_update : actualization_procedure.updates) {
                if (plan_update.state != Enum_CProgram_updater_state.complete) {
                    actualization_procedure.execute_update_procedure();
                    return;
                }
            }

            actualization_procedure.state = Enum_Update_group_procedure_state.successful_complete;
            actualization_procedure.date_of_finish = actualization_procedure.created;
            actualization_procedure.update();

        } catch (Exception e) {
            logger.internalServerError( e);
        }
    }*/
}