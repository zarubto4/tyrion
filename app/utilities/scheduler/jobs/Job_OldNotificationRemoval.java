package utilities.scheduler.jobs;


import models.Model_Notification;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.logger.Class_Logger;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Job_OldNotificationRemoval implements Job {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Job_OldNotificationRemoval.class);

//**********************************************************************************************************************

    public Job_OldNotificationRemoval(){}
    
    public void execute(JobExecutionContext context) throws JobExecutionException {

        terminal_logger.info("execute: Executing Job_OldNotificationRemoval");

        if(!remove_notification_thread.isAlive()) remove_notification_thread.start();
    }

    private Thread remove_notification_thread = new Thread() {

        @Override
        public void run() {

            try {

                terminal_logger.debug("remove_notification_thread:: concurrent thread started on {}", new Date());

                Date created = new Date(new Date().getTime() - TimeUnit.DAYS.toMillis(30)); // before one month

                while (true) {

                    List<Model_Notification> notifications = Model_Notification.find.where().lt("created", created).setMaxRows(100).findList();
                    if (notifications.isEmpty()) {
                        terminal_logger.debug("remove_notification_thread:: no notifications to remove");
                        break;
                    }

                    terminal_logger.debug("remove_notification_thread:: removing old notifications (100 per cycle)");

                    notifications.forEach(Model_Notification::delete);
                }

            } catch (Exception e) {
                terminal_logger.internalServerError(e);
            }

            terminal_logger.debug("remove_notification_thread:: thread stopped on {} ", new Date());
        }
    };
}
