package utilities.scheduler.jobs;

import models.Model_Notification;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import play.inject.ApplicationLifecycle;
import utilities.logger.Logger;
import utilities.scheduler.Scheduled;


import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Class serves to remove old notifications.
 */
@Scheduled("0 10 3 * * ?")
public class Job_OldNotificationRemoval implements Job {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Job_OldNotificationRemoval.class);

//**********************************************************************************************************************

    public Job_OldNotificationRemoval(ApplicationLifecycle appLifecycle) {

        appLifecycle.addStopHook(() -> {
            try {
                logger.warn("Interupt Thread ", this.getClass().getSimpleName());
                this.thread.interrupt();
            } catch (Exception e){
                //
            };
            return CompletableFuture.completedFuture(null);
        });
    }
    
    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("execute: Executing Job_OldNotificationRemoval");

        if (!thread.isAlive()) thread.start();
    }

    /**
     * Thread finds all notifications older than one month.
     */
    private Thread thread = new Thread() {

        @Override
        public void run() {

            try {

                logger.debug("thread: concurrent thread started on {}", new Date());

                Date created = new Date(new Date().getTime() - TimeUnit.DAYS.toMillis(30)); // before one month

                while (true) {

                    List<Model_Notification> notifications = Model_Notification.find.query().where().lt("created", created).setMaxRows(100).findList();
                    if (notifications.isEmpty()) {
                        logger.debug("thread: no notifications to remove");
                        break;
                    }

                    logger.debug("thread: removing old notifications (100 per cycle)");

                    notifications.forEach(Model_Notification::delete);
                }

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            logger.debug("thread: thread stopped on {} ", new Date());
        }
    };
}
