package web_socket.message_objects.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerInstance;
import play.libs.Json;
import utilities.errors.ErrorCode;
import utilities.logger.Class_Logger;
import utilities.response.GlobalResult;
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

        System.out.println(result.get("messageId").asText());

        terminal_logger.trace("insert_result:: MessageID:: {}  insert result {} ", result.get("messageId").asText() , result.toString());

        // Pokud existuje zpráva v zásobníku a Json obsahuje messageId - smažu ze zásobníku
        try {
            sender_object.sendMessageMap.remove(result.get("messageId").asText());
        }catch (Exception e){/* Nic neprovedu - pro jistotu - většinou sem zapadne zpráva z kompilátoru - která je ale odchycená v jiné vrstvě */}


        terminal_logger.trace("insert_result:: MessageID:: {}  saving result to variable " , messageId );
        this.result = result;

        terminal_logger.trace("insert_result:: MessageID:: {}  Terminating message thread");
        future.cancel(true); // Terminuji zprávu k odeslání

    }



    public ObjectNode send_with_response() throws TimeoutException, ExecutionException, InterruptedException {
        try {
            terminal_logger.trace("send_with_response:: Sending message: {} " , this.messageId);
            return future.get();
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

               int i = 0;

                while (number_of_retries >= 0) {


                    if(json != null) {

                        terminal_logger.trace("thread:: MessageID:: {} , MessageType:: {} , Number of RetiresTime:: {} , RecurencyTime:: {} ", messageId, json.get("messageType"), number_of_retries, time);

                        sender_object.out.write(json.toString());
                        --number_of_retries;

                        Thread.sleep(25);

                        if (result != null) {
                            return result;
                        }

                    }else {
                        terminal_logger.trace("thread:: MessageID:: {} , MessageType:: {} , Nothing for sending - just waiting for result - It can be from Compilator? Cycle:: {}", messageId, i++);
                    }

                    Thread.sleep(time);
                }

                if(!sender_object.is_online())  {
                    terminal_logger.error("thread:: MessageID:: {} , MessageType:: {} ,  Sender is offine!!! ", messageId ,  json.get("messageType"));
                    sender_object.close();
                }

                terminal_logger.error("thread:: Message ID:: {}  time is gone!! :( Responde with Error!!", messageId );
                return time_out_exception_error_response();

            }catch (InterruptedException|CancellationException e){
                throw e;
            } catch (Exception e) {
                terminal_logger.internalServerError(e);
                throw e;
            }
        }
    }
}



