package controllers.finance;

import com.google.inject.Inject;
import controllers._BaseController;
import controllers._BaseFormFactory;
import io.swagger.annotations.*;
import models.Model_Product;
import models.Model_ProductExtension;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.authentication.Authentication;
import utilities.enums.ExtensionType;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.financial.extensions.configurations.Configuration;
import utilities.financial.fakturoid.Fakturoid;
import utilities.financial.goPay.GoPay;
import utilities.logger.Logger;
import utilities.swagger.input.Swagger_ProductExtension_Edit;
import utilities.swagger.input.Swagger_ProductExtension_New;

import java.util.UUID;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Authentication.class)
public class Controller_Finance_ProductExtension extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Finance_ProductExtension.class);

// CONTROLLER CONFIGURATION ############################################################################################

    private _BaseFormFactory baseFormFactory;

    @Inject
    public Controller_Finance_ProductExtension(_BaseFormFactory formFactory) {
        this.baseFormFactory = formFactory;
    }

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
}
