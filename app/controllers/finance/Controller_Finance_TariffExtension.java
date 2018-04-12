package controllers.finance;

import com.google.inject.Inject;
import controllers._BaseController;
import controllers._BaseFormFactory;
import io.swagger.annotations.*;
import models.Model_Tariff;
import models.Model_TariffExtension;
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
import utilities.swagger.output.Swagger_TariffExtension_Type;

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

    @ApiOperation(value = "get Tariff Extensions all",
            tags = {"Price & Invoice & Tariffs"},
            notes = "get all Tariffs - required for every else action in system. For example: Project is created under the Product which is under some Tariff",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_TariffExtension.class, responseContainer = "list"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result tariffExtension_getAll() {
        try {

            // Pokud má uživatel oprávnění vracím upravený SQL
            if (person().has_permission(Model_TariffExtension.Permission.TariffExtension_update.name())) {

                return ok(Model_TariffExtension.find.query().where().order().asc("name").findList());

            } else {

                return ok(Model_TariffExtension.find.query().where().eq("active", true).order().asc("name").findList());

            }

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "Get Tariff_Extension",
            tags = {"Price & Invoice & Tariffs"},
            notes = "", //TODO
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_TariffExtension.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    public Result tariffExtension_get(UUID extension_id) {
        try {
            Model_TariffExtension extension = Model_TariffExtension.getById(extension_id);
            return created(extension);

        } catch (Exception e) {
            return controllerServerError(e);
        }
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
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_TariffExtension.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result tariffExtension_create() {
        try {

            // Get and Validate Object
            Swagger_TariffExtension_New help  = baseFormFactory.formFromRequestWithValidation(Swagger_TariffExtension_New.class);

            try {
                ExtensionType type = ExtensionType.valueOf(help.extension_type);
            } catch (Exception e) {
                return notFound("Extension Type not found");
            }

            Model_TariffExtension extension = new Model_TariffExtension();
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_TariffExtension.class),
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
            Model_TariffExtension extension = Model_TariffExtension.getById(extension_id);

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

            extension.update();

            return ok(extension);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "deactivate Tariff_Extension",
            tags = {"Admin-Tariff"},
            notes = "deactivate Tariff Extension",
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
            Model_TariffExtension extension = Model_TariffExtension.getById(extension_id);
            if(extension == null) {
                return notFound("Invalid extension id.");
            }

            if (!extension.active) {
                return badRequest("Tariff extension is already deactivated");
            }
            extension.active = false;

            extension.update();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "activate Tariff_Extension",
            tags = {"Admin-Extension"},
            notes = "",
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
            Model_TariffExtension extension = Model_TariffExtension.getById(extension_id);
            if(extension == null) {
                return notFound("Invalid extension id.");
            }

            if (extension.active) {
                return badRequest("Tariff Extension is already activated");
            }

            extension.active = true;
            extension.update();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Tariff_Extension",
            tags = {"Admin-Extension"},
            notes = "",
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
            Model_TariffExtension extension = Model_TariffExtension.getById(extension_id);
            if(extension == null) {
                return notFound("Invalid extension id.");
            }

            extension.delete();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
//

    @ApiOperation(value = "get Tariff All types",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Extension is used to somehow(based on configuration and type) extend product capabilities. (e.g. how many projects can user have)",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",                 response = Swagger_TariffExtension_Type.class, responseContainer = "list"),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    public Result tariff_getAllTypes() {
        try {


            List<Swagger_TariffExtension_Type> types = new ArrayList<>();

            for (ExtensionType e : ExtensionType.values()) {

                Class<? extends utilities.financial.extensions.extensions.Extension> clazz = e.getExtensionClass();
                if (clazz != null) {
                    utilities.financial.extensions.extensions.Extension extension = clazz.newInstance();

                    Swagger_TariffExtension_Type type = new Swagger_TariffExtension_Type();
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
