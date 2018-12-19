package utilities.hardware.update;

import com.google.inject.Inject;
import exceptions.ServerOfflineException;
import models.Model_HardwareUpdate;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.hardware.HardwareInterface;
import utilities.hardware.HardwareService;
import utilities.logger.Logger;

import java.util.UUID;

/**
 * This UpdateJob can be scheduled via the scheduler. Performs the hardware update.
 */
public class UpdateJob implements Job {

    private static final Logger logger = new Logger(UpdateJob.class);

    public static final String UPDATE_ID = "update_id";

    private final HardwareService hardwareService;

    @Inject
    public UpdateJob(HardwareService hardwareService) {
        this.hardwareService = hardwareService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {

            UUID updateId = UUID.fromString(context.getMergedJobDataMap().getString(UPDATE_ID));

            Model_HardwareUpdate update = Model_HardwareUpdate.find.byId(updateId);

            try {

                HardwareInterface hardwareInterface = hardwareService.getInterface(update.getHardware());
                hardwareInterface.update(update);

            } catch (ServerOfflineException e) {
                logger.info("execute - server is currently offline");
            }

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }
}
