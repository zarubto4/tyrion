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

        System.out.println("server_id:: " + server_id);

        this.server_id = server_id;
        this.message = message;
        this.time = time;
        this.delay = delay;
        this.number_of_retries = number_of_retries;
    }
    @Override
    public ObjectNode call() throws Exception {
        try {

            return Model_HomerServer.get_byId(server_id).sender().write_with_confirmation(message, time, delay, number_of_retries);

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }
}
