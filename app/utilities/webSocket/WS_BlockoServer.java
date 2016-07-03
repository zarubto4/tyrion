package utilities.webSocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.WebSocketController_Incoming;
import utilities.hardware_updater.Actualization_Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

        System.out.println("Server se odpojil a tak je nutné zabít všechny jeho instnace Homerů v globální mapě");
        for (Map.Entry<String, WebSCType> entry : virtual_homers.entrySet())
        {
            System.out.println("Zabíjím virtuální instanci " +entry.getKey());
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
                 logger.warn("Komunikuje se mnou instance, která nebyla vytvořená Tyrionem");
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
                            JsonNode result = WebSocketController_Incoming.homer_update_Yoda_firmware(task.homer, task.code);
                            System.out.println("Odpověď na Aktualizaci:" + result.toString());
                            System.out.println("Ještě neřeším reakci");
                            task_list.remove(task);
                        }
                        else {

                            try {
                                System.out.println("Homer ještě neexistuje a tak je ho nutné vytvořit");

                                WS_Homer_Cloud homer = (WS_Homer_Cloud) WebSocketController_Incoming.blocko_server_add_fake_instance(this_server, UUID.randomUUID().toString(),  task.device_ids );
                                JsonNode result = WebSocketController_Incoming.homer_update_Yoda_firmware(homer, task.code);
                                System.out.println("Odpověď na Aktualizaci:" + result.toString());

                                result = WebSocketController_Incoming.blocko_server_remove_instance(this_server, homer.identifikator);
                                System.out.println("Odpověď na Odstranění intance:" + result.toString());

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
