package controllers;

import com.avaje.ebean.Expr;
import io.swagger.annotations.*;
import models.compiler.Board;
import models.compiler.FileRecord;
import models.compiler.Version_Object;
import models.project.b_program.B_Pair;
import models.project.b_program.B_Program_Hw_Group;
import models.project.b_program.instnace.Homer_Instance;
import models.project.c_program.actualization.Actualization_procedure;
import models.project.c_program.actualization.C_Program_Update_Plan;
import models.project.global.Project;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.enums.Firmware_type;
import utilities.hardware_updater.Master_Updater;
import utilities.hardware_updater.States.Actual_procedure_State;
import utilities.hardware_updater.States.C_ProgramUpdater_State;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured_API;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_NotFound;
import utilities.response.response_objects.Result_PermissionRequired;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.swagger.documentationClass.Swagger_WebSocket_Device_connected;
import utilities.swagger.documentationClass.Swagger_WebSocket_Yoda_connected;

import java.util.*;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured_API.class)
public class ActualizationController extends Controller {

    // Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

// REST - API ----------------------------------------------------------------------------------------------------------

    @ApiOperation(value = "get actualization Procedure",
            tags = {"Actualization"},
            notes = "get all versions (content) from independent BlockoBlock",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Actualization_Procedure.read_permission", value = Actualization_procedure.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Actualization_Procedure.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Actualization_Procedure_read_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Actualization_procedure.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Actualization_Procedure(@ApiParam(required = true) String actualization_procedure_id){
        try {

            // Kontrola objektu
            Actualization_procedure procedure = Actualization_procedure.find.byId(actualization_procedure_id);
            if (procedure == null) return GlobalResult.notFoundObject("Actualization_Procedure actualization_procedure_id not found");

            // Kontrola oprávnění
            if (! procedure.read_permission()) return GlobalResult.forbidden_Permission();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(procedure));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    // TODO
    public Result get_Actualization_progress(@ApiParam(required = true)   String project_id) {
        try {

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    // TODO
    public Result set_actualization_sheduling(@ApiParam(required = true)  String project_id) {
        try {

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "cancel actualization Procedure",
            tags = {"Actualization"},
            notes = "cancel (terminate) procedure",
            produces = "application/json",
            protocols = "https",
            code = 200,
            hidden = false,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Actualization_Procedure.read_permission", value = Actualization_procedure.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Actualization_Procedure.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Actualization_Procedure_read_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Actualization_procedure.class),
            @ApiResponse(code = 400, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result canceled_procedure(@ApiParam(required = true) String actualization_procedure_id) {
        try {

            // Kontrola objektu
            Actualization_procedure procedure = Actualization_procedure.find.byId(actualization_procedure_id);
            if (procedure == null) return GlobalResult.notFoundObject("Actualization_Procedure actualization_procedure_id not found");

            // Kontrola oprávnění
            if (! procedure.read_permission()) return GlobalResult.forbidden_Permission();

            procedure.cancel_procedure();

            return GlobalResult.result_ok(Json.toJson(procedure));
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


// Private -------------------------------------------------------------------------------------------------------------

    public static void add_new_actualization_request_with_user_file(Project project, Firmware_type command, Board board, FileRecord file_record){

            NotificationController.new_actualization_request_with_file( SecurityController.getPerson(), board);

            List<Board> boards = new ArrayList<>();
            boards.add(board);
            add_new_actualization_request_with_user_file(project, command, boards, file_record);
    }

    public static void add_new_actualization_request_with_user_file(Project project, Firmware_type firmware_type, List<Board> boards, FileRecord file_record){
        try {

            System.out.print("\n\n");
            logger.debug("Incoming new Actualization request with user bin file! ");

            logger.debug("Creating new actualization procedure");
            Actualization_procedure procedure = new Actualization_procedure();
            procedure.date_of_create = new Date();
            procedure.state = Actual_procedure_State.in_progress;
            procedure.project = project;
            procedure.save();

            // Sem sesbírám aktualizační procedury, kterých se týkají změny v old_plans
            Map<String, Actualization_procedure> actualization_procedures = new HashMap<>();

            for (Board board : boards) {

                logger.debug("Checking boar:" + board.id + " for actualization");

                // Tady chci zrušit všechny předchozí procedury vázající se na seznam příchozího hardwaru!

                    //1. Najdu předchozí procedury, které nejsou nějakým způsobem ukončené
                    List<C_Program_Update_Plan> old_plans = C_Program_Update_Plan.find.where()
                            .eq("board.id", board.id).where()
                                .disjunction()
                                    .add(Expr.eq("state", C_ProgramUpdater_State.waiting_for_device     ))
                                    .add(Expr.eq("state", C_ProgramUpdater_State.instance_inaccessible  ))
                                    .add(Expr.eq("state", C_ProgramUpdater_State.homer_server_is_offline))
                                    .add(Expr.isNull("state"))
                            .findList();

                    //2 Měl bych zkontrolovat zda ještě nejsou nějaké aktualizace v chodu

                    logger.debug("The number still valid update plans that must be override: " + old_plans.size());

                    //3. Neukončené procedury ukončím
                    for(C_Program_Update_Plan old_plan : old_plans){

                        logger.debug("Old plan for override: " + old_plan.id);
                        if( !actualization_procedures.containsKey( old_plan.actualization_procedure.id) ){
                            actualization_procedures.put(old_plan.actualization_procedure.id, old_plan.actualization_procedure);
                        }

                        old_plan.state = C_ProgramUpdater_State.overwritten;
                        old_plan.update();
                    }

                logger.debug("Crating new update plan procedure");

                // Vytvářím nový aktualizační plán
                C_Program_Update_Plan plan = new C_Program_Update_Plan();
                plan.board = board;

                plan.binary_file = file_record;

                plan.actualization_procedure = procedure;
                plan.firmware_type = firmware_type;
                plan.save();
                procedure.updates.add(plan);

                logger.debug("Crating update procedure done");
            }

            // Kontroluji a uzavírám stavy "složky" pro aktuazlizace hardwaru a to objektu Actualization_procedure
            logger.debug("Number of Actualization_procedures for update: " + actualization_procedures.size());
            for (String key : actualization_procedures.keySet()) {
                actualization_procedures.get(key).update_state();
            }

            logger.debug("Sending new Actualization procedure to Master Updater");
            Master_Updater.add_new_Procedure(procedure);

        }catch (Exception e){
            logger.error("Add new Actualization request Error ", e);
        }
    }

    public static void add_new_actualization_request_with_user_file(Project project, Board board, Version_Object c_program_version){

        NotificationController.new_actualization_request_on_version( SecurityController.getPerson(), c_program_version);

        List<Board> boards = new ArrayList<>();
        boards.add(board);
        add_new_actualization_request_with_user_file(project, boards, c_program_version);

    }

    public static void add_new_actualization_request_with_user_file(Project project, List<Board> boards, Version_Object c_program_version){
        System.out.print("\n\n");
        logger.debug("Incoming new Actualization request with version of C_program!! ");

        logger.debug("Creating new actualization procedure");
        Actualization_procedure procedure = new Actualization_procedure();
        procedure.date_of_create = new Date();
        procedure.state = Actual_procedure_State.in_progress;
        procedure.project = project;
        procedure.save();

        // Sem sesbírám aktualizační procedury, kterých se týkají změny v old_plans
        Map<String, Actualization_procedure> actualization_procedures = new HashMap<>();

        for (Board board : boards) {

            logger.debug("Checking boar:" + board.id + " for actualization");
            // Tady chci zrušit všechny předchozí procedury vázající se na seznam příchozího hardwaru!

            //1. Najdu předchozí procedury, které nejsou nějakým způsobem ukončené
            List<C_Program_Update_Plan> old_plans = C_Program_Update_Plan.find.where()
                    .eq("board.id", board.id).where()
                    .disjunction()
                        .add(Expr.eq("state", C_ProgramUpdater_State.waiting_for_device      ))
                        .add(Expr.eq("state", C_ProgramUpdater_State.instance_inaccessible   ))
                        .add(Expr.eq("state", C_ProgramUpdater_State.homer_server_is_offline ))
                        .add(Expr.isNull("state"))
                    .findList();

            //2 Měl bych zkontrolovat zda ještě nejsou nějaké aktualizace v chodu

            logger.debug("The number still valid update plans that must be override: " + old_plans.size());

            //3. Neukončené procedury ukončím
            for(C_Program_Update_Plan old_plan : old_plans){

                logger.debug("Old plan in actualization request for override: " + old_plan.id);

                if( !actualization_procedures.containsKey( old_plan.actualization_procedure.id) ){
                    actualization_procedures.put(old_plan.actualization_procedure.id, old_plan.actualization_procedure);
                }

                old_plan.state = C_ProgramUpdater_State.overwritten;
                old_plan.update();
            }

            logger.debug("Crating new update plan procedure");

            // Vytvářím nový aktualizační plán
            C_Program_Update_Plan plan = new C_Program_Update_Plan();
            plan.board = board;
            plan.c_program_version_for_update = c_program_version;
            plan.actualization_procedure = procedure;

            plan.firmware_type = Firmware_type.FIRMWARE;

            plan.save();
            procedure.updates.add(plan);

            logger.debug("Crating update procedure done");
        }

        logger.debug("Number of Actualization_procedures for update: " + actualization_procedures.size());
        for (String key : actualization_procedures.keySet()) {
            actualization_procedures.get(key).update_state();
        }


        logger.debug("Sending new Actualization procedure to Master Updater");
        Master_Updater.add_new_Procedure(procedure);

    }

    public static void add_new_actualization_request_Checking_HW_Firmware(Project project, Homer_Instance program_cloud) {

        System.out.print("\n\n");
        logger.debug("New actualization request Checking HW Firmware!");

        try {

            NotificationController.new_actualization_request_homer_instance( SecurityController.getPerson(), program_cloud);

            logger.debug("Incoming new Actualization request under program_cloud!");

            logger.debug("Creating new actualization procedure");
            Actualization_procedure procedure = new Actualization_procedure();
            procedure.b_program_version_procedure = program_cloud.actual_instance.version_object;
            procedure.date_of_create = new Date();
            procedure.state = Actual_procedure_State.in_progress;
            procedure.project = project;
            procedure.save();


            // - Uložení bych nechal nakonec??? Co když nemám  nic k aktualizaci????


            // Sem sesbírám aktualizační procedury, kterých se týkají změny v old_plans
            Map<String, Actualization_procedure> actualization_procedures = new HashMap<>();

            for(B_Program_Hw_Group group : program_cloud.actual_instance.version_object.b_program_hw_groups) {

                List<B_Pair> list = group.device_board_pairs;
                list.add(group.main_board_pair);

                for (B_Pair p : list) {

                    logger.debug("Checking Pair: " + p.id + " for actualization where is board: " + p.board.id);

                    // Tady chci zrušit všechny předchozí procedury vázající se na seznam příchozího hardwaru!

                    //1. Najdu předchozí procedury, které nejsou nějakým způsobem ukončené
                    List<C_Program_Update_Plan> old_plans = C_Program_Update_Plan.find.where()
                            .eq("board.id", p.board.id).where()
                            .disjunction()
                            .add(Expr.eq("state", C_ProgramUpdater_State.waiting_for_device))
                            .add(Expr.eq("state", C_ProgramUpdater_State.instance_inaccessible))
                            .add(Expr.eq("state", C_ProgramUpdater_State.homer_server_is_offline))
                            .add(Expr.isNull("state"))
                            .findList();

                    //2 Měl bych zkontrolovat zda ještě nejsou nějaké aktualizace v chodu

                    logger.debug("The number still valid update plans that must be override: " + old_plans.size());

                    //3. Neukončené procedury ukončím
                    for (C_Program_Update_Plan old_plan : old_plans) {

                        logger.debug("Old plan for override under B_Program in Cloud: " + old_plan.id);

                        if (!actualization_procedures.containsKey(old_plan.actualization_procedure.id)) {
                            actualization_procedures.put(old_plan.actualization_procedure.id, old_plan.actualization_procedure);
                        }

                        old_plan.state = C_ProgramUpdater_State.overwritten;
                        old_plan.update();
                    }

                    logger.debug("Crating new update plan procedure");

                    // Vytvářím nový aktualizační plán
                    C_Program_Update_Plan plan = new C_Program_Update_Plan();
                    plan.board = p.board;
                    plan.c_program_version_for_update = p.c_program_version;
                    plan.actualization_procedure = procedure;

                    plan.firmware_type = Firmware_type.FIRMWARE;

                    procedure.updates.add(plan);

                    logger.debug("Crating update procedure done");
                }
            }

            // Nakonci uložím
            // Ale zkontroluji jestli tam jsou nějaké procedury před tím???
            if(procedure.updates.size() > 0){
                logger.debug("Procedura má co k práci a tak bude uložena");
                procedure.save();
            }
            else{
                logger.debug("Procedura nemá nic k práci a tak jí neuložím a ukončuji");
                return;
            }

            // Kontroluji a uzavírám stavy "složky" pro aktuazlizace hardwaru a to objektu Actualization_procedure
            logger.debug("Number of Actualization_procedures for update: " + actualization_procedures.size());
            for (String key : actualization_procedures.keySet()) {
                actualization_procedures.get(key).update_state();
            }

            logger.debug("Sending new Actualization procedure to Master Updater");
            Master_Updater.add_new_Procedure(procedure);

        }catch (Exception e){
            logger.error("Add new Actualization request Error ", e);
        }
    }

// ---------------------------------------------------------------------------------------------------------------------

    public static void hardware_connected(Board board, Swagger_WebSocket_Yoda_connected report){

        logger.debug("Tyrion Checking summary information of connected master board: ", board.id);


        // Kontrola nastavení Backup modu
        logger.trace("Checking autobackup");
        if(board.backup_mode != report.autobackup){
            // TODO

        }

        // Pokusím se najít Aktualizační proceduru jestli existuje s následujícími stavy
        logger.debug("Tyrion Checking actualization state of connected board: ", board.id);
        List<C_Program_Update_Plan> plans = C_Program_Update_Plan.find.where().eq("board.id", board.id).disjunction()
                .add(   Expr.eq("state", C_ProgramUpdater_State.in_progress)         )
                .add(   Expr.eq("state", C_ProgramUpdater_State.waiting_for_device)         )
                .add(   Expr.eq("state", C_ProgramUpdater_State.instance_inaccessible)      )
                .add(   Expr.eq("state", C_ProgramUpdater_State.critical_error)      )
                .add(   Expr.eq("state", C_ProgramUpdater_State.homer_server_is_offline)    ).order().asc("id").findList();


        if(plans.size() > 1){
            logger.error("Hardware Yoda: ", board.id, " connected into system, but we have mote than 2 update-plan!!!");
            logger.error("Earlier plans are terminate! Last one - by ID is used now!");

            for(int i = 1; i < plans.size(); i++ ){
                plans.get(i).state = C_ProgramUpdater_State.overwritten;
                plans.get(i).update();
                plans.remove(i);
            }
        }

        if(plans.size() == 1){

            logger.debug("Found one actualization procedure on ", board.id);

            C_Program_Update_Plan plan = plans.get(0);


                  if(plan.firmware_type == Firmware_type.FIRMWARE){

                      logger.debug("Checking Firmware");

                      // Mám shodu oproti očekávánemů
                      if(plan.c_program_version_for_update.c_compilation.firmware_build_id .equals( report.firmware_build_id )){

                          plan.state = C_ProgramUpdater_State.complete;
                          plan.update();

                      }else {

                          plan.state = C_ProgramUpdater_State.in_progress;
                          plan.update();

                          Master_Updater.add_new_Procedure(plan.actualization_procedure);

                      }

            }else if(plan.firmware_type == Firmware_type.BOOTLOADER){

                      logger.debug("Checking Firmware");

                      // Mám shodu oproti očekávánemů
                      if(plan.binary_file.boot_loader.version_identificator.equals( report.bootloader_build_id )){

                          plan.state = C_ProgramUpdater_State.complete;
                          plan.update();

                      }else {

                          plan.state = C_ProgramUpdater_State.in_progress;
                          plan.update();

                          Master_Updater.add_new_Procedure(plan.actualization_procedure);
                      }

            }else if(plan.firmware_type == Firmware_type.BACKUP){

                      logger.debug("Checking Backup");

                      plan.state = C_ProgramUpdater_State.complete;
                      plan.update();
            }
        }else {
            logger.debug("No actualization plan found for Master Device: " + board.id);
        }


        for(Swagger_WebSocket_Device_connected device_report : report.devices_summary){

            Board device = Board.find.byId(device_report.deviceId);

            // Smazat device z instance a tím i z yody
            if(device == null){
                logger.error("Unauthorized device connected to Yoda!" + board.id);
                //TODO

            }else {
                ActualizationController.hardware_connected(device, device_report);
            }

        }

    }

    public static void hardware_connected(Board board, Swagger_WebSocket_Device_connected report) {
        logger.debug("Tyrion Checking summary information of connected padavan board: ", board.id);

        // Pokusím se najít Aktualizační proceduru jestli existuje s následujícími stavy
        logger.debug("Tyrion Checking actualization state of connected board: ", board.id);
        List<C_Program_Update_Plan> plans = C_Program_Update_Plan.find.where().eq("board.id", board.id).disjunction()
                .add(   Expr.eq("state", C_ProgramUpdater_State.in_progress)         )
                .add(   Expr.eq("state", C_ProgramUpdater_State.waiting_for_device)         )
                .add(   Expr.eq("state", C_ProgramUpdater_State.instance_inaccessible)      )
                .add(   Expr.eq("state", C_ProgramUpdater_State.critical_error)      )
                .add(   Expr.eq("state", C_ProgramUpdater_State.homer_server_is_offline)    ).order().asc("id").findList();


        if(plans.size() > 1){
            logger.error("Hardware Board: ", board.id, " connected into system, but we have mote than 2 update-plan!!!");
            logger.error("Earlier plans are terminate! Last one - by ID is used now!");

            for(int i = 1; i < plans.size(); i++ ){
                plans.get(i).state = C_ProgramUpdater_State.overwritten;
                plans.get(i).update();
                plans.remove(i);
            }
        }

        if(plans.size() == 1){

            logger.debug("Found one actualization procedure on ", board.id);

            C_Program_Update_Plan plan = plans.get(0);

            if(plan.firmware_type == Firmware_type.FIRMWARE){

                logger.debug("Checking Firmware");

                // Mám shodu oproti očekávánemů
                if(plan.c_program_version_for_update.c_compilation.firmware_build_id .equals( report.firmware_build_id )){

                    plan.state = C_ProgramUpdater_State.complete;
                    plan.update();

                }else {

                    plan.state = C_ProgramUpdater_State.in_progress;
                    plan.update();

                    Master_Updater.add_new_Procedure(plan.actualization_procedure);

                }

            }else if(plan.firmware_type == Firmware_type.BOOTLOADER) {

                logger.debug("Checking Firmware");

                // Mám shodu oproti očekávánemů
                if (plan.binary_file.boot_loader.version_identificator.equals(report.bootloader_build_id)) {

                    plan.state = C_ProgramUpdater_State.complete;
                    plan.update();

                } else {

                    plan.state = C_ProgramUpdater_State.in_progress;
                    plan.update();

                    Master_Updater.add_new_Procedure(plan.actualization_procedure);
                }
            }

        }else {
            logger.debug("No actualization plan found for Master Device: " + board.id);
        }


    }

    public static void hardware_disconnected(Board board){
        logger.debug("Device disconnected: " + board.id);
    }



}


