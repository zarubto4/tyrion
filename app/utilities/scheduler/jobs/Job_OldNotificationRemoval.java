package utilities.scheduler.jobs;


import models.Model_Notification;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.loggy.Loggy;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Job_OldNotificationRemoval implements Job {

    public Job_OldNotificationRemoval(){}

    // Logger
    private static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("Job_OldNotificationRemoval:: execute: Executing Job_OldNotificationRemoval");

        if(!remove_notification_thread.isAlive()) remove_notification_thread.start();
    }

    private Thread remove_notification_thread = new Thread() {

        @Override
        public void run() {

            try {

                logger.debug("Job_OldNotificationRemoval:: remove_notification_thread: concurrent thread started on {}", new Date());

                Date created = new Date(new Date().getTime() - TimeUnit.DAYS.toMillis(30)); // before one month

                while (true) {

                    List<Model_Notification> notifications = Model_Notification.find.where().lt("created", created).setMaxRows(100).findList();
                    if (notifications.isEmpty()) {
                        logger.debug("Job_OldNotificationRemoval:: remove_notification_thread: no notifications to remove");
                        break;
                    }

                    logger.debug("Job_OldNotificationRemoval:: remove_notification_thread: removing old notifications (100 per cycle)");

                    notifications.forEach(Model_Notification::delete);
                }

            } catch (Exception e) {
                Loggy.internalServerError("Job_OldNotificationRemoval:: remove_notification_thread:", e);
            }

            logger.debug("Job_OldNotificationRemoval:: remove_notification_thread: thread stopped on {}", new Date());
        }
    };
}
