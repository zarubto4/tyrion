package utilities.schedules_activities;


import models.notification.Notification;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;
import java.util.List;

public class Old_Notification_Removal implements Job {

    public Old_Notification_Removal(){ /** do nothing */ }

    // Logger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public void execute(JobExecutionContext context) throws JobExecutionException {
        if(!remove_notification_thread.isAlive()) remove_notification_thread.start();
    }

    static Thread remove_notification_thread = new Thread() {

        @Override
        public void run() {

            logger.info("Independent Thread in Old_Notification_Removal now working");

            Long month = new Long("2592000000");
            Long before_month = new Date().getTime() - month;
            Date created = new Date(before_month);

            while (true){

                List<Notification> notifications = Notification.find.where().lt("created", created).setMaxRows(100).findList();
                if (notifications.isEmpty()) {
                    logger.info("Old_Notification_Removal has no notifications to remove");
                    break;
                }

                logger.info("CRON Task is removing old notifications (100 per cycle)");

                for (Notification notification : notifications){
                    notification.delete();
                }
            }

            logger.info("Independent Thread in Old_Notification_Removal stopped!");
        }
    };
}
