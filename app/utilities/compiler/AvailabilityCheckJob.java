package utilities.compiler;

import com.google.inject.Inject;
import models.Model_CompilationServer;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utilities.enums.NetworkStatus;
import utilities.logger.Logger;
import utilities.network.NetworkStatusService;
import websocket.WebSocketService;
import websocket.interfaces.Compiler;

import java.util.Date;
import java.util.List;

public class AvailabilityCheckJob implements Job {

    public static final String KEY = "availabilityCheck";

    private static final Logger logger = new Logger(AvailabilityCheckJob.class);

    private final WebSocketService webSocketService;
    private final CompilerService compilerService;
    private final NetworkStatusService networkStatusService;

    @Inject
    public AvailabilityCheckJob(WebSocketService webSocketService, CompilerService compilerService, NetworkStatusService networkStatusService) {
        this.webSocketService = webSocketService;
        this.compilerService = compilerService;
        this.networkStatusService = networkStatusService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {

            logger.info("execute - execution started on {}", new Date());

            List<Model_CompilationServer> compilers = Model_CompilationServer.find.all();

            compilers.forEach(compiler -> {
                if (!this.webSocketService.isRegistered(compiler.getId()) && compiler.server_url != null) {
                    this.webSocketService.create(Compiler.class, compiler.getId(), compiler.server_url)
                            .handle((nothing, exception) -> {
                                if (exception != null) {
                                    logger.internalServerError(exception);
                                } else {
                                    this.compilerService.checkAvailability();
                                    this.networkStatusService.setStatus(compiler, NetworkStatus.ONLINE);
                                }
                                return null;
                            });
                }
            });
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }
}
