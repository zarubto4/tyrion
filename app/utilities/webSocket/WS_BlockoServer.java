package utilities.webSocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.WebSocketController_Incoming;
import models.project.b_program.Homer_Instance;
import models.project.b_program.servers.Cloud_Homer_Server;
import utilities.hardware_updater.Actualization_Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WS_BlockoServer extends WebSCType{

    // Zde si udržuju referenci na tímto serverem vytvořené virtuální homery, které jsem dal do globální mapy (incomingConnections_homers)
    public Map<String, WebSCType> virtual_homers = new HashMap<>();
    private WS_BlockoServer this_server;

    public WS_BlockoServer(String server_name, Map<String, WebSCType> blocko_servers) {
        super();
        super.identifikator = server_name;
        super.maps = blocko_servers;
        super.webSCtype = this;
        this_server = this;
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
        WebSocketController_Incoming.blocko_server_is_disconnect(this);
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
            WebSocketController_Incoming.blocko_server_incoming_message(this, json);
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

                        if(task.homer != null){
                            JsonNode result = WebSocketController_Incoming.homer_update_Yoda_firmware(task.homer, task.board.id, task.firmware_type, task.code);
                            System.out.println("Odpověď na Aktualizaci:" + result.toString());
                            System.out.println("Ještě neřeším reakci");
                            task_list.remove(task);
                        }
                        else {

                            try {
                                System.out.println("Homer ještě neexistuje a tak je ho nutné vytvořit");

                                Homer_Instance temporary_instance = new Homer_Instance();
                                temporary_instance.setUnique_blocko_instance_name();
                                temporary_instance.cloud_homer_server = Cloud_Homer_Server.find.where().eq("server_name", this_server.identifikator).findUnique();
                                temporary_instance.private_instance_board = task.board;
                                temporary_instance.project = task.board.project;
                                temporary_instance.save();

                                task.board.refresh();
                                task.board.private_instance = temporary_instance;
                                task.board.update();

                                WS_Homer_Cloud homer = (WS_Homer_Cloud) WebSocketController_Incoming.blocko_server_add_instance(this_server, temporary_instance, true );


                                JsonNode result = WebSocketController_Incoming.homer_update_Yoda_firmware(homer, task.board.id, task.firmware_type, task.code);
                                System.out.println("Odpověď na Aktualizaci:" + result.toString());

                                task_list.remove(task);

                            }catch (Exception e){
                                e.printStackTrace();
                                task_list.remove(task);
                            }

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
