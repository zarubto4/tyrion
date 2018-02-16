package utilities.scheduler.jobs;


import com.typesafe.config.Config;
import models.Model_HomerServer;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import utilities.logger.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
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

    public Job_UpdateServer(WSClient ws, Config config) {
        this.ws = ws;
        this.config = config;
    }

    private JobDataMap jobData;
    
    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("execute: Executing Job_UpdateServer");

        jobData = context.getMergedJobDataMap();

        if (!update_server_thread.isAlive()) update_server_thread.start();
    }

    private Thread update_server_thread = new Thread() {

        @Override
        public void run() {
            try {

                logger.trace("update_server_thread: concurrent thread started on {}", new Date());

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

                        logger.trace("update_server_thread: Updating Tyrion");

                        WSResponse wsResponse = ws.url(jobData.getString("url"))
                                .addHeader("Authorization", "token " + config.getString("GitHub.apiKey"))
                                .addHeader("Accept", "application/octet-stream")
                                .setFollowRedirects(false)
                                .get()
                                .toCompletableFuture()
                                .get();

                        logger.trace("update_server_thread: Got file download url");

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

                        logger.debug("update_server_thread: Status for file download is {}", response.getStatus());

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

                        logger.trace("update_server_thread: File downloaded, run update script");

                        Process proc = Runtime.getRuntime().exec("./update_server.sh " + jobData.getString("version") + " 2>&1 > ./update.log");

                        BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                        String line;
                        while ((line = in.readLine()) != null) {
                            logger.info("update_server_thread: Process output: {}", line);
                        }

                        BufferedReader err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                        StringBuilder err_log = new StringBuilder();
                        String err_line;
                        while ((err_line = err.readLine()) != null) {
                            logger.info("update_server_thread: Process output: {}", err_line);
                            err_log.append(err_line);
                            err_log.append("\n");
                        }

                        int exitCode = proc.waitFor();

                        logger.debug("update_server_thread: Process exit code: {}", exitCode);

                        if (exitCode != 0) {
                            logger.internalServerError(new Exception("Process exited with non-zero code " + exitCode + ". Errors:\n" + err_log.toString()));
                        }

                        break;
                    }
                    case "homer": {
                        Model_HomerServer homerServer = Model_HomerServer.getById(""); // TODO [LEXA]
                        if (homerServer == null) throw new NullPointerException("Cannot find the Homer Server in the DB.");

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

            logger.trace("update_server_thread: thread stopped on {}", new Date());
        }
    };
}