package utilities.web_socket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_WebSocket;
import models.compiler.Model_Board;
import models.project.b_program.instnace.Model_HomerInstance;
import models.project.b_program.servers.Model_HomerServer;
import utilities.hardware_updater.Actualization_Task;
import utilities.independent_threads.Check_update_for_hw_under_homer_ws;
import utilities.independent_threads.Security_WS_token_confirm_procedure;
import utilities.independent_threads.SynchronizeHomerServer;
import utilities.web_socket.message_objects.homer_instance.WS_Update_device_firmware;
import utilities.web_socket.message_objects.homer_tyrion.WS_Rejection_homer_server;

import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class WS_HomerServer extends WebSCType{

    public Map<String, WebSCType> virtual_homers = new HashMap<>();  // Zde si udržuju referenci na tímto serverem vytvořené virtuální homery, které jsem dal do globální mapy (incomingConnections_homers) - určeno pro vývojáře
    public Model_HomerServer server;
    public boolean security_token_confirm;

    //public TokenCache token_grid_Cache    = new TokenCache( (long) 15, (long) 500, 5000);
    //public TokenCache token_webview_Cache = new TokenCache( (long) 15, (long) 500, 5000);

    public Check_update_for_hw_under_homer_ws check_update_for_hw_under_homer_ws = null;

    public WS_HomerServer(Model_HomerServer server, Map<String, WebSCType> blocko_servers) {
        super();
        super.identifikator = server != null ? server.unique_identificator : null;
        super.maps = blocko_servers;
        super.webSCtype = this;
        this.server = server;
        this.update_thread.start();
        this.check_update_for_hw_under_homer_ws = new Check_update_for_hw_under_homer_ws(this);
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
        Controller_WebSocket.homer_servers.remove(super.identifikator);
        server.is_disconnect();
    }

    @Override
    public void onMessage(ObjectNode json) {

        logger.debug("BlockoServer: "+ super.identifikator + " Incoming message: " + json.toString());

        // Pokud není token - není dovoleno zasílat nic do WebSocketu a ani nic z něj
        if(!security_token_confirm){
            logger.warn("WS_HomerServer:: onMessage:: This Websocket is not confirm");
            security_token_confirm_procedure();

            super.write_without_confirmation(new WS_Rejection_homer_server().make_request());
            return;
        }


            if(json.has("messageChannel")){

                switch (json.get("messageChannel").asText()){


                    case Model_HomerServer.CHANNEL : { // Komunikace mezi Tyrion server a Homer Server
                        Model_HomerServer.Messages(this, json);
                        return;
                    }


                    case Model_HomerInstance.CHANNEL: {    // Komunikace mezi Tyrion server a Homer Instance
                        Model_HomerInstance.Messages(this, json);
                        return;
                    }

                    // TODO Becki
                    case WS_Becki_Website.CHANNEL: {    // Komunikace mezi Becki a Homer Instance

                        switch (json.get("messageType").asText()){

                            case "notification" : {

                                return;
                            }

                            default: {
                                logger.error("Homer Server:: Incoming message:: Chanel becki:: not recognize messageType ->" + json.get("messageType").asText());
                                return;
                            }

                        }
                    }

                    default: {
                        logger.error("Homer Server Incoming message not recognize incoming messageChanel!!! ->" + json.get("messageChannel").asText());

                    }

                }

            }else {
                logger.error("Homer Server: "+ super.identifikator + " Incoming message has not messageChannel!!!!");
            }

    }


    // Independent Threads -----------------------------------------------------------------------------------------------

    public Security_WS_token_confirm_procedure procedure; // Vlákno po úspěšném ověření samo sebe odstraní z Objektu a tím se nechá odmazat Garbarage Collectorem
    public void security_token_confirm_procedure(){
        if(procedure == null ){
            procedure = new Security_WS_token_confirm_procedure(this);
            logger.debug("WS_HomerServer:: security_token_confirm_procedure: Independent Thread for secure starting");
            procedure.start();
        }else {
            logger.debug("WS_HomerServer:: security_token_confirm_procedure: Independent Thread for secure was started already");
        }
    }

    //
    public ObjectNode super_write_with_confirmation(ObjectNode json, Integer time, Integer delay, Integer number_of_retries)  throws TimeoutException, ClosedChannelException, ExecutionException, InterruptedException {
        if(procedure == null) {
            logger.error("WS_HomerServer:: Its prohybited send WS message to Homer server witch Super - its allowed only for Security_WS_token_confirm_procedure");
        }
        return super.write_with_confirmation(json,time,delay, number_of_retries);
    }

    public SynchronizeHomerServer synchronize;
    public void synchronize_configuration(){
        if(synchronize == null ){
            synchronize = new SynchronizeHomerServer(this);
            logger.debug("WS_HomerServer:: synchronize_configuration: Independent Thread for secure starting");
            synchronize.start();
        }else {
            logger.debug("WS_HomerServer:: synchronize_configuration: Independent Thread for secure was started already");
        }
    }

    public WebSCType get_Super(){
        return super.webSCtype;
    }


    // IO -----------------------------------------------------------------------------------------------------------------

    @Override
    public ObjectNode write_with_confirmation(ObjectNode json, Integer time, Integer delay, Integer number_of_retries)  throws TimeoutException, ClosedChannelException, ExecutionException, InterruptedException{

        if(!security_token_confirm){
            logger.warn("WS_HomerServer:: write_with_confirmation:: This Websocket is not confirm");
            security_token_confirm_procedure();
            throw new InterruptedException();
        }

        return super.write_with_confirmation(json,time,delay, number_of_retries);

    }

    @Override
    public void write_without_confirmation(ObjectNode json){

        if(!security_token_confirm){
            logger.warn("WS_HomerServer:: write_without_confirmation:: This Websocket is not confirm");
            security_token_confirm_procedure();
        }

        super.write_without_confirmation(json);
    }

    @Override
    public void write_without_confirmation(String messageId, ObjectNode json){

        if(!security_token_confirm){
            logger.warn("WS_HomerServer:: write_without_confirmation:: This Websocket is not confirm");
            security_token_confirm_procedure();
        }

        super.write_without_confirmation(messageId,json);
    }


    /**
     * Odešle se serveru - který není akceptován - Unique name není známo
     */
    public void unique_connection_name_not_valid(){

        // Potvrzení Homer serveru, že je vše v pořádku
        super.write_without_confirmation(new WS_Rejection_homer_server().make_request());
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


                            WS_Update_device_firmware result = Model_Board.update_devices_firmware(task.instance, task.procedures);
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
