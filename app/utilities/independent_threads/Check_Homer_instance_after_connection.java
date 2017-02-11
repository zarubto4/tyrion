package utilities.independent_threads;

import com.fasterxml.jackson.databind.JsonNode;
import models.project.b_program.instnace.Model_HomerInstance;
import models.project.b_program.servers.Model_HomerServer;
import utilities.web_socket.WS_HomerServer;

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

                    JsonNode result = model_server.get_homer_server_listOfInstance();
                    if (!result.get("status").asText().equals("success")) {interrupt();}

                    // Vylistuji si seznam instnancí, které běží na serveru
                    List<String> instances_on_server = new ArrayList<>();
                    final JsonNode arrNode = result.get("instances");
                    for (final JsonNode objNode : arrNode) instances_on_server.add(objNode.asText());
                    logger.trace("Homer Server:: Connection:: Number of instances on cloud_blocko_server: " + instances_on_server.size());


                    // Vylistuji si seznam instnancí, které by měli běžet na serveru

                    List<Model_HomerInstance> instances_in_database_for_uploud = new ArrayList<>();

                    // Přidám všechny reálné instance, které mají běžet.
                    instances_in_database_for_uploud.addAll( Model_HomerInstance.find.where().eq("cloud_homer_server.unique_identificator", model_server.unique_identificator).eq("virtual_instance", false).isNotNull("actual_instance").select("blocko_instance_name").findList());

                    // Přidám všechny virtuální instance, kde je ještě alespoň jeden Yoda
                    instances_in_database_for_uploud.addAll( Model_HomerInstance.find.where().eq("cloud_homer_server.unique_identificator", model_server.unique_identificator).eq("virtual_instance", true).isNotNull("boards_in_virtual_instance").select("blocko_instance_name").findList());


                    List<String> instances_for_removing = new ArrayList<>();

                    // Vytvořím kopii seznamu instancí, které by měli běžet na Homer Serveru
                    for(String  identificator : instances_on_server){

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
                            logger.warn("Blocko Server: removing instnace:: ", identificator);
                            instances_for_removing.add(identificator);
                        }
                    }

                    logger.debug("Blocko Server: The number of instances for removing from homer server: ");


                    if (!instances_for_removing.isEmpty()) {
                        for (String identificator : instances_for_removing) {
                            JsonNode remove_result = model_server.remove_instance(identificator);
                            if(!remove_result.has("status") || !remove_result.get("status").asText().equals("success"))   logger.error("Blocko Server: Removing instance Error: ", remove_result.toString());
                        }
                    }


                    // Nahraji tam ty co tam patří
                    logger.trace("Homer Server:: Connection::Starting to uploud new instances to cloud_blocko_server");
                    for (Model_HomerInstance instance : instances_in_database_for_uploud) {

                        if(instances_on_server.contains(instance.blocko_instance_name)){
                            logger.debug("Homer Server:: Connection:: ", instance.blocko_instance_name , " is on server already");
                        }else {
                            JsonNode add_instance = instance.add_instance_to_server();
                            logger.debug("add_instance: " + add_instance.toString());

                            if (add_instance.get("status").asText().equals("success")) {
                                logger.trace("Blocko Server: Uploud instance was successful");
                            }
                            else if (add_instance.get("status").asText().equals("error")) {
                                logger.warn("Blocko Server: Fail when Tyrion try to add instance from Blocko cloud_blocko_server:: ", add_instance.toString());
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
