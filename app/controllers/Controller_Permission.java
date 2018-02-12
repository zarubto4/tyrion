package controllers;

import com.google.inject.Inject;
import io.ebean.Ebean;
import io.ebean.Expr;
import io.swagger.annotations.*;
import models.Model_Person;
import models.Model_Permission;
import models.Model_Role;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.authentication.Authentication;
import utilities.logger.Logger;
import utilities.swagger.input.*;
import utilities.swagger.output.Swagger_System_Access;

import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Authentication.class)
public class Controller_Permission extends BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Permission.class);

    private FormFactory formFactory;

    @Inject
    public Controller_Permission(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

///###################################################################################################################*/

    @ApiOperation(value = "add Permission to Person",
            tags = {"Admin-Permission"},
            notes = "If you want add permission to Person. You need permission for that or have right system Roles",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "PersonPermission_edit_person_permission", value = "true"),
                    })
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result", response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 400, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result permission_person_add(@ApiParam(required = true) String person_id, @ApiParam(required = true) String permission_id) {
        try {

            Model_Person person = Model_Person.getById(person_id);
            if (person == null) return notFound("Person person_id not found");

            Model_Permission personPermission = Model_Permission.getById(permission_id);

            if (personPermission == null)
                return notFound("PersonPermission permission_id not found");

            if (!personPermission.edit_person_permission()) return forbidden();


            if (!person.permissions.contains(personPermission)) person.permissions.add(personPermission);
            person.update();

            return ok();


        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "remove Permission from Person",
            tags = {"Admin-Permission"},
            notes = "If you want remove permission from Person. You need permission for that or have right system Roles",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "PersonPermission_edit_person_permission", value = "true"),
                    })
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result", response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result permission_person_remove(@ApiParam(required = true) String person_id, @ApiParam(required = true) String permission_id) {
        try {

            Model_Person person = Model_Person.getById(person_id);
            if (person == null) return notFound("Person person_id not found");

            Model_Permission personPermission = Model_Permission.getById(permission_id);
            if (personPermission == null)
                return notFound("PersonPermission permission_id not found");

            if (!personPermission.edit_person_permission()) return forbidden();

            if (person.permissions.contains(personPermission))
                person.permissions.remove(personPermission);
            person.update();

            return ok();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get Permissions All",
            tags = {"Admin-Permission"},
            notes = "Get all user Permission. You need permission for that or have right system Roles",
            produces = "application/json",
            response = Model_Permission.class,
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_description", properties = {
                            @ExtensionProperty(name = "Public", value = "Without Permission"),
                    })
            }
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
            return ok(Json.toJson(permissions));

        } catch (Exception e) {
            return internalServerError(e);
        }

    }


    @ApiOperation(value = "edit Permission",
            tags = {"Admin-Permission"},
            notes = "edit permission description",
            produces = "application/json",
            response = Model_Permission.class,
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_description", properties = {
                            @ExtensionProperty(name = "edit_permission", value = "true"),
                    })
            }
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
    public Result permission_edit(String permission_id) {
        try {

            final Form<Swagger_Permission_Edit> form = formFactory.form(Swagger_Permission_Edit.class).bindFromRequest();
            if (form.hasErrors()) {
                return invalidBody(form.errorsAsJson());
            }
            Swagger_Permission_Edit help = form.get();

            Model_Permission permission = Model_Permission.find.query().where().eq("name", permission_id).findOne();
            if (permission == null) return notFound("PersonPermission permission_id not found");

            if (!permission.edit_person_permission())
                return forbidden("PersonPermission you have no permission");

            permission.description = help.description;
            permission.update();

            return ok(Json.toJson(permission));

        } catch (Exception e) {
            return internalServerError(e);
        }

    }

//######################################################################################################################

    @ApiOperation(value = "add Role Permissions",
            tags = {"Admin-Permission", "Admin-Role"},
            notes = "If you want add system permissions to Role. You need permission for that or have right system Roles",
            produces = "application/json",
            response = Result_Ok.class,
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Role_update", value = "true"),
                    })
            }

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
            @ApiResponse(code = 200, message = "Ok Result", response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result permission_add_to_role(@ApiParam(required = true) String role_id) {
        try {

            final Form<Swagger_Role_Add_Permission> form = formFactory.form(Swagger_Role_Add_Permission.class).bindFromRequest();
            if (form.hasErrors()) {
                return invalidBody(form.errorsAsJson());
            }
            Swagger_Role_Add_Permission help = form.get();

            List<Model_Permission> personPermissions = Model_Permission.find.query().where().in("name", help.permissions).findList();

            Model_Role securityRole = Model_Role.getById(role_id);
            if (securityRole == null) return notFound("SecurityRole role_id not found");

            if (!securityRole.update_permission()) return forbidden();

            for(Model_Permission permission : personPermissions) {
                if (!securityRole.permissions.contains(permission)) {
                    securityRole.permissions.add(permission);
                }
            }

            securityRole.update();
            securityRole.refresh();

            return ok(Json.toJson(securityRole));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "remove Role Permission",
            tags = {"Admin-Permission", "Admin-Role"},
            notes = "If you want remove system permissions from Role. You need permission for that or have right system Roles",
            produces = "application/json",
            protocols = "https",
            consumes = "text/html",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Role_update", value = "true"),
                    })
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result", response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result permission_remove_from_role(@ApiParam(required = true) String permission_id, @ApiParam(required = true) String role_id) {
        try {

            Model_Permission personPermission = Model_Permission.getById(permission_id);
            if (personPermission == null)
                return notFound("PersonPermission permission_id not found");

            Model_Role securityRole = Model_Role.getById(role_id);
            if (securityRole == null) return notFound("SecurityRole role_id not found");

            if (!securityRole.update_permission()) return forbidden();

            if (securityRole.permissions.contains(personPermission))
                securityRole.permissions.remove(personPermission);

            securityRole.update();

            return ok();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

//######################################################################################################################

    @ApiOperation(value = "create Role",
            tags = {"Admin-Role"},
            notes = "If you want create new Role in system. You need permission for that or have right system Roles",
            produces = "application/json",
            response = Model_Role.class,
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
            @ApiResponse(code = 201, message = "Successfully created", response = Model_Role.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result role_create() {
        try {
            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors()) {
                return invalidBody(form.errorsAsJson());
            }
            Swagger_NameAndDescription help = form.get();

            Model_Role securityRole = new Model_Role();

            securityRole.name = help.name;
            securityRole.description = help.description;

            if (!securityRole.create_permission()) return forbidden();

            securityRole.save();

            return created(Json.toJson(securityRole));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete Role",
            tags = {"Admin-Role"},
            notes = "If you want delete  Role from system. You need permission for that or have right system Roles",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Role_delete", value = "true"),
                    })
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully created", response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result role_delete(@ApiParam(required = true) String role_id) {
        try {

            Model_Role securityRole = Model_Role.getById(role_id);
            if (securityRole == null) return notFound("SecurityRole role_id not found");

            if (!securityRole.delete_permission()) return forbidden();

            securityRole.delete();

            return ok();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit Role",
            tags = {"Admin-Role"},
            notes = "edit description",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "SecurityRole_uddate", value = "true"),
                    })
            }
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
    public Result role_edit(@ApiParam(required = true) String role_id) {
        try {

            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_NameAndDescription help = form.get();

            Model_Role role = Model_Role.getById(role_id);
            if (role == null) return notFound("Role not found");

            if (!role.update_permission()) return forbidden();

            role.name = help.name;
            role.description = help.description;
            role.update();

            return ok(Json.toJson(role));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get Role",
            tags = {"Admin-Role"},
            notes = "get description",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "SecurityRole_uddate", value = "true"),
                    })
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully created", response = Model_Role.class),
            @ApiResponse(code = 400, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result role_get(@ApiParam(required = true) String role_id) {
        try {

            Model_Role securityRole = Model_Role.getById(role_id);
            if (securityRole == null) return notFound("SecurityRole role_id not found");

            if (!securityRole.read_permission()) return forbidden();

            return ok(Json.toJson(securityRole));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "add Role Person",
            tags = {"Admin-Role", "Admin-Person"},
            notes = "If you set Role to Person. You need permission for that or have right system Roles",
            produces = "application/json",
            response = Result_Ok.class,
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "Role_update", value = "true"),
                    })
            }
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
            @ApiResponse(code = 200, message = "Ok Result", response = Model_Role.class),
            @ApiResponse(code = 400, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result role_add_person(@ApiParam(required = true) String role_id) {
        try {

            // Zpracování Json
            final Form<Swagger_Invite_Person> form = formFactory.form(Swagger_Invite_Person.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_Invite_Person help = form.get();

            if (help.persons_mail.isEmpty()) {
                return badRequest("Fill in some emails.");
            }

            // Kontrola objektu
            Model_Role securityRole = Model_Role.getById(role_id);
            if (securityRole == null) return notFound("SecurityRole not found");

            // Kontrola oprávnění
            if (!securityRole.update_permission()) return forbidden();

            logger.debug("role_add_person: Finding {} person(s)", help.persons_mail.size());

            List<Model_Person> persons = Model_Person.find.query().where().notExists(Ebean.find(Model_Role.class).where(Expr.in("persons.email", help.persons_mail))).in("email", help.persons_mail).findList();

            if (persons.isEmpty()) {
                return badRequest("No person to add was found for given email values.");
            }

            logger.debug("role_add_person: Adding {} person(s)", persons.size());
            securityRole.persons.addAll(persons);
            securityRole.update();

            securityRole.refresh();

            return ok(Json.toJson(securityRole));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "remove Role Person ",
            tags = {"Admin-Role", "Admin-Person"},
            notes = "If you set Role to Person. You need permission for that or have right system Roles",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result", response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result role_remove_person(@ApiParam(required = true) String role_id, @ApiParam(required = true) String person_id) {
        try {

            Model_Person person = Model_Person.getById(person_id);
            if (person == null) return notFound("Person person_id not found");

            Model_Role securityRole = Model_Role.getById(role_id);
            if (securityRole == null) return notFound("SecurityRole role_id not found");

            if (!securityRole.update_permission()) return forbidden();

            if (person.roles.contains(securityRole)) person.roles.remove(securityRole);
            person.update();

            return ok();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get Role All",
            tags = {"Admin-Role"},
            notes = "If you set Role to Person. You need permission for that or have right system Roles",
            produces = "application/json",
            protocols = "https",
            code = 200
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

            return ok(Json.toJson(roles));

        } catch (Exception e) {
            return internalServerError(e);
        }

    }

//######################################################################################################################

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
    public Result system_access_get_everything(@ApiParam(required = true) String person_id) {
        try {

            Model_Person person = Model_Person.getById(person_id);
            if (person == null) return notFound("Person person_id not found");

            Swagger_System_Access system_access = new Swagger_System_Access();
            system_access.roles = person.roles;
            system_access.permissions = person.permissions;

            return ok(Json.toJson(system_access));

        } catch (Exception e) {
            return internalServerError(e);
        }


    }


//######################################################################################################################

}
