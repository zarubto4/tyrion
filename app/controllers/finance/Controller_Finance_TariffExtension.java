package controllers.finance;

import com.google.inject.Inject;
import controllers._BaseController;
import controllers._BaseFormFactory;
import io.swagger.annotations.*;
import models.Model_ProductExtension;
import models.Model_Tariff;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.authentication.Authentication;
import utilities.enums.ExtensionType;
import utilities.financial.extensions.configurations.Configuration;
import utilities.logger.Logger;
import utilities.swagger.input.Swagger_TariffExtension_Edit;
import utilities.swagger.input.Swagger_TariffExtension_New;
import utilities.swagger.output.Swagger_ProductExtension_Type;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Authentication.class)
public class Controller_Finance_TariffExtension extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Finance_TariffExtension.class);

// CONTROLLER CONFIGURATION ############################################################################################

    private _BaseFormFactory baseFormFactory;

    @Inject
    public Controller_Finance_TariffExtension(_BaseFormFactory formFactory) {
        this.baseFormFactory = formFactory;
    }

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
}
