package utilities.independent_threads.homer_server;

import com.avaje.ebean.Expr;
import models.Model_CProgramUpdatePlan;
import models.Model_HomerServer;
import utilities.enums.Enum_CProgram_updater_state;
import utilities.logger.Class_Logger;
import utilities.swagger.outboundClass.Swagger_UpdatePlan_brief_for_homer;
import web_socket.services.WS_HomerServer;

import java.util.ArrayList;
import java.util.List;

public class Synchronize_Homer_Unresolved_Updates extends Thread {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Synchronize_Homer_Unresolved_Updates.class);

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    private WS_HomerServer ws_homerServer = null;

    // Umožněno kontrolovat COmpilator i Homer server
    public Synchronize_Homer_Unresolved_Updates(WS_HomerServer ws_homerServer){
        this.ws_homerServer = ws_homerServer;

    }


    @Override
    public void run(){

        terminal_logger.debug("Synchronize_Homer_Unresolved_Updates:: Independent Thread under server:: " + ws_homerServer.identifikator + " started");

        try {

            sleep(1000 * 10);

            List<Model_CProgramUpdatePlan> old_not_finished_plans = Model_CProgramUpdatePlan.find.where()
                    .eq("board.connected_server_id", ws_homerServer.identifikator)
                    .where()
                        .disjunction()
                            .add(Expr.eq("state", Enum_CProgram_updater_state.in_progress))
                            .add(Expr.eq("state", Enum_CProgram_updater_state.not_updated))
                            .add(Expr.eq("state", Enum_CProgram_updater_state.not_start_yet))
                            .add(Expr.eq("state", Enum_CProgram_updater_state.waiting_for_device))
                            .add(Expr.eq("state", Enum_CProgram_updater_state.instance_inaccessible))
                            .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_is_offline))
                            .add(Expr.isNull("state"))
                        .endJunction()
                    .findList();


            List<Swagger_UpdatePlan_brief_for_homer> tasks = new ArrayList<>();

            if(tasks.isEmpty()){
                terminal_logger.debug("Zero execution Model_CProgramUpdatePlan for Homer Server");
                return;
            }

            for(Model_CProgramUpdatePlan plan : old_not_finished_plans){
                tasks.add(plan.get_brief_for_update_homer_server());
            }


            Model_HomerServer.get_byId(ws_homerServer.identifikator).update_devices_firmware(tasks);

        }catch (Exception e){
            terminal_logger.internalServerError("run:", e);
        }
    }
}