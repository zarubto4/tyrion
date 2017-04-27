package utilities.scheduler.jobs;


import models.Model_FloatingPersonToken;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.logger.Class_Logger;
import web_socket.message_objects.common.WS_Send_message;

import java.util.Date;
import java.util.List;

public class Job_OldFloatingTokenRemoval implements Job {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Job_OldFloatingTokenRemoval.class);

//**********************************************************************************************************************

    public Job_OldFloatingTokenRemoval(){}

    public void execute(JobExecutionContext context) throws JobExecutionException {

        terminal_logger.info("execute: Executing Job_OldFloatingTokenRemoval");

        if(!remove_floating_person_token_thread.isAlive()) remove_floating_person_token_thread.start();
    }

    private Thread remove_floating_person_token_thread = new Thread() {

        @Override
        public void run() {

            try {

                terminal_logger.debug("remove_floating_person_token_thread: concurrent thread started on {}", new Date());

                while (true) {

                    List<Model_FloatingPersonToken> tokens = Model_FloatingPersonToken.find.where().gt("access_age", new Date()).setMaxRows(100).findList();
                    if (tokens.isEmpty()) {
                        terminal_logger.debug("remove_floating_person_token_thread: no tokens to remove");
                        break;
                    }

                    terminal_logger.debug("remove_floating_person_token_thread: removing old tokens (100 per cycle)");

                    tokens.forEach(Model_FloatingPersonToken::delete);
                }

            } catch (Exception e) {
                terminal_logger.internalServerError("remove_floating_person_token_thread:", e);
            }

            terminal_logger.debug("remove_floating_person_token_thread: thread stopped on {}", new Date());
        }
    };
}
