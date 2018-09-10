package utilities.threads.homer_server;

import models.Model_Instance;
import models.Model_HomerServer;
import utilities.logger.Logger;
import websocket.interfaces.WS_Homer;
import websocket.messages.homer_with_tyrion.WS_Message_Homer_Instance_destroy;
import websocket.messages.homer_with_tyrion.WS_Message_Homer_Instance_list;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Synchronize_Homer_Instance_after_connection extends Thread {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Synchronize_Homer_Instance_after_connection.class);

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    WS_Homer homer = null;

    // Umožněno kontrolovat COmpilator i Homer server
    public Synchronize_Homer_Instance_after_connection(WS_Homer homer) {
        this.homer = homer;
    }

    @Override
    public void run() {
        try {

            logger.info("Synchronize_Homer_Instance_after_connection:: run:: Tyrion send to Homer Server request for listInstances");

            List<UUID> instances_required_by_tyrion = required_instance_on_server();
            List<UUID> instances_actual_on_server = actual_on_server();

            List<UUID> instances_for_removing = new ArrayList<>();
            List<UUID> instances_for_add = new ArrayList<>();

            for (UUID instance_id : instances_required_by_tyrion) {
                if (!instances_actual_on_server.contains(instance_id)) {
                    instances_for_add.add(instance_id);
                }
            }

            for (UUID instance_id : instances_actual_on_server) {
                if (!instances_required_by_tyrion.contains(instance_id)) {
                    instances_for_removing.add(instance_id);
                }
            }

            if (!instances_for_removing.isEmpty()) {

                logger.trace("Synchronize_Homer_Instance_after_connection:: run::  The number of instance_ids for removing from homer server:: {}" , instances_for_removing.size());
                WS_Message_Homer_Instance_destroy remove_result  = Model_HomerServer.find.byId(homer.id).remove_instance(instances_for_removing);
                if (!remove_result.status.equals("success")) {
                    logger.internalServerError(new Exception("Blocko Server: Error while removing instances: " + remove_result.toString()));
                }
            }

            if (!instances_for_add.isEmpty()) {
                logger.trace("Synchronize_Homer_Instance_after_connection:: run:: Connection::Starting to upload new instance_ids to cloud_blocko_server. Size: {}" , instances_for_add.size());

                for (UUID instance_id : instances_for_add) {
                    try {

                        Model_Instance.find.byId(instance_id).deploy();
                        sleep(50);

                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }
            }

            logger.trace("Synchronize_Homer_Instance_after_connection:: run:: Successfully finished connection procedure");

        } catch (InterruptedException e) {
            logger.warn("Synchronize_Homer_Instance_after_connection:: run:: Connection Error: TimeOutException - Tyrion close connection");

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    private List<UUID> required_instance_on_server() {

        // Vylistuji si seznam instnancí, které by měli běžet na serveru

        // Přidám všechny reálné instance, které mají běžet.
        return Model_Instance.find.query().where()
                .eq("server_main.id", homer.id)
                .eq("deleted", false)
                .isNotNull("current_snapshot_id")
                .select("id")
                .findSingleAttributeList();

    }

    private List<UUID> actual_on_server() throws InterruptedException{
        try {

            WS_Message_Homer_Instance_list list_instances = Model_HomerServer.find.byId(homer.id).get_homer_server_list_of_instance();
            return list_instances.instance_ids;

        } catch (Exception e) {
            logger.warn("Homer server not response for Get WS_Message_Homer_Instance_list");
            homer.close();
            throw new InterruptedException();
        }
    }
}
