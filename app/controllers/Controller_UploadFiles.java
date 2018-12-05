package controllers;

import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import play.http.HttpEntity;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.ResponseHeader;
import play.mvc.Result;
import utilities.logger.Logger;
import utilities.notifications.NotificationService;
import utilities.permission.PermissionService;
import utilities.swagger.input.Swagger_GitHubReleases;
import utilities.swagger.input.Swagger_GitHubReleases_Asset;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_UploadFiles extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_UploadFiles.class);


// CONTROLLER CONFIGURATION ############################################################################################

    @Inject
    public Controller_UploadFiles(WSClient ws, _BaseFormFactory formFactory, Config config, PermissionService permissionService, NotificationService notificationService) {
        super(ws, formFactory, config, permissionService, notificationService);
    }

// UPLOUD FILES ########################################################################################################

    @ApiOperation(value = "download Homer Server", hidden = true)
    public Result homer_server_download(String component) {
        try {

            if(!(component.equals("mac") || component.equals("linux") || component.equals("windows"))) {
                return badRequest("Supported parameters are only 'linux', 'windows' and 'mac'");
            }


            /**
             * Stáhnu si poslední release
             * https://developer.github.com/v3/repos/releases/#get-the-latest-release
             *
             */
            WSResponse ws_response_get_all_releases = ws.url("https://api.github.com/repos/ByzanceIoT/homer-core/releases/latest")
                    .addHeader("Authorization", "token " + config.getString("GitHub.apiKey"))
                    .addHeader("Accept", "application/json")
                    .get()
                    .toCompletableFuture()
                    .get();

            if (ws_response_get_all_releases.getStatus() != 200) {
                logger.error("Error Message from Github: {}", ws_response_get_all_releases.getBody());
                return badRequest("Invalid latest Release - ERROR on Github - Contact Technical Support");
            }

            // Get and Validate Object
            Swagger_GitHubReleases help = formFactory.formFromJsonWithValidation(Swagger_GitHubReleases.class, ws_response_get_all_releases.asJson());

            System.out.println("Release Number: " +  help.name);
            System.out.println("Component Name: " +  component);

            //  http://localhost:9000/homer_server/download/linux


            Integer file_id = null;

            for(Swagger_GitHubReleases_Asset asset : help.assets) {

                System.out.println("Asset File name: "+  asset.name);

                if(component.equals("mac") && asset.name.equals("homer-server-mac")) {
                    file_id = asset.id;
                }

                if(component.equals("linux") && asset.name.equals("homer-server-linux")) {
                    file_id = asset.id;
                }

                if(component.equals("windows") && asset.name.equals("homer-server-windows")) {
                    file_id = asset.id;
                }
            }

            if (file_id == null) {
                return badRequest("Latest Release not contains your selected platform");
            }

            WSResponse download = ws.url("https://api.github.com/repos/ByzanceIoT/homer-core/releases/assets/" + file_id + "?access_token=" + config.getString("GitHub.apiKey"))
                    .addHeader("Accept", "application/octet-stream")
                    .setFollowRedirects(true)
                    .setRequestTimeout(Duration.ofMinutes(60))
                    .stream()
                    .toCompletableFuture()
                    .get();

            Source<ByteString, ?> source = download.getBodyAsSource();

            String file_name = "";

            if(component.equals("mac")) {
                file_name = "homer_server_mac";
            }

            if(component.equals("linux")) {
                file_name = "homer_server_linux";
            }

            if(component.equals("windows")) {
                file_name = "homer_server_windows.exe";
            }

            Controller.response().setHeader("Content-Disposition", "attachment; filename=" + file_name);
            return new Result(
                    new ResponseHeader(200, Collections.emptyMap()),
                    new HttpEntity.Streamed(source, Optional.empty(), Optional.of("application/octet-stream"))
            );

        } catch (Exception e) {
            e.printStackTrace();
            return badRequest(e.toString());
        }
    }

}
