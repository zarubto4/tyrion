package controllers;

import com.google.inject.Inject;
import io.ebean.Ebean;
import io.swagger.annotations.*;
import models.*;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.authentication.Authentication;
import utilities.enums.*;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.financial.extensions.configurations.*;
import utilities.financial.fakturoid.Fakturoid;
import utilities.financial.goPay.GoPay;
import utilities.logger.Logger;
import utilities.swagger.input.*;
import utilities.swagger.output.Swagger_Invoice_FullDetails;
import utilities.swagger.output.Swagger_ProductExtension_Type;
import utilities.swagger.output.Swagger_Product_Active;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Authentication.class)
public class Controller_Finance extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Finance.class);

// CONTROLLER CONFIGURATION ############################################################################################

    private _BaseFormFactory baseFormFactory;
    private Fakturoid fakturoid;
    private GoPay goPay;

    @Inject public Controller_Finance(_BaseFormFactory formFactory, Fakturoid fakturoid, GoPay goPay) {
        this.baseFormFactory = formFactory;
        this.fakturoid = fakturoid;
        this.goPay = goPay;
    }

// ADMIN - TARIFF SETTINGS #############################################################################################//

    @ApiOperation(value = "create Tariff",
            tags = {"Admin-Tariff"},
            notes = "create new Tariff",
            produces = "application/json",
            protocols = "https",
            code = 201

    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Tariff_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Ok Result",                 response = Model_Tariff.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result tariff_create() {
        try {

            // Get and Validate Object
            Swagger_Tariff_New help  = baseFormFactory.formFromRequestWithValidation(Swagger_Tariff_New.class);

            if (Model_Tariff.find.query().where().eq("identifier", help.identifier).findOne() != null) return badRequest("Identifier must be unique!");

            Model_Tariff tariff = new Model_Tariff();

            tariff.name                     = help.name;
            tariff.identifier               = help.identifier;
            tariff.description              = help.description;

            tariff.color                    = help.color;
            tariff.awesome_icon             = help.awesome_icon;

            tariff.credit_for_beginning     = (long) (help.credit_for_beginning * 1);

            tariff.company_details_required = help.company_details_required;
            tariff.payment_details_required = help.payment_details_required;
            tariff.payment_method_required = help.payment_method_required;

            tariff.labels_json = Json.toJson(help.labels).toString();


            tariff.active                   = false;
            
            tariff.save();

            return created(tariff);
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Tariff",
            tags = {"Admin-Tariff"},
            notes = "create new Tariff",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Tariff_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Tariff.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result tariff_edit(UUID tariff_id) {
        try {

            // Get and Validate Object
            Swagger_Tariff_New help  = baseFormFactory.formFromRequestWithValidation(Swagger_Tariff_New.class);

            Model_Tariff tariff = Model_Tariff.getById(tariff_id);
            
            if (Model_Tariff.find.query().where().ne("id", tariff_id).eq("identifier", help.identifier).findOne() != null) {
                return badRequest("Identifier must be unique!");
            }
            
            tariff.name                     = help.name;
            tariff.identifier               = help.identifier;
            tariff.description              = help.description;

            tariff.color                    = help.color;
            tariff.awesome_icon             = help.awesome_icon;

            tariff.payment_details_required = help.payment_details_required;
            tariff.payment_method_required  = help.payment_method_required;
            tariff.company_details_required = help.company_details_required;

            tariff.credit_for_beginning     = (long) (help.credit_for_beginning * 1);

            tariff.labels_json = Json.toJson(help.labels).toString();

            tariff.update();

            return ok(tariff);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "deactivate Tariff",
            tags = {"Admin-Tariff"},
            notes = "deactivate Tariff",
            produces = "application/json",
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
    public Result tariff_deactivate(UUID tariff_id) {
        try {

            Model_Tariff tariff = Model_Tariff.getById(tariff_id);
            
            if (!tariff.active) return badRequest("Tariff is already deactivated");
            tariff.active = false;

            tariff.update();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "activate Tariff",
            tags = {"Admin-Tariff"},
            notes = "activate Tariff",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Tariff.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result tariff_activate(UUID tariff_id) {
        try {

            Model_Tariff tariff = Model_Tariff.getById(tariff_id);
            
            if (tariff.active) return badRequest("Tariff is already activated");
            
            tariff.active = true;

            tariff.update();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "order Tariff Up",
            tags = {"Admin-Tariff"},
            notes = "activate Tariff",
            produces = "application/json",
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
    public Result tariff_up(UUID tariff_id) {
        try {

            Model_Tariff tariff =  Model_Tariff.getById(tariff_id);
   
            tariff.up();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "order Tariff Down",
            tags = {"Admin-Tariff"},
            notes = "activate Tariff",
            produces = "application/json",
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
    public Result tariff_down(UUID tariff_id) {
        try {

            Model_Tariff tariff =  Model_Tariff.getById(tariff_id);
  
            tariff.down();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Tariff",
            tags = {"Admin-Tariff"},
            notes = "activate Tariff",
            produces = "application/json",
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
    public Result tariff_delete(UUID tariff_id) {
        try {

            Model_Tariff tariff =  Model_Tariff.getById(tariff_id);
      
            tariff.delete();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Tariff",
            tags = {"Admin-Tariff"},
            notes = "activate Tariff",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Tariff.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result tariff_get(UUID tariff_id) {
        try {

            Model_Tariff tariff =  Model_Tariff.getById(tariff_id);
          
            return ok(tariff);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// USER -  EXTENSION PACKAGES ##########################################################################################

    @ApiOperation(value = "create Product_Extension",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Extension is used to somehow(based on configuration and type) extend product capabilities. (e.g. how many projects can user have)",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_ProductExtension_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_ProductExtension.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result productExtension_create(UUID product_id) {
        try {

            // Get and Validate Object
            Swagger_ProductExtension_New help  = baseFormFactory.formFromRequestWithValidation(Swagger_ProductExtension_New.class);

            // Kontrola objektu
            Model_Product product = Model_Product.getById(product_id);
            
            try {
                ExtensionType type = ExtensionType.valueOf(help.extension_type);
            } catch (Exception e) {
                throw new Result_Error_NotFound(ExtensionType.class);
            }

            Model_ProductExtension extension = new Model_ProductExtension();
            extension.name = help.name;
            extension.description = help.description;
            extension.color = help.color;

            extension.type = ExtensionType.valueOf(help.extension_type);
            extension.active = true;
            extension.product = product;

            Object config = Configuration.getConfiguration( extension.type , help.config);
            extension.configuration = Json.toJson(config).toString();
            
            extension.save();

            return ok(extension);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Product_Extension",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Extension is used to somehow(based on configuration and type) extend product capabilities. (e.g. how many projects can user have)",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",                 response = Model_ProductExtension.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    public Result productExtension_get(UUID extension_id) {
        try {

            // Kontrola objektu
            Model_ProductExtension extension = Model_ProductExtension.getById(extension_id);
            
            return ok(extension);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Product_Extension List user Own",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Extension is used to somehow(based on configuration and type) extend product capabilities. (e.g. how many projects can user have)",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",                 response = Model_ProductExtension.class, responseContainer = "list"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    public Result productExtension_getAll() {
        try {

            return ok(Model_ProductExtension.getByUser(personId()));

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "update Product_Extension",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Updates extension. User can change name, description or color.",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_ProductExtension_Edit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",                 response = Model_ProductExtension.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result productExtension_update(UUID extension_id) {
        try {

            // Get and Validate Object
            Swagger_ProductExtension_Edit help  = baseFormFactory.formFromRequestWithValidation(Swagger_ProductExtension_Edit.class);

            // Kontrola objektu
            Model_ProductExtension extension = Model_ProductExtension.getById(extension_id);
 
            extension.name = help.name;
            extension.description = help.description;
            extension.color = help.color;

            extension.update();

            return ok(extension);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "activate Product_Extension",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Extension is used to somehow(based on configuration and type) extend product capabilities. (e.g. how many projects can user have)",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",                 response = Model_ProductExtension.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    public Result productExtension_activate(UUID extension_id) {
        try {

            // Kontrola objektu
            Model_ProductExtension extension = Model_ProductExtension.getById(extension_id);

            // Check Permission
            extension.check_act_deactivate_permission();

            if (extension.active) return badRequest("Extension is already activated");
            extension.active = true;

            extension.update();

            return ok(extension);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "deactivate Product_Extension",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Extension is used to somehow(based on configuration and type) extend product capabilities. (e.g. how many projects can user have)",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",                 response = Model_ProductExtension.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    public Result productExtension_deactivate(UUID extension_id) {
        try {

            // Kontrola objektu
            Model_ProductExtension extension = Model_ProductExtension.getById(extension_id);

            // Check Permission
            extension.check_act_deactivate_permission();

            if (!extension.active) return badRequest("Extension is already deactivated");
            extension.active = false;

            extension.update();

            return ok(extension);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Product_Extension",
            tags = {"Admin-Extension"},
            notes = "Extension is used to somehow(based on configuration and type) extend product capabilities. (e.g. how many projects can user have)",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    public Result productExtension_delete(UUID extension_id) {
        try {

            // Kontrola objektu
            Model_ProductExtension extension = Model_ProductExtension.getById(extension_id);

            extension.delete();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// tariffExtension ########################################################################################################

    @ApiOperation(value = "create Tariff_Extension",
            tags = {"Admin-Extension"},
            notes = "", //TODO
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_TariffExtension_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_ProductExtension.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result tariffExtension_create(UUID tariff_id) {
        try {

            // Get and Validate Object
            Swagger_TariffExtension_New help  = baseFormFactory.formFromRequestWithValidation(Swagger_TariffExtension_New.class);

            // Kontrola objektu
            Model_Tariff tariff = Model_Tariff.getById(tariff_id);

            try {
                ExtensionType type = ExtensionType.valueOf(help.extension_type);
            } catch (Exception e) {
                return notFound("Extension Type not found");
            }

            Model_ProductExtension extension = new Model_ProductExtension();
            extension.name = help.name;
            extension.description = help.description;
            extension.color = help.color;
            extension.type = ExtensionType.valueOf(help.extension_type);
            extension.active = true;


            // Config Validation
            try {

                Object config = Configuration.getConfiguration(extension.type, help.config);

            } catch (Exception e) {
                logger.warn("Tariff Extension Create - Invalid Json Format ");
                return badRequest("Invalid Configuration Json");
            }

            Object config = Configuration.getConfiguration(extension.type, help.config);
            extension.configuration = Json.toJson(config).toString();

            if (help.included) {
                extension.tariff_included = tariff;
            } else {
                extension.tariff_optional = tariff;
            }

            extension.save();

            return created(extension);

        } catch (IllegalStateException e) {
            return badRequest("Illegal or not Valid Config");
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Tariff_Extension",
            tags = {"Admin-Extension"},
            notes = "create new Tariff",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_TariffExtension_Edit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_ProductExtension.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result tariffExtension_update(UUID extension_id) {
        try {

            // Get and Validate Object
            Swagger_TariffExtension_Edit help  = baseFormFactory.formFromRequestWithValidation(Swagger_TariffExtension_Edit.class);

            // Kontrola objektu
            Model_ProductExtension extension = Model_ProductExtension.getById(extension_id);

            extension.name = help.name;
            extension.description = help.description;
            extension.color = help.color;
            extension.active = true;

            // Config Validation
            try {
                Object config = Configuration.getConfiguration(extension.type, help.config);
            } catch (Exception e) {
                logger.warn("Tariff Extension Create - Invalid Json Format ");
                return badRequest("Invalid Configuration Json");
            }

            extension.configuration = Json.toJson(Configuration.getConfiguration(extension.type, help.config)).toString();

            Model_Tariff tariff =   extension.tariff_optional;
            if (tariff == null) tariff = extension.tariff_included;

            if (help.included) {
                extension.tariff_optional = null;
                extension.tariff_included = tariff;
            } else {
                extension.tariff_included = null;
                extension.tariff_optional = tariff;
            }

            extension.update();

            return ok(extension);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "order Tariff_Extension UP",
            tags = {"Admin-Extension"},
            notes = "order Tariff in list",
            produces = "application/json",
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
    public Result tariffExtension_up(UUID extension_id) {
        try {

            // Kontrola objektu
            Model_ProductExtension extension = Model_ProductExtension.getById(extension_id);

            // Shift Up
            extension.up();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "order Tariff_Extension Down",
            tags = {"Admin-Extension"},
            notes = "order Tariff_Extension Down",
            produces = "application/json",
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
    public Result tariffExtension_down(UUID extension_id) {
        try {

            // Kontrola objektu
            Model_ProductExtension extension = Model_ProductExtension.getById(extension_id);

            // Shift Down
            extension.down();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "deactivate Tariff_Extension",
            tags = {"Admin-Extension"},
            notes = "order Tariff_Extension Down",
            produces = "application/json",
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
    public Result tariffExtension_deactivate(UUID extension_id) {
        try {

            // Kontrola objektu
            Model_ProductExtension extension = Model_ProductExtension.getById(extension_id);

            if (!extension.active) return badRequest("Tariff is already deactivated");
            extension.active = false;

            extension.update();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "active Tariff_Extension",
            tags = {"Admin-Extension"},
            notes = "order Tariff_Extension Down",
            produces = "application/json",
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
    public Result tariffExtension_activate(UUID extension_id) {
        try {

            // Kontrola objektu
            Model_ProductExtension extension = Model_ProductExtension.getById(extension_id);

            if (extension.active) return badRequest("Tariff is already activated");
            extension.active = true;

            extension.update();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Tariff_Extension",
            tags = {"Admin-Extension"},
            notes = "order Tariff_Extension Down",
            produces = "application/json",
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
    public Result tariffExtension_delete(UUID extension_id) {
        try {

            // Kontrola objektu
            Model_ProductExtension extension = Model_ProductExtension.getById(extension_id);

            extension.delete();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Tariff All types",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Extension is used to somehow(based on configuration and type) extend product capabilities. (e.g. how many projects can user have)",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",                 response = Swagger_ProductExtension_Type.class, responseContainer = "list"),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    public Result tariff_getAllTypes() {
        try {


            List<Swagger_ProductExtension_Type> types = new ArrayList<>();

            for (ExtensionType e : ExtensionType.values()) {

                Class<? extends utilities.financial.extensions.extensions.Extension> clazz = e.getExtensionClass();
                if (clazz != null) {
                    utilities.financial.extensions.extensions.Extension extension = clazz.newInstance();

                    Swagger_ProductExtension_Type type = new Swagger_ProductExtension_Type();
                    type.type = extension.getType().name();
                    type.name = extension.getName();
                    type.description = extension.getDescription();

                    types.add(type);
                }

            }
            return ok(types);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// USER PRODUCT ########################################################################################################

    @ApiOperation(value = "get Tariffs all",
            tags = {"Price & Invoice & Tariffs"},
            notes = "get all Tariffs - required for every else action in system. For example: Project is created under the Product which is under some Tariff",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Tariff.class, responseContainer = "list"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result tariff_getAll() {
        try {

            // Pokud má uživatel oprávnění vracím upravený SQL
            if (person().has_permission(Model_Tariff.Permission.Tariff_update.name())) {

                return ok(Model_Tariff.find.query().where().order().asc("order_position").findList());

            } else {

                return ok(Model_Tariff.find.query().where().eq("active", true).order().asc("order_position").findList());

            }

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "create Product",
            tags = {"Price & Invoice & Tariffs"},
            notes = "It is the base object. Peak of Pyramid :). This Api is used for its creation. You can get two kind of response: \n\n" +
                    "First(201):  System create new Object - Product \n\n" +
                    "Second(200): The product requires payment - The server creates the object, but returns an Invoice \n\n" +
                    "If the user choose credit card payment, the invoice will contain gw_url, which is a link to the payment gate, you can redirect him there. " +
                    "If bank transfer is chosen, server will return an Invoice, but the user will pay it independently via his bank account.",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Product_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created successfully",      response = Model_Product.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result product_create() {
        try {

            logger.debug("product_create: Creating new product");

            // Get and Validate Object
            Swagger_Product_New help  = baseFormFactory.formFromRequestWithValidation(Swagger_Product_New.class);

            // Kontrola Objektu
            Model_Tariff tariff = Model_Tariff.getById(help.tariff_id);

            Model_Customer customer = null;
            Model_Person person = _BaseController.person();

            Ebean.beginTransaction();

            if (help.customer_id != null) {

                customer = Model_Customer.getById(help.customer_id);

            } else {

                if (help.integrator_registration) {
                    return badRequest("Create Integrator Company First");
                }

                customer = new Model_Customer();

                Model_PaymentDetails customer_payment_details = new Model_PaymentDetails();
                if (help.company_name != null && help.company_name.length() > 0) {

                    customer_payment_details.company_account = true;

                    if (help.company_name != null)              customer_payment_details.company_name = help.company_name;
                    if (help.company_authorized_email != null)  customer_payment_details.company_authorized_email = help.company_authorized_email;
                    if (help.company_authorized_phone != null)  customer_payment_details.company_authorized_phone = help.company_authorized_phone;
                    if (help.company_web != null)               customer_payment_details.company_web = help.company_web;
                    if (help.street != null)                    customer_payment_details.street = help.street;
                    if (help.street_number != null)             customer_payment_details.street_number = help.street_number;
                    if (help.city != null)                      customer_payment_details.city = help.city;
                    if (help.zip_code != null)                  customer_payment_details.zip_code = help.zip_code;
                    if (help.country != null)                   customer_payment_details.country = help.country;
                    if (help.zip_code != null)                  customer_payment_details.company_vat_number = help.company_vat_number;
                    if (help.country != null)                   customer_payment_details.company_registration_no = help.company_registration_no;
                    if (help.invoice_email != null)             customer_payment_details.invoice_email = help.invoice_email;
                }

                Model_Employee employee = new Model_Employee();
                employee.state = ParticipantStatus.OWNER;
                employee.person = person;

                employee.customer = customer;
                customer.payment_details = customer_payment_details;
                customer.employees.add(employee);
                customer.save();
            }


            Model_Product product   = new Model_Product();
            product.name            = help.name;
            product.description     = help.description;
            product.active          = true;
            product.method          = help.payment_method;
            product.business_model  = tariff.business_model;
            product.credit          = tariff.credit_for_beginning;

            if (customer != null) product.customer = customer;


            Model_PaymentDetails payment_details = new Model_PaymentDetails();

            if (help.integrator_registration) {

                product.client_billing = true;
                payment_details.company_name = help.company_name;
                payment_details.company_authorized_email = help.company_authorized_email;
                payment_details.company_authorized_phone = help.company_authorized_phone;
                payment_details.company_web = help.company_web;
                payment_details.street = help.street;
                payment_details.street_number = help.street_number;
                payment_details.city = help.city;
                payment_details.zip_code = help.zip_code;
                payment_details.country = help.country;
                payment_details.company_vat_number = help.company_vat_number;
                payment_details.company_registration_no = help.company_registration_no;
                payment_details.invoice_email = help.invoice_email;


            } else {

                product.client_billing = false;

                if (customer.payment_details != null)       payment_details = customer.payment_details.copy();
                if (customer.fakturoid_subject_id != null)  product.fakturoid_subject_id = customer.fakturoid_subject_id;

                if (help.company_name != null)              payment_details.company_name = help.company_name;
                if (help.company_authorized_email != null)  payment_details.company_authorized_email = help.company_authorized_email;
                if (help.company_authorized_phone != null)  payment_details.company_authorized_phone = help.company_authorized_phone;
                if (help.company_web != null)               payment_details.company_web = help.company_web;
                if (help.street != null)                    payment_details.street = help.street;
                if (help.street_number != null)             payment_details.street_number = help.street_number;
                if (help.city != null)                      payment_details.city = help.city;
                if (help.zip_code != null)                  payment_details.zip_code = help.zip_code;
                if (help.country != null)                   payment_details.country = help.country;
                if (help.zip_code != null)                  payment_details.company_vat_number = help.company_vat_number;
                if (help.country != null)                   payment_details.company_registration_no = help.company_registration_no;
                if (help.invoice_email != null)             payment_details.invoice_email = help.invoice_email;

            }


            payment_details.save();

            product.payment_details = payment_details;
            product.save();

            payment_details.product = product;
            payment_details.update();

            payment_details.refresh();

            if (payment_details.isComplete()) {
                logger.trace("product_create:: payment_details.isComplete()");
            }

            if (payment_details.isCompleteCompany()) {
                logger.trace("product_create:: payment_details.isCompleteCompany()");
                logger.trace("product_create:: payment_details::" + Json.toJson(payment_details).toString());

            }


            if (product.fakturoid_subject_id == null) {
                logger.trace("product_create:: fakturoid_subject_id == null");
            }


            if ((payment_details.isComplete() || payment_details.isCompleteCompany()) && product.fakturoid_subject_id == null) {

                product.fakturoid_subject_id = fakturoid.create_subject(payment_details);

                if (product.fakturoid_subject_id == null) return badRequest("Payment details are invalid.");

                 product.update();

            }

            product.refresh();

            logger.debug("product_create: Adding extensions");

            List<Model_ProductExtension> extensions = new ArrayList<>();

            if (help.extension_ids.size() > 0) extensions.addAll( Model_ProductExtension.find.query().where().in("id", help.extension_ids).eq("tariff_optional.id", tariff.id).findList());
            extensions.addAll(tariff.extensions_included);

            for (Model_ProductExtension ext : extensions) {

                if (ext.active) {

                    Model_ProductExtension extension = ext.copy();
                    extension.product = product;
                    extension.save();
                }
            }

            Ebean.commitTransaction();

            return created(product);

        } catch (Exception e) {
            Ebean.endTransaction();
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Products user Own List",
            tags = {"Price & Invoice & Tariffs"},
            notes = "",     //TODO
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "List of users Products",    response = Model_Product.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_getAll() {
        try {
            // Kontrola objektu
            List<Model_Product> products = Model_Product.getByOwner(personId());

            // Vrácení seznamu
            return ok(products); // todo

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Product",
            tags = {"Price & Invoice & Tariffs"},
            notes = "",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "List of users Products",    response = Model_Product.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_get(UUID product_id) {
        try {

            // Kontrola Objektu
            Model_Product product = Model_Product.getById(product_id);

            // Vrácení seznamu
            return ok(product);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Product details",
            tags = {"Price & Invoice & Tariffs"},
            notes = "edit basic details of Product",
            produces = "application/json",
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
            @ApiResponse(code = 200, message = "Successfully updated",      response = Model_Product.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_update(UUID product_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help = baseFormFactory.formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola Objektu
            Model_Product product = Model_Product.getById(product_id);

            // úpravy objektu
            product.name = help.name;
            product.description = help.description;

            // Updatování do databáze
            product.update();

            // Vrácení objektu
            return  ok(product);

        } catch (Exception e) {
            return controllerServerError(e);
        }

    }

    @ApiOperation(value = "deactivate Product",
            tags = {"Price & Invoice & Tariffs"},
            notes = "deactivate product Tariff and deactivate all stuff under it",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Deactivating was successful",   response = Model_Product.class),
            @ApiResponse(code = 400, message = "Something is wrong",            response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",          response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",      response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",              response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",             response = Result_InternalServerError.class)
    })
    public Result product_deactivate(UUID product_id) {
        try {

            // Kontrola objektu
            Model_Product product = Model_Product.getById(product_id);

            // Kontrola oprávnění
            product.check_act_deactivate_permission();

            if (!product.active) return badRequest("Product is already deactivated");

            // Deaktivování (vyřazení všech funkcionalit produktu
            product.active = false;
            product.update();

            product.notificationDeactivation();

            // Vrácení potvrzení
            return ok(product);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "activate Product",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Activate product Tariff and deactivate all staff around that",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Activating was successful", response = Model_Product.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_activate(UUID product_id) {
        try {

            // Kontrola objektu
            Model_Product product = Model_Product.getById(product_id);

            // Kontrola oprávnění
            product.check_act_deactivate_permission();

            if (product.active) return badRequest("Product is already activated");

            // Aktivování
            product.active = true;
            product.update();

            product.notificationActivation();

            // Vrácení potvrzení
            return ok(product);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "buy Credit for given product",
            tags = {"Price & Invoice & Tariffs"},
            notes = "creates invoice - credit will be added after payment if payment method is bank transfer or if getting money from credit card is successful",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Product_Credit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",                 response = Model_Invoice.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_credit(UUID product_id) {
        try {

            // Get and Validate Object
            Swagger_Product_Credit help  = baseFormFactory.formFromRequestWithValidation(Swagger_Product_Credit.class);

            if (!(help.credit > 0)) return badRequest("Credit must be positive double number");

            // Find object
            Model_Product product = Model_Product.getById(product_id);

            Model_Invoice invoice = new Model_Invoice();
            invoice.product = product;
            invoice.method = product.method;

            Model_InvoiceItem invoice_item = new Model_InvoiceItem();
            invoice_item.name = "Credit upload";
            invoice_item.unit_price = 1L;
            invoice_item.quantity = (long) (help.credit * 1000);
            invoice_item.unit_name = "Credit";
            invoice_item.currency = Currency.USD;

            invoice.invoice_items.add(invoice_item);

            invoice = fakturoid.create_proforma(invoice);
            if (invoice == null) return badRequest("Failed to make an invoice, check your provided payment information");

            if (product.method == PaymentMethod.CREDIT_CARD) {

                invoice = goPay.singlePayment("Credit upload payment", product, invoice);
            }

            // Return serialized object
            return  ok(invoice);

        } catch (Exception e) {
            return controllerServerError(e);
        }

    }
    
    @ApiOperation(value = "delete Product Tariff",
            tags = {"Admin"},
            notes = "get PDF invoice file",
            produces = "multipartFormData",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_delete(UUID product_id) {
        try {

            // URČENO POUZE PRO ADMINISTRÁTORY S OPRÁVNĚNÍM MAZAT!

            // Kontrola objektu
            Model_Product product = Model_Product.getById(product_id);

            // Trvalé odstranění produktu!
            product.delete();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "create Product payment details",
            tags = {"Price & Invoice & Tariffs"},
            notes = "create payments details in Product",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_PaymentDetails_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_PaymentDetails.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result paymentDetails_create(UUID product_id) {
        try {

            // Get and Validate Object
            Swagger_PaymentDetails_New help  = baseFormFactory.formFromRequestWithValidation(Swagger_PaymentDetails_New.class);

            // Kontrola Objektu
            Model_Product product = Model_Product.getById(product_id);
            if (product.payment_details != null) return badRequest("Product already has Payment Details");


            Model_PaymentDetails payment_details = new Model_PaymentDetails();
            payment_details.street        = help.street;
            payment_details.street_number = help.street_number;
            payment_details.city          = help.city;
            payment_details.zip_code      = help.zip_code;
            payment_details.country       = help.country;
            payment_details.invoice_email = help.invoice_email;

            // Pokud je účet business - jsou vyžadovány následující informace
            if (payment_details.company_account) {

                if (help.company_vat_number != null) {
                    if (!Model_PaymentDetails.control_vat_number(help.company_vat_number))
                        return badRequest("Prefix code in VatNumber is not valid");
                    payment_details.company_vat_number   = help.company_vat_number;
                }

                payment_details.company_registration_no  = help.company_registration_no;
                payment_details.company_name             = help.company_name;
                payment_details.company_authorized_email = help.company_authorized_email;
                payment_details.company_authorized_phone = help.company_authorized_phone;
                payment_details.company_web              = help.company_web;
            }

            product.check_update_permission();

            product.fakturoid_subject_id = fakturoid.create_subject(payment_details);
            if (product.fakturoid_subject_id == null) return badRequest("Unable to create your payment details, check provided information.");

            product.method = help.method;
            product.update();

            payment_details.save();

            // Vrácení objektu
            return created(payment_details);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Product payment details",
            tags = {"Price & Invoice & Tariffs"},
            notes = "edit payments details in Product",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_PaymentDetails_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully updated",      response = Model_PaymentDetails.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result paymentDetails_update(UUID payment_details_id) {
        try {

            // Get and Validate Object
            Swagger_PaymentDetails_New help  = baseFormFactory.formFromRequestWithValidation(Swagger_PaymentDetails_New.class);

            // Kontrola Objektu
            Model_PaymentDetails payment_details = Model_PaymentDetails.getById(payment_details_id);

            payment_details.street          = help.street;
            payment_details.street_number   = help.street_number;
            payment_details.city            = help.city;
            payment_details.zip_code        = help.zip_code;
            payment_details.country         = help.country;
            payment_details.invoice_email   = help.invoice_email;
            payment_details.product.method  = help.method;

            // Pokud se změní nastavení na true (tedy jde o business účet změní se i objekt v databázi
            if (help.company_account && !payment_details.company_account) {
                payment_details.company_account = true;
            }

            if (!help.company_account && payment_details.company_account) {
                payment_details.company_account          = false;
                payment_details.company_registration_no  = null;
                payment_details.company_name             = null;
                payment_details.company_authorized_email = null;
                payment_details.company_authorized_phone = null;
                payment_details.company_web              = null;
                payment_details.invoice_email            = null;
            }

            // Pokud je účet business - jsou vyžadovány následující informace
            if (payment_details.company_account) {

                if (help.company_vat_number != null) {
                    if (!Model_PaymentDetails.control_vat_number(help.company_vat_number))
                        return badRequest("Prefix code in VatNumber is not valid");
                    payment_details.company_vat_number   = help.company_vat_number;
                }

                payment_details.company_registration_no  = help.company_registration_no;
                payment_details.company_name             = help.company_name;
                payment_details.company_authorized_email = help.company_authorized_email;
                payment_details.company_authorized_phone = help.company_authorized_phone;
                payment_details.company_web              = help.company_web;
            }

            if (payment_details.product.fakturoid_subject_id == null) {

                payment_details.product.fakturoid_subject_id = fakturoid.create_subject(payment_details);
                if (payment_details.product.fakturoid_subject_id == null) return badRequest("Unable to update your payment details, check provided information.");

                payment_details.update();

            } else {

                if (!fakturoid.update_subject(payment_details))
                    return badRequest("Unable to update your payment details, check provided information.");
            }

            payment_details.product.method = help.method;

            payment_details.product.update();

            // Vrácení objektu
            return  ok(payment_details);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Products user can used",
            tags = {"Price & Invoice & Tariffs"},
            notes = "get all the products that the user can use when creating new projects",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Product_Active.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_getActive() {
        try {

            List<Swagger_Product_Active> products = new ArrayList<>();

            for (Model_Product product : Model_Product.getApplicableByOwner(_BaseController.personId())) {
                Swagger_Product_Active help = new Swagger_Product_Active();
                help.id = product.id;
                help.name = product.name;

                products.add(help);
            }

            // Vrácení objektu
            return ok(products);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "terminate on demand",
            tags = {"Price & Invoice & Tariffs"},
            notes = "cancel automatic payments in Product",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully updated",      response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_terminateOnDemand(UUID product_id) {
        try {

            // Kontrola objektu
            Model_Product product = Model_Product.getById(product_id);

            if (product.gopay_id == null) return badRequest("Product has on demand payments turned off.");

            // Zrušení automatického strhávání z kreditní karty
            try {

                goPay.terminateOnDemand(product);

                product.gopay_id = null;
                product.on_demand = false;
                product.update();

                return ok("Successfully terminated on demand payment.");

            } catch (Exception e) {
                logger.internalServerError(e);
            }

            return badRequest("Request was unsuccessful.");

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// INVOICE #############################################################################################################

    @ApiOperation(value = "get Invoice",
            tags = {"Price & Invoice & Tariffs"},
            notes = "get summary information from invoice",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Invoice_FullDetails.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result invoice_get(UUID invoice_id) {
        try {

            // Kontrola objektu
            Model_Invoice invoice = Model_Invoice.getById(invoice_id);

            Swagger_Invoice_FullDetails help = new Swagger_Invoice_FullDetails();
            help.invoice = invoice;
            help.invoice_items = Model_InvoiceItem.find.query().where().eq("invoice.id", invoice_id).findList();

            return ok(help);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "resend Invoice",
            tags = {"Price & Invoice & Tariffs"},
            notes = "resend Invoice to specific email",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Resend_Email",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values - values in Json is not requierd"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong ",       response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result invoice_resend(UUID invoice_id) {
        try {

            // Get and Validate Object
            Swagger_Resend_Email help = baseFormFactory.formFromRequestWithValidation(Swagger_Resend_Email.class);

            // Kontrola objektu
            Model_Invoice invoice = Model_Invoice.getById(invoice_id);

            fakturoid.sendInvoiceEmail(invoice, help.email);

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "send Invoice reimbursement",
            tags = {"Price & Invoice & Tariffs"},
            notes = "reimbursement of an unpaid invoice - with settings from creating product before",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Invoice.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })  /**  Uživatel může zaplatit neúspěšně zaplacenou fakturu (službu) */
    public Result invoice_reimbursement(UUID invoice_id) {
        try {

            Model_Invoice invoice = Model_Invoice.getById(invoice_id);

            if ( invoice.status.equals(PaymentStatus.PAID)) return badRequest("Invoice is already paid");

            // vyvolání nové platby ale bez vytváření faktury nebo promofaktury
            invoice = goPay.singlePayment("First Payment", invoice.getProduct(), invoice);

            // Vrácení ID s možností uhrazení
            return ok(invoice);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Invoice PDF file",
            tags = {"Price & Invoice & Tariffs"},
            notes = "get PDF invoice file",
            produces = "multipartFormData",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result invoice_getPdf(String kind, UUID invoice_id) {
        try {

            if (!kind.equals("proforma") && !kind.equals("invoice")) return badRequest("kind should be 'proforma' or 'invoice'");

            Model_Invoice invoice = Model_Invoice.getById(invoice_id);


            if (kind.equals("proforma") && invoice.proforma_pdf_url == null) return badRequest("Proforma PDF is unavailable");


            byte[] pdf_in_array = fakturoid.download_PDF_invoice(kind, invoice);

            return file(pdf_in_array, kind.equals("proforma") ? "proforma_" + invoice.invoice_number + ".pdf" : invoice.invoice_number + ".pdf");

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "send Invoice Reminder",
            tags = {"Admin-Invoice"},
            notes = "get PDF invoice file",
            produces = "multipartFormData",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result invoice_reminder(UUID invoice_id) {
        try {

            // Kontrola objektu
            Model_Invoice invoice = Model_Invoice.getById(invoice_id);

            fakturoid.sendInvoiceReminderEmail(invoice,"You have pending unpaid invoice.");

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Invoice",
            tags = {"Admin-Invoice"},
            notes = "remove Invoice only with permission",
            produces = "multipartFormData",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result invoice_delete(UUID invoice_id) {
        try {

            // Kontrola objektu
            Model_Invoice invoice = Model_Invoice.getById(invoice_id);
          
            // TODO - Chybí navázání na fakturoid - smazání faktury (nějaký proces?)
           
            //Fakturoid_Controller.fakturoid_delete()
            logger.error("invoice_delete: Not Supported");
            throw new Result_Error_NotSupportedException();
            

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Invoice Synchronize with Fakturoid",
            tags = {"Admin"},
            notes = "remove Invoice only with permission",
            produces = "multipartFormData",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result invoice_synchronizeFakturoid(UUID invoice_id) {

        try {
            // TODO invoice_synchronizeFakturoid
            logger.error("invoice_synchronizeFakturoid: Not Supported");
            throw new Result_Error_NotSupportedException();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
    
    @ApiOperation(value = "edit Invoice Set As Paid",
            tags = {"Admin-Invoice"},
            notes = "remove Invoice only with permission",
            produces = "multipartFormData",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result invoice_set_as_paid(UUID invoice_id) {
        try {

            // TODO invoice_set_as_paid
            logger.error("invoice_set_as_paid: Not Supported");
            throw new Result_Error_NotSupportedException();
         
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// CUSTOMER ############################################################################################################

    @ApiOperation(value = "create Company",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Creates new Customer (type: company), you can crate new product under Customer(company) or under Customer(person)",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Customer_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created successfully",      response = Model_Customer.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result customer_create_company() {
        try {

            // Get and Validate Object
            Swagger_Customer_New help = baseFormFactory.formFromRequestWithValidation(Swagger_Customer_New.class);

            Model_Customer customer = new Model_Customer();
            
            customer.save();

            Model_Employee employee = new Model_Employee();
            employee.person = _BaseController.person();
            employee.state = ParticipantStatus.OWNER;
            employee.customer = customer;
            employee.save();

            customer.refresh();

            Model_PaymentDetails details = new Model_PaymentDetails();
            details.street = help.street;
            details.street_number = help.street_number;
            details.city = help.city;
            details.zip_code = help.zip_code;
            details.country = help.country;

            details.invoice_email = help.invoice_email;

            details.company_name = help.company_name;
            details.company_authorized_phone = help.company_authorized_phone;
            details.company_authorized_email = help.company_authorized_email;
            details.company_vat_number = help.company_vat_number;
            details.company_registration_no = help.company_registration_no;
            details.company_web = help.company_web;

            details.customer = customer;
            details.company_account = true;
            details.save();

            customer.fakturoid_subject_id = fakturoid.create_subject(details);
            customer.update();

            return created(customer);
        } catch (IllegalArgumentException e) {
            return badRequest("Payment details are invalid.");
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Companies All",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Gets all companies by logged user.",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Created successfully",      response = Model_Customer.class, responseContainer = "list"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result customer_get_all() {
        try {

            List<Model_Customer> customers = Model_Customer.find.query().where().eq("employees.person.id", _BaseController.personId()).eq("payment_details.company_account", true).findList();

            return ok(customers);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "update Company",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Updates payment details of a company.",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Customer_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Updated successfully",      response = Model_Customer.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result customer_update_company(UUID customer_id) {
        try {

            // Get and Validate Object
            Swagger_Customer_New help = baseFormFactory.formFromRequestWithValidation(Swagger_Customer_New.class);

            Model_Customer customer = Model_Customer.getById(customer_id);
        
            Model_PaymentDetails details = customer.payment_details;
            details.street          = help.street;
            details.street_number   = help.street_number;
            details.city            = help.city;
            details.zip_code        = help.zip_code;
            details.country         = help.country;

            details.invoice_email   = help.invoice_email;

            details.company_name             = help.company_name;
            details.company_authorized_phone = help.company_authorized_phone;
            details.company_authorized_email = help.company_authorized_email;
            details.company_vat_number       = help.company_vat_number;
            details.company_registration_no  = help.company_registration_no;
            details.company_web              = help.company_web;

            details.update();

            if (!fakturoid.update_subject(details))
                return badRequest("Payment details are invalid.");

            return ok(customer);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "add Employee",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Adds employee to a company.",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Customer_Employee",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Updated successfully",      response = Model_Customer.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result customer_add_employee() {
        try {

            // Get and Validate Object
            Swagger_Customer_Employee help  = baseFormFactory.formFromRequestWithValidation(Swagger_Customer_Employee.class);

            Model_Customer customer = Model_Customer.getById(help.customer_id);
         
            for (Model_Person person : Model_Person.find.query().where().in("email", help.mails).findList()) {

                // Abych nepřidával ty co už tam jsou
                if (customer.employees.stream().anyMatch(employee -> employee.get_person_id().equals(person.id))) continue;

                Model_Employee employee = new Model_Employee();
                employee.person     = person;
                employee.state      = ParticipantStatus.MEMBER;
                employee.customer   = customer;
                employee.save();
            }

            customer.refresh();

            return ok(customer);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "remove Employee",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Removes employee from a company.",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Removed successfully",      response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result customer_remove_employee(UUID employee_id) {
        try {
            
            Model_Employee employee = Model_Employee.getById(employee_id);
  
            employee.delete();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Company",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Deletes company.",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Removed successfully",      response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result customer_delete_company(UUID customer_id) {
        try {

            Model_Customer customer = Model_Customer.getById(customer_id);
       
            customer.delete();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
}