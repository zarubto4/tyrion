package utilities.threads.homer_server;

import io.ebean.Expr;
import io.ebean.PagedList;
import models.Model_HardwareUpdate;
import models.Model_HomerServer;
import play.libs.Json;
import play.mvc.BodyParser;
import utilities.enums.HardwareUpdateState;
import utilities.logger.Logger;
import utilities.swagger.output.Swagger_UpdatePlan_brief_for_homer;
import websocket.interfaces.WS_Homer;
import websocket.messages.homer_hardware_with_tyrion.updates.WS_Message_Hardware_UpdateProcedure_Command;

import java.util.ArrayList;
import java.util.List;

public class Synchronize_Homer_Unresolved_Updates extends Thread {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Synchronize_Homer_Unresolved_Updates.class);

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    private WS_Homer homer = null;

    // Umožněno kontrolovat COmpilator i Homer server
    public Synchronize_Homer_Unresolved_Updates(WS_Homer homer) {
        this.homer = homer;
    }


    @Override
    public void run() {

        logger.debug("Synchronize_Homer_Unresolved_Updates:: Independent Thread under server:: " + homer.id + " started");

        try {

            logger.debug("5. Spouštím Sycnhronizační proceduru Synchronize_Homer_Unresolved_Updates");

            int page = 0;
            int page_size = 100;

            while (true) {

                PagedList<Model_HardwareUpdate> paging_list = Model_HardwareUpdate.find.query().where()
                        .eq("hardware.connected_server_id", homer.id)
                        .disjunction()
                            .add(Expr.eq("state", HardwareUpdateState.IN_PROGRESS))
                            .add(Expr.eq("state", HardwareUpdateState.NOT_UPDATED))
                            .add(Expr.eq("state", HardwareUpdateState.NOT_YET_STARTED))
                            .add(Expr.eq("state", HardwareUpdateState.HOMER_SERVER_IS_OFFLINE))
                            .add(Expr.isNull("state"))
                        .endJunction()
                        .ne("deleted", true)
                        .setFirstRow(page)
                        .setMaxRows(page_size)
                            .findPagedList();


                List<Swagger_UpdatePlan_brief_for_homer> tasks = new ArrayList<>();

                for (Model_HardwareUpdate plan : paging_list.getList()) {
                    tasks.add(plan.get_brief_for_update_homer_server());
                }


                if (!tasks.isEmpty()) {

                    WS_Message_Hardware_UpdateProcedure_Command result = Model_HomerServer.find.byId(homer.id).update_devices_firmware(tasks);
                    if (result.status.equals("success")) {
                        // Nothing
                    } else {
                        logger.error("Result status was not 'success':: result: {}", Json.toJson(result));
                    }

                }

                // Pokud je počet stránek shodný
                if ( paging_list.getTotalPageCount() == page) break;

                page++;

            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }
}