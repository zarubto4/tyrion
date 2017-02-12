package utilities.independent_threads;

import models.project.b_program.instnace.Model_HomerInstance;
import models.project.b_program.servers.Model_HomerServer;
import utilities.web_socket.WS_HomerServer;
import utilities.web_socket.message_objects.homer_instance.WS_Update_device_summary_collection;
import utilities.web_socket.message_objects.homer_tyrion.WS_Destroy_instance;
import utilities.web_socket.message_objects.homer_tyrion.WS_Get_instance_list;

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

                    logger.trace("Homer Server:: Connection::  Tyrion send to Homer Server request for listInstances");

                    WS_Get_instance_list list_instances = model_server.get_homer_server_listOfInstance();


                    // Vylistuji si seznam instnancí, které by měli běžet na serveru

                    List<Model_HomerInstance> instances_in_database_for_uploud = new ArrayList<>();

                    // Přidám všechny reálné instance, které mají běžet.
                    instances_in_database_for_uploud.addAll( Model_HomerInstance.find.where().eq("cloud_homer_server.unique_identificator", model_server.unique_identificator).eq("virtual_instance", false).isNotNull("actual_instance").select("blocko_instance_name").findList());

                    // Přidám všechny virtuální instance, kde je ještě alespoň jeden Yoda
                    instances_in_database_for_uploud.addAll( Model_HomerInstance.find.where().eq("cloud_homer_server.unique_identificator", model_server.unique_identificator).eq("virtual_instance", true).isNotNull("boards_in_virtual_instance").select("blocko_instance_name").findList());

                    logger.debug("Blocko Server: The number of instances that would have run on the server:: " + instances_in_database_for_uploud.size());

                    List<String> instances_for_removing = new ArrayList<>();

                    // Vytvořím kopii seznamu instancí, které by měli běžet na Homer Serveru
                    for(String  identificator : list_instances.instances){

                        // NAjdu jestli instance má oprávnění být nazasená podle parametrů nasaditelné instnace
                        Integer size = Model_HomerInstance.find.where().eq("blocko_instance_name", identificator)
                                .disjunction()
                                .conjunction()
                                .eq("virtual_instance", false)
                                .isNotNull("actual_instance")
                                .endJunction()
                                .conjunction()
                                .eq("virtual_instance", true)
                                .isNotNull("boards_in_virtual_instance")
                                .endJunction()
                                .endJunction().findRowCount();

                        if(size < 1){
                            logger.warn("Blocko Server: removing instance:: ", identificator);
                            instances_for_removing.add(identificator);
                        }
                    }

                    logger.debug("Blocko Server: The number of instances for removing from homer server:: " + instances_for_removing.size());

                    if (!instances_for_removing.isEmpty()) {
                        for (String identificator : instances_for_removing) {
                            WS_Destroy_instance remove_result = model_server.remove_instance(identificator);
                            if(!remove_result.status.equals("success"))   logger.error("Blocko Server: Removing instance Error: "+ remove_result.toString());
                        }
                    }


                    // Nahraji tam ty co tam patří
                    logger.debug("Homer Server:: Connection::Starting to uploud new instances to cloud_blocko_server" + instances_in_database_for_uploud.size());

                    for (Model_HomerInstance instance : instances_in_database_for_uploud) {

                        logger.debug("Homer Server:: Connection:: Procedure for " + instance.blocko_instance_name);

                        if(list_instances.instances.contains(instance.blocko_instance_name)){
                            logger.debug("Homer Server:: Connection:: " + instance.blocko_instance_name + " is on server already");

                        }else {

                            if(instance.virtual_instance){
                                logger.debug("Homer Server:: Connection:: " + instance.blocko_instance_name + " its Virtual instance");
                                if(instance.getBoards_in_virtual_instance().size() == 0) {
                                    logger.debug("Homer Server:: Connection:: " + instance.blocko_instance_name + " its Virtual instance and is empty - for cycle continue");
                                    continue;
                                }
                            }

                            logger.debug("Homer Server:: Connection:: "+   instance.blocko_instance_name +" add instance to server");
                            WS_Update_device_summary_collection add_instance = instance.add_instance_to_server();

                            if (add_instance.status.equals("success")) {
                                logger.trace("Blocko Server: Upload instance was successful");
                            }
                            else if (add_instance.status.equals("error")) {
                                logger.warn("Blocko Server: Fail when Tyrion try to add instance from Blocko cloud_blocko_server:: "+ add_instance.toString());
                            }

                            sleep(50); // Abych Homer server tolik nevytížil
                        }
                    }


                    logger.debug("Blocko Server: Successfully finished connection procedure");
                    break;

                }
            }

            model_server.check_HW_updates_on_server();

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
