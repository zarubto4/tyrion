package controllers;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import exceptions.ForbiddenException;
import exceptions.NotSupportedException;
import io.ebean.Ebean;
import io.swagger.annotations.*;
import models.*;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.financial.fakturoid.FakturoidService;
import utilities.financial.services.ProductService;
import utilities.authentication.Authentication;
import utilities.enums.*;
import utilities.financial.extensions.configurations.Configuration;
import utilities.financial.extensions.consumptions.ResourceConsumption;
import utilities.financial.goPay.GoPay;
import utilities.logger.Logger;
import utilities.notifications.NotificationService;
import utilities.permission.PermissionService;
import utilities.swagger.input.*;
import utilities.swagger.output.Swagger_Invoice_FullDetails;
import utilities.swagger.output.Swagger_Product_Active;
import utilities.swagger.output.Swagger_TariffExtension_Type;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Authentication.class)
public class Controller_Finance extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Finance.class);

// CONTROLLER CONFIGURATION ############################################################################################


    private FakturoidService fakturoid;
    private ProductService productService;
    private GoPay goPay;

    @Inject
    public Controller_Finance(WSClient ws, _BaseFormFactory formFactory, Config config, FakturoidService fakturoid,
                              ProductService productService, GoPay goPay, PermissionService permissionService, NotificationService notificationService) {
        super(ws, formFactory, config, permissionService, notificationService);
        this.fakturoid = fakturoid;
        this.goPay = goPay;
        this.productService = productService;
    }

    // TARIFF ##########################################################################################################
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
            if (isAdmin()) {
                List<Model_Tariff> tariffs = Model_Tariff.find.query().where().order().asc("order_position").findList();
                return ok(tariffs);
            } else {
                return ok(Model_Tariff.find.query().where().eq("active", true).order().asc("order_position").findList());
            }

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

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
            Swagger_Tariff_New help = formFactory.formFromRequestWithValidation(Swagger_Tariff_New.class);
            if(!help.owner_details_required && help.payment_details_required) {
                badRequest("When payment details are required, we need owner's details as well!");
            }

            if (Model_Tariff.find.query().nullable().where().eq("identifier", help.identifier).findOne() != null) return badRequest("Identifier must be unique!");

            Model_Tariff tariff = new Model_Tariff();

            tariff.name                     = help.name;
            tariff.identifier               = help.identifier;
            tariff.description              = help.description;

            tariff.color                    = help.color;
            tariff.awesome_icon             = help.awesome_icon;

            tariff.credit_for_beginning     = new BigDecimal(help.credit_for_beginning);

            tariff.owner_details_required = help.owner_details_required;
            tariff.payment_details_required = help.payment_details_required;

            tariff.labels_json = Json.toJson(help.labels).toString();

            tariff.active                   = false;

            return create(tariff);
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
            Swagger_Tariff_New help = formFactory.formFromRequestWithValidation(Swagger_Tariff_New.class);

            Model_Tariff tariff = Model_Tariff.find.byId(tariff_id);

            if (Model_Tariff.find.query().nullable().where().ne("id", tariff_id).eq("identifier", help.identifier).findOne() != null) {
                return badRequest("Identifier must be unique!");
            }

            tariff.name                     = help.name;
            tariff.identifier               = help.identifier;
            tariff.description              = help.description;

            tariff.color                    = help.color;
            tariff.awesome_icon             = help.awesome_icon;

            tariff.payment_details_required = help.payment_details_required;
            tariff.owner_details_required = help.owner_details_required;

            tariff.credit_for_beginning     = new BigDecimal(help.credit_for_beginning);

            tariff.labels_json = Json.toJson(help.labels).toString();

            return update(tariff);

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

            Model_Tariff tariff = Model_Tariff.find.byId(tariff_id);

            this.checkActivatePermission(tariff);

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

            Model_Tariff tariff = Model_Tariff.find.byId(tariff_id);

            this.checkActivatePermission(tariff);

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
    public Result tariff_up(UUID tariff_id) {
        try {

            Model_Tariff tariff =  Model_Tariff.find.byId(tariff_id);

            tariff.up();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "order Tariff Down",
            tags = {"Admin-Tariff"},
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
    public Result tariff_down(UUID tariff_id) {
        try {

            Model_Tariff tariff =  Model_Tariff.find.byId(tariff_id);

            tariff.down();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Tariff",
            tags = {"Admin-Tariff"},
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
    public Result tariff_delete(UUID tariff_id) {
        try {
            return delete(Model_Tariff.find.byId(tariff_id));
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Tariff",
            tags = {"Admin-Tariff"},
            notes = "",
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
            return read(Model_Tariff.find.byId(tariff_id));
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "add Tariff Extension included",
            tags = {"Admin-Tariff"},
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
    public Result tariff_add_extension_included(UUID tariff_id, UUID extension_id) {
        try {
            Model_Tariff tariff = Model_Tariff.find.byId(tariff_id);

            this.checkUpdatePermission(tariff);

            Model_TariffExtension extension = Model_TariffExtension.find.byId(extension_id);

            if(tariff.extensions_included.contains(extension)) {
                return badRequest("Tariff already contains given extension.");
            }

            if(tariff.extensions_recommended.contains(extension)) {
                tariff.extensions_recommended.remove(extension);
            }

            if(!tariff.extensions_included.add(extension)) {
                return badRequest("Tariff cannot be added.");
            }

            tariff.save();
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "add Tariff Extension recommended",
            tags = {"Admin-Tariff"},
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
    public Result tariff_add_extension_recommended(UUID tariff_id, UUID extension_id) {
        try {

            Model_Tariff tariff = Model_Tariff.find.byId(tariff_id);

            this.checkUpdatePermission(tariff);

            Model_TariffExtension extension = Model_TariffExtension.find.byId(extension_id);

            if(tariff.extensions_recommended.contains(extension)) {
                return badRequest("Tariff already contains given extension.");
            }

            if(tariff.extensions_included.contains(extension)) {
                tariff.extensions_included.remove(extension);
            }

            if(!tariff.extensions_recommended.add(extension)) {
                return externalServerError("Tariff cannot be added.");
            }

            tariff.save();
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "remove Tariff Extension",
            tags = {"Admin-Tariff"},
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
    public Result tariff_remove_extension(UUID tariff_id, UUID extension_id) {
        try {

            Model_Tariff tariff = Model_Tariff.find.byId(tariff_id);

            this.checkUpdatePermission(tariff);

            Model_TariffExtension extension = Model_TariffExtension.find.byId(extension_id);

            boolean removed = tariff.extensions_included.remove(extension) || tariff.extensions_recommended.remove(extension);

            if(!removed) {
                return badRequest("Extension was not part of the tariff.");
            }

            tariff.save();
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    // TARIFF EXTENSION ################################################################################################
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
            if (isAdmin()) {

                return ok(Model_TariffExtension.find.query().where().order().asc("order_position").findList());

            } else {

                return ok(Model_TariffExtension.find.query().where().eq("active", true).order().asc("order_position").findList());

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
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully created",      response = Model_TariffExtension.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    public Result tariffExtension_get(UUID extension_id) {
        try {
            return read(Model_TariffExtension.find.byId(extension_id));
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
            Swagger_TariffExtension_New help  = formFactory.formFromRequestWithValidation(Swagger_TariffExtension_New.class);

            Model_TariffExtension extension = new Model_TariffExtension();
            extension.name = help.name;
            extension.description = help.description;
            extension.color = help.color;
            extension.active = true;

            try {
                ExtensionType type = ExtensionType.valueOf(help.extension_type);
                extension.type = type;
            } catch (Exception e) {
                return notFound("Extension Type not found");
            }

            // Config Validation
            try {

                Configuration config = Configuration.getConfiguration(extension.type, help.config);
                extension.configuration = Json.toJson(config).toString();

            } catch (Exception e) {
                logger.warn("Tariff Extension Create - Invalid Configuration Json Format ");
                return badRequest("Invalid Configuration Json");
            }

            // Config ResourceConsumption
            try {

                ResourceConsumption consumption = ResourceConsumption.getConsumption(extension.type, help.consumption);
                extension.consumption = Json.toJson(consumption).toString();

            } catch (Exception e) {
                logger.warn("Tariff Extension Create - Invalid ResourceConsumption Json Format ");
                return badRequest("Invalid ResourceConsumption Json");
            }

            return create(extension);

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
            Swagger_TariffExtension_Edit help  = formFactory.formFromRequestWithValidation(Swagger_TariffExtension_Edit.class);

            // Kontrola objektu
            Model_TariffExtension extension = Model_TariffExtension.find.byId(extension_id);

            extension.name = help.name;
            extension.description = help.description;
            extension.color = help.color;

            // Config Validation
            try {

                Configuration config = Configuration.getConfiguration(extension.type, help.config);
                extension.configuration = Json.toJson(config).toString();

            } catch (Exception e) {
                logger.warn("Tariff Extension Create - Invalid Configuration Json Format ");
                return badRequest("Invalid Configuration Json");
            }

            // Config ResourceConsumption
            try {

                ResourceConsumption consumption = ResourceConsumption.getConsumption(extension.type, help.consumption);
                extension.consumption = Json.toJson(consumption).toString();

            } catch (Exception e) {
                logger.warn("Tariff Extension Create - Invalid ResourceConsumption Json Format ");
                return badRequest("Invalid ResourceConsumption Json");
            }

            return update(extension);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "deactivate Tariff_Extension",
            tags = {"Admin-Extension"},
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
            Model_TariffExtension extension = Model_TariffExtension.find.byId(extension_id);

            this.checkActivatePermission(extension);

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
            Model_TariffExtension extension = Model_TariffExtension.find.byId(extension_id);

            this.checkActivatePermission(extension);

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

    @ApiOperation(value = "order TariffExtension Up",
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
    public Result tariffExtension_up(UUID extension_id) {
        try {

            Model_TariffExtension tariffExtension =  Model_TariffExtension.find.byId(extension_id);

            tariffExtension.up();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "order TariffExtension Down",
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
    public Result tariffExtension_down(UUID extension_id) {
        try {

            Model_TariffExtension extension = Model_TariffExtension.find.byId(extension_id);

            extension.down();

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
            return delete(Model_TariffExtension.find.byId(extension_id));
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
            @ApiResponse(code = 200, message = "OK Result",                 response = Swagger_TariffExtension_Type.class, responseContainer = "list"),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    public Result tariff_getAllTypes() {
        try {
            List<Swagger_TariffExtension_Type> types = new ArrayList<>();
            for (ExtensionType e : ExtensionType.values()) {
                Swagger_TariffExtension_Type type = new Swagger_TariffExtension_Type();
                type.type = e.name();
                type.name = e.getTypeName();
                type.description = e.getTypeDescription();

                types.add(type);
            }

            return ok(types);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    // PRODUCT #########################################################################################################
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
            Swagger_Product_New help = formFactory.formFromRequestWithValidation(Swagger_Product_New.class);
            Model_Product product = productService.createAndActivateProduct(help);

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
            return read(Model_Product.find.byId(product_id));
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
            Swagger_NameAndDescription help = formFactory.formFromRequestWithValidation(Swagger_NameAndDescription.class);

            Model_Product product = Model_Product.find.byId(product_id);
            product.name = help.name;
            product.description = help.description;

            return update(product);

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
            Model_Product product = Model_Product.find.byId(product_id);

            this.checkActivatePermission(product);

            if (!product.active) return badRequest("Product is already deactivated");

            // Deaktivování (vyřazení všech funkcionalit produktu
            product.setActive(false);

            for(UUID id : product.get_projects_ids()){
                Model_Project.find.evict(id);
            }

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
            Model_Product product = Model_Product.find.byId(product_id);

            this.checkActivatePermission(product);

            if (product.active) return badRequest("Product is already activated");

            // Aktivování
            product.setActive(true);

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
//        try {
//
//            // Get and Validate Object
//            Swagger_Product_Credit help  = formFactory.formFromRequestWithValidation(Swagger_Product_Credit.class);
//
//            if (!(help.credit > 0)) return badRequest("Credit must be positive double number");
//
//            // Find object
//            Model_Product product = Model_Product.find.byId(product_id);
//
//            Model_Invoice invoice = new Model_Invoice();
//            invoice.product = product;
//            invoice.method = product.method;
//
//            Model_InvoiceItem invoice_item = new Model_InvoiceItem();
//            invoice_item.name = "Credit upload";
//            invoice_item.unit_price = 1L;
//            invoice_item.quantity = (long) (help.credit * 1000);
//            invoice_item.unit_name = "Credit";
//            invoice_item.currency = Currency.USD;
//
//            invoice.invoice_items.add(invoice_item);
//
//            Fakturoid_Invoice proforma = fakturoid.create_proforma(invoice);
//            if (proforma == null) return badRequest("Failed to make an invoice, check your provided payment information");
//            // TODO use information
//
//            if (product.method == PaymentMethod.CREDIT_CARD) {
//
//                invoice = goPay.singlePayment("Credit upload payment", product, invoice);
//            }
//
//            // Return serialized object
//            return  ok(invoice);
//
//        } catch (Exception e) {
//            return controllerServerError(e);
//        }
          return ok();
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
            return delete(Model_Product.find.byId(product_id));
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "create contact customer",
            tags = {"Price & Invoice & Tariffs"},
            notes = "create contact in Product",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Contact_Update",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_Contact.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result contact_customer_create(UUID customer_id) {
        try {

            // Get and Validate Object
            Swagger_Contact_Update help = formFactory.formFromRequestWithValidation(Swagger_Contact_Update.class);

            // Kontrola Objektu
            Model_Customer customer = Model_Customer.find.byId(customer_id);
            if (customer.contact != null) {
                return badRequest("Customer already has Contact");
            }

            this.checkUpdatePermission(customer);

            Model_Contact contact = productService.setContact(help, null);
            contact.save();

            customer.contact = contact;
            customer.update();

            // Vrácení objektu
            return created(contact);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Contact details",
            tags = {"Price & Invoice & Tariffs"},
            notes = "edit contact",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Contact_Update",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully updated",      response = Model_Contact.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result contact_update(UUID contact_id) {
        try {

            // Get and Validate Object
            Swagger_Contact_Update help = formFactory.formFromRequestWithValidation(Swagger_Contact_Update.class);

            // Kontrola Objektu
            Model_Contact contact = Model_Contact.find.byId(contact_id);
            this.checkUpdatePermission(contact);

            productService.setContact(help, contact);

            // Vrácení objektu
            return  ok(contact);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Payment Details",
            tags = {"Price & Invoice & Tariffs"},
            notes = "edit payment details",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_PaymentDetails_Update",
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
    public Result payment_details_update(UUID payment_details_id) {
        try {

            // Get and Validate Object
            Swagger_PaymentDetails_Update help  = formFactory.formFromRequestWithValidation(Swagger_PaymentDetails_Update.class);

            // Kontrola Objektu
            Model_PaymentDetails paymentDetails = Model_PaymentDetails.find.byId(payment_details_id);
            paymentDetails.payment_method = help.payment_method;

            return  update(paymentDetails);

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
            Model_Product product = Model_Product.find.byId(product_id);

//            if (product.contact.gopay_id == null) return badRequest("Product has on demand payments turned off.");
//
//            // Zrušení automatického strhávání z kreditní karty
//            try {
//
//                goPay.terminateOnDemand(product);
//
//                product.contact.gopay_id = null;
//                product.contact.update();
//
//                product.on_demand = false;
//                product.update();
//
//                return ok("Successfully terminated on demand payment.");
//
//            } catch (Exception e) {
//                logger.internalServerError(e);
//            }

            return badRequest("Request was unsuccessful.");

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "product events",
            tags = {"Product"},
            notes = "get product history events",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully updated",      response = Model_ProductEvent.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_getEvents(UUID product_id, int page, int items) {
        try {
            Model_Product product = Model_Product.find.byId(product_id);

            this.checkReadPermission(product);

            page--;
            page = Math.max(0, page);

            ProductEventTypeReadPermission permission = ProductEventTypeReadPermission.USER;

            if(isAdmin()) {
                permission = ProductEventTypeReadPermission.ADMIN;
            }

            List<Model_ProductEvent> productEvents = product.getProductEvents(page, items, false, permission).getList();
            return ok(productEvents);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Product invoices",
            tags = {"Price & Invoice & Tariffs"},
            notes = "get all invoices for product",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Invoice.class, responseContainer = "list"),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_getInvoices(UUID product_id) {
        try {
            // Kontrola objektu
            Model_Product product = Model_Product.find.byId(product_id);

            this.checkReadPermission(product);

            List<Model_Invoice> invoicesList = product.getInvoices(isAdmin());
            return ok(invoicesList);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// PRODUCT EXTENSION ###############################################################################################

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
            Swagger_ProductExtension_New help = formFactory.formFromRequestWithValidation(Swagger_ProductExtension_New.class);
            Model_Product product = Model_Product.find.byId(product_id);

            this.checkUpdatePermission(product);

            Model_ProductExtension extension = productService.createAndActivateExtension(product, help);

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
            return read(Model_ProductExtension.find.byId(extension_id));
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

            return ok(Model_ProductExtension.getByUser(personId())); // TODO permissions

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Product_Extension List Product",
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
    public Result productExtension_getAllProduct(UUID product_id) {
        try {
            // Kontrola Objektu
            Model_Product product = Model_Product.find.byId(product_id);

            this.checkReadPermission(product);

            return ok(product.getExtensions());

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
            Model_ProductExtension extension = Model_ProductExtension.find.byId(extension_id);
            this.checkActivatePermission(extension);
            extension.setActive(true);

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

            Model_ProductExtension extension = Model_ProductExtension.find.byId(extension_id);
            this.checkActivatePermission(extension);
            extension.setActive(false);

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
            return delete(Model_ProductExtension.find.byId(extension_id));
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Product Extension Financial Events",
            tags = {"Price & Invoice & Extension"},
            notes = "",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_ExtensionFinancialEvent_Search",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",                 response = Model_ExtensionFinancialEvent.class, responseContainer="List"),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    public Result productExtensionFinancialEvents_get() {
        try {

            Swagger_ExtensionFinancialEvent_Search help = formFactory.formFromRequestWithValidation(Swagger_ExtensionFinancialEvent_Search.class);

            List<Model_ExtensionFinancialEvent> events = Model_ExtensionFinancialEvent.getFinancialEvents(help.product_id, help.invoice_id, help.extension_id, help.from, help.to, false);

            return ok(events);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// INVOICE #############################################################################################################

    @ApiOperation(value = "get Invoice",
            tags = {"Price & Invoice & Tariffs"},
            notes = "get summary information from invoice, invoice items only for user with update permission",
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
            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);

            this.checkReadPermission(invoice);

            Swagger_Invoice_FullDetails help = new Swagger_Invoice_FullDetails();
            help.invoice = invoice;

            try {
                this.checkUpdatePermission(invoice); // kompletní detaily může získat pouze uživatel s oprávněním update!
                help.invoice_items = Model_InvoiceItem.find.query().where().eq("invoice.id", invoice_id).findList();

            } catch (ForbiddenException e) {
                // nothing
            }

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
            Swagger_Resend_Email help = formFromRequestWithValidation(Swagger_Resend_Email.class);

            // Kontrola objektu
            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);

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

            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);

            if (invoice.status.equals(InvoiceStatus.PAID)) return badRequest("Invoice is already paid");

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

            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);

            this.checkReadPermission(invoice);

            if (kind.equals("proforma") && invoice.proforma_pdf_url == null) return badRequest("Proforma PDF is unavailable");


            byte[] pdf_in_array = fakturoid.downloadPdfInvoice(kind, invoice);
            String fileName = kind.equals("proforma") ? "proforma_" + invoice.invoice_number + ".pdf" : invoice.invoice_number + ".pdf";
            return file(pdf_in_array, fileName).as("application/pdf");

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
            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);

            this.checkReadPermission(invoice);

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
            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);

            // TODO - Chybí navázání na fakturoid - smazání faktury (nějaký proces?)

            //Fakturoid_Controller.fakturoid_delete()
            logger.error("invoice_delete: Not Supported");
            throw new NotSupportedException();


        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "Synchronize Invoice with Fakturoid",
            tags = {"Admin"},
            notes = "remove Invoice only with permission",
            produces = "multipartFormData",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Invoice.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result invoice_synchronize_fakturoid(UUID invoice_id) {

        try {
            // Kontrola objektu
            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);

            this.checkUpdatePermission(invoice);

            try {
                if (invoice.status == InvoiceStatus.UNCONFIRMED) {
                    fakturoid.createAndUpdateProforma(invoice);
                    return ok(invoice);
                }

                if (invoice.status == InvoiceStatus.PENDING || invoice.status == InvoiceStatus.OVERDUE) {
                    fakturoid.checkPaidProforma(invoice);
                    return ok(invoice);
                }
            }
            catch (Exception e) {
                return badRequest("Synchronization was not successful.");
            }

            logger.error("invoice_synchronizeFakturoid: Not Supported for Status {}.", invoice.status);
            throw new NotSupportedException();

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
            throw new NotSupportedException();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "confirm Invoice",
            tags = {"Admin-Invoice"},
            notes = "confirm Invoice above product monthly limit only with permission",
            produces = "multipartFormData",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Invoice.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result invoice_confirm(UUID invoice_id) {
        try {
            // Kontrola objektu
            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);

            this.checkUpdatePermission(invoice);

            if(invoice.status != InvoiceStatus.UNCONFIRMED) {
                return badRequest("Invoice is not in state UNCONFIRMED.");
            }

            invoice.status = InvoiceStatus.UNFINISHED;
            invoice.update();

            invoice.saveEvent(invoice.updated, ProductEventType.INVOICE_CONFIRMED, "{personId:" + personId() + "}");

            fakturoid.createAndUpdateProforma(invoice);

            return ok(invoice);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// CUSTOMER ############################################################################################################

    @ApiOperation(value = "Create Customer",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Creates new Customer",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Contact_Update",
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
    public Result create_customer() {
        try {

            // Get and Validate Object
            Swagger_Contact_Update help = formFactory.formFromRequestWithValidation(Swagger_Contact_Update.class);

            Model_Customer customer = new Model_Customer();

            customer.save();

            Model_Employee employee = new Model_Employee();
            employee.person = _BaseController.person();
            employee.state = ParticipantStatus.OWNER;
            employee.customer = customer;
            employee.save();

            customer.refresh();

            Model_Contact contact = productService.setContact(help, null);

            customer.contact = contact;
            customer.update();

            return created(customer);
        } catch (IllegalArgumentException e) {
            return badRequest("Contact data are invalid.");
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Customers All",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Gets all customers by logged user.",
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

            List<Model_Customer> customers = Model_Customer.find.query().where().eq("employees.person.id", _BaseController.personId()).findList();

            return ok(customers);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "add Employee",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Adds employee to a company. Add them again for send new invitations",
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
            Swagger_Customer_Employee help  = formFromRequestWithValidation(Swagger_Customer_Employee.class);

            Model_Customer customer = Model_Customer.find.byId(help.customer_id);

            for (Model_Person person : Model_Person.find.query().where().in("email", help.mails).findList()) {

                // Abych nepřidával ty co už tam jsou
                if (customer.employees.stream().anyMatch(employee -> employee.get_person_id().equals(person.id))) continue;

                Model_Employee employee = new Model_Employee();
                employee.person     = person;
                employee.state      = ParticipantStatus.MEMBER;
                employee.customer   = customer;

                this.checkCreatePermission(employee);

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
            return delete(Model_Employee.find.byId(employee_id));
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
}