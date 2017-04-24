package web_socket.message_objects.common;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
    private Integer awaiting_time = 0;                                      // Čas o který dodatečné vlákno počká
    private String messageId = null;

    // Výsledek
    private ObjectNode result = null;   // Výyledek - který je se zpožděním do objektu vložen - a měl by být okamžitě vrácen


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


        terminal_logger.trace("Sending message:  {}  insert result {} " , messageId , result.toString());

        terminal_logger.trace("Sending message:  {} not contains Sub-Message - its regular message",  messageId);


        // Pokud existuje zpráva v zásobníku a Json obsahuje messageId - smažu ze zásobníku
        try {
            sender_object.sendMessageMap.remove(json.get("messageId").asText());
        }catch (Exception e){/* Nic neprovedu - pro jistotu - většinou sem zapadne zpráva z kompilátoru - která je ale odchycená v jiné vrstvě */}


        terminal_logger.trace("Sending message: {} saving result to variable " , messageId );
        this.result = result;

        terminal_logger.trace("Terminating message thread");
        future.cancel(true); // Terminuji zprávu k odeslání

    }



    public ObjectNode send_with_response() throws TimeoutException, ExecutionException, InterruptedException {
        try {
            terminal_logger.trace("Sending message: {} " , this.messageId);
            return future.get();
        }catch (CancellationException e){
            terminal_logger.trace("CancellationException: {} result: {} " , this.messageId ,  result.toString() );
            return result;
        }
    }


    class Confirmation_Thread implements Callable<ObjectNode> {

        @Override
        public ObjectNode call() throws Exception {
            try {

               Thread.sleep(delay);

               while (number_of_retries >= 0 || awaiting_time > 0) {

                    // Slouží odsunutí vykonání odeslání další zprávy - awaiting_time je defaultně 0, ale v případě příchozí zprávy,
                    // která odsune terminator (vypršení) zprávy, uloží se do proměné thohoto objektu se čas TimeoutException posunu
                    // toto lze defakto dělat do nekonečna, kdy například Tyrion updatuje seznam deviců - to trvá dlouho na vykonání
                    // Homer požádá o delší čas - pošle submessage s dodatečným časem - vlákno tento čas spí (odmaže čas na nulu) do další iterace
                    // while vlákna - pokud mezi spánkem opět přijde další požadavek na další spaní z nuly se opět stane číslo a vlákno opět upadne do spánku
                    if(awaiting_time > 0) {
                        terminal_logger.debug("Vlákno bylo ze strany příjemce požádáno, aby vyčkalo v milisekundách o něco déle: " + awaiting_time );
                        Thread.sleep(awaiting_time);
                        awaiting_time = 0;
                    }else {

                        if(json != null) {
                          //  logger.debug("Sending message");
                            sender_object.out.write(json.toString());
                            --number_of_retries;

                            Thread.sleep(25);

                            if (result != null) {
                                return result;
                            }
                        }else {
                            terminal_logger.trace("Sending message: " + messageId + "Nothing for sending - just waiting for result");
                        }

                        Thread.sleep(time);
                    }
                }

                throw new TimeoutException();

            }catch (InterruptedException|CancellationException e){
                throw e;
            } catch (Exception e) {
                terminal_logger.internalServerError(e);
                throw e;
            }
        }
    }
}



