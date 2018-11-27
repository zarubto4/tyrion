package controllers;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.ebean.*;
import io.swagger.annotations.*;
import models.Model_Permission;
import models.Model_Person;
import models.Model_Project;
import models.Model_Role;
import play.libs.ws.WSClient;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.authentication.Authentication;
import utilities.logger.Logger;
import utilities.permission.PermissionService;
import utilities.swagger.input.*;
import utilities.swagger.output.filter_results.Swagger_Role_List;

import java.util.List;
import java.util.UUID;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Authentication.class)
public class Controller_Role extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Role.class);

    @Inject
    public Controller_Role(WSClient ws, _BaseFormFactory formFactory, Config config, PermissionService permissionService) {
        super(ws, formFactory, config, permissionService);
    }

// API #################################################################################################################

    @ApiOperation(value = "create Role",
            tags = {"Role"},
            notes = "If you want create new Role in system. You need permission for that or have right system Roles",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_NameAndDesc_ProjectIdOptional",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_Role.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result role_create() {
        try {

            Swagger_NameAndDesc_ProjectIdOptional help = formFromRequestWithValidation(Swagger_NameAndDesc_ProjectIdOptional.class);

            if (help.name.equals("SuperAdmin")) {
                return badRequest("This name is reserved for the system.");
            }

            Model_Role role = new Model_Role();
            role.name = help.name;
            role.description = help.description;

            if (help.project_id != null) {
                role.project = Model_Project.find.byId(help.project_id);
            }

            return create(role);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Role",
            tags = {"Role"},
            notes = "get description",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Role.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result role_get(UUID role_id) {
        try {
            return read(Model_Role.find.byId(role_id));
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Role List by Filter",
            tags = {"Role"},
            notes = "get Role List",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Role_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",             response = Swagger_Role_List.class),
            @ApiResponse(code = 400, message = "Invalid body",          response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",  response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",     response = Result_InternalServerError.class)
    })
    public Result role_getByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) int page_number) {
        try {

            Swagger_Role_Filter help = formFromRequestWithValidation(Swagger_Role_Filter.class);

            Query<Model_Role> query = Ebean.find(Model_Role.class);

            query.where().eq("deleted", false);

            if (help.project_id != null) {
                Model_Project.find.byId(help.project_id);

                query.where().eq("project.id", help.project_id);
            }

            Swagger_Role_List result = new Swagger_Role_List(query, page_number, help);

            // TODO permissions

            return ok(result);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Role",
            tags = {"Role"},
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Role.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result role_update(UUID role_id) {
        try {

            Swagger_NameAndDescription help = formFromRequestWithValidation(Swagger_NameAndDescription.class);

            Model_Role role = Model_Role.find.byId(role_id);

            if (role.name.equals("SuperAdmin") && !help.name.equals("SuperAdmin")) {
                return badRequest("This name is unchangeable.");
            } else if (!role.name.equals("SuperAdmin") && help.name.equals("SuperAdmin")) {
                return badRequest("This name is reserved for the system.");
            }

            role.name = help.name;
            role.description = help.description;

            return update(role);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Role",
            tags = {"Role"},
            notes = "If you want delete  Role from system. You need permission for that or have right system Roles",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result role_delete(UUID role_id) {
        try {
            return delete(Model_Role.find.byId(role_id));
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "add Role Person",
            tags = {"Role", "Admin-Person"},
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
    @BodyParser.Of(BodyParser.Json.class)
    public Result role_addPerson(UUID role_id) {
        try {

            // Get and Validate Object
            Swagger_Invite_Person help = formFromRequestWithValidation(Swagger_Invite_Person.class);

            if (help.persons_mail.isEmpty()) {
                return badRequest("Fill in some emails.");
            }

            // Kontrola objektu
            Model_Role role = Model_Role.find.byId(role_id);

            this.checkUpdatePermission(role);

            List<Model_Person> persons = Model_Person.find.query().where().notExists(Ebean.find(Model_Role.class).where(Expr.in("persons.email", help.persons_mail))).in("email", help.persons_mail).findList();

            if (persons.isEmpty()) {
                return badRequest("No person to add was found for given email values.");
            }

            // Check Permission
            for(Model_Person person: persons){
                this.checkUpdatePermission(person);
            }

            logger.debug("role_addPerson: Adding {} person(s)", persons.size());

            role.persons.addAll(persons);
            role.update();

            return ok(role);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "remove Role Person ",
            tags = {"Role", "Admin-Person"},
            notes = "If you set Role to Person. You need permission for that or have right system Roles",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result role_removePerson(UUID role_id, UUID person_id) {
        try {

            // Kontrola objektu
            Model_Person person = Model_Person.find.byId(person_id);

            // Kontrola objektu
            Model_Role role = Model_Role.find.byId(role_id);

            if (role.persons.contains(person)) {
                role.persons.remove(person);
            } else {
                return badRequest("Role does not contain this person");
            }

            return update(role);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Role All",
            tags = {"Role"},
            notes = "If you set Role to Person. You need permission for that or have right system Roles",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Role.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result role_get_all() {
        try {
            return ok(Model_Role.find.query().where().isNull("project").orderBy("UPPER(name) ASC").findList());
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "add Role Permissions",
            tags = {"Role"},
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
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result role_addPermission(UUID role_id) {
        try {

            Swagger_Role_Add_Permission help = formFromRequestWithValidation(Swagger_Role_Add_Permission.class);

            List<Model_Permission> permissions = Model_Permission.find.query().where().in("id", help.permissions).findList();

            Model_Role role = Model_Role.find.byId(role_id);

            permissions.forEach(permission -> {
                if (!role.permissions.contains(permission)) {
                    role.permissions.add(permission);
                }
            });

            return update(role);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "remove Role Permission",
            tags = {"Role"},
            notes = "If you want remove system permissions from Role. You need permission for that or have right system Roles",
            produces = "application/json",
            protocols = "https",
            consumes = "text/html"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result role_removePermission(UUID role_id, UUID permission_id) {
        try {

            Model_Permission permission = Model_Permission.find.byId(permission_id);

            Model_Role role = Model_Role.find.byId(role_id);

            role.permissions.remove(permission);

            return update(role);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
}
