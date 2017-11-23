package utilities.independent_threads.homer_server;

import com.avaje.ebean.Expr;
import com.avaje.ebean.PagedList;
import models.Model_CProgramUpdatePlan;
import models.Model_HomerServer;
import utilities.enums.Enum_CProgram_updater_state;
import utilities.logger.Class_Logger;
import utilities.swagger.outboundClass.Swagger_UpdatePlan_brief_for_homer;
import web_socket.message_objects.homer_hardware_with_tyrion.updates.WS_Message_Hardware_UpdateProcedure_Command;
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

            terminal_logger.debug("5. Spouštím Sycnhronizační proceduru Synchronize_Homer_Unresolved_Updates");

            int page = 0;
            int page_size = 100;

            while (true) {

                PagedList<Model_CProgramUpdatePlan> paging_list = Model_CProgramUpdatePlan.find.where()
                            .eq("board.connected_server_id", ws_homerServer.identifikator)
                            .disjunction()
                                .add(Expr.eq("state", Enum_CProgram_updater_state.in_progress))
                                .add(Expr.eq("state", Enum_CProgram_updater_state.not_updated))
                                .add(Expr.eq("state", Enum_CProgram_updater_state.not_start_yet))
                                .add(Expr.eq("state", Enum_CProgram_updater_state.waiting_for_device))
                                .add(Expr.eq("state", Enum_CProgram_updater_state.instance_inaccessible))
                                .add(Expr.eq("state", Enum_CProgram_updater_state.homer_server_is_offline))
                                .add(Expr.isNull("state"))
                            .endJunction()
                            .findPagedList(page, page_size);


                List<Swagger_UpdatePlan_brief_for_homer> tasks = new ArrayList<>();

                for (Model_CProgramUpdatePlan plan : paging_list.getList()) {
                    tasks.add(plan.get_brief_for_update_homer_server());
                }


                if(!tasks.isEmpty()){

                    WS_Message_Hardware_UpdateProcedure_Command result = Model_HomerServer.get_byId(ws_homerServer.identifikator).update_devices_firmware(tasks);
                    if(!result.status.equals("success")){
                        terminal_logger.internalServerError(new Exception("Result status was not 'success'"));
                    }
                }

                // Pokud je počet stránek shodný
                if( paging_list.getTotalPageCount() == page) break;

                page++;

            }
        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }
    }
}