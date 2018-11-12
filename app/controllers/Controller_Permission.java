package controllers;

import com.typesafe.config.Config;
import io.swagger.annotations.*;
import models.Model_Person;
import models.Model_Permission;
import play.Environment;
import play.libs.ws.WSClient;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.authentication.Authentication;
import utilities.logger.Logger;
import utilities.logger.YouTrack;
import utilities.permission.PermissionService;
import utilities.scheduler.SchedulerService;
import utilities.swagger.output.Swagger_System_Access;

import java.util.UUID;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Authentication.class)
public class Controller_Permission extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Permission.class);

// CONTROLLER CONFIGURATION ############################################################################################

    @javax.inject.Inject
    public Controller_Permission(Environment environment, WSClient ws, _BaseFormFactory formFactory, YouTrack youTrack, Config config, SchedulerService scheduler, PermissionService permissionService) {
        super(environment, ws, formFactory, youTrack, config, scheduler, permissionService);
    }

// #####################################################################################################################

    @ApiOperation(value = "get Permissions All",
            tags = {"Admin-Permission"},
            notes = "Get all user Permission. You need permission for that or have right system Roles",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result", response = Model_Permission.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result permission_get_all() {
        try {
            return ok(Model_Permission.find.all());
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// ######################################################################################################################

    @ApiOperation(value = "get Person Roles and Permissions",
            tags = {"Admin-Role", "Admin-Permission", "Person"},
            notes = "This api return List of Roles and List of Permission",
            produces = "application/json",
            response = Swagger_System_Access.class,
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_System_Access.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result system_access_get_everything(UUID person_id) {
        try {

            Model_Person person = Model_Person.find.byId(person_id);

            this.checkReadPermission(person);

            Swagger_System_Access system_access = new Swagger_System_Access();
            system_access.roles = person.roles;
            system_access.permissions = person.permissions;

            return ok(system_access);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
}
