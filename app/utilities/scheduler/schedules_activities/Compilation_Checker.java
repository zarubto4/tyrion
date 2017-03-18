package utilities.scheduler.schedules_activities;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.Controller_WebSocket;
import models.Model_VersionObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.enums.Enum_Compile_status;

import java.util.Date;
import java.util.List;

public class Compilation_Checker implements Job {

    public Compilation_Checker(){ /** do nothing */ }

    // Logger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public void execute(JobExecutionContext context) throws JobExecutionException {

        if(thread == null){
            logger.warn("Compilation_Checker:: execute:: Thread is null!!!");
        }

        if(!thread.isAlive()) thread.start();
    }

    Thread thread = new Thread() {

        @Override
        public void run() {
            try {
                logger.info("Compilation_Checker:: run:: Independent Thread in Compilation_Checker now working");

                Long before_5_minutes = new Date().getTime() - (5 * 60 * 1000);
                Date created = new Date(before_5_minutes);

                // Zarážka pro
                if( !Controller_WebSocket.compiler_cloud_servers.isEmpty()) {

                    // Vyhledání všech, které je nutné projit
                    List<Model_VersionObject> version_objects = Model_VersionObject.find.where()
                            .disjunction()
                            .eq("c_compilation.status", Enum_Compile_status.server_was_offline.name())
                            .eq("c_compilation.status", Enum_Compile_status.compilation_server_error.name())
                            .endJunction()
                            .lt("date_of_create", created).order().desc("date_of_create").findList();


                    // Postupná procházení a kompilování
                    for (Model_VersionObject version_object : version_objects) {

                        // Pokud neobsahuje verzi - je to špatně, ale zde neřešitelné - proto se to přeskočí.
                        if (version_object == null) {
                            continue;
                        }
                        logger.debug("Compilation_Checker:: run::  Checking stuck compilation -  starting compilation");
                        version_object.c_compilation.status = Enum_Compile_status.compilation_in_progress;
                        version_object.c_compilation.update();

                        // Výsledek se kterým se dále nic neděje
                        JsonNode jsonNode = version_object.compile_program_procedure();
                    }
                }

                logger.info("Independent Thread in Compilation_Checker finish!");

            }catch(Exception e){
                e.printStackTrace();
            }

            logger.info("Independent Thread in Compilation_Checker stopped!");
        }
    };
}
