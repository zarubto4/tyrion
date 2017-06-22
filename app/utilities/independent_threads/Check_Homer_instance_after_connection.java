package utilities.independent_threads;

import models.Model_HomerInstance;
import models.Model_HomerServer;
import utilities.enums.Enum_Homer_instance_type;
import utilities.logger.Class_Logger;
import web_socket.services.WS_HomerServer;
import web_socket.message_objects.homer_instance.WS_Message_Update_device_summary_collection;
import web_socket.message_objects.homerServer_with_tyrion.WS_Message_Destroy_instance;
import web_socket.message_objects.homerServer_with_tyrion.WS_Message_Get_instance_list;

import java.util.ArrayList;
import java.util.List;

public class Check_Homer_instance_after_connection extends Thread {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Check_Update_for_hw_on_homer.class);

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    WS_HomerServer ws_homerServer = null;
    Model_HomerServer model_server = null;

    // Umožněno kontrolovat COmpilator i Homer server
    public Check_Homer_instance_after_connection(WS_HomerServer ws_homerServer, Model_HomerServer model_server){
        this.ws_homerServer = ws_homerServer;
        this.model_server = model_server;
    }


    @Override
    public void run(){

        Long interrupter = (long) 15000;

        try {

            while (interrupter > 0) {

                try {

                    sleep(3000);
                    interrupter -= 3000;

                    if (ws_homerServer.isReady()) {

                        terminal_logger.info("Check_Homer_instance_after_connection:: run:: Tyrion send to Homer Server request for listInstances");

                        WS_Message_Get_instance_list list_instances = model_server.get_homer_server_listOfInstance();


                        // Vylistuji si seznam instnancí, které by měli běžet na serveru

                        List<Model_HomerInstance> instances_in_database_for_uploud = new ArrayList<>();

                        // Přidám všechny reálné instance, které mají běžet.
                        instances_in_database_for_uploud.addAll(Model_HomerInstance.find.where().eq("cloud_homer_server.unique_identificator", model_server.unique_identificator).ne("removed_by_user", true).eq("instance_type", Enum_Homer_instance_type.INDIVIDUAL).isNotNull("actual_instance").select("id").findList());

                        // Přidám všechny virtuální instance, kde je ještě alespoň jeden Yoda
                        instances_in_database_for_uploud.addAll(Model_HomerInstance.find.where().eq("cloud_homer_server.unique_identificator", model_server.unique_identificator).ne("removed_by_user", true).eq("instance_type", Enum_Homer_instance_type.VIRTUAL).isNotNull("boards_in_virtual_instance").select("id").findList());

                        terminal_logger.trace("Check_Homer_instance_after_connection:: run::  The number of instances that would have run on the server::  {} ",instances_in_database_for_uploud.size());

                        List<String> instances_for_removing = new ArrayList<>();

                        // Vytvořím kopii seznamu instancí, které by měli běžet na Homer Serveru
                        for (String identificator : list_instances.instances) {

                            // NAjdu jestli instance má oprávnění být nazasená podle parametrů nasaditelné instnace
                            Integer size = Model_HomerInstance.find.where().eq("id", identificator)
                                    .disjunction()
                                    .conjunction()
                                    .eq("instance_type", Enum_Homer_instance_type.INDIVIDUAL)
                                    .isNotNull("actual_instance")
                                    .endJunction()
                                    .conjunction()
                                    .eq("instance_type", Enum_Homer_instance_type.VIRTUAL)
                                    .isNotNull("boards_in_virtual_instance")
                                    .endJunction()
                                    .endJunction().ne("removed_by_user", true).findRowCount();

                            if (size < 1) {
                                terminal_logger.warn("Blocko Server: removing instance:: {} ", identificator);
                                instances_for_removing.add(identificator);
                            }
                        }

                        terminal_logger.trace("Check_Homer_instance_after_connection:: run::  The number of instances for removing from homer server:: {}" , instances_for_removing.size());

                        if (!instances_for_removing.isEmpty()) {
                            for (String identificator : instances_for_removing) {
                                WS_Message_Destroy_instance remove_result = model_server.remove_instance(identificator);
                                if (!remove_result.status.equals("success"))
                                    terminal_logger.error("Blocko Server: Removing instance Error: " + remove_result.toString());
                            }
                        }


                        // Nahraji tam ty co tam patří
                        terminal_logger.trace("Check_Homer_instance_after_connection:: run:: Connection::Starting to uploud new instances to cloud_blocko_server {}" , instances_in_database_for_uploud.size());

                        for (Model_HomerInstance instance : instances_in_database_for_uploud) {

                            terminal_logger.trace("Check_Homer_instance_after_connection:: run::  Connection:: Procedure for {}" ,  instance.id);

                            if (list_instances.instances.contains(instance.id)) {
                                terminal_logger.trace("Check_Homer_instance_after_connection:: run:: {} is on server already", instance.id);
                            } else {

                                if (instance.instance_type == Enum_Homer_instance_type.VIRTUAL) {
                                    terminal_logger.trace("Check_Homer_instance_after_connection:: run:: Instance:: {} its Virtual instance", instance.id);
                                    if (instance.getBoards_in_virtual_instance().size() == 0) {
                                        terminal_logger.trace("Check_Homer_instance_after_connection:: run:: Instance {} its Virtual instance and is empty - for cycle continue", instance.id);
                                        continue;
                                    }
                                }

                                terminal_logger.trace("Check_Homer_instance_after_connection:: run:: {} add instance to server",  instance.id);
                                WS_Message_Update_device_summary_collection add_instance = instance.add_instance_to_server();

                                if (add_instance.status.equals("success")) {
                                    terminal_logger.trace("Check_Homer_instance_after_connection:: run::Upload instance was successful");
                                } else if (add_instance.status.equals("error")) {
                                    terminal_logger.warn("Check_Homer_instance_after_connection:: run:: Fail when Tyrion try to add instance from Blocko cloud_blocko_server:: {} " , add_instance.toString());
                                }

                                sleep(50); // Abych Homer server tolik nevytížil
                            }
                        }

                        terminal_logger.trace("Check_Homer_instance_after_connection:: run:: Successfully finished connection procedure");
                        break;

                    }

                } catch (Exception e) {
                    terminal_logger.internalServerError("run:", e);

                }
            }

            model_server.synchronize_all_device_state_with_cache();

        }catch(Exception e){
            terminal_logger.internalServerError("run:", e);
        }
    }
}
