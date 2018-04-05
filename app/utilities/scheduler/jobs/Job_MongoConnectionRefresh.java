package utilities.scheduler.jobs;

import models.Model_InstanceSnapshot;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.document_mongo_db.MongoDB;
import utilities.logger.Logger;
import utilities.scheduler.Scheduled;

import java.util.Date;
import java.util.UUID;

/**
 * Uploads blocko program to homer.
 */
@Scheduled("0 0/10 * 1/1 * ? *")
public class Job_MongoConnectionRefresh implements Job {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Job_MongoConnectionRefresh.class);

//**********************************************************************************************************************

    public Job_MongoConnectionRefresh() {}

    
    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("execute: Executing Job_MongoConnectionRefresh");
        if (!thread.isAlive()) thread.start();
    }

    private Thread thread = new Thread() {

        @Override
        public void run() {
            try {

                logger.trace("thread: concurrent thread started on {}", new Date());
                MongoDB.init();

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            logger.trace("thread: thread stopped on {}", new Date());
        }
    };
}