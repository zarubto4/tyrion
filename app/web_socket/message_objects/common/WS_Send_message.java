package web_socket.message_objects.common;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import utilities.errors.ErrorCode;
import utilities.logger.Class_Logger;
import web_socket.services.WS_Interface_type;

import java.util.concurrent.*;

public class WS_Send_message {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(WS_Send_message.class);

//**********************************************************************************************************************

    // Proměnné
    private ObjectNode json;                                                // Zpráva
    private Integer time,  delay = 0;                                       // V milisekundách
    private Integer number_of_retries;                                      // počet opakování
    private WS_Interface_type sender_object;                                        // Soket
    private String messageId = null;

    // Výsledek
    private ObjectNode result = null;   // Výyledek - který je se zpožděním do objektu vložen - a měl by být okamžitě vrácen

    public ObjectNode time_out_exception_error_response() {


        ObjectNode request = Json.newObject();
            json.put("error", ErrorCode.WEBSOCKET_TIME_OUT_EXCEPTION.error_message());
            json.put("error_code", ErrorCode.WEBSOCKET_TIME_OUT_EXCEPTION.error_code());

        if(messageId!= null) sender_object.sendMessageMap.remove(messageId);

        return request;
    }

    public static ExecutorService pool = Executors.newFixedThreadPool(500);

    private Callable<ObjectNode> callable = new Confirmation_Thread();
    private Future<ObjectNode> future = pool.submit(callable);


    public WS_Send_message(WS_Interface_type sender_object, ObjectNode json, String messageId, Integer time, Integer delay, Integer number_of_retries){
        this.sender_object = sender_object;                         // Socet k odeslání
        this.json = json;                                           // Json k odeslání
        this.time = time;                                           // Doba odesílání - pokud nepřijde odpověď - vyvolá se vyjímka
        this.delay = delay;                                         // Doba pro kterou se odloží odeslání dotazu
        this.number_of_retries = number_of_retries;                 // Počet opakování
        this.messageId = messageId;
    }

    public void insert_result(ObjectNode result) {

        terminal_logger.trace("insert_result:: MessageID:: {}  insert result {} ", result.get("message_id").asText() , result.toString());

        // Pokud existuje zpráva v zásobníku a Json obsahuje message_id - smažu ze zásobníku
        try {

            if(sender_object.sendMessageMap.size() > 10) terminal_logger.internalServerError(new Exception("insert_result:: Map contains " + sender_object.sendMessageMap.size() + " objects"));
            sender_object.sendMessageMap.remove(messageId);

        }catch (Exception e){/* Nic neprovedu - pro jistotu - většinou sem zapadne zpráva z kompilátoru - která je ale odchycená v jiné vrstvě */}


        terminal_logger.trace("insert_result: MessageID: {}  saving result to variable " , messageId );
        this.result = result;
        future.cancel(true);
    }



    public ObjectNode send_with_response() throws TimeoutException, ExecutionException, InterruptedException {
        try {

            if(this.messageId == null) {
                terminal_logger.internalServerError(new Exception("message_id is null."));
            }

            if(this.json == null){
                terminal_logger.internalServerError(new Exception("JSON is null."));
            }

            terminal_logger.trace("send_with_response: Sending message: {} Message :: {} " , this.messageId, json );

            if(future != null) {

                return future.get();
            }else {
                terminal_logger.internalServerError(new Exception("future parameter is null"));
                throw new TimeoutException();
            }
        }catch (CancellationException e){
            terminal_logger.trace("send_with_response:: CancellationException: {} result: {} " , this.messageId ,  result.toString() );
            return result;
        }
    }


    class Confirmation_Thread implements Callable<ObjectNode> {

        @Override
        public ObjectNode call() throws Exception {
            try {

                Thread.sleep(delay);

                terminal_logger.trace("thread: ");

                while (number_of_retries >= 0) {


                    if(json != null) {

                        terminal_logger.trace("thread: MessageID: {} , MessageType: {} , Number of RetiresTime: {} , Time to wait: {} ", messageId, json.get("message_type"), number_of_retries, time);

                        sender_object.out.write(json.toString());
                        --number_of_retries;

                        Thread.sleep(25);

                        if (result != null) {
                            return result;
                        }

                    }else {
                        terminal_logger.trace("thread: MessageID: {}, Waiting for the result", messageId);
                    }

                    Thread.sleep(time);
                }

                if(!sender_object.is_online())  {
                    terminal_logger.warn("Sender is offline. MessageID: " + messageId + ", MessageType: " + json.get("message_type"));
                    sender_object.close();
                }

                terminal_logger.warn("Timeout. Responding with Error. Message ID: {} ", messageId);
                return time_out_exception_error_response();

            } catch (NullPointerException e){

                terminal_logger.warn("thread: MessageId = {}, JSON = {}, number of retries = {}, time = {}, delay = {}", messageId, json, number_of_retries, time, delay);

                throw e;

            } catch (InterruptedException|CancellationException e){

                if(messageId!= null) sender_object.sendMessageMap.remove(messageId);

                throw e;

            } catch (Exception e) {

                if(messageId!= null) sender_object.sendMessageMap.remove(messageId);

                terminal_logger.internalServerError(e);
                throw e;
            }
        }
    }
}