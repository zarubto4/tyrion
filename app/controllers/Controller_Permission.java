package controllers;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.ebean.Ebean;
import io.ebean.Expr;
import io.swagger.annotations.*;
import models.Model_Person;
import models.Model_Permission;
import models.Model_Role;
import play.Environment;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.authentication.Authentication;
import utilities.logger.Logger;
import utilities.logger.YouTrack;
import utilities.scheduler.SchedulerController;
import utilities.swagger.input.*;
import utilities.swagger.output.Swagger_System_Access;

import java.util.List;
import java.util.UUID;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Authentication.class)
public class Controller_Permission extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Permission.class);

// CONTROLLER CONFIGURATION ############################################################################################

    @javax.inject.Inject
    public Controller_Permission(Environment environment, WSClient ws, _BaseFormFactory formFactory, YouTrack youTrack, Config config, SchedulerController scheduler) {
        super(environment, ws, formFactory, youTrack, config, scheduler);
    }

// #####################################################################################################################

    @ApiOperation(value = "add Permission to Person",
            tags = {"Admin-Permission"},
            notes = "If you want add permission to Person. You need permission for that or have right system Roles",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result", response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 400, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result permission_person_add(UUID person_id, UUID permission_id) {
        try {

            // Kontrola objektu
            Model_Person person = Model_Person.getById(person_id);

            // Kontrola objektu
            Model_Permission personPermission = Model_Permission.getById(permission_id);

            if (!person.permissions.contains(personPermission)) person.permissions.add(personPermission);
            person.update();

            return ok();


        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "remove Permission from Person",
            tags = {"Admin-Permission"},
            notes = "If you want remove permission from Person. You need permission for that or have right system Roles",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result", response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result permission_person_remove(UUID person_id, UUID permission_id) {
        try {

            // Kontrola objektu
            Model_Person person = Model_Person.getById(person_id);

            // Kontrola objektu
            Model_Permission personPermission = Model_Permission.getById(permission_id);

            if (person.permissions.contains(personPermission)) person.permissions.remove(personPermission);
            person.update();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

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

            List<Model_Permission> permissions = Model_Permission.find.query().where().orderBy("UPPER(name) ASC").findList();
            return ok(permissions);

        } catch (Exception e) {
            return controllerServerError(e);
        }

    }


    @ApiOperation(value = "edit Permission",
            tags = {"Admin-Permission"},
            notes = "edit permission description",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Permission_Edit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result", response = Model_Permission.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result permission_edit(UUID permission_id) {
        try {

            // Get and Validate Object
            Swagger_Permission_Edit help = formFromRequestWithValidation(Swagger_Permission_Edit.class);

            // Kontrola objektu
            Model_Permission permission = Model_Permission.getById(permission_id);

            permission.description = help.description;
            permission.update();

            return ok(permission);

        } catch (Exception e) {
            return controllerServerError(e);
        }

    }

// #####################################################################################################################

    @ApiOperation(value = "add Role Permissions",
            tags = {"Admin-Permission", "Admin-Role"},
            notes = "If you want add system permissions to Role. You need permission for that or have right system Roles",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Role_Add_Permission",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result permission_add_to_role(UUID role_id) {
        try {

            // Get and Validate Object
            Swagger_Role_Add_Permission help = formFromRequestWithValidation(Swagger_Role_Add_Permission.class);

            // Kontrola objektu
            List<Model_Permission> personPermissions = Model_Permission.find.query().where().in("name", help.permissions).findList();

            // Kontrola objektu
            Model_Role securityRole = Model_Role.getById(role_id);

            for(Model_Permission permission : personPermissions) {
                if (!securityRole.permissions.contains(permission)) {
                    securityRole.permissions.add(permission);
                }
            }

            securityRole.update();

            return ok(securityRole);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "remove Role Permission",
            tags = {"Admin-Permission", "Admin-Role"},
            notes = "If you want remove system permissions from Role. You need permission for that or have right system Roles",
            produces = "application/json",
            protocols = "https",
            consumes = "text/html"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result permission_remove_from_role(UUID permission_id, UUID role_id) {
        try {

            // Kontrola objektu
            Model_Permission personPermission = Model_Permission.getById(permission_id);

            // Kontrola objektu
            Model_Role securityRole = Model_Role.getById(role_id);

            if (securityRole.permissions.contains(personPermission)) securityRole.permissions.remove(personPermission);

            securityRole.update();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// ######################################################################################################################

    @ApiOperation(value = "create Role",
            tags = {"Admin-Role"},
            notes = "If you want create new Role in system. You need permission for that or have right system Roles",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_NameAndDescription",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_Role.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result role_create() {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help = formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_Role securityRole = new Model_Role();

            securityRole.name = help.name;
            securityRole.description = help.description;

            securityRole.save();

            return created(securityRole);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Role",
            tags = {"Admin-Role"},
            notes = "If you want delete  Role from system. You need permission for that or have right system Roles",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully created", response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result role_delete(UUID role_id) {
        try {

            // Kontrola objektu
            Model_Role securityRole = Model_Role.getById(role_id);

            securityRole.delete();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Role",
            tags = {"Admin-Role"},
            notes = "edit description",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_NameAndDescription",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result", response = Model_Role.class),
            @ApiResponse(code = 400, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result role_edit(UUID role_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help = formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_Role role = Model_Role.getById(role_id);

            role.name = help.name;
            role.description = help.description;

            role.update();

            return ok(role);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Role",
            tags = {"Admin-Role"},
            notes = "get description",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully created",      response = Model_Role.class),
            @ApiResponse(code = 400, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result role_get(UUID role_id) {
        try {

            // Kontrola objektu
            Model_Role role = Model_Role.getById(role_id);

            return ok(role);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "add Role Person",
            tags = {"Admin-Role", "Admin-Person"},
            notes = "If you set Role to Person. You need permission for that or have right system Roles",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Invite_Person",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Role.class),
            @ApiResponse(code = 400, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result role_add_person(UUID role_id) {
        try {

            // Get and Validate Object
            Swagger_Invite_Person help = formFromRequestWithValidation(Swagger_Invite_Person.class);

            if (help.persons_mail.isEmpty()) {
                return badRequest("Fill in some emails.");
            }

            // Kontrola objektu
            Model_Role securityRole = Model_Role.getById(role_id);


            List<Model_Person> persons = Model_Person.find.query().where().notExists(Ebean.find(Model_Role.class).where(Expr.in("persons.email", help.persons_mail))).in("email", help.persons_mail).findList();

            if (persons.isEmpty()) {
                return badRequest("No person to add was found for given email values.");
            }

            // Check Permission
            for(Model_Person person: persons){
                person.check_update_permission();
            }

            logger.debug("role_add_person: Adding {} person(s)", persons.size());

            securityRole.persons.addAll(persons);
            securityRole.update();


            return ok(securityRole);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "remove Role Person ",
            tags = {"Admin-Role", "Admin-Person"},
            notes = "If you set Role to Person. You need permission for that or have right system Roles",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result role_remove_person(UUID role_id, UUID person_id) {
        try {

            // Kontrola objektu
            Model_Person person = Model_Person.getById(person_id);

            // Kontrola objektu
            Model_Role securityRole = Model_Role.getById(role_id);

            if (person.roles.contains(securityRole)) person.roles.remove(securityRole);
            person.update();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Role All",
            tags = {"Admin-Role"},
            notes = "If you set Role to Person. You need permission for that or have right system Roles",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result", response = Model_Role.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result role_get_all() {
        try {

            List<Model_Role> roles = Model_Role.find.query().orderBy("UPPER(name) ASC").findList();
            return ok(roles);

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
            @ApiResponse(code = 200, message = "Ok Result", response = Swagger_System_Access.class),
            @ApiResponse(code = 400, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result system_access_get_everything(UUID person_id) {
        try {

            Model_Person person = Model_Person.getById(person_id);

            Swagger_System_Access system_access = new Swagger_System_Access();
            system_access.roles = person.roles;
            system_access.permissions = person.permissions;

            return ok(system_access);

        } catch (Exception e) {
            return controllerServerError(e);
        }


    }


// ######################################################################################################################

}