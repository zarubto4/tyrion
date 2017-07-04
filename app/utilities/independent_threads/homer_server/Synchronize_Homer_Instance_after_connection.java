package utilities.independent_threads.homer_server;

import models.Model_HomerInstance;
import models.Model_HomerServer;
import utilities.enums.Enum_Homer_instance_type;
import utilities.logger.Class_Logger;
import web_socket.message_objects.homer_with_tyrion.WS_Message_Homer_Instance_destroy;
import web_socket.message_objects.homer_with_tyrion.WS_Message_Homer_Instance_list;
import web_socket.services.WS_HomerServer;

import java.util.ArrayList;
import java.util.List;

public class Synchronize_Homer_Instance_after_connection extends Thread {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Synchronize_Homer_Unresolved_Updates.class);

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    WS_HomerServer ws_homerServer = null;

    // Umožněno kontrolovat COmpilator i Homer server
    public Synchronize_Homer_Instance_after_connection(WS_HomerServer ws_homerServer){
        this.ws_homerServer = ws_homerServer;
    }

    @Override
    public void run(){

        try {


            terminal_logger.info("Synchronize_Homer_Instance_after_connection:: run:: Tyrion send to Homer Server request for listInstances");

            List<String> instances_required_by_tyrion = required_instance_on_server();
            List<String> instances_actual_on_server = actual_on_server();

            List<String> instances_for_removing = new ArrayList<>();
            List<String> instances_for_add = new ArrayList<>();



            for(String instance_id : instances_required_by_tyrion){
                if(!instances_actual_on_server.contains(instance_id))instances_for_add.add(instance_id);
            }

            for(String instance_id : instances_actual_on_server){
                if(!instances_required_by_tyrion.contains(instance_id))instances_for_removing.add(instance_id);
            }


            if (!instances_for_removing.isEmpty()) {

                terminal_logger.trace("Synchronize_Homer_Instance_after_connection:: run::  The number of instance_ids for removing from homer server:: {}" , instances_for_removing.size());

                WS_Message_Homer_Instance_destroy remove_result  = Model_HomerServer.get_byId(ws_homerServer.identifikator).remove_instance(instances_for_removing);
                if (!remove_result.status.equals("success")){
                    terminal_logger.internalServerError(new Exception("Blocko Server: Error while removing instances: " + remove_result.toString()));
                }

            }

            if(!instances_for_add.isEmpty()){

                terminal_logger.trace("Synchronize_Homer_Instance_after_connection:: run:: Connection::Starting to upload new instance_ids to cloud_blocko_server. Size: {}" , instances_for_add.size());

                for (String instance_id : instances_for_add) {
                    try {
                        Model_HomerInstance.get_byId(instance_id).upload_to_cloud();
                    }catch (Exception e) {
                        terminal_logger.internalServerError(e);
                    }
                }
            }

            terminal_logger.trace("Synchronize_Homer_Instance_after_connection:: run:: Successfully finished connection procedure");


        }catch(Exception e){
            terminal_logger.internalServerError(e);
        }
    }

    private List<String> required_instance_on_server(){

        // Vylistuji si seznam instnancí, které by měli běžet na serveru
        List<Model_HomerInstance> instances_in_database_for_uploud = new ArrayList<>();

        // Přidám všechny reálné instance, které mají běžet.
        instances_in_database_for_uploud.addAll(
                Model_HomerInstance.find.where()
                        .eq("cloud_homer_server.unique_identificator", ws_homerServer.identifikator)
                        .ne("removed_by_user", true)
                        .eq("instance_type", Enum_Homer_instance_type.INDIVIDUAL)
                        .isNotNull("actual_instance")
                        .select("id")
                        .findList());


        List<String> instance_ids = new ArrayList<>();

        for(Model_HomerInstance homerInstance : instances_in_database_for_uploud){
            instance_ids.add(homerInstance.id);
        }

        return instance_ids;

    }

    private List<String> actual_on_server(){

        WS_Message_Homer_Instance_list list_instances = Model_HomerServer.get_byId(ws_homerServer.identifikator).get_homer_server_list_od_instance();
        return list_instances.instance_ids;

    }

}
