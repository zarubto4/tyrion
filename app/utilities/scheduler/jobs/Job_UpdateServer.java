package utilities.scheduler.jobs;


import com.google.inject.Inject;
import com.typesafe.config.Config;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import play.inject.ApplicationLifecycle;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import utilities.logger.Logger;

import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Updates various servers, even itself
 */
public class Job_UpdateServer implements Job {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Job_UpdateServer.class);

//**********************************************************************************************************************

    private WSClient ws;
    private Config config;

    @Inject
    public Job_UpdateServer(WSClient ws, Config config, ApplicationLifecycle appLifecycle) {
        this.ws = ws;
        this.config = config;
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

    private JobDataMap jobData;
    
    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("execute: Executing Job_UpdateServer");

        jobData = context.getMergedJobDataMap();

        if (!thread.isAlive()) thread.start();
    }

    private Thread thread = new Thread() {

        @Override
        public void run() {
            try {

                logger.trace("thread: concurrent thread started on {}", new Date());

                if (jobData == null) {
                    throw new NullPointerException("Job was instantiated without jobData in the JobExecutionContext.");
                }

                if (!jobData.containsKey("server")) {
                    throw new NullPointerException("Job was instantiated without server in the JobExecutionContext or the server is null for some reason.");
                }

                String server = jobData.getString("server");

                if (!jobData.containsKey("identifier") && !server.equals("tyrion")) {
                    throw new NullPointerException("Job was instantiated without identifier in the JobExecutionContext or the identifier is null for some reason.");
                }

                if (!jobData.containsKey("version")) {
                    throw new NullPointerException("Job was instantiated without version in the JobExecutionContext or the version is null for some reason.");
                }

                switch (server) {
                    case "tyrion": {

                        logger.trace("thread: Updating Tyrion");

                        WSResponse wsResponse = ws.url(jobData.getString("url"))
                                .addHeader("Authorization", "token " + config.getString("GitHub.apiKey"))
                                .addHeader("Accept", "application/octet-stream")
                                .setFollowRedirects(false)
                                .get()
                                .toCompletableFuture()
                                .get();

                        logger.trace("thread: Got file download url");

                        // Redirect URL from response
                        String url;

                        Optional<String> optional = wsResponse.getSingleHeader("location");
                        if (optional.isPresent()) {
                            url = optional.get();
                        } else {
                            throw new Exception("Location header is missing");
                        }

                        // Request for URL without query params
                        WSRequest request = ws.url(url.substring(0, url.indexOf("?")))
                                .setRequestTimeout(Duration.ofMinutes(30));

                        // Query params must be decoded and added one by one, because of bug in Play! (query was getting double encoded)
                        String[] pairs = url.substring(url.indexOf("?") + 1).split("&");
                        for (String pair : pairs) {
                            int idx = pair.indexOf("=");
                            request.addQueryParameter(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                        }

                        WSResponse response = request.get().toCompletableFuture().get(30, TimeUnit.MINUTES);

                        logger.debug("thread: Status for file download is {}", response.getStatus());

                        Path path = Paths.get("../dist.zip");
                        InputStream inputStream = response.getBodyAsStream();
                        OutputStream outputStream = Files.newOutputStream(path);

                        int read;
                        byte[] buffer = new byte[1024];
                        while ((read = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, read);
                        }

                        if (inputStream != null) {inputStream.close();}
                        if (outputStream != null) {outputStream.close();}

                        logger.trace("thread: File downloaded, run update script");

                        // Runtime.getRuntime().exec("./update_wrapper.sh " + jobData.getString("version") + " &");

                        ProcessBuilder processBuilder = new ProcessBuilder("./update.sh", jobData.getString("version"));
                        processBuilder.redirectOutput(new File("./update.log"));
                        processBuilder.redirectError(new File("./update.log"));
                        processBuilder.start();

                        break;
                    }
                    case "homer": {
                        // Model_HomerServer homerServer = Model_HomerServer.find.byId(""); // TODO [LEXA]
                        // if (homerServer == null) throw new NullPointerException("Cannot find the Homer Server in the DB.");

                        // TODO do request
                        break;
                    }
                    case "code": {
                        // TODO do update
                        break;
                    }
                    default: throw new IllegalStateException("Server must be set to one of: tyrion, homer or code. Value was: " + server);
                }

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            logger.trace("thread: thread stopped on {}", new Date());
        }
    };
}