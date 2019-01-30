package controllers;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.swagger.annotations.*;
import mongo.mongo_services._ModelMongo_Example;
import play.libs.ws.WSClient;
import play.mvc.Result;
import responses.*;
import utilities.logger.Logger;
import utilities.model.EchoService;
import utilities.notifications.NotificationService;
import utilities.permission.PermissionService;
import utilities.swagger.input._Swagger_Example_with_private_validation;

/**
 *  Průvodní komentář:
 *
 *  Controller zpracovává příchozí requesty. Je zde několik implementací usnadňující psaní aplikační logiky.
 *
 *  1) Anotace pro Swagger / postman Export import. Popisujeme co zpracováváme za objekt v JSON, typy proměných,
 *  délka stringu, od do int atd.
 *
 *      ApiImplicitParams  - říká co příjmáme!
 *      ApiResponses       - co vracíme pode status http code
 *
 *  2) Ověřujeme automaticky parserem "formFromRequestWithValidation" příchozí JSON. Tato metoda zajistí jednak validní
 *  vytažení JSON z body requestu, jeho naparsování na MODEL, ale i vygenerování případné chybové hlášky, pokud JSON
 *  nesplňuje požadavky třídy, na kterou parsujeme. Pokud JSON není validní vytvoří se Exception "_Base_Result_Exception".
 *  Tanto Exception Interface sjednocuje řadu dalších Exceptions s detaily a na základě typu Exception se uživateli vrací chyba.
 *  Typicky "Result_Error_InvalidBody - kdy je JSON poškozený nebo nevalidní", Result_Error_NotFound kdy vyhledávaný objekt neexistuje
 *  atd. Validace je prováděna anotacemi uvnitř třídy na kterou parsujeme JSON.
 *
 *  3) Response je řešen pomocí volání ok(..) create(..) kde výsledné parsování na JSON a přiřazení HTTP Reqest čísla řeší _BaseController
 *
 */


/*
 * Annotation "Not Documented API - InProgress or Stuck" is for collection not branded API points (methods
 * in this controller. We use that for filtering all not properly set methods for Swagger or Postman
 */
@Api(
        value = "Not Documented API - InProgress or Stuck",
        protocols = "https",
        produces = "application/json"
)
/*
 * Security Annotation - if its allowed only registred and validated HTTP request will be proceed in assigned methods
 */
// @Security.Authenticated(Authentication.class)
public class _Controller_MongoExample extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(_Controller_MongoExample.class);

// CONTROLLER CONFIGURATION ############################################################################################


    @Inject
    public _Controller_MongoExample(WSClient ws, _BaseFormFactory formFactory, Config config, PermissionService permissionService, NotificationService notificationService, EchoService echoService) {
        super(ws, formFactory, config, permissionService, notificationService, echoService);
    }

// CONTROLLER CONTENT ##################################################################################################

    @ApiOperation(
            value = "test api POST",
            tags = {"_Controller_Example"},
            notes = "Create Basic Object",
            code = 201  // Only if we have 201 Code (for 200 its not required, its default value)
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input._Swagger_Example_with_private_validation", // Class that describes what I consume
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values" // Description about body in http request
            )
    })
    @ApiResponses({
            @ApiResponse(code = 201, message = "Ok Result",                 response = _ModelMongo_Example.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result example_post() {
        try {

            // Get and Validate Object
            _Swagger_Example_with_private_validation help = formFromRequestWithValidation(_Swagger_Example_with_private_validation.class);

            // Creeate and set Object
            _ModelMongo_Example example  =  new _ModelMongo_Example();
            example.example_name    =  help.example_name;
            example.example_age     =  help.example_age;
            example.example_salary  =  help.example_salary;
            example.example_boolean =  help.example_boolean;

            // Save Object
            example.save();

            return created(example);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "test api PUT",
            tags = {"_Controller_Example"},
            notes = "Edit Basic Object"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input._Swagger_Example_with_private_validation", // Class that describes what I consume
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values" // Description about body in http request
            )
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = _ModelMongo_Example.class),       // Response is Object!
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result example_put(String id) {
        try {

            // Get and Validate Object
            _Swagger_Example_with_private_validation help = formFromRequestWithValidation(_Swagger_Example_with_private_validation.class);

            _ModelMongo_Example muj = _ModelMongo_Example.find.bySingleArgument("email", "papě@.cz");

            // Creeate and set Object
            _ModelMongo_Example example  =  _ModelMongo_Example.find.byId(id);
            example.example_name    =  help.example_name;
            example.example_age     =  help.example_age;
            example.example_salary  =  help.example_salary;
            example.example_boolean =  help.example_boolean;

            example.update();

            return ok(example); // Response is Object extends _Swagger_Abstract_Default!!

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }


    @ApiOperation(
            value = "test api GET",
            tags = {"_Controller_Example"},
            notes = "Get Basic Object by ID"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = _ModelMongo_Example.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result example_get(String id) {
        try {

            // Find Object
            _ModelMongo_Example model = _ModelMongo_Example.find.byId(id);

            // Return object
            return ok(model);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "test api Remove",
            tags = {"_Controller_Example"},
            notes = "Super description about GET",
            hidden = false   // !! Warning - if you change that, this method is not visible for swagger generator!
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result example_delete(String id) {
        try {

            // Find Object
            _ModelMongo_Example model = _ModelMongo_Example.find.byId(id);

            model.delete();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }



}