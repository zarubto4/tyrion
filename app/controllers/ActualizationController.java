package controllers;

import com.avaje.ebean.Expr;
import io.swagger.annotations.*;
import models.compiler.Board;
import models.compiler.FileRecord;
import models.compiler.Version_Object;
import models.project.b_program.B_Pair;
import models.project.b_program.Homer_Instance;
import models.project.c_program.actualization.Actualization_procedure;
import models.project.c_program.actualization.C_Program_Update_Plan;
import models.project.global.Project;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.UtilTools;
import utilities.hardware_updater.Master_Updater;
import utilities.hardware_updater.States.Actual_procedure_State;
import utilities.hardware_updater.States.C_ProgramUpdater_State;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_NotFound;
import utilities.response.response_objects.Result_PermissionRequired;
import utilities.response.response_objects.Result_Unauthorized;

import javax.websocket.server.PathParam;
import java.io.File;
import java.util.*;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured.class)
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
    public Result get_Actualization_Procedure(@ApiParam(required = true) @PathParam("actualization_procedure_id")  String actualization_procedure_id){
        try {

            // Kontrola objektu
            Actualization_procedure procedure = Actualization_procedure.find.byId(actualization_procedure_id);
            if (procedure == null) return GlobalResult.notFoundObject("Actualization_Procedure actualization_procedure_id not found");

            // Kontrola oprávnění
            if (! procedure.read_permission()) return GlobalResult.forbidden_Permission();

            // Vrácení objektu
            return GlobalResult.ok(Json.toJson(procedure));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


    public Result get_Actualization_progress(@ApiParam(required = true) @PathParam("board_id")  String project_id) {
        try {

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    public Result set_actualization_sheduling(@ApiParam(required = true) @PathParam("board_id")  String project_id) {
        try {

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


    @ApiOperation(value = "cancel Procedure",
            tags = {"Actualization"},
            notes = "cancel (terminate) procedure",
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
    public Result canceled_procedure(@ApiParam(required = true) @PathParam("actualization_procedure_id")  String actualization_procedure_id) {
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

    public static void add_new_actualization_request(Project project, Board board, File file, String file_name){

            NotificationController.new_actualization_request_with_file( SecurityController.getPerson(), board,  file_name );

            List<Board> boards = new ArrayList<>();
            boards.add(board);
            add_new_actualization_request(project, boards, file, file_name);
    }

    public static void add_new_actualization_request(Project project, List<Board> boards, File file, String file_name){
        try {


            logger.debug("Incoming new Actualization request with user bin file! ");

            String binary_file = UtilTools.get_encoded_binary_string_from_File(file);
            FileRecord fileRecord = UtilTools.create_Binary_file(binary_file, file_name);

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
                                    .add(Expr.eq("state",C_ProgramUpdater_State.waiting_for_device     ))
                                    .add(Expr.eq("state",C_ProgramUpdater_State.instance_inaccessible))
                                    .add(Expr.eq("state",C_ProgramUpdater_State.homer_server_is_offline))
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
                plan.binary_file = fileRecord;
                plan.actualization_procedure = procedure;
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

    public static void add_new_actualization_request(Project project, Board board, Version_Object c_program_version){

        NotificationController.new_actualization_request_on_version( SecurityController.getPerson(), c_program_version);

        List<Board> boards = new ArrayList<>();
        boards.add(board);
        add_new_actualization_request(project, boards, c_program_version);

    }

    public static void add_new_actualization_request(Project project, List<Board> boards, Version_Object c_program_version){

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
                    .add(Expr.eq("state",C_ProgramUpdater_State.waiting_for_device     ))
                    .add(Expr.eq("state",C_ProgramUpdater_State.instance_inaccessible))
                    .add(Expr.eq("state",C_ProgramUpdater_State.homer_server_is_offline))
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

    public static void add_new_actualization_request(Project project, Homer_Instance program_cloud) {

        try {

            NotificationController.new_actualization_request_homer_instance( SecurityController.getPerson(), program_cloud);

            logger.debug("Incoming new Actualization request under program_cloud!");

            logger.debug("Creating new actualization procedure");
            Actualization_procedure procedure = new Actualization_procedure();
            procedure.b_program_version_procedure = program_cloud.version_object;
            procedure.date_of_create = new Date();
            procedure.state = Actual_procedure_State.in_progress;
            procedure.project = project;
            procedure.save();

            // Seznam zařízení určených k Updatu
            List<B_Pair> list = program_cloud.version_object.padavan_board_pairs;

            // Přidání do seznamu Master Yodu
            list.add(program_cloud.version_object.yoda_board_pair);

            // Sem sesbírám aktualizační procedury, kterých se týkají změny v old_plans
            Map<String, Actualization_procedure> actualization_procedures = new HashMap<>();

            for (B_Pair p : list) {

                logger.debug("Checking Pair:" + p.id + " for actualization where is board: " + p.board.id);

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

// ---------------------------------------------------------------------------------------------------------------------

    public static void hardware_connected(Board board){

        logger.debug("Tyrion Checking actualization state of connected board:" + board.id);

        List<C_Program_Update_Plan> plans = C_Program_Update_Plan.find.where().eq("board.id", board.id).disjunction()
                .add(   Expr.eq("state", C_ProgramUpdater_State.waiting_for_device)         )
                .add(   Expr.eq("state", C_ProgramUpdater_State.instance_inaccessible)      )
                .add(   Expr.eq("state", C_ProgramUpdater_State.homer_server_is_offline)    ).order().asc("id").findList();

        if(plans.size() == 1){

            logger.debug("Found one actualization procedure on" + board.id);

            Actualization_procedure procedure = new Actualization_procedure();
            procedure.updates.add(plans.get(0));

            logger.debug("Sending new actualization request to Master Updater Thread");
            Master_Updater.add_new_Procedure(procedure);
        }
        if(plans.size() > 1){
            logger.error("Hardware: " + board.id + " connected into system, but we have mote than 2 update-plan!!!");
            logger.error("Earlier plans are terminate! Last one - by ID is used now!");

            for(int i = 1; i < plans.size(); i++ ){
                plans.get(i).state = C_ProgramUpdater_State.overwritten;
                plans.get(i).update();
            }

            Actualization_procedure procedure = new Actualization_procedure();
            procedure.updates.add(plans.get(0));
            Master_Updater.add_new_Procedure(procedure);

        }

        logger.debug("No actualization plan found for hardware: " + board.id);

    }

    public static void hardware_disconnected(Board board){
        logger.debug("Device disconnected: " + board.id);
    }



}


