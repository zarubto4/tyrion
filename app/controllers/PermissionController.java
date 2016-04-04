package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.*;
import models.persons.Person;
import models.persons.PersonPermission;
import models.persons.SecurityRole;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.loginEntities.Secured;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_PermissionRequired;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.response.response_objects.Result_ok;
import utilities.swagger.documentationClass.Swagger_SecurityRole_New;

import javax.websocket.server.PathParam;
import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured.class)
public class PermissionController extends Controller {

// SYSTEM PERMISSION ###################################################################################################
      public static void set_System_Permission(){

        new PersonPermission("role.read", "description");
        new PersonPermission("role.person", "description");
        new PersonPermission("role.manager", "description");
        new PersonPermission("role.delete", "description");

        new PersonPermission("permission.connect_with_person", "description");
        new PersonPermission("permission.disconnect_with_person", "description");
        new PersonPermission("permission.edit", "description");
        new PersonPermission("permission.delete", "description");
        new PersonPermission("permission.read", "description");
    }

//######################################################################################################################


    @ApiOperation(value = "add Permission to the Person",
            tags = {"Permission"},
            notes = "If you want add permission to Person. You need permission for that or have right system Roles",
            produces = "application/json",
            response =  Result_ok.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "permission.connect_with_person", description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
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

            if (!person.permissions.contains(personPermission)) person.permissions.add(personPermission);
            person.update();

            return GlobalResult.result_ok();


        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "remove Permission from the Person",
            tags = {"Permission"},
            notes = "If you want remove permission from Person. You need permission for that or have right system Roles",
            produces = "application/json",
            response =  Result_ok.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "permission.disconnect_with_person", description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("permission.disconnect_with_person")
    public Result remove_Permission_Person(@ApiParam(required = true) @PathParam("person_id") String person_id, @ApiParam(required = true) @PathParam("permission_id")String permission_id) {
        try {

            Person person = Person.find.byId(person_id);
            if(person == null) return GlobalResult.notFoundObject("Person person_id not found");

            PersonPermission personPermission = PersonPermission.find.byId(permission_id);
            if(personPermission == null ) return GlobalResult.notFoundObject("PersonPermission permission_id not found");

            if(person.permissions.contains(personPermission)) person.permissions.remove(personPermission);
            person.update();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get all system Permissions",
            tags = {"Permission"},
            notes = "If you want get all system permissions (Really all!). You need permission for that or have right system Roles",
            produces = "application/json",
            response =  PersonPermission.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "permission.read", description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = PersonPermission.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("permission.read")
    public Result get_Permission_All(){
        try {

            List<PersonPermission> permissions = PersonPermission.find.all();
            return GlobalResult.result_ok(Json.toJson(permissions));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            return GlobalResult.internalServerError();
        }

    }

//######################################################################################################################

    @ApiOperation(value = "add Permission to the Role",
            tags = {"Permission", "Role"},
            notes = "If you want add system permissions to Role. You need permission for that or have right system Roles",
            produces = "application/json",
            response =  Result_ok.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "role.edit", description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("permission.edit")
    public Result add_Permission_to_Role(@ApiParam(required = true) @PathParam("permission_id") String permission_id, @ApiParam(required = true) @PathParam("role_id") String role_id){
        try {

            PersonPermission personPermission = PersonPermission.find.byId(permission_id);
            if(personPermission == null ) return GlobalResult.notFoundObject("PersonPermission permission_id not found");

            SecurityRole securityRole = SecurityRole.find.byId(role_id);
            if(securityRole == null ) return GlobalResult.notFoundObject("SecurityRole role_id not found");

            if( ! securityRole.permissions.contains(personPermission)) securityRole.permissions.add(personPermission);

            securityRole.update();


            return GlobalResult.result_ok();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get all Permission on Role (Group)",
            tags = {"Permission", "Role"},
            notes = "If you want get all permissions in Role (Admins, Monkeys..etc). You need also permission for that or have right system Roles",
            produces = "application/json",
            response =  PersonPermission.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "permission.disconnectWithPerson", description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
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
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            return GlobalResult.internalServerError();
        }
    }



    @ApiOperation(value = "get all Person from Role (Group)",
            tags = {"Permission", "Role"},
            notes = "If you want get all person from Role. You need also permission for that or have right system Roles",
            produces = "application/json",
            protocols = "https",
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "permission.disconnectWithPerson", description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
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
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "remove Permission from the Role",
            tags = {"Permission", "Role"},
            notes = "If you want remove system permissions from Role. You need permission for that or have right system Roles",
            produces = "application/json",
            protocols = "https",
            response = Result_ok.class,
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "permission.disconnectWithPerson", description = "Person need this permission"),
                                    @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
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

            if(securityRole.permissions.contains(personPermission)) securityRole.permissions.remove(personPermission);

            securityRole.update();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            return GlobalResult.internalServerError();
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
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "role.create", description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
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
            securityRole.save();

            return GlobalResult.created(Json.toJson(securityRole));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "delete Role",
            tags = {"Role"},
            notes = "If you want delete  Role from system. You need permission for that or have right system Roles",
            produces = "application/json",
            response =  Result_ok.class,
            protocols = "https",
            code = 201,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "role.deleter", description = "Person need this permission"),
                                    @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
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

            securityRole.delete();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "add Person to Role (Group) ",
            tags = {"Role","Person"},
            notes = "If you set Role to Person. You need permission for that or have right system Roles",
            produces = "application/json",
            response =  Result_ok.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "role.person", description = "Person need this permission"),
                                    @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
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

            if(!person.roles.contains(securityRole)) person.roles.add(securityRole);
            person.update();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "remove Person from the Role  (Group)",
            tags = {"Role", "Person"},
            notes = "If you set Role to Person. You need permission for that or have right system Roles",
            produces = "application/json",
            response =  Result_ok.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "role.manager", description = "Person need this permission"),
                                    @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
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

            if(person.roles.contains(securityRole)) person.roles.remove(securityRole);
            person.update();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get all Role from system",
            tags = {"Role"},
            notes = "If you set Role to Person. You need permission for that or have right system Roles",
            produces = "application/json",
            response =  SecurityRole.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "role.manager", description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin",   description = "Or person must be SuperAdmin role")}
                    )
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
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            return GlobalResult.internalServerError();
        }

    }


//######################################################################################################################

    // Private class for Swagger documentation
    private class SystemAccess {
        public List<SecurityRole> roles;
        public List<PersonPermission> permissions;
    }
    @ApiOperation(value = "get all system permissions & Roles",
            tags = {"Role", "Permission", "Person"},
            notes = "This api return List of Roles and List of Permission",
            produces = "application/json",
            response =  SystemAccess.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "role.manager", description = "Person need this permission"),
                                    @AuthorizationScope(scope = "SuperAdmin",   description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = SystemAccess.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_System_Acces(@ApiParam(required = true) @PathParam("person_id") String person_id){

        Person person = Person.find.byId(person_id);
        ObjectNode result = Json.newObject();
        result.set("roles", Json.toJson(person.roles));
        result.set("permission", Json.toJson(person.permissions));

        return GlobalResult.result_ok(Json.toJson(result));



    }

}
