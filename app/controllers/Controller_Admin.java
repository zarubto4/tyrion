package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;
import models.*;
import play.Application;
import play.Configuration;
import play.data.Form;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.Server;
import utilities.logger.Class_Logger;
import utilities.logger.ServerLogger;
import utilities.logger.YouTrack;
import utilities.login_entities.Secured_API;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.scheduler.CustomScheduler;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Swagger_Report_Admin_Dashboard;
import utilities.swagger.outboundClass.Swagger_ServerUpdates;
import utilities.update_server.GitHub_Asset;
import utilities.update_server.GitHub_Release;
import utilities.update_server.ServerUpdate;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured_API.class)
public class Controller_Admin extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Admin.class);

    @Inject
    WSClient ws;

    @Inject
    Application application;

    @ApiOperation(
            value = "get Report_Admin_Dashboard",
            tags = {"Admin-Report"},
            notes = "Special API only For PRIVATE use",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully removed",      response = Swagger_Report_Admin_Dashboard.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result report_admin_dashboard() {
        try {

            Swagger_Report_Admin_Dashboard report = new Swagger_Report_Admin_Dashboard();

            report.person_registration = Model_Person.find.findRowCount();
            report.project_created     = Model_Project.find.findRowCount();
            report.board_registered = Model_Board.find.findRowCount();

            report.homer_server_public_created     = Model_HomerServer.find.findRowCount();
            report.homer_server_private_created    = 0;

            report.homer_server_public_online      = Controller_WebSocket.homer_servers.size();
            report.homer_server_private_online     = 0;

            report.compilation_server_public_created = Model_CompilationServer.find.where().findRowCount();
            report.compilation_server_public_online  = Controller_WebSocket.compiler_cloud_servers.size();

            report.bugs_reported = Model_ServerError.find.findRowCount();

            return GlobalResult.result_ok(Json.toJson(report));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Bugs get all",
            tags = {"Admin-Report"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",                 response = Model_ServerError.class, responseContainer = "list"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result serverError_getAll() {
        try {

            List<Model_ServerError> errors = Model_ServerError.find.all();

            return GlobalResult.result_ok(Json.toJson(errors));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Bug get",
            tags = {"Admin-Report"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",                 response = Model_ServerError.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result serverError_get(@ApiParam(value = "bug_id String path", required = true) String bug_id) {
        try {

            Model_ServerError error = Model_ServerError.find.byId(bug_id);
            if (error == null) return GlobalResult.result_notFound("Bug not found");

            return GlobalResult.result_ok(Json.toJson(error));

        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
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
                            dataType = "utilities.swagger.documentationClass.Swagger_Bug_Description",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_ServerError.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result serverError_addDescription(@ApiParam(value = "bug_id String path", required = true) String bug_id) {
        try {

            final Form<Swagger_Bug_Description> form = Form.form(Swagger_Bug_Description.class).bindFromRequest();
            if (form.hasErrors()) return GlobalResult.result_invalidBody(form.errorsAsJson());
            Swagger_Bug_Description help = form.get();

            Model_ServerError error = Model_ServerError.find.byId(bug_id);
            if (error == null) return GlobalResult.result_notFound("Bug not found");

            if (!error.edit_permission()) return GlobalResult.result_forbidden();

            error.description = help.description;
            error.update();

            return GlobalResult.result_ok(Json.toJson(error));
        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Bug report",
            tags = { "Admin-Report"},
            notes = "Reports bug to YouTrack.",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_ServerError.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result serverError_report(@ApiParam(value = "bug_id String path", required = true) String bug_id) {
        try {

            Model_ServerError error = Model_ServerError.find.byId(bug_id);
            if (error == null) return GlobalResult.result_notFound("Bug not found");

            if (!error.edit_permission()) return GlobalResult.result_forbidden();

            error.youtrack_url = YouTrack.report(error);
            error.update();

            return GlobalResult.result_ok(Json.toJson(error));
        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Bug delete",
            tags = {"Admin-Report"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result serverError_delete(@ApiParam(value = "bug_id String path", required = true) String bug_id) {
        try {
            Model_ServerError error = Model_ServerError.find.byId(bug_id);
            if (error == null) return GlobalResult.result_notFound("Bug not found");

            if (!error.delete_permission()) return GlobalResult.result_forbidden();

            error.delete();

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Bug delete all",
            tags = {"Admin-Report"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result serverError_deleteAll() {
        try {
            List<Model_ServerError> errors = Model_ServerError.find.all();

            if (!errors.isEmpty()) {
                if (!errors.get(0).delete_permission()) return GlobalResult.result_forbidden();
                Ebean.delete(errors);
            }

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

// UPDATE SERVER #######################################################################################################

    @ApiOperation(value = "update Server Server_Component",
            tags = {"Admin"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_ServerUpdate",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result server_scheduleUpdate() {
        try {

            // Must be built by 'activator dist' for this feature to work correctly
            if (application.isDev()) {
                return GlobalResult.result_badRequest("This feature is available only in production mode.");
            }

            // Zpracování Json
            final Form<Swagger_ServerUpdate> form = Form.form(Swagger_ServerUpdate.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_ServerUpdate help = form.get();

            terminal_logger.debug("server_scheduleUpdate - requesting releases");

            WSResponse releases = ws.url(Configuration.root().getString("GitHub.releasesUrl") + "/tags/" + help.version)
                    .setHeader("Authorization", "token " + Configuration.root().getString("GitHub.apiKey"))
                    .get()
                    .get(10000);

            final Form<GitHub_Release> release_form = Form.form(GitHub_Release.class).bind(releases.asJson());
            if (release_form.hasErrors()) {return GlobalResult.result_externalServerError(release_form.errorsAsJson());}
            GitHub_Release release = release_form.get();

            terminal_logger.debug("server_scheduleUpdate - got release");

            Optional<GitHub_Asset> optional = release.assets.stream().filter(a -> a.name.equals("dist.zip")).findAny();
            if (optional.isPresent()) {
                GitHub_Asset asset = optional.get();

                terminal_logger.debug("server_scheduleUpdate - Asset was found");

                ServerUpdate update = new ServerUpdate();
                update.server = "tyrion";
                update.version = help.version;
                update.time = help.update_time;
                update.url = asset.url;

                CustomScheduler.scheduleUpdateServer(update);
            } else {
                return GlobalResult.result_badRequest("Bad release, cannot find the asset file");
            }

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get server updates",
            tags = {"Admin"},
            notes = "",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",                 response = Swagger_ServerUpdates.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result server_getUpdates() {
        try {

            terminal_logger.debug("server_getUpdates - requesting releases");

            WSResponse response = ws.url(Configuration.root().getString("GitHub.releasesUrl"))
                    .setHeader("Authorization", "token " + Configuration.root().getString("GitHub.apiKey"))
                    .setHeader("Accept", "application/json")
                    .get()
                    .get(10000);

            int status = response.getStatus();

            terminal_logger.debug("server_getUpdates - got response, status {}", status);

            if (status != 200) {
                String body = response.getBody();
                terminal_logger.internalServerError(new Exception("Error response from GitHub. Status was " + status + " and body: " + body));
                return GlobalResult.result_custom(status, body);
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            List<Swagger_GitHubReleases> releases;

            try {
                releases = mapper.readValue(response.asJson().toString(), new TypeReference<List<Swagger_GitHubReleases>>() {});
            } catch (Exception e) {
                terminal_logger.internalServerError(e);
                return GlobalResult.result_externalServerError("Cannot parse response from GitHub");
            }

            terminal_logger.debug("server_getUpdates - number of releases {}", releases.size());

            Swagger_ServerUpdates updates = new Swagger_ServerUpdates();

            releases.stream().filter(release -> {

                if (release.draft || release.prerelease || release.assets.stream().noneMatch(asset -> asset.name.equals("dist.zip"))) {
                    terminal_logger.debug("server_getUpdates - release is only draft or has not dist package");
                    return false;
                }

                terminal_logger.debug("server_getUpdates - filtering depending on mode, release: {}", Json.toJson(release));

                switch (Server.server_mode) {
                    case developer: {
                        Pattern pattern = Pattern.compile("^(v)(\\d+\\.)(\\d+\\.)(\\d+)(-(.)*)?$");
                        Matcher matcher = pattern.matcher(release.tag_name);
                        if (!matcher.find()) {
                            terminal_logger.debug("server_getUpdates - release is invalid for developer mode");
                            return false;
                        }
                        break;
                    }
                    case stage: {
                        Pattern pattern = Pattern.compile("^(v)(\\d+\\.)(\\d+\\.)(\\d+)(-beta(.)*)?$");
                        Matcher matcher = pattern.matcher(release.tag_name);
                        if (!matcher.find()) {
                            terminal_logger.debug("server_getUpdates - release is invalid for stage mode");
                            return false;
                        }
                        break;
                    }
                    case production: {
                        Pattern pattern = Pattern.compile("^(v)(\\d+\\.)(\\d+\\.)(\\d+)$");
                        Matcher matcher = pattern.matcher(release.tag_name);
                        if (!matcher.find()) {
                            terminal_logger.debug("server_getUpdates - release is invalid for production mode");
                            return false;
                        }
                        break;
                    }
                    default: // empty
                }

                String version = release.tag_name.replace("v", "");
                String current = Server.server_version;

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
                        terminal_logger.debug("server_getUpdates - release is older than current running version");
                        return false;
                    }
                }

                return true;

            }).forEach(release -> updates.releases.add(release));

            terminal_logger.debug("server_getUpdates - got releases");

            return GlobalResult.result_ok(Json.toJson(updates));
        } catch (Exception e) {
            return ServerLogger.result_internalServerError(e, request());
        }
    }
}
