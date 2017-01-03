package utilities.schedules_activities;

import com.fasterxml.jackson.databind.JsonNode;
import models.compiler.Model_VersionObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.enums.Compile_Status;

import java.util.Date;

public class Compilation_Checker implements Job {

    public Compilation_Checker(){ /** do nothing */ }

    // Logger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public void execute(JobExecutionContext context) throws JobExecutionException {
        if(!thread.isAlive()) thread.start();
    }

    static Thread thread = new Thread() {

        @Override
        public void run() {
            try {
            logger.info("Independent Thread in Compilation_Checker now working");

            Long before_5_minutes = new Date().getTime() - (5 * 60 * 1000);
            Date created = new Date(before_5_minutes);


                while (true) {

                    Model_VersionObject version_object = Model_VersionObject.find.where().eq("c_compilation.status", Compile_Status.server_was_offline.name()).lt("date_of_create", created).order().desc("date_of_create").setMaxRows(1).findUnique();
                    if(version_object == null){
                        break;
                    }
                    logger.debug("Compilation_Checker:: Checking stuck compilation -  starting compilation");
                    version_object.c_compilation.status = Compile_Status.compilation_in_progress;
                    version_object.c_compilation.update();

                    // Výsledek se kterým se dále nic neděje
                    JsonNode jsonNode = version_object.compile_program_procedure();
                }

            }catch(Exception e){
                e.printStackTrace();
            }

            logger.info("Independent Thread in Old_Floating_Person_Token_Removal stopped!");
        }
    };
}
