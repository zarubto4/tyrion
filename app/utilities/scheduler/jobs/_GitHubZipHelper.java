package utilities.scheduler.jobs;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import controllers._BaseFormFactory;
import org.apache.commons.io.FileUtils;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import utilities.logger.Logger;
import utilities.swagger.input.Swagger_GitHubReleases;
import utilities.swagger.input.Swagger_GitHubReleases_List;

import java.io.*;
import java.net.URLDecoder;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public abstract class _GitHubZipHelper {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(_GitHubZipHelper.class);

    protected WSClient ws;
    protected Config config;
    protected _BaseFormFactory formFactory;


    public _GitHubZipHelper(WSClient ws, Config config, _BaseFormFactory formFactory) {
        this.ws = ws;
        this.config = config;
        this.formFactory = formFactory;
    }
//**********************************************************************************************************************

    public void remove_file (String path) {

        try {

            if (new File(path).isDirectory()) {
                FileUtils.deleteDirectory(new File(path));
            } else {

                if(new File(path).delete()){
                    logger.trace(path + " is deleted!");
                }else{

                    logger.error("Delete " +path + " operation is failed.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final int BUFFER_SIZE = 4096;




    public List<Swagger_GitHubReleases> request_list() throws ExecutionException, InterruptedException {


        /**
         * Stáhnu si seznam všech releasů z GitHubu. Podle dohody je zde mix releasů.
         *
         *
         * Distribuce knihoven:
         *
         * Distribuce Bootloader Verze - kde je povolená jediná vyjímka na konvenci verzování a to na začátku.
         * Nejdříve je použit Target name a poté "mark" že jde o bootloader a verzi.
         * Například: YODA_G3E_bootloader_v1.0.2, opět stejnou logikou -beta a alfa.
         * Vše ostatní je automatizované. Tyrion si stáhne, překopíruje do vlasntího archivu na BLOB server,
         * doplní si údaje a dále distribuje.
         */
        WSResponse ws_response_get_all_releases = ws.url("https://api.github.com/repos/ByzanceIoT/hw-libs/releases")
                .addHeader("Authorization", "token " + config.getString("GitHub.apiKey"))
                .addHeader("Accept", "application/json")
                .get()
                .toCompletableFuture()
                .get();

        if (ws_response_get_all_releases.getStatus() != 200) {
            logger.error("Permission Error in Job_CheckCompilationLibraries. Please Check it");
            logger.error("Error Message from Github: {}", ws_response_get_all_releases.getBody());
            return null;
        }

        // Získám seznam všech objektů z Githubu
        ObjectNode request_list = Json.newObject();
        request_list.set("list", ws_response_get_all_releases.asJson());

        Swagger_GitHubReleases_List list = formFactory.formFromJsonWithValidation(Swagger_GitHubReleases_List.class, request_list);
        return list.list;
    }



    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     * @param zipFilePath
     * @param destDirectory
     * @throws IOException
     */
    public void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();

            if(filePath.charAt(0) != '.') {

                if (!entry.isDirectory()) {
                    // if the entry is a file, extracts it

                    try {
                        extractFile(zipIn, filePath);

                    } catch (Exception e) {
                        logger.error("unzip:error: name: {} filePath {} ", entry.getName(),  filePath);
                        logger.error("unzip:error:" + e.getMessage());
                        break;
                    }
                } else {
                    // if the entry is a directory, make the directory
                    File dir = new File(filePath);
                    dir.mkdir();
                }
            }

            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }


    public WSResponse download_file(String assets_url) {
        try {

            WSResponse wsResponse = ws.url(assets_url)
                    .addHeader("Authorization", "token 4d89903b259510a1257a67d396bd4aaf10cdde6a")
                    .addHeader("Accept", "application/octet-stream")
                    .setFollowRedirects(false)
                    .get()
                    .toCompletableFuture()
                    .get();

            logger.trace("download_file: Got file download url");

            String url;

            Optional<String> optional = wsResponse.getSingleHeader("location");
            if (optional.isPresent()) {
                url = optional.get();
            } else {
                logger.error("download_file: optional.isPresent() == false");
                return null;
            }

            logger.trace("update_server_thread - url: {}", url);

            // Request for URL without query params
            WSRequest request = ws.url(url.substring(0, url.indexOf("?")))
                    .setRequestTimeout(Duration.ofMinutes(30));

            // Query params must be decoded and added one by one, because of bug in Play! (query was getting double encoded)
            String[] pairs = url.substring(url.indexOf("?") + 1).split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                request.addQueryParameter(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }

            return request.get().toCompletableFuture().get(30, TimeUnit.MINUTES);

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

}
