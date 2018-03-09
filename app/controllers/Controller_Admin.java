package controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.ebean.Ebean;
import io.swagger.annotations.*;
import models.*;
import play.Environment;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.Server;
import utilities.authentication.Authentication;
import utilities.logger.Logger;
import utilities.logger.YouTrack;
import utilities.scheduler.SchedulerController;
import utilities.swagger.input.*;
import utilities.swagger.output.Swagger_Report_Admin_Dashboard;
import utilities.swagger.output.Swagger_ServerUpdates;
import utilities.update_server.GitHub_Asset;
import utilities.update_server.GitHub_Release;
import utilities.update_server.ServerUpdate;


import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Authentication.class)
public class Controller_Admin extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Admin.class);

// CONTROLLER CONFIGURATION ############################################################################################

    private _BaseFormFactory baseFormFactory;
    private WSClient ws;
    private Environment environment;
    private YouTrack youTrack;
    private Config config;
    private SchedulerController scheduler;

    @Inject
    public Controller_Admin(Environment environment, WSClient ws, _BaseFormFactory formFactory, YouTrack youTrack, Config config, SchedulerController scheduler) {
        this.environment = environment;
        this.ws = ws;
        this.baseFormFactory = formFactory;
        this.youTrack = youTrack;
        this.config = config;
        this.scheduler = scheduler;
    }

// CONTROLLER CONTENT ##################################################################################################

    @ApiOperation(
            value = "get Report_Admin_Dashboard",
            tags = {"Admin-Report"},
            notes = "Special API only For PRIVATE use",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok result",                 response = Swagger_Report_Admin_Dashboard.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result report_admin_dashboard() {
        try {

            person().is_admin();

            Swagger_Report_Admin_Dashboard report = new Swagger_Report_Admin_Dashboard();

            report.person_registration = Model_Person.find.query().findCount();
            report.project_created = Model_Project.find.query().findCount();
            report.board_registered = Model_Hardware.find.query().findCount();

            report.homer_server_public_created     = Model_HomerServer.find.query().findCount();
            report.homer_server_private_created    = 0;

            report.homer_server_public_online      = Controller_WebSocket.homers.size();
            report.homer_server_private_online     = 0;

            report.compilation_server_public_created = Model_CompilationServer.find.query().where().findCount();
            report.compilation_server_public_online  = Controller_WebSocket.compilers.size();

            report.bugs_reported = Model_ServerError.find.query().findCount();

            return ok(Json.toJson(report));

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "Bugs get all",
            tags = {"Admin-Report"},
            notes = "", //TODO
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",                 response = Model_ServerError.class, responseContainer = "list"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result serverError_getAll() {
        try {

            person().is_admin();

            List<Model_ServerError> errors = Model_ServerError.find.all();

            return ok(Json.toJson(errors));

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "Bug get",
            tags = {"Admin-Report"},
            notes = "",     //TODO
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",                 response = Model_ServerError.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    
    public Result serverError_get(@ApiParam(value = "bug_id String path", required = true) UUID bug_id) {
        try {

            Model_ServerError error = Model_ServerError.getById(bug_id);

            return ok(Json.toJson(error));

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "Bug report",
            tags = { "Admin-Report"},
            notes = "Reports bug to YouTrack with description.",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Bug_Description",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_ServerError.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result serverError_addDescription(@ApiParam(value = "bug_id String path", required = true) UUID bug_id) {
        try {

            // Get and Validate Object
            Swagger_Bug_Description help  = baseFormFactory.formFromRequestWithValidation(Swagger_Bug_Description.class);

            // Kontrola objektu
            Model_ServerError error = Model_ServerError.getById(bug_id);

            error.description = help.description;
            error.update();

            return ok(Json.toJson(error));
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "Bug report",
            tags = { "Admin-Report"},
            notes = "Reports bug to YouTrack.",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_ServerError.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result serverError_report(@ApiParam(value = "bug_id String path", required = true) UUID bug_id) {
        try {

            Model_ServerError error = Model_ServerError.getById(bug_id);

            error.youtrack_url = youTrack.report(error);
            error.update();

            return ok(Json.toJson(error));
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "Bug delete",
            tags = {"Admin-Report"},
            notes = "",     //TODO
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })

    public Result serverError_delete(@ApiParam(value = "bug_id String path", required = true) UUID bug_id) {
        try {

            Model_ServerError error = Model_ServerError.getById(bug_id);
            error.delete();

            return ok();
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "Bug delete all",
            tags = {"Admin-Report"},
            notes = "",     //TODO
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result serverError_deleteAll() {
        try {
            List<Model_ServerError> errors = Model_ServerError.find.all();

            if (!errors.isEmpty()) {
                errors.get(0).check_delete_permission();
                Ebean.delete(errors);
            }

            return ok();
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// UPDATE SERVER #######################################################################################################

    @ApiOperation(value = "update Server Server_Component",
            tags = {"Admin"},
            notes = "",         //TODO
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_ServerUpdate",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result server_scheduleUpdate() {
        try {

            // Must be built by 'activator dist' for this feature to work correctly
            if (environment.isDev()) {
                return badRequest("This feature is available only in production mode.");
            }

            // Get and Validate Object
            Swagger_ServerUpdate help  = baseFormFactory.formFromRequestWithValidation(Swagger_ServerUpdate.class);

            logger.debug("server_scheduleUpdate - requesting releases");

            WSResponse releases = ws.url(config.getString("GitHub.releasesUrl") + "/tags/" + help.version)
                    .addHeader("Authorization", "token " + config.getString("GitHub.apiKey"))
                    .get()
                    .toCompletableFuture()
                    .get();

            // Get and Validate Object
            GitHub_Release release = baseFormFactory.formFromJsonWithValidation(GitHub_Release.class, (releases.asJson()));

            logger.debug("server_scheduleUpdate - got release");

            Optional<GitHub_Asset> optional = release.assets.stream().filter(a -> a.name.equals("dist.zip")).findAny();
            if (optional.isPresent()) {
                GitHub_Asset asset = optional.get();

                logger.debug("server_scheduleUpdate - Asset was found");

                ServerUpdate update = new ServerUpdate();
                update.server = "tyrion";
                update.version = help.version;
                update.time = help.update_time;
                update.url = asset.url;

                scheduler.scheduleUpdateServer(update);
            } else {
                return badRequest("Bad release, cannot find the asset file");
            }

            return ok();
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get server updates",
            tags = {"Admin"},
            notes = "",     //TODO
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",                 response = Swagger_ServerUpdates.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result server_getUpdates() {
        try {

            logger.debug("server_getUpdates - requesting releases");

            WSResponse response = ws.url(config.getString("GitHub.releasesUrl"))
                    .addHeader("Authorization", "token " + config.getString("GitHub.apiKey"))
                    .addHeader("Accept", "application/json")
                    .get()
                    .toCompletableFuture()
                    .get();

            int status = response.getStatus();

            logger.debug("server_getUpdates - got response, status {}", status);

            if (status != 200) {
                String body = response.getBody();
                logger.internalServerError(new Exception("Error response from GitHub. Status was " + status + " and body: " + body));
                return customResult(status, "GitHub_Error", body);
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            List<Swagger_GitHubReleases> releases;

            try {
                releases = mapper.readValue(response.asJson().toString(), new TypeReference<List<Swagger_GitHubReleases>>() {});
            } catch (Exception e) {
                logger.internalServerError(e);
                return externalServerError("Cannot parse response from GitHub");
            }

            logger.debug("server_getUpdates - number of releases {}", releases.size());

            Swagger_ServerUpdates updates = new Swagger_ServerUpdates();

            releases.stream().filter(release -> {

                if (release.draft || release.prerelease || release.assets.stream().noneMatch(asset -> asset.name.equals("dist.zip"))) {
                    logger.debug("server_getUpdates - release is only draft or has not dist package");
                    return false;
                }

                logger.debug("server_getUpdates - filtering depending on mode, release: {}", Json.toJson(release));

                switch (Server.mode) {
                    case DEVELOPER: {
                        Pattern pattern = Pattern.compile("^(v)(\\d+\\.)(\\d+\\.)(\\d+)(-(.)*)?$");
                        Matcher matcher = pattern.matcher(release.tag_name);
                        if (!matcher.find()) {
                            logger.debug("server_getUpdates - release is invalid for developer mode");
                            return false;
                        }
                        break;
                    }
                    case STAGE: {
                        Pattern pattern = Pattern.compile("^(v)(\\d+\\.)(\\d+\\.)(\\d+)(-beta(.)*)?$");
                        Matcher matcher = pattern.matcher(release.tag_name);
                        if (!matcher.find()) {
                            logger.debug("server_getUpdates - release is invalid for stage mode");
                            return false;
                        }
                        break;
                    }
                    case PRODUCTION: {
                        Pattern pattern = Pattern.compile("^(v)(\\d+\\.)(\\d+\\.)(\\d+)$");
                        Matcher matcher = pattern.matcher(release.tag_name);
                        if (!matcher.find()) {
                            logger.debug("server_getUpdates - release is invalid for production mode");
                            return false;
                        }
                        break;
                    }
                    default: // empty
                }

                String version = release.tag_name.replace("v", "");
                String current = Server.version;

                if (version.contains("-")) {
                    version = version.substring(0, version.indexOf("-"));
                }

                String[] versionNumbers = version.split("\\.");
                String[] currentNumbers = current.split("\\.");

                // If version is higher than current
                for (int i = 0; i < versionNumbers.length; i++) {
                    Long versionNumber = new Long(versionNumbers[i]);
                    Long currentNumber = new Long(currentNumbers[i]);
                    if (versionNumber < currentNumber || (i == versionNumbers.length - 1 && versionNumber.equals(currentNumber))) {
                        logger.debug("server_getUpdates - release is older than current running version");
                        return false;
                    }
                }

                return true;

            }).forEach(release -> updates.releases.add(release));

            logger.debug("server_getUpdates - got releases");

            return ok(Json.toJson(updates));
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
}
