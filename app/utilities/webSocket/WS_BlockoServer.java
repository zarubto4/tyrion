package utilities.webSocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.WebSocketController;
import models.project.b_program.servers.Cloud_Homer_Server;
import utilities.hardware_updater.Actualization_Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WS_BlockoServer extends WebSCType{

    public Map<String, WebSCType> virtual_homers = new HashMap<>();  // Zde si udržuju referenci na tímto serverem vytvořené virtuální homery, které jsem dal do globální mapy (incomingConnections_homers) - určeno pro vývojáře
    public Cloud_Homer_Server server;

    public WS_BlockoServer(Cloud_Homer_Server server, Map<String, WebSCType> blocko_servers) {
        super();
        super.identifikator = server.server_name;
        super.maps = blocko_servers;
        super.webSCtype = this;
        this.server = server;
        this.update_thread.start();
    }


    @Override
    public void onClose() {

        logger.warn("Blocko server disconnected - Starting cancaled procedure with virtual Homers");

        for (Map.Entry<String, WebSCType> entry : virtual_homers.entrySet())
        {
            logger.debug("Killing virtual instance: " +entry.getKey());
            entry.getValue().onClose();
        }

        this.update_thread.stop();
        WebSocketController.blocko_servers.remove(super.identifikator);
        server.is_disconnect();
    }

    @Override
    public void onMessage(ObjectNode json) {

        // Zpráva je z virtuální instance
        if(json.has ("instanceId") ){
            try {
                WebSCType ws = virtual_homers.get(json.get("instanceId").asText());
                ws.onMessage(json);
            }catch (NullPointerException e){
                if(! virtual_homers.containsKey (json.get("instanceId").asText())){
                    logger.warn("Something is wrong. Message from Instance, which not created by Tyrion!");
                    logger.warn("Message is interrupt!");
                }
            }
        }

        // Zpráva je ze serveru
        else {
            WebSocketController.homer_server_incoming_message(this, json);
        }

    }


// Aktualizační procedury ---------------------------------------------------------------------------------------------


    private ArrayList<Actualization_Task> task_list = new ArrayList<>();

    public void add_task(Actualization_Task task){


        System.out.println("Server převzal Task ");
        task_list.add(task);


        if(update_thread.getState() == Thread.State.TIMED_WAITING) {
            System.out.println("Vlákno v BLocko Server trvale spí a proto ho probudím");

            update_thread.interrupt();
        }
    }

    private Thread update_thread = new Thread() {

        @Override
        public void run() {
            while(true){
                try {

                    if (!task_list.isEmpty()) {

                        System.out.println("Počet zařízení k aktualizaci ještě: " + task_list.size());

                        Actualization_Task task = task_list.get(0);


                        System.out.println("Odesílám požadavek na aktualizaci!");

                        if(task.instance != null){
                            JsonNode result = task.instance.actual_instance.update_devices_firmware(task.actualization_procedure_id, task.get_ids(), task.firmware_type, task.file_record);
                            System.out.println("Odpověď na Aktualizaci:" + result.toString());
                            System.out.println("Ještě neřeším reakci");
                            task_list.remove(task);
                        }
                        else {

                           System.err.println("Homer Neexistuje!!!");
                        }

                    } else {
                            System.out.println("Ukládám vlákno v Blocko serveru k trvalému spánku");
                            sleep(500000000);

                    }
                }catch (InterruptedException i){
                    // Do nothing
                }catch (Exception e){
                    logger.error("Master Updater Error", e);
                }
            }
        }
    };


}
