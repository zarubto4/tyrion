package controllers;

import io.swagger.annotations.*;
import models.Model_Person;
import models.Model_Permission;
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
import utilities.swagger.outboundClass.Swagger_System_Access;

import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured_API.class)
public class Controller_Permission extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Permission.class);

///###################################################################################################################*/
    
    @ApiOperation(value = "add Permission to the Person",
            hidden = true,
            tags = {"Permission"},
            notes = "If you want add permission to Person. You need permission for that or have right system Roles",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "PersonPermission_edit_person_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result add_Permission_Person(@ApiParam(required = true)  String person_id, @ApiParam(required = true) String permission_id) {
        try {

            Model_Person person = Model_Person.find.byId(person_id);
            if (person == null) return GlobalResult.result_notFound("Person person_id not found");

            Model_Permission personPermission = Model_Permission.find.byId(permission_id);

            if (personPermission == null) return GlobalResult.result_notFound("PersonPermission permission_id not found");

            if(!personPermission.edit_person_permission()) return GlobalResult.result_forbidden();



            if (!person.person_permissions.contains(personPermission)) person.person_permissions.add(personPermission);
            person.update();

            return GlobalResult.result_ok();


        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove the person Permission",
            hidden = true,
            tags = {"Permission"},
            notes = "If you want remove permission from Person. You need permission for that or have right system Roles",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "PersonPermission_edit_person_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result remove_Permission_Person(@ApiParam(required = true)  String person_id, @ApiParam(required = true) String permission_id) {
        try {

            Model_Person person = Model_Person.find.byId(person_id);
            if(person == null) return GlobalResult.result_notFound("Person person_id not found");

            Model_Permission personPermission = Model_Permission.find.byId(permission_id);
            if(personPermission == null ) return GlobalResult.result_notFound("PersonPermission permission_id not found");

            if(!personPermission.edit_person_permission()) return GlobalResult.result_forbidden();

            if(person.person_permissions.contains(personPermission)) person.person_permissions.remove(personPermission);
            person.update();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all system Permissions",
            tags = {"Permission"},
            notes = "Get all user Permission. You need permission for that or have right system Roles",
            produces = "application/json",
            response =  Model_Permission.class,
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Public", value = "Without Permission"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Permission.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Permission_All(){
        try {

            List<Model_Permission> permissions = Model_Permission.find.all();
            return GlobalResult.result_ok(Json.toJson(permissions));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }

    }


    @ApiOperation(value = "edit Permission description",
            hidden = true,
            tags = {"Permission"},
            notes = "edit permission description",
            produces = "application/json",
            response =  Model_Permission.class,
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Permission.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result edit_permission_desciption(String permission_id){
        try {

            final Form<Swagger_Permission_Edit> form = Form.form(Swagger_Permission_Edit.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Permission_Edit help = form.get();

           Model_Permission permission = Model_Permission.find.where().eq("value", permission_id).findUnique();
           if(permission == null) return GlobalResult.result_notFound("PersonPermission permission_id not found");

            if(!permission.edit_person_permission()) return GlobalResult.result_forbidden("PersonPermission you have no permission");

            permission.description = help.description;
            permission.update();

           return GlobalResult.result_ok(Json.toJson(permission));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }

    }

//######################################################################################################################

    @ApiOperation(value = "add Permission to the Role",
            hidden = true,
            tags = {"Permission", "Role"},
            notes = "If you want add system person_permissions to Role. You need permission for that or have right system Roles",
            produces = "application/json",
            response =  Result_Ok.class,
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "SecurityRole_update", value = "true"),
                    })
            }

    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result add_Permission_to_Role(@ApiParam(required = true) String permission_id, @ApiParam(required = true)  String role_id){
        try {

            Model_Permission personPermission = Model_Permission.find.byId(permission_id);
            if(personPermission == null ) return GlobalResult.result_notFound("PersonPermission permission_id not found");

            Model_SecurityRole securityRole = Model_SecurityRole.find.byId(role_id);
            if(securityRole == null ) return GlobalResult.result_notFound("SecurityRole role_id not found");

            if(! securityRole.update_permission()) return GlobalResult.result_forbidden();

            if( ! securityRole.person_permissions.contains(personPermission)) securityRole.person_permissions.add(personPermission);

            securityRole.update();


            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }



    @ApiOperation(value = "remove Permission from the Role",
            hidden = true,
            tags = {"Permission", "Role"},
            notes = "If you want remove system person_permissions from Role. You need permission for that or have right system Roles",
            produces = "application/json",
            protocols = "https",
            consumes = "text/html",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "SecurityRole_update", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result remove_Permission_from_Role(@ApiParam(required = true)  String permission_id, @ApiParam(required = true) String role_id){
        try {

            Model_Permission personPermission = Model_Permission.find.byId(permission_id);
            if(personPermission == null ) return GlobalResult.result_notFound("PersonPermission permission_id not found");

            Model_SecurityRole securityRole = Model_SecurityRole.find.byId(role_id);
            if(securityRole == null ) return GlobalResult.result_notFound("SecurityRole role_id not found");

            if (!securityRole.update_permission() ) return GlobalResult.result_forbidden();

            if(securityRole.person_permissions.contains(personPermission)) securityRole.person_permissions.remove(personPermission);

            securityRole.update();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

//######################################################################################################################

    @ApiOperation(value = "create new Role",
            tags = {"Role"},
            notes = "If you want create new Role in system. You need permission for that or have right system Roles",
            produces = "application/json",
            response =  Model_SecurityRole.class,
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
            @ApiResponse(code = 201, message = "Successfully created",    response = Model_SecurityRole.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_Role(){
        try {
            final Form<Swagger_SecurityRole_New> form = Form.form(Swagger_SecurityRole_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_SecurityRole_New help = form.get();

            Model_SecurityRole securityRole = new Model_SecurityRole();

            securityRole.name = help.name;
            securityRole.description =help.description;

            if ( !securityRole.create_permission()) return GlobalResult.result_forbidden();

            securityRole.save();

            return GlobalResult.result_created(Json.toJson(securityRole));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Role",
            hidden = true,
            tags = {"Role"},
            notes = "If you want delete  Role from system. You need permission for that or have right system Roles",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "SecurityRole_delete", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully created",    response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result delete_Role(@ApiParam(required = true) String role_id){
        try {

            Model_SecurityRole securityRole = Model_SecurityRole.find.byId(role_id);
            if(securityRole == null ) return GlobalResult.result_notFound("SecurityRole role_id not found");

            if (!securityRole.delete_permission()) return GlobalResult.result_forbidden();

            securityRole.delete();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Role description",
            hidden = true,
            tags = {"Role"},
            notes = "edit description",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "SecurityRole_uddate", value = "true"),
                    })
            }
    )@ApiImplicitParams(
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
            @ApiResponse(code = 200, message = "Successfully created",    response = Model_SecurityRole.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result edit_Role(@ApiParam(required = true) String role_id){
        try {

            final Form<Swagger_Role_Edit> form = Form.form(Swagger_Role_Edit.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Role_Edit help = form.get();

            Model_SecurityRole securityRole = Model_SecurityRole.find.byId(role_id);
            if(securityRole == null ) return GlobalResult.result_notFound("SecurityRole role_id not found");

            if (!securityRole.update_permission()) return GlobalResult.result_forbidden();

            securityRole.name = help.name;
            securityRole.description = help.description;
            securityRole.update();

            return GlobalResult.result_ok(Json.toJson(securityRole));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }
    @ApiOperation(value = "add Person to Role (Group) ",
            tags = {"Role","Person"},
            notes = "If you set Role to Person. You need permission for that or have right system Roles",
            produces = "application/json",
            response =  Result_Ok.class,
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "SecurityRole_update", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result add_Role_Person( @ApiParam(required = true)  String mail , @ApiParam(required = true)  String role_id) {
        try {

            Model_Person person = Model_Person.find.where().eq("mail", mail).findUnique();
            if(person == null) return GlobalResult.result_notFound("Person email not found");

            Model_SecurityRole securityRole = Model_SecurityRole.find.byId(role_id);
            if(securityRole == null ) return GlobalResult.result_notFound("SecurityRole role_id not found");

            if ( !securityRole.update_permission()) return GlobalResult.result_forbidden();

            if(!person.roles.contains(securityRole)) person.roles.add(securityRole);
            person.update();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove Person from the Role  (Group)",
            hidden = true,
            tags = {"Role", "Person"},
            notes = "If you set Role to Person. You need permission for that or have right system Roles",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result remove_Role_Person(@ApiParam(required = true)  String person_id, @ApiParam(required = true) String role_id) {
        try {

            Model_Person person = Model_Person.find.byId(person_id);
            if(person == null) return GlobalResult.result_notFound("Person person_id not found");

            Model_SecurityRole securityRole = Model_SecurityRole.find.byId(role_id);
            if(securityRole == null ) return GlobalResult.result_notFound("SecurityRole role_id not found");

            if (!securityRole.update_permission()) return GlobalResult.result_forbidden();

            if(person.roles.contains(securityRole)) person.roles.remove(securityRole);
            person.update();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all Role from system",
            tags = {"Role"},
            notes = "If you set Role to Person. You need permission for that or have right system Roles",
            produces = "application/json",
            response =  Model_SecurityRole.class,
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_SecurityRole.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Role_All(){
        try {

            List<Model_SecurityRole> roles = Model_SecurityRole.find.all();
            return GlobalResult.result_ok(Json.toJson(roles));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }

    }

//######################################################################################################################

    @ApiOperation(value = "get all system person_permissions & Roles",
            tags = {"Role", "Permission", "Person"},
            notes = "This api return List of Roles and List of Permission",
            produces = "application/json",
            response =  Swagger_System_Access.class,
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_System_Access.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_System_Access(@ApiParam(required = true) String person_id){
        try {

            Model_Person person = Model_Person.find.byId(person_id);
            if(person == null) return GlobalResult.result_notFound("Person person_id not found");

            Swagger_System_Access system_access = new Swagger_System_Access();
            system_access.roles = person.roles;
            system_access.permissions = person.person_permissions;

        return GlobalResult.result_ok(Json.toJson(system_access));

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }


    }


//######################################################################################################################

}
