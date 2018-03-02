package websocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;
import utilities.logger.Logger;

import java.util.UUID;
import java.util.concurrent.Callable;

public class ParallelTask implements Callable<ObjectNode> {

    private static final Logger logger = new Logger(ParallelTask.class);

    private UUID server_id;
    private ObjectNode message;
    private Integer time;
    private Integer delay;
    private Integer number_of_retries;

    public ParallelTask(UUID server_id, ObjectNode message, Integer delay, Integer time, Integer number_of_retries) {

        this.server_id = server_id;
        this.message = message;
        this.time = time;
        this.delay = delay;
        this.number_of_retries = number_of_retries;
    }
    @Override
    public ObjectNode call() throws Exception {
        try {


            Model_HomerServer server = Model_HomerServer.getById(this.server_id);

            if (server == null) {
               throw new Exception("ParallelTask:: call server id" + this.server_id + " not exist");
            }

            return server.write_with_confirmation(message, time, delay, number_of_retries);

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }
}
