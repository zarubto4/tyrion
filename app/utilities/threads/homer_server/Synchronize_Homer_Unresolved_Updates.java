package utilities.threads.homer_server;

import io.ebean.Expr;
import io.ebean.PagedList;
import models.Model_CProgramUpdatePlan;
import models.Model_HomerServer;
import utilities.enums.Enum_CProgram_updater_state;
import utilities.logger.Logger;
import utilities.swagger.output.Swagger_UpdatePlan_brief_for_homer;
import websocket.interfaces.WS_Homer;
import websocket.messages.homer_hardware_with_tyrion.updates.WS_Message_Hardware_UpdateProcedure_Command;

import java.util.ArrayList;
import java.util.List;

public class Synchronize_Homer_Unresolved_Updates extends Thread {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger terminal_logger = new Logger(Synchronize_Homer_Unresolved_Updates.class);

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    private WS_Homer homer = null;

    // Umožněno kontrolovat COmpilator i Homer server
    public Synchronize_Homer_Unresolved_Updates(WS_Homer homer) {
        this.homer = homer;
    }


    @Override
    public void run() {

        terminal_logger.debug("Synchronize_Homer_Unresolved_Updates:: Independent Thread under server:: " + homer.id + " started");

        try {

            terminal_logger.debug("5. Spouštím Sycnhronizační proceduru Synchronize_Homer_Unresolved_Updates");

            int page = 0;
            int page_size = 100;

            while (true) {

                PagedList<Model_CProgramUpdatePlan> paging_list = Model_CProgramUpdatePlan.find.query().where()
                        .eq("board.connected_server_id", homer.id)
                        .disjunction()
                            .add(Expr.eq("state", Enum_CProgram_updater_state.in_progress))
                            .add(Expr.eq("state", Enum_CProgram_updater_state.not_updated))
                            .add(Expr.eq("state", Enum_CProgram_updater_state.not_start_yet))
                            .add(Expr.eq("state", Enum_CProgram_updater_state.waiting_for_device))
                            .add(Expr.eq("state", Enum_CProgram_updater_state.instance_inaccessible))
                            .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_is_offline))
                            .add(Expr.isNull("state"))
                        .endJunction()
                        .setFirstRow(page)
                        .setMaxRows(page_size)
                            .findPagedList();


                List<Swagger_UpdatePlan_brief_for_homer> tasks = new ArrayList<>();

                for (Model_CProgramUpdatePlan plan : paging_list.getList()) {
                    tasks.add(plan.get_brief_for_update_homer_server());
                }


                if (!tasks.isEmpty()) {

                    WS_Message_Hardware_UpdateProcedure_Command result = Model_HomerServer.getById(homer.id).update_devices_firmware(tasks);
                    if (!result.status.equals("success")) {
                        terminal_logger.internalServerError(new Exception("Result status was not 'success'"));
                    }
                }

                // Pokud je počet stránek shodný
                if ( paging_list.getTotalPageCount() == page) break;

                page++;

            }
        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }
}