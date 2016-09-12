package utilities.webSocket;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.concurrent.*;

public class SendMessage{

    // Logger
    static play.Logger.ALogger logger = play.Logger.of("SendMessage");

    // Proměnné
    private ObjectNode json;            // Zpráva
    private Integer time,  delay = 0;   // V milisekundách
    private Integer number_of_retries;  // počet opakování
    private WebSCType webSCType;        // Soket


    // Výsledek
    private ObjectNode result = null;   // Výyledek - který je se zpožděním do objektu vložen - a měl by být okamžitě vrácen


    public static ExecutorService pool = Executors.newFixedThreadPool(500);

    private Callable<ObjectNode> callable = new Confirmation_Thread();
    private Future<ObjectNode> future = pool.submit(callable);


    public SendMessage(WebSCType webSCType, ObjectNode json, Integer time, Integer delay, Integer number_of_retries){
        this.webSCType = webSCType;                     // Socet k odeslání
        this.json = json;                               // Json k odeslání
        this.time = time;                               // Doba odesílání - pokud nepřijde odpověď - vyvolá se vyjímka
        this.delay = delay;                             // Doba pro kterou se odloží odeslání dotazu
        this.number_of_retries = number_of_retries;     // Počet opakování
    }

    public void insert_result(ObjectNode result) {
     //   logger.debug("Incoming result:" + result.toString());
        future.cancel(true);
        this.result = result;
    }


    public ObjectNode send_with_response() throws TimeoutException, ExecutionException, InterruptedException {
        try {
            return future.get(time * number_of_retries + 250, TimeUnit.MILLISECONDS);
        }catch (CancellationException e){
            //logger.debug("Došlo k přerušení příchozí zprávou - vracím výsledek: " + result.toString());
            return result;
        }
    }


    class Confirmation_Thread implements Callable<ObjectNode> {

        @Override
        public ObjectNode call() throws Exception {
            try {
                Thread.sleep(delay);

                while (number_of_retries >= 0) {

                    if(json != null) {
                      //  logger.debug("Sending message");
                        webSCType.out.write(json.toString());
                        --number_of_retries;

                        Thread.sleep(25);

                        if (result != null) {
                            return result;
                        }
                    }else {
                       // logger.debug("Nothing for sending - just waiting for result");
                    }

                    Thread.sleep(time);
                }

                throw new TimeoutException();
            }catch (CancellationException e){
                logger.debug("CancellationException");
                throw e;
            }catch (InterruptedException e){
                logger.debug("InterruptedException");
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
    }
}



