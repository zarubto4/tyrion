package utilities.independent_threads;

import models.Model_HomerInstance;
import models.Model_HomerServer;
import utilities.enums.Enum_Homer_instance_type;
import web_socket.services.WS_HomerServer;
import web_socket.message_objects.homer_instance.WS_Message_Update_device_summary_collection;
import web_socket.message_objects.homerServer_with_tyrion.WS_Message_Destroy_instance;
import web_socket.message_objects.homerServer_with_tyrion.WS_Message_Get_instance_list;

import java.util.ArrayList;
import java.util.List;

public class Check_Homer_instance_after_connection extends Thread {


    // Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    WS_HomerServer ws_homerServer = null;
    Model_HomerServer model_server = null;

    // Umožněno kontrolovat COmpilator i Homer server
    public Check_Homer_instance_after_connection(WS_HomerServer ws_homerServer, Model_HomerServer model_server){
        this.ws_homerServer = ws_homerServer;
        this.model_server = model_server;
    }


    @Override
    public void run(){
        Long interrupter = (long) 6000;
        try {

            while (interrupter > 0) {

                sleep(1000);
                interrupter -= 500;

                if (ws_homerServer.isReady()){

                    logger.trace("Check_Homer_instance_after_connection:: run:: Tyrion send to Homer Server request for listInstances");

                    WS_Message_Get_instance_list list_instances = model_server.get_homer_server_listOfInstance();


                    // Vylistuji si seznam instnancí, které by měli běžet na serveru

                    List<Model_HomerInstance> instances_in_database_for_uploud = new ArrayList<>();

                    // Přidám všechny reálné instance, které mají běžet.
                    instances_in_database_for_uploud.addAll( Model_HomerInstance.find.where().eq("cloud_homer_server.unique_identificator", model_server.unique_identificator).eq("instance_type", Enum_Homer_instance_type.INDIVIDUAL).isNotNull("actual_instance").select("blocko_instance_name").findList());

                    // Přidám všechny virtuální instance, kde je ještě alespoň jeden Yoda
                    instances_in_database_for_uploud.addAll( Model_HomerInstance.find.where().eq("cloud_homer_server.unique_identificator", model_server.unique_identificator).eq("instance_type",  Enum_Homer_instance_type.VIRTUAL).isNotNull("boards_in_virtual_instance").select("blocko_instance_name").findList());

                    logger.trace("Check_Homer_instance_after_connection:: run::  The number of instances that would have run on the server:: " + instances_in_database_for_uploud.size());

                    List<String> instances_for_removing = new ArrayList<>();

                    // Vytvořím kopii seznamu instancí, které by měli běžet na Homer Serveru
                    for(String  identificator : list_instances.instances){

                        // NAjdu jestli instance má oprávnění být nazasená podle parametrů nasaditelné instnace
                        Integer size = Model_HomerInstance.find.where().eq("blocko_instance_name", identificator)
                                .disjunction()
                                    .conjunction()
                                        .eq("instance_type", Enum_Homer_instance_type.INDIVIDUAL)
                                        .isNotNull("actual_instance")
                                    .endJunction()
                                    .conjunction()
                                        .eq("instance_type", Enum_Homer_instance_type.VIRTUAL)
                                        .isNotNull("boards_in_virtual_instance")
                                    .endJunction()
                                .endJunction().findRowCount();

                        if(size < 1){
                            logger.warn("Blocko Server: removing instance:: ", identificator);
                            instances_for_removing.add(identificator);
                        }
                    }

                    logger.trace("Check_Homer_instance_after_connection:: run::  The number of instances for removing from homer server:: " + instances_for_removing.size());

                    if (!instances_for_removing.isEmpty()) {
                        for (String identificator : instances_for_removing) {
                            WS_Message_Destroy_instance remove_result = model_server.remove_instance(identificator);
                            if(!remove_result.status.equals("success"))   logger.error("Blocko Server: Removing instance Error: "+ remove_result.toString());
                        }
                    }


                    // Nahraji tam ty co tam patří
                    logger.trace("Check_Homer_instance_after_connection:: run:: Connection::Starting to uploud new instances to cloud_blocko_server" + instances_in_database_for_uploud.size());

                    for (Model_HomerInstance instance : instances_in_database_for_uploud) {

                        logger.debug("Check_Homer_instance_after_connection:: run::  Connection:: Procedure for " + instance.blocko_instance_name);

                        if(list_instances.instances.contains(instance.blocko_instance_name)){
                            logger.trace("Check_Homer_instance_after_connection:: run::  " + instance.blocko_instance_name + " is on server already");
                        }else {

                            if(instance.instance_type == Enum_Homer_instance_type.VIRTUAL){
                                logger.trace("Check_Homer_instance_after_connection:: run:: Instance:: " + instance.blocko_instance_name + " its Virtual instance");
                                if(instance.getBoards_in_virtual_instance().size() == 0) {
                                    logger.debug("Check_Homer_instance_after_connection:: run:: Instance " + instance.blocko_instance_name + " its Virtual instance and is empty - for cycle continue");
                                    continue;
                                }
                            }

                            logger.trace("Check_Homer_instance_after_connection:: run:: "+   instance.blocko_instance_name +" add instance to server");
                            WS_Message_Update_device_summary_collection add_instance = instance.add_instance_to_server();

                            if (add_instance.status.equals("success")) {
                                logger.trace("Check_Homer_instance_after_connection:: run::Upload instance was successful");
                            }
                            else if (add_instance.status.equals("error")) {
                                logger.warn("Check_Homer_instance_after_connection:: run:: Fail when Tyrion try to add instance from Blocko cloud_blocko_server:: "+ add_instance.toString());
                            }

                            sleep(50); // Abych Homer server tolik nevytížil
                        }
                    }


                    logger.debug("Check_Homer_instance_after_connection:: run:: Successfully finished connection procedure");
                    break;

                }
            }

            model_server.check_HW_updates_on_server();

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
