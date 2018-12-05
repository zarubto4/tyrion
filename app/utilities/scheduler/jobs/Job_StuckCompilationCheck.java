package utilities.scheduler.jobs;

import models.Model_CProgramVersion;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.enums.CompilationStatus;
import utilities.logger.Logger;
import utilities.scheduler.Scheduled;


import java.util.Date;
import java.util.List;

@Scheduled("30 0/10 * * * ?")
public class Job_StuckCompilationCheck implements Job {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Job_StuckCompilationCheck.class);

//**********************************************************************************************************************

    public Job_StuckCompilationCheck() {}

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("execute:: Executing Job_StuckCompilationCheck");

        if (!compilation_check_thread.isAlive()) compilation_check_thread.start();
    }

    private Thread compilation_check_thread = new Thread() {

        @Override
        public void run() {
            try {

                logger.debug("compilation_check_thread: concurrent thread started on {}", new Date());

                Date created = new Date(new Date().getTime() - (5 * 60 * 1000)); // before 5 minutes
                while (true) {
                    // Vyhledání všech, které je nutné projit
                    List<Model_CProgramVersion> versions = Model_CProgramVersion.find.query()
                            .where()
                            .disjunction()
                            .eq("compilation.status", CompilationStatus.SERVER_OFFLINE.name())
                            .eq("compilation.status", CompilationStatus.SERVER_ERROR.name())
                            .endJunction()
                            .lt("created", created).order().desc("created").setMaxRows(100).findList();

                    if (versions.isEmpty()) {

                        logger.debug("compilation_check_thread:: no versions to compile");
                        break;
                    }

                    logger.debug("compilation_check_thread:: compiling versions (100 per cycle)");

                    // Postupná procházení a kompilování
                    for (Model_CProgramVersion version : versions) {

                        // Pokud neobsahuje verzi - je to špatně, ale zde neřešitelné - proto se to přeskočí.
                        if (version == null) {
                            continue;
                        }

                        logger.debug("compilation_check_thread?: starting compilation of version {} with ID: {}", version.name, version.id);
                        version.compilation.status = CompilationStatus.IN_PROGRESS;
                        version.compilation.update();

                        // Výsledek se kterým se dále nic neděje
                        // TODO version.compile_program_procedure();
                    }
                }

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            logger.debug("compilation_check_thread:: thread stopped on {}", new Date());
        }
    };
}
