package controllers;

import io.swagger.annotations.*;
import models.person.Person;
import models.person.PersonPermission;
import models.person.SecurityRole;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_PermissionRequired;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.response.response_objects.Result_ok;
import utilities.swagger.documentationClass.Swagger_SecurityRole_New;
import utilities.swagger.documentationClass.Swagger_System_Access;

import javax.websocket.server.PathParam;
import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured.class)
public class PermissionController extends Controller {


    @ApiOperation(value = "add Permission to the Person",
            tags = {"Permission"},
            notes = "If you want add permission to Person. You need permission for that or have right system Roles",
            produces = "application/json",
            response =  Result_ok.class,
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "PersonPermission_edit_person_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result add_Permission_Person(@ApiParam(required = true) @PathParam("person_id") String person_id, @ApiParam(required = true) @PathParam("permission_id") String permission_id) {
        try {

            Person person = Person.find.byId(person_id);
            if (person == null) return GlobalResult.notFoundObject("Person person_id not found");

            PersonPermission personPermission = PersonPermission.find.byId(permission_id);

            if (personPermission == null) return GlobalResult.notFoundObject("PersonPermission permission_id not found");

            if(!personPermission.edit_person_permission()) return GlobalResult.forbidden_Permission();



            if (!person.person_permissions.contains(personPermission)) person.person_permissions.add(personPermission);
            person.update();

            return GlobalResult.result_ok();


        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove the person Permission",
            tags = {"Permission"},
            notes = "If you want remove permission from Person. You need permission for that or have right system Roles",
            produces = "application/json",
            response =  Result_ok.class,
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "PersonPermission_edit_person_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result remove_Permission_Person(@ApiParam(required = true) @PathParam("person_id") String person_id, @ApiParam(required = true) @PathParam("permission_id")String permission_id) {
        try {

            Person person = Person.find.byId(person_id);
            if(person == null) return GlobalResult.notFoundObject("Person person_id not found");

            PersonPermission personPermission = PersonPermission.find.byId(permission_id);
            if(personPermission == null ) return GlobalResult.notFoundObject("PersonPermission permission_id not found");

            if(!personPermission.edit_person_permission()) return GlobalResult.forbidden_Permission();

            if(person.person_permissions.contains(personPermission)) person.person_permissions.remove(personPermission);
            person.update();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all system Permissions",
            tags = {"Permission"},
            notes = "Get all user Permission. You need permission for that or have right system Roles",
            produces = "application/json",
            response =  PersonPermission.class,
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Public", value = "Without Permission"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = PersonPermission.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Permission_All(){
        try {

            List<PersonPermission> permissions = PersonPermission.find.all();
            return GlobalResult.result_ok(Json.toJson(permissions));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }

    }

//######################################################################################################################

    @ApiOperation(value = "add Permission to the Role",
            tags = {"Permission", "Role"},
            notes = "If you want add system person_permissions to Role. You need permission for that or have right system Roles",
            produces = "application/json",
            response =  Result_ok.class,
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "SecurityRole_update", value = "true"),
                    })
            }

    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result add_Permission_to_Role(@ApiParam(required = true) @PathParam("permission_id") String permission_id, @ApiParam(required = true) @PathParam("role_id") String role_id){
        try {

            PersonPermission personPermission = PersonPermission.find.byId(permission_id);
            if(personPermission == null ) return GlobalResult.notFoundObject("PersonPermission permission_id not found");

            SecurityRole securityRole = SecurityRole.find.byId(role_id);
            if(securityRole == null ) return GlobalResult.notFoundObject("SecurityRole role_id not found");

            if(! personPermission.edit_permission()) return GlobalResult.forbidden_Permission();

            if( ! securityRole.permissions.contains(personPermission)) securityRole.permissions.add(personPermission);

            securityRole.update();


            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all Permissions from Role (Group)",
            tags = {"Permission", "Role"},
            notes = "If you want get all person_permissions in Role (Admins, Monkeys..etc). You need also permission for that or have right system Roles",
            produces = "application/json",
            response =  PersonPermission.class,
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Public", value = "Without Permission"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = PersonPermission.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Permission_in_Group( @ApiParam(required = true) @PathParam("role_id" )String role_id){
        try {

            SecurityRole securityRole = SecurityRole.find.byId(role_id);
            if(securityRole == null ) return GlobalResult.notFoundObject("SecurityRole role_id not found");

            return GlobalResult.result_ok(Json.toJson(securityRole.permissions));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all Person from Role (Group)",
            tags = {"Permission", "Role"},
            notes = "If you want get all person from Role. You need also permission for that or have right system Roles",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Public", value = "Without Permisison"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Person.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Person_from_Role_Group( @ApiParam(required = true) @PathParam("role_id" )String role_id){
        try {

            SecurityRole securityRole = SecurityRole.find.byId(role_id);
            if(securityRole == null ) return GlobalResult.notFoundObject("SecurityRole role_id not found");

            return GlobalResult.result_ok(Json.toJson(securityRole.persons));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove Permission from the Role",
            tags = {"Permission", "Role"},
            notes = "If you want remove system person_permissions from Role. You need permission for that or have right system Roles",
            produces = "application/json",
            protocols = "https",
            response = Result_ok.class,
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "SecurityRole_update", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result remove_Permission_from_Role(@ApiParam(required = true) @PathParam("permission_id") String permission_id, @ApiParam(required = true) @PathParam("role_id")String role_id){
        try {

            PersonPermission personPermission = PersonPermission.find.byId(permission_id);
            if(personPermission == null ) return GlobalResult.notFoundObject("PersonPermission permission_id not found");

            SecurityRole securityRole = SecurityRole.find.byId(role_id);
            if(securityRole == null ) return GlobalResult.notFoundObject("SecurityRole role_id not found");

            if (!securityRole.update_permission() ) return GlobalResult.forbidden_Permission();

            if(securityRole.permissions.contains(personPermission)) securityRole.permissions.remove(personPermission);

            securityRole.update();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

//######################################################################################################################

    @ApiOperation(value = "create new Role",
            tags = {"Role"},
            notes = "If you want create new Role in system. You need permission for that or have right system Roles",
            produces = "application/json",
            response =  SecurityRole.class,
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "SecurityRole_create", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = SecurityRole.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
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
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_Role(){
        try {
            final Form<Swagger_SecurityRole_New> form = Form.form(Swagger_SecurityRole_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_SecurityRole_New help = form.get();

            SecurityRole securityRole = new SecurityRole();

            securityRole.name = help.name;
            securityRole.description =help.description;

            if ( !securityRole.create_permission()) return GlobalResult.forbidden_Permission();

            securityRole.save();

            return GlobalResult.created(Json.toJson(securityRole));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Role",
            tags = {"Role"},
            notes = "If you want delete  Role from system. You need permission for that or have right system Roles",
            produces = "application/json",
            response =  Result_ok.class,
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "SecurityRole_delete", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful created",      response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result delete_Role(@ApiParam(required = true) @PathParam("role_id")String role_id){
        try {

            SecurityRole securityRole = SecurityRole.find.byId(role_id);
            if(securityRole == null ) return GlobalResult.notFoundObject("SecurityRole role_id not found");

            if (!securityRole.delete_permission()) return GlobalResult.forbidden_Permission();

            securityRole.delete();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "add Person to Role (Group) ",
            tags = {"Role","Person"},
            notes = "If you set Role to Person. You need permission for that or have right system Roles",
            produces = "application/json",
            response =  Result_ok.class,
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "SecurityRole_update", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result add_Role_Person( @ApiParam(required = true) @PathParam("person_id") String person_id , @ApiParam(required = true) @PathParam("role_id") String role_id) {
        try {

            Person person = Person.find.byId(person_id);
            if(person == null) return GlobalResult.notFoundObject("Person person_id not found");

            SecurityRole securityRole = SecurityRole.find.byId(role_id);
            if(securityRole == null ) return GlobalResult.notFoundObject("SecurityRole role_id not found");

            if ( !securityRole.update_permission()) return GlobalResult.forbidden_Permission();

            if(!person.roles.contains(securityRole)) person.roles.add(securityRole);
            person.update();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove Person from the Role  (Group)",
            tags = {"Role", "Person"},
            notes = "If you set Role to Person. You need permission for that or have right system Roles",
            produces = "application/json",
            response =  Result_ok.class,
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "SecurityRole_update", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result remove_Role_Person(@ApiParam(required = true) @PathParam("person_id") String person_id, @ApiParam(required = true) @PathParam("role_id")String role_id) {
        try {

            Person person = Person.find.byId(person_id);
            if(person == null) return GlobalResult.notFoundObject("Person person_id not found");

            SecurityRole securityRole = SecurityRole.find.byId(role_id);
            if(securityRole == null ) return GlobalResult.notFoundObject("SecurityRole role_id not found");

            if (!securityRole.update_permission()) return GlobalResult.forbidden_Permission();

            if(person.roles.contains(securityRole)) person.roles.remove(securityRole);
            person.update();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all Role from system",
            tags = {"Role"},
            notes = "If you set Role to Person. You need permission for that or have right system Roles",
            produces = "application/json",
            response =  SecurityRole.class,
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Public", value = "Without Permisison"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = SecurityRole.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Role_All(){
        try {

            List<SecurityRole> roles = SecurityRole.find.all();
            return GlobalResult.result_ok(Json.toJson(roles));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }

    }


//######################################################################################################################

    @ApiOperation(value = "get all system person_permissions & Roles",
            tags = {"Role", "Permission", "Person"},
            notes = "This api return List of Roles and List of Permission",
            produces = "application/json",
            response =  Swagger_System_Access.class,
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Person.all_permission", value = "its possible get only own (personú permission"),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "owner", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_System_Access.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_System_Acces(@ApiParam(required = true) @PathParam("person_id") String person_id){
        try {

            Person person = Person.find.byId(person_id);
            if(person == null) return GlobalResult.notFoundObject("Person person_id not found");

            Swagger_System_Access system_access = new Swagger_System_Access();
            system_access.roles = person.roles;
            system_access.permissions = person.person_permissions;

        return GlobalResult.result_ok(Json.toJson(system_access));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }


    }

}
