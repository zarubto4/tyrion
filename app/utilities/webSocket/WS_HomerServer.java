package utilities.webSocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_WebSocket;
import models.project.b_program.instnace.Model_HomerInstance;
import models.project.b_program.servers.Model_HomerServer;
import play.data.Form;
import play.libs.Json;
import utilities.enums.Log_Level;
import utilities.hardware_updater.Actualization_Task;
import utilities.loginEntities.TokenCache;
import utilities.webSocket.messageObjects.WS_CheckHomerServerConfiguration;
import utilities.webSocket.messageObjects.WS_CheckHomerServerPermission;

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

    public TokenCache token_grid_Cache    = new TokenCache( (long) 15, (long) 500, 5000);
    public TokenCache token_webview_Cache = new TokenCache( (long) 15, (long) 500, 5000);


    public WS_HomerServer(Model_HomerServer server, Map<String, WebSCType> blocko_servers) {
        super();
        super.identifikator = server != null ? server.unique_identificator : null;
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
            ObjectNode response = Json.newObject();
            response.put("messageType", "verificationFirstRequired");
            response.put("messageChannel", Model_HomerServer.CHANNEL);
            response.put("message", " Yor server is not verified yet");
            super.write_without_confirmation(response);
            return;
        }


            if(json.has("messageChannel")){

                switch (json.get("messageChannel").asText()){


                    case "homer-server" : { // Komunikace mezi Tyrion server a Homer Server
                        Model_HomerServer.Messages(this, json);
                        return;
                    }


                    case "tyrion": {    // Komunikace mezi Tyrion server a Homer Instance
                        Model_HomerInstance.Messages(this, json);
                        return;
                    }


                    case "becki": {    // Komunikace mezi Becki a Homer Instance

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

    public void security_token_confirm_procedure(){

        try {
            logger.warn("WS_HomerServer:: security_token_confirm_procedure:: Trying to Confirm WebSocket");


            // Požádáme o token
            ObjectNode request = Json.newObject();
            request.put("messageType", "getVerificationToken");
            request.put("messageChannel", Model_HomerServer.CHANNEL);
            ObjectNode ask_for_token = super.write_with_confirmation(request, 1000 * 5, 0, 2);


            final Form<WS_CheckHomerServerPermission> form = Form.form(WS_CheckHomerServerPermission.class).bind(ask_for_token);
            if (form.hasErrors()) {
                logger.error("WS_HomerServer:: Security_token_confirm_procedure: Error:: Some value missing:: " + form.errorsAsJson().toString());
                // Ukončim ověřování - ale nechám websocket připojený
                return;
            }

            // Vytovření objektu
            WS_CheckHomerServerPermission help = form.get();

            // Vyhledání DB reference
            Model_HomerServer check_server = Model_HomerServer.find.where().eq("hash_certificate", help.hashToken).findUnique();

            // Kontrola
            if(!check_server.unique_identificator.equals(server.unique_identificator)) return;


            // Potvrzení Homer serveru, že je vše v pořádku
            ObjectNode request_2 = Json.newObject();
            request_2.put("messageType", "verificationTokenApprove");
            request_2.put("messageChannel", Model_HomerServer.CHANNEL);
            ObjectNode approve_result = super.write_with_confirmation(request_2, 1000 * 5, 0, 2);

            // Změna FlagRegistru
            this.security_token_confirm = true;

            // Sesynchronizuj Configuraci serveru s tím co ví a co zná Tyrion
            synchronize_configuration();

            // GET state - a vyhodnocením v jakém stavu se cloud_blocko_server nachází a popřípadě
            // na něj nahraji nebo smažu nekonzistenntí clou dprogramy, které by na něm měly být
            server.check_after_connection(this);

        }catch(ClosedChannelException e){
            logger.warn("WS_HomerServer:: security_token_confirm_procedure :: ClosedChannelException");
        }catch (TimeoutException e){
            logger.error("WS_HomerServer:: security_token_confirm_procedure :: TimeoutException");
        }catch (Exception e){
            logger.error("WS_HomerServer:: security_token_confirm_procedure :: Error", e);
        }
    }

    public void synchronize_configuration(){

        try{
        // Požádáme o token
        ObjectNode request = Json.newObject();
        request.put("messageType", "getServerConfiguration");
        request.put("messageChannel", Model_HomerServer.CHANNEL);
        ObjectNode ask_for_configuration = super.write_with_confirmation(request, 1000 * 5, 0, 2);

            // Vytovření objektu
            WS_CheckHomerServerConfiguration help = WS_CheckHomerServerConfiguration.getObject(ask_for_configuration);

            if(help.timeStamp.compareTo(server.time_stamp_configuration) == 0){
                // Nedochází k žádným změnám
                logger.debug("WS_HomerServer:: synchronize_configuration: configuration without changes");
                return;

            }else if(help.timeStamp.compareTo(server.time_stamp_configuration) > 0){
                // Homer server má novější novou konfiguraci
                logger.debug("WS_HomerServer:: synchronize_configuration: Homer server has new configuration");


                server.personal_server_name = help.serverName;
                server.mqtt_port = help.mqttPort;
                server.mqtt_password = help.mqttPassword;
                server.mqtt_username = help.mqttUser;
                server.grid_port = help.gridPort;

                server.webView_port = help.beckiPort;
                server.server_remote_port = help.webPort;

                server.mqtt_username = help.mqttUser;
                server.grid_port = help.gridPort;
                server.webView_port = help.beckiPort;
                server.days_in_archive = help.daysInArchive;
                server.time_stamp_configuration = help.timeStamp;

                server.logging = help.logging;
                server.interactive = help.interactive;
                server.logLevel = Log_Level.fromString(help.logLevel);
                server.update();

                return;

            }else {
                // Tyrion server má novější konfiguraci
                logger.debug("WS_HomerServer:: synchronize_configuration: Tyrion server has new configuration");
                JsonNode response = server.set_new_configuration_on_homer();
                logger.debug("WS_HomerServer:: synchronize_configuration: New Config state:: " + response.get("status"));

                return;
            }

        }catch(ClosedChannelException e){
            logger.warn("WS_HomerServer:: security_token_confirm_procedure :: ClosedChannelException");
        }catch (TimeoutException e){
            logger.error("WS_HomerServer:: security_token_confirm_procedure :: TimeoutException");
        }catch (Exception e){
            logger.error("WS_HomerServer:: security_token_confirm_procedure :: Error", e);
        }
    }



// Přepsané oprávnění - jen kontrola zda se WebSocketu může něco posílat

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
        ObjectNode request_2 = Json.newObject();
        request_2.put("messageType", "server_validation");
        request_2.put("messageChannel", Model_HomerServer.CHANNEL);
        request_2.put("message", "Unique server identificator is not recognize!");
        super.write_without_confirmation(request_2);
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
