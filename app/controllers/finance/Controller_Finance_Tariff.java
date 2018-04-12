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
import utilities.logger.Logger;
import utilities.swagger.input.Swagger_Tariff_New;

import java.util.UUID;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Authentication.class)
public class Controller_Finance_Tariff extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Finance_Tariff.class);

// CONTROLLER CONFIGURATION ############################################################################################

    private _BaseFormFactory baseFormFactory;

    @Inject
    public Controller_Finance_Tariff(_BaseFormFactory formFactory) {
        this.baseFormFactory = formFactory;
    }

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

            Model_Tariff tariff =  Model_Tariff.getById(tariff_id);

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

            Model_Tariff tariff =  Model_Tariff.getById(tariff_id);

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

            Model_Tariff tariff =  Model_Tariff.getById(tariff_id);

            tariff.delete();

            return ok();

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

            Model_Tariff tariff =  Model_Tariff.getById(tariff_id);
            return ok(tariff);

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
            Model_Tariff tariff =  Model_Tariff.getById(tariff_id);
            if(tariff == null) {
                return notFound("Invalid tariff id.");
            }

            Model_TariffExtension extension =  Model_TariffExtension.getById(extension_id);
            if(extension == null) {
                return notFound("Invalid extension id.");
            }

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

            Model_Tariff tariff =  Model_Tariff.getById(tariff_id);
            if(tariff == null) {
                return notFound("Invalid tariff id.");
            }

            Model_TariffExtension extension =  Model_TariffExtension.getById(extension_id);
            if(extension == null) {
                return notFound("Invalid extension id.");
            }

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

            Model_Tariff tariff =  Model_Tariff.getById(tariff_id);
            if(tariff == null) {
                return notFound("Invalid tariff id.");
            }

            Model_TariffExtension extension =  Model_TariffExtension.getById(extension_id);
            if(extension == null) {
                return notFound("Invalid extension id.");
            }

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
}
