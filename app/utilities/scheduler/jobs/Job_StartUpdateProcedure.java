package utilities.scheduler.jobs;

import models.Model_ActualizationProcedure;
import models.Model_HomerInstanceRecord;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.logger.Class_Logger;

import java.util.Date;

/**
 * Start or Execute with Update Procedure on required time.
 */
public class Job_StartUpdateProcedure implements Job {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Job_StartUpdateProcedure.class);

//**********************************************************************************************************************

    public Job_StartUpdateProcedure(){}

    private String procedure_id;
    
    public void execute(JobExecutionContext context) throws JobExecutionException {

        terminal_logger.info("execute: Executing Job_StartUpdateProcedure");

        procedure_id = context.getMergedJobDataMap().getString("procedure_id");

        if(!start_update_procedure_thread.isAlive()) start_update_procedure_thread.start();
    }

    private Thread start_update_procedure_thread = new Thread() {

        @Override
        public void run() {
            try {

                terminal_logger.trace("start_update_procedure_thread:: concurrent thread started on {}", new Date());

                if (procedure_id == null) throw new NullPointerException("start_update_procedure_thread:: Job was instantiated without record_id in the JobExecutionContext or the record_id is null for some reason.");

                Model_ActualizationProcedure procedure = Model_ActualizationProcedure.get_byId(procedure_id);
                if (procedure == null) throw new NullPointerException("start_update_procedure_thread:: Cannot find the Instance Record in the DB.");

                terminal_logger.trace("start_update_procedure_thread:: uploading the record");

                procedure.execute_update_procedure();

            } catch (Exception e) {
                terminal_logger.internalServerError(e);
            }

            terminal_logger.trace("upload_blocko_thread: thread stopped on {}", new Date());
        }
    };
}