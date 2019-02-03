package utilities.scheduler.jobs;

import com.google.inject.Inject;
import models.Model_CProgramVersion;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import play.inject.ApplicationLifecycle;
import utilities.compiler.CompilationService;
import utilities.enums.CompilationStatus;
import utilities.logger.Logger;
import utilities.scheduler.Scheduled;


import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Scheduled("30 0/10 * * * ?")
public class Job_StuckCompilationCheck implements Job {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Job_StuckCompilationCheck.class);

//**********************************************************************************************************************

    public final CompilationService compilationService;

    @Inject
    public Job_StuckCompilationCheck(CompilationService compilationService, ApplicationLifecycle appLifecycle) {
        this.compilationService = compilationService;
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

        logger.info("execute:: Executing Job_StuckCompilationCheck");

        if (!thread.isAlive()) thread.start();
    }

    private Thread thread = new Thread() {

        @Override
        public void run() {
            try {

                logger.debug("thread: concurrent thread started on {}", new Date());

                if (compilationService.isAvailable()) {
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

                            logger.debug("thread - no versions to compile");
                            break;
                        }

                        logger.debug("thread - compiling versions (100 per cycle)");

                        // Postupná procházení a kompilování
                        for (Model_CProgramVersion version : versions) {

                            logger.debug("thread - starting compilation of version {} with ID: {}", version.name, version.id);

                            compilationService.compileAsync(version, version.getCompilation().firmware_version_lib);
                        }

                        sleep(10000);
                    }
                }

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            logger.debug("thread - thread stopped on {}", new Date());
        }
    };
}
