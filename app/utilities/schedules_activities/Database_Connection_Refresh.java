package utilities.schedules_activities;


import models.project.b_program.servers.Model_HomerServer;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.SQLTimeoutException;

public class Database_Connection_Refresh implements Job {

    public Database_Connection_Refresh(){ /** do nothing */ }

    // Logger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public void execute(JobExecutionContext context) throws JobExecutionException {

        if(refresh_connection_thread.getState() == Thread.State.NEW) {

            refresh_connection_thread.start();
        } else {

            refresh_connection_thread.interrupt();
        }
    }

    static Thread refresh_connection_thread = new Thread() {

        @Override
        public void run() {

            while (true) {
                try {

                    int count = Model_HomerServer.find.findRowCount();

                    logger.info("Database connection refreshed");

                    sleep(70000);

                } catch (InterruptedException i) {
                    // Do nothing
                } catch (Exception e) {
                    logger.error("Database connection lost! Immediately restart Tyrion Server! Or bad things will happen.");
                    break;
                    // TODO když ztratím spojení s databází
                }
            }

        }
    };
}
