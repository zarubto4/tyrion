package controllers;

import io.swagger.annotations.*;
import models.Model_Person;
import models.Model_Permission;
import models.Model_Project;
import models.Model_SecurityRole;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.login_entities.Secured_API;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_NotFound;
import utilities.response.response_objects.Result_Forbidden;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.response.response_objects.Result_Ok;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Swagger_Role_Short_Detail;
import utilities.swagger.outboundClass.Swagger_System_Access;

import java.util.ArrayList;
import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured_API.class)
public class Controller_Permission extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Permission.class);

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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 400, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result permission_person_add(@ApiParam(required = true) String person_id, @ApiParam(required = true) String permission_id) {
        try {

            Model_Person person = Model_Person.find.byId(person_id);
            if (person == null) return GlobalResult.result_notFound("Person person_id not found");

            Model_Permission personPermission = Model_Permission.find.byId(permission_id);

            if (personPermission == null)
                return GlobalResult.result_notFound("PersonPermission permission_id not found");

            if (!personPermission.edit_person_permission()) return GlobalResult.result_forbidden();


            if (!person.person_permissions.contains(personPermission)) person.person_permissions.add(personPermission);
            person.update();

            return GlobalResult.result_ok();


        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result permission_person_remove(@ApiParam(required = true) String person_id, @ApiParam(required = true) String permission_id) {
        try {

            Model_Person person = Model_Person.find.byId(person_id);
            if (person == null) return GlobalResult.result_notFound("Person person_id not found");

            Model_Permission personPermission = Model_Permission.find.byId(permission_id);
            if (personPermission == null)
                return GlobalResult.result_notFound("PersonPermission permission_id not found");

            if (!personPermission.edit_person_permission()) return GlobalResult.result_forbidden();

            if (person.person_permissions.contains(personPermission))
                person.person_permissions.remove(personPermission);
            person.update();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Model_Permission.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result permission_get_all() {
        try {

            List<Model_Permission> permissions = Model_Permission.find.where().orderBy("UPPER(permission_key) ASC").findList();
            return GlobalResult.result_ok(Json.toJson(permissions));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
                            dataType = "utilities.swagger.documentationClass.Swagger_Permission_Edit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Model_Permission.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result permission_edit(String permission_id) {
        try {

            final Form<Swagger_Permission_Edit> form = Form.form(Swagger_Permission_Edit.class).bindFromRequest();
            if (form.hasErrors()) {
                return GlobalResult.result_invalidBody(form.errorsAsJson());
            }
            Swagger_Permission_Edit help = form.get();

            Model_Permission permission = Model_Permission.find.where().eq("permission_key", permission_id).findUnique();
            if (permission == null) return GlobalResult.result_notFound("PersonPermission permission_id not found");

            if (!permission.edit_person_permission())
                return GlobalResult.result_forbidden("PersonPermission you have no permission");

            permission.description = help.description;
            permission.update();

            return GlobalResult.result_ok(Json.toJson(permission));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }

    }

//######################################################################################################################

    @ApiOperation(value = "add Role Permissions",
            tags = {"Admin-Permission", "Admin-Role"},
            notes = "If you want add system person_permissions to Role. You need permission for that or have right system Roles",
            produces = "application/json",
            response = Result_Ok.class,
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "SecurityRole_update", value = "true"),
                    })
            }

    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Role_Add_Permission",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result permission_add_to_role(@ApiParam(required = true) String role_id) {
        try {

            final Form<Swagger_Role_Add_Permission> form = Form.form(Swagger_Role_Add_Permission.class).bindFromRequest();
            if (form.hasErrors()) {
                return GlobalResult.result_invalidBody(form.errorsAsJson());
            }
            Swagger_Role_Add_Permission help = form.get();

            List<Model_Permission> personPermissions = Model_Permission.find.where().in("permission_key", help.permissions).findList();

            Model_SecurityRole securityRole = Model_SecurityRole.find.byId(role_id);
            if (securityRole == null) return GlobalResult.result_notFound("SecurityRole role_id not found");

            if (!securityRole.update_permission()) return GlobalResult.result_forbidden();

            for(Model_Permission permission : personPermissions){
                if(!securityRole.person_permissions.contains(permission)){
                    securityRole.person_permissions.add(permission);
                }
            }

            securityRole.update();
            securityRole.refresh();

            return GlobalResult.result_ok(Json.toJson(securityRole));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove Role Permission",
            tags = {"Admin-Permission", "Admin-Role"},
            notes = "If you want remove system person_permissions from Role. You need permission for that or have right system Roles",
            produces = "application/json",
            protocols = "https",
            consumes = "text/html",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "SecurityRole_update", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result permission_remove_from_role(@ApiParam(required = true) String permission_id, @ApiParam(required = true) String role_id) {
        try {

            Model_Permission personPermission = Model_Permission.find.byId(permission_id);
            if (personPermission == null)
                return GlobalResult.result_notFound("PersonPermission permission_id not found");

            Model_SecurityRole securityRole = Model_SecurityRole.find.byId(role_id);
            if (securityRole == null) return GlobalResult.result_notFound("SecurityRole role_id not found");

            if (!securityRole.update_permission()) return GlobalResult.result_forbidden();

            if (securityRole.person_permissions.contains(personPermission))
                securityRole.person_permissions.remove(personPermission);

            securityRole.update();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

//######################################################################################################################

    @ApiOperation(value = "create Role",
            tags = {"Admin-Role"},
            notes = "If you want create new Role in system. You need permission for that or have right system Roles",
            produces = "application/json",
            response = Model_SecurityRole.class,
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_SecurityRole_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created", response = Model_SecurityRole.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result role_create() {
        try {
            final Form<Swagger_SecurityRole_New> form = Form.form(Swagger_SecurityRole_New.class).bindFromRequest();
            if (form.hasErrors()) {
                return GlobalResult.result_invalidBody(form.errorsAsJson());
            }
            Swagger_SecurityRole_New help = form.get();

            Model_SecurityRole securityRole = new Model_SecurityRole();

            securityRole.name = help.name;
            securityRole.description = help.description;

            if (!securityRole.create_permission()) return GlobalResult.result_forbidden();

            securityRole.save();

            return GlobalResult.result_created(Json.toJson(securityRole));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
                            @ExtensionProperty(name = "SecurityRole_delete", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully created", response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result role_delete(@ApiParam(required = true) String role_id) {
        try {

            Model_SecurityRole securityRole = Model_SecurityRole.find.byId(role_id);
            if (securityRole == null) return GlobalResult.result_notFound("SecurityRole role_id not found");

            if (!securityRole.delete_permission()) return GlobalResult.result_forbidden();

            securityRole.delete();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
                            dataType = "utilities.swagger.documentationClass.Swagger_Role_Edit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully created", response = Model_SecurityRole.class),
            @ApiResponse(code = 400, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result role_edit(@ApiParam(required = true) String role_id) {
        try {

            final Form<Swagger_Role_Edit> form = Form.form(Swagger_Role_Edit.class).bindFromRequest();
            if (form.hasErrors()) {
                return GlobalResult.result_invalidBody(form.errorsAsJson());
            }
            Swagger_Role_Edit help = form.get();

            Model_SecurityRole securityRole = Model_SecurityRole.find.byId(role_id);
            if (securityRole == null) return GlobalResult.result_notFound("SecurityRole role_id not found");

            if (!securityRole.update_permission()) return GlobalResult.result_forbidden();

            securityRole.name = help.name;
            securityRole.description = help.description;
            securityRole.update();

            return GlobalResult.result_ok(Json.toJson(securityRole));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully created", response = Model_SecurityRole.class),
            @ApiResponse(code = 400, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result role_get(@ApiParam(required = true) String role_id) {
        try {

            Model_SecurityRole securityRole = Model_SecurityRole.find.byId(role_id);
            if (securityRole == null) return GlobalResult.result_notFound("SecurityRole role_id not found");

            if (!securityRole.read_permission()) return GlobalResult.result_forbidden();

            return GlobalResult.result_ok(Json.toJson(securityRole));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
                            @ExtensionProperty(name = "SecurityRole_update", value = "true"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Invite_Person",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Model_SecurityRole.class),
            @ApiResponse(code = 400, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result role_add_person(@ApiParam(required = true) String role_id) {
        try {

            // Zpracování Json
            final Form<Swagger_Invite_Person> form = Form.form(Swagger_Invite_Person.class).bindFromRequest();
            if (form.hasErrors()) {
                return GlobalResult.result_invalidBody(form.errorsAsJson());
            }
            Swagger_Invite_Person help = form.get();

            // Kontrola objektu
            Model_SecurityRole securityRole = Model_SecurityRole.find.byId(role_id);
            if (securityRole == null) return GlobalResult.result_notFound("SecurityRole role_id not found");

            // Kontrola oprávnění
            if (!securityRole.update_permission()) return GlobalResult.result_forbidden();

            // Získání seznamu uživatelů, kteří jsou registrovaní(listIn) a kteří ne(listOut)
            List<Model_Person> listIn = new ArrayList<>();
            List<String> toRemove = new ArrayList<>();


            List<Model_Person> persons = Model_Person.find.where().in("mail", help.persons_mail).ne("roles.id", securityRole.id).findList();

            securityRole.persons.addAll(persons);
            securityRole.update();

            securityRole.refresh();

            return GlobalResult.result_ok(Json.toJson(securityRole));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result role_remove_person(@ApiParam(required = true) String role_id, @ApiParam(required = true) String person_id) {
        try {

            Model_Person person = Model_Person.find.byId(person_id);
            if (person == null) return GlobalResult.result_notFound("Person person_id not found");

            Model_SecurityRole securityRole = Model_SecurityRole.find.byId(role_id);
            if (securityRole == null) return GlobalResult.result_notFound("SecurityRole role_id not found");

            if (!securityRole.update_permission()) return GlobalResult.result_forbidden();

            if (person.roles.contains(securityRole)) person.roles.remove(securityRole);
            person.update();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Role All",
            tags = {"Admin-Role"},
            notes = "If you set Role to Person. You need permission for that or have right system Roles",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Swagger_Role_Short_Detail.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result role_get_all() {
        try {

            List<Model_SecurityRole> roles = Model_SecurityRole.find.orderBy("UPPER(name) ASC").findList();

            List<Swagger_Role_Short_Detail> details = new ArrayList<>();
            for (Model_SecurityRole role : roles) details.add(role.get_group_short_detail());

            return GlobalResult.result_ok(Json.toJson(details));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Swagger_System_Access.class),
            @ApiResponse(code = 400, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result system_access_get_everything(@ApiParam(required = true) String person_id) {
        try {

            Model_Person person = Model_Person.find.byId(person_id);
            if (person == null) return GlobalResult.result_notFound("Person person_id not found");

            Swagger_System_Access system_access = new Swagger_System_Access();
            system_access.roles = person.roles;
            system_access.permissions = person.person_permissions;

            return GlobalResult.result_ok(Json.toJson(system_access));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }


    }


//######################################################################################################################

}
