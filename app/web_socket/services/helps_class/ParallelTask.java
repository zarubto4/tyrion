package web_socket.services.helps_class;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;

import java.util.concurrent.Callable;

import static models.Model_HomerInstance.terminal_logger;


public class ParallelTask implements Callable<ObjectNode> {

    private String server_id;
    private ObjectNode message;
    private Integer time;
    private Integer delay;
    private Integer number_of_retries;

    public ParallelTask(String server_id, ObjectNode message, Integer time, Integer delay, Integer number_of_retries){

        this.server_id = server_id;
        this.message = message;
        this.time = time;
        this.delay = delay;
        this.number_of_retries = number_of_retries;
    }
    @Override
    public ObjectNode call() throws Exception {
        try {


            Model_HomerServer server = Model_HomerServer.get_byId(this.server_id);

            if(server == null){
               throw new Exception("ParallelTask:: call server id" + this.server_id + " not exist");
            }

            return server.write_with_confirmation(message, time, delay, number_of_retries);

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }
}
