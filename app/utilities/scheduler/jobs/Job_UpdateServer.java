package utilities.scheduler.jobs;

import models.Model_HomerServer;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import play.Configuration;
import play.api.Play;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import utilities.logger.Class_Logger;
import utilities.update_server.GitHub_Asset;
import utilities.update_server.GitHub_Release;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Updates various servers, even itself
 */
public class Job_UpdateServer implements Job {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Job_UpdateServer.class);

//**********************************************************************************************************************

    public Job_UpdateServer(){}

    private JobDataMap jobData;
    
    public void execute(JobExecutionContext context) throws JobExecutionException {

        terminal_logger.info("execute: Executing Job_UpdateServer");

        jobData = context.getMergedJobDataMap();

        if(!update_server_thread.isAlive()) update_server_thread.start();
    }

    private Thread update_server_thread = new Thread() {

        @Override
        public void run() {
            try {

                terminal_logger.trace("update_server_thread: concurrent thread started on {}", new Date());

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

                WSClient ws = Play.current().injector().instanceOf(WSClient.class);

                switch (server) {
                    case "tyrion": {
                        // TODO execute script
                        terminal_logger.trace("update_server_thread: Updating Tyrion");

                        WSResponse wsResponse = ws.url(jobData.getString("url"))
                                .setHeader("Authorization", "token " + Configuration.root().getString("GitHub.apiKey"))
                                .setHeader("Accept", "application/octet-stream")
                                .setFollowRedirects(false)
                                .get()
                                .get(10000);

                        terminal_logger.trace("update_server_thread: Got file download url");
                        terminal_logger.trace(wsResponse.getHeader("location"));

                        // Redirect URL from response
                        String url = wsResponse.getHeader("location");

                        // Request for URL without query params
                        WSRequest request = ws.url(url.substring(0, url.indexOf("?")))
                                .setRequestTimeout(-1);

                        // Query params must be decoded and added one by one, because of bug in Play! (query was getting double encoded)
                        String[] pairs = url.substring(url.indexOf("?") + 1).split("&");
                        for (String pair : pairs) {
                            int idx = pair.indexOf("=");
                            request.setQueryParameter(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                        }

                        WSResponse response = request.get().get(30, TimeUnit.MINUTES);

                        terminal_logger.debug("update_server_thread: Status for file download is {}", response.getStatus());

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

                        terminal_logger.trace("update_server_thread: File downloaded, run update script");

                        Process proc = Runtime.getRuntime().exec("./update_server.sh " + jobData.getString("version"));

                        BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                        String line;
                        while ((line = in.readLine()) != null) {
                            terminal_logger.info("update_server_thread: Process output: {}", line);
                        }

                        BufferedReader err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                        String err_line;
                        while ((err_line = err.readLine()) != null) {
                            terminal_logger.info("update_server_thread: Process output: {}", err_line);
                        }

                        terminal_logger.debug("update_server_thread: Process exit code: {}", proc.waitFor());

                        break;
                    }
                    case "homer": {
                        Model_HomerServer homerServer = Model_HomerServer.get_byId(""); // TODO
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
                terminal_logger.internalServerError(e);
            }

            terminal_logger.trace("update_server_thread: thread stopped on {}", new Date());
        }
    };
}