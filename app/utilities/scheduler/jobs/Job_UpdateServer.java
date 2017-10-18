package utilities.scheduler.jobs;

import models.Model_HomerInstanceRecord;
import models.Model_HomerServer;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.logger.Class_Logger;

import java.util.Date;

/**
 * Updates various servers, even itself
 */
public class Job_UpdateServer implements Job {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Job_UpdateServer.class);

//**********************************************************************************************************************

    public Job_UpdateServer(){}

    private String server;
    private String version;
    private String identifier;
    
    public void execute(JobExecutionContext context) throws JobExecutionException {

        terminal_logger.info("execute: Executing Job_UpdateServer");

        server = context.getMergedJobDataMap().getString("server");
        identifier = context.getMergedJobDataMap().getString("identifier");
        version = context.getMergedJobDataMap().getString("version");

        if(!update_server_thread.isAlive()) update_server_thread.start();
    }

    private Thread update_server_thread = new Thread() {

        @Override
        public void run() {
            try {

                terminal_logger.trace("update_server_thread: concurrent thread started on {}", new Date());

                if (server == null) throw new NullPointerException("Job was instantiated without server in the JobExecutionContext or the server is null for some reason.");
                if (identifier == null && !server.equals("tyrion")) throw new NullPointerException("Job was instantiated without identifier in the JobExecutionContext or the identifier is null for some reason.");
                if (version == null) throw new NullPointerException("Job was instantiated without version in the JobExecutionContext or the version is null for some reason.");

                switch (server) {
                    case "tyrion": {
                        // TODO execute script
                        break;
                    }
                    case "homer": {
                        Model_HomerServer homerServer = Model_HomerServer.get_byId(identifier);
                        if (homerServer == null) throw new NullPointerException("Cannot find the Homer Server in the DB.");

                        // TODO do request
                        break;
                    }
                    case "code": {
                        // TODO do update
                        break;
                    }
                    default: throw new IllegalStateException("Server must be set to one of: tyrion, homer or code. Value was: " + server);
                }

            } catch (Exception e) {
                terminal_logger.internalServerError(e);
            }

            terminal_logger.trace("update_server_thread: thread stopped on {}", new Date());
        }
    };
}