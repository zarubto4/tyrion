package utilities.scheduler.jobs;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.Controller_WebSocket;
import models.Model_VersionObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.enums.Enum_Compile_status;
import utilities.logger.Class_Logger;
import java.util.Date;
import java.util.List;

public class Job_StuckCompilationCheck implements Job {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Job_StuckCompilationCheck.class);

//**********************************************************************************************************************

    public Job_StuckCompilationCheck(){}

    public void execute(JobExecutionContext context) throws JobExecutionException {

        terminal_logger.info("execute:: Executing Job_StuckCompilationCheck");

        if(!compilation_check_thread.isAlive()) compilation_check_thread.start();
    }

    private Thread compilation_check_thread = new Thread() {

        @Override
        public void run() {
            try {

                terminal_logger.debug("compilation_check_thread: concurrent thread started on {}", new Date());

                Date created = new Date(new Date().getTime() - (5 * 60 * 1000)); // before 5 minutes

                // Zarážka pro
                if(!Controller_WebSocket.compiler_cloud_servers.isEmpty()) {

                    while (true) {
                        // Vyhledání všech, které je nutné projit
                        List<Model_VersionObject> version_objects = Model_VersionObject.find.where()
                                .disjunction()
                                .eq("c_compilation.status", Enum_Compile_status.server_was_offline.name())
                                .eq("c_compilation.status", Enum_Compile_status.compilation_server_error.name())
                                .endJunction()
                                .lt("date_of_create", created).order().desc("date_of_create").setMaxRows(100).findList();

                        if (version_objects.isEmpty()){

                            terminal_logger.debug("compilation_check_thread:: no versions to compile");
                            break;
                        }

                        terminal_logger.debug("compilation_check_thread:: compiling versions (100 per cycle)");

                        // Postupná procházení a kompilování
                        for (Model_VersionObject version_object : version_objects) {

                            // Pokud neobsahuje verzi - je to špatně, ale zde neřešitelné - proto se to přeskočí.
                            if (version_object == null) {
                                continue;
                            }

                            terminal_logger.debug("compilation_check_thread?: starting compilation of version {} with ID: {}", version_object.version_name, version_object.id);
                            version_object.c_compilation.status = Enum_Compile_status.compilation_in_progress;
                            version_object.c_compilation.update();

                            // Výsledek se kterým se dále nic neděje
                            version_object.compile_program_procedure();
                        }
                    }
                }

            }catch(Exception e){
                terminal_logger.internalServerError("compilation_check_thread:", e);
            }

            terminal_logger.debug("compilation_check_thread:: thread stopped on {}", new Date());
        }
    };
}
