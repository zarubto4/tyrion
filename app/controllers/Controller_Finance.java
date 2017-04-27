package controllers;

import io.swagger.annotations.*;
import models.*;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.enums.*;
import utilities.fakturoid.Utilities_Fakturoid_Controller;
import utilities.goPay.Utilities_GoPay_Controller;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.login_entities.Secured_API;
import utilities.login_entities.Secured_Admin;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Swagger_Product_Active;
import utilities.swagger.outboundClass.Swagger_Invoice_FullDetails;
import utilities.swagger.outboundClass.Swagger_ProductExtension_Type;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured_API.class)
public class Controller_Finance extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Finance.class);

// ADMIN - TARIFF SETTINGS #############################################################################################

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result tariff_create(){
        try {
            final Form<Swagger_Tariff_New> form = Form.form(Swagger_Tariff_New.class).bindFromRequest();
            if (form.hasErrors()) return GlobalResult.formExcepting(form.errorsAsJson());
            Swagger_Tariff_New help = form.get();

            if (Model_Tariff.find.where().eq("identifier", help.identifier).findUnique() != null)
                return GlobalResult.result_BadRequest("Identifier must be unique!");

            Model_Tariff tariff = new Model_Tariff();

            tariff.name                     = help.name;
            tariff.identifier               = help.identifier;
            tariff.description              = help.description;

            tariff.color                    = help.color;

            tariff.payment_required         = help.payment_required;
            tariff.credit_for_beginning     = (long) (help.credit_for_beginning * 1000);

            tariff.company_details_required = help.company_details_required;
            tariff.payment_mode_required    = help.payment_mode_required;
            tariff.payment_method_required  = help.payment_method_required;

            tariff.credit_card_support      = help.credit_card_support;
            tariff.bank_transfer_support    = help.bank_transfer_support;

            tariff.mode_annually            = help.mode_annually;
            tariff.mode_credit              = help.mode_credit;
            tariff.free_tariff              = help.free_tariff;

            tariff.active                   = true;

            tariff.save();

            return GlobalResult.result_ok(Json.toJson(tariff));
        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result tariff_update(){
        try {

            final Form<Swagger_Tariff_New> form = Form.form(Swagger_Tariff_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Tariff_New help = form.get();

            if (help.id == null) return GlobalResult.result_BadRequest("Tariff id is required");

            Model_Tariff tariff = Model_Tariff.find.byId(help.id);
            if(tariff == null) return GlobalResult.notFoundObject("Tariff not found");

            if (Model_Tariff.find.where().ne("id", help.id).eq("identifier", help.identifier).findUnique() != null)
                return GlobalResult.result_BadRequest("Identifier must be unique!");

            tariff.name                     = help.name;
            tariff.identifier               = help.identifier;
            tariff.description              = help.description;

            tariff.color                    = help.color;

            tariff.payment_required         = help.payment_required;

            tariff.company_details_required = help.company_details_required;
            tariff.payment_mode_required    = help.payment_mode_required;
            tariff.payment_method_required  = help.payment_method_required;

            tariff.credit_card_support      = help.credit_card_support;
            tariff.bank_transfer_support    = help.bank_transfer_support;

            tariff.mode_annually            = help.mode_annually;
            tariff.mode_credit              = help.mode_credit;
            tariff.free_tariff              = help.free_tariff;

            tariff.update();

            return GlobalResult.result_ok(Json.toJson(tariff));

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend ", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    public Result tariff_deactivate(String tariff_id){
        try {

            Model_Tariff tariff = Model_Tariff.find.byId(tariff_id);
            if(tariff == null) return GlobalResult.notFoundObject("Tariff not found");

            if (!tariff.active) return GlobalResult.result_BadRequest("Tariff is already deactivated");

            tariff.active = false;

            tariff.update();

            return GlobalResult.result_ok(Json.toJson(tariff));

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend ", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    public Result tariff_activate(String tariff_id){
        try {

            Model_Tariff tariff = Model_Tariff.find.byId(tariff_id);
            if(tariff == null) return GlobalResult.notFoundObject("Tariff not found");

            if (tariff.active) return GlobalResult.result_BadRequest("Tariff is already activated");

            tariff.active = true;

            tariff.update();

            return GlobalResult.result_ok(Json.toJson(tariff));

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    public Result tariff_up(String tariff_id){
        try{

            Model_Tariff tariff =  Model_Tariff.find.byId(tariff_id);
            if(tariff == null) return GlobalResult.notFoundObject("Tariff not found");

            tariff.up();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    public Result tariff_down(String tariff_id){
        try{

            Model_Tariff tariff =  Model_Tariff.find.byId(tariff_id);
            if(tariff == null) return GlobalResult.notFoundObject("Tariff not found");

            tariff.down();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

// ADMIN - TARIFF LABEL ################################################################################################

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result tariffLabel_create(){
        try {

            final Form<Swagger_TariffLabel_New> form = Form.form(Swagger_TariffLabel_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TariffLabel_New help = form.get();

            Model_Tariff tariff = Model_Tariff.find.byId(help.id);
            if(tariff == null) return GlobalResult.notFoundObject("Tariff not found");

            Model_TariffLabel label = new Model_TariffLabel();
            label.tariff = tariff;
            label.description = help.description;
            label.label = help.label;
            label.icon = help.icon;
            label.save();

            return GlobalResult.result_ok(Json.toJson(label));

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result tariffLabel_update(){
        try {

            final Form<Swagger_TariffLabel_New> form = Form.form(Swagger_TariffLabel_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TariffLabel_New help = form.get();

            Model_TariffLabel label = Model_TariffLabel.find.byId(help.id);
            if(label == null) return GlobalResult.notFoundObject("TariffLabel not found");

            label.description = help.description;
            label.label = help.label;
            label.icon = help.icon;
            label.update();

            return GlobalResult.result_ok(Json.toJson(label));

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    public Result tariffLabel_up(String label_id){
        try{

            Model_TariffLabel label =  Model_TariffLabel.find.byId(label_id);
            if(label == null) return GlobalResult.notFoundObject("TariffLabel not found");

            label.up();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    public Result tariffLabel_down(String label_id){
        try{

            Model_TariffLabel label =  Model_TariffLabel.find.byId(label_id);
            if(label == null) return GlobalResult.notFoundObject("TariffLabel not found");

            label.down();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove label from general Tariffs", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    public Result tariffLabel_delete(String label_id){
        try{

            Model_TariffLabel label =  Model_TariffLabel.find.byId(label_id);
            if(label == null) return GlobalResult.notFoundObject("TariffLabel not found");

            label.delete();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

// USER -  EXTENSION PACKAGES ##########################################################################################

    @ApiOperation(value = "create Product Extension",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Extension is used to somehow(based on config and type) extend product capabilities. (e.g. how many projects can user have)",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_ProductExtension_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_ProductExtension.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result productExtension_create(){
        try{

            final Form<Swagger_ProductExtension_New> form = Form.form(Swagger_ProductExtension_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_ProductExtension_New help = form.get();

            Model_Product product = Model_Product.find.byId(help.product_id);
            if(product == null) return GlobalResult.notFoundObject("Product not found");

            Model_ProductExtension extension = new Model_ProductExtension();
            extension.name = help.name;
            extension.description = help.description;
            extension.color = help.color;
            extension.type = help.type;
            extension.active = true;
            extension.removed = false;
            extension.product = product;

            Model_ProductExtension.Config config = new Model_ProductExtension.Config();
            config.price = extension.getExtensionType().getDefaultDailyPrice();
            config.count = help.count;

            extension.config = Json.toJson(config).toString();

            if (!extension.create_permission()) return GlobalResult.forbidden_Permission();

            extension.save();

            return GlobalResult.result_ok(Json.toJson(extension));

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Product Extension by ID",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Extension is used to somehow(based on config and type) extend product capabilities. (e.g. how many projects can user have)",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",                 response = Model_ProductExtension.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    public Result productExtension_get(@ApiParam(value = "extension_id String query", required = true) String extension_id){
        try{

            Model_ProductExtension extension = Model_ProductExtension.get_byId(extension_id);
            if (extension == null) return GlobalResult.notFoundObject("Extension not found");

            if (!extension.read_permission()) return GlobalResult.forbidden_Permission();

            return GlobalResult.result_ok(Json.toJson(extension));

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all Product Extension of logged user",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Extension is used to somehow(based on config and type) extend product capabilities. (e.g. how many projects can user have)",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",                 response = Model_ProductExtension.class, responseContainer = "list"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    public Result productExtension_getAll(){
        try{

            return GlobalResult.result_ok(Json.toJson(Model_ProductExtension.get_byUser(Controller_Security.get_person_id())));

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all Product Extension types",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Extension is used to somehow(based on config and type) extend product capabilities. (e.g. how many projects can user have)",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",                 response = Swagger_ProductExtension_Type.class, responseContainer = "list"),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    public Result productExtension_getAllTypes(){
        try{

            return GlobalResult.result_ok(Json.toJson(Model_ProductExtension.getExtensionTypes()));

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "update Product Extension",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Updates extension. User can change name, description or color.",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_ProductExtension_Edit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",                 response = Model_ProductExtension.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result productExtension_update(@ApiParam(value = "extension_id String query", required = true) String extension_id){
        try{

            final Form<Swagger_ProductExtension_Edit> form = Form.form(Swagger_ProductExtension_Edit.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_ProductExtension_Edit help = form.get();

            Model_ProductExtension extension = Model_ProductExtension.get_byId(extension_id);
            if(extension == null) return GlobalResult.notFoundObject("Extension not found");

            if (!extension.edit_permission()) return GlobalResult.forbidden_Permission();

            extension.name = help.name;
            extension.description = help.description;
            extension.color = help.color;

            extension.update();

            return GlobalResult.result_ok(Json.toJson(extension));

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "activate Product Extension",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Extension is used to somehow(based on config and type) extend product capabilities. (e.g. how many projects can user have)",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",                 response = Model_ProductExtension.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    public Result productExtension_activate(@ApiParam(value = "extension_id String query", required = true) String extension_id){
        try{

            Model_ProductExtension extension = Model_ProductExtension.get_byId(extension_id);
            if (extension == null) return GlobalResult.notFoundObject("Extension not found");

            if (!extension.act_deactivate_permission()) return GlobalResult.forbidden_Permission();

            if (extension.active) return GlobalResult.result_BadRequest("Extension is already activated");

            extension.active = true;

            extension.update();

            return GlobalResult.result_ok(Json.toJson(extension));

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "deactivate Product Extension",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Extension is used to somehow(based on config and type) extend product capabilities. (e.g. how many projects can user have)",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",                 response = Model_ProductExtension.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    public Result productExtension_deactivate(@ApiParam(value = "extension_id String query", required = true) String extension_id){
        try{

            Model_ProductExtension extension = Model_ProductExtension.get_byId(extension_id);
            if (extension == null) return GlobalResult.notFoundObject("Extension not found");

            if (!extension.act_deactivate_permission()) return GlobalResult.forbidden_Permission();

            if (!extension.active) return GlobalResult.result_BadRequest("Extension is already deactivated");

            extension.active = false;

            extension.update();

            return GlobalResult.result_ok(Json.toJson(extension));

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Product Extension",
            tags = {"Price & Invoice & Tariffs"},
            notes = "Extension is used to somehow(based on config and type) extend product capabilities. (e.g. how many projects can user have)",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",                 response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side error" ,        response = Result_InternalServerError.class)
    })
    public Result productExtension_delete(@ApiParam(value = "extension_id String query", required = true) String extension_id){
        try{

            Model_ProductExtension extension = Model_ProductExtension.get_byId(extension_id);
            if (extension == null) return GlobalResult.notFoundObject("Extension not found");

            if (!extension.delete_permission()) return GlobalResult.forbidden_Permission();

            extension.removed = true;

            extension.update();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    @BodyParser.Of(BodyParser.Json.class)
    public Result tariffExtension_create(){
        try{

            final Form<Swagger_TariffExtension_New> form = Form.form(Swagger_TariffExtension_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TariffExtension_New help = form.get();

            Model_Tariff tariff = Model_Tariff.find.byId(help.id);
            if(tariff == null) return GlobalResult.notFoundObject("Tariff not found");

            Model_ProductExtension extension = new Model_ProductExtension();
            extension.name = help.name;
            extension.description = help.description;
            extension.color = help.color;
            extension.type = help.type;
            extension.active = true;

            Model_ProductExtension.Config config = new Model_ProductExtension.Config();
            config.price = (long) (help.price * 1000);
            config.count = help.count;

            extension.config = Json.toJson(config).toString();

            if (help.included){
                extension.tariff_included = tariff;
            } else {
                extension.tariff_optional = tariff;
            }

            if (!extension.create_permission()) return GlobalResult.forbidden_Permission();

            extension.save();

            return GlobalResult.result_ok(Json.toJson(extension));

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    @BodyParser.Of(BodyParser.Json.class)
    public Result tariffExtension_update(){
        try{

            final Form<Swagger_TariffExtension_New> form = Form.form(Swagger_TariffExtension_New.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TariffExtension_New help = form.get();

            Model_ProductExtension extension = Model_ProductExtension.find.byId(help.id);
            if(extension == null) return GlobalResult.notFoundObject("Extension not found");

            extension.name = help.name;
            extension.description = help.description;
            extension.type = help.type;
            extension.color = help.color;
            extension.active = true;

            Model_ProductExtension.Config config = new Model_ProductExtension.Config();
            config.price = (long) (help.price * 1000);
            config.count = help.count;

            extension.config = Json.toJson(config).toString();

            if (!extension.edit_permission()) return GlobalResult.forbidden_Permission();

            extension.update();

            return GlobalResult.result_ok(Json.toJson(extension));

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "up label from general Tariffs", hidden = true)
    public Result tariffExtension_up(String extension_id){
        try{

            Model_ProductExtension extension = Model_ProductExtension.find.byId(extension_id);
            if(extension == null) return GlobalResult.notFoundObject("Extension not found");
            extension.up();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariffExtension_down(String extension_id){
        try{

            Model_ProductExtension extension = Model_ProductExtension.find.byId(extension_id);
            if(extension == null) return GlobalResult.notFoundObject("Extension not found");

            extension.down();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariffExtension_deactivate(String extension_id){
        try{

            Model_ProductExtension extension = Model_ProductExtension.find.byId(extension_id);
            if(extension == null) return GlobalResult.notFoundObject("Extension not found");

            extension.active = false;
            extension.update();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariffExtension_activate(String extension_id){
        try{

            Model_ProductExtension extension = Model_ProductExtension.find.byId(extension_id);
            if(extension == null) return GlobalResult.notFoundObject("Extension not found");

            extension.active = true;
            extension.update();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result tariffExtension_delete(String extension_id){
        try{

            Model_ProductExtension extension = Model_ProductExtension.find.byId(extension_id);
            if(extension == null) return GlobalResult.notFoundObject("Extension not found");

            extension.delete();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

// USER PRODUCT ########################################################################################################

    @ApiOperation(value = "get all Tariffs",
            tags = {"Price & Invoice & Tariffs"},
            notes = "get all Tariffs - required for every else action in system. For example: Project is created under the Product which is under some Tariff",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Tariff.class, responseContainer = "list"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result tariff_getAll(){
        try{

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(Model_Tariff.find.where().eq("active", true).order().asc("order_position").findList()));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "create Product under Tariff",
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
                            dataType = "utilities.swagger.documentationClass.Swagger_Product_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Created successfully - payment is required",    response = Model_Invoice.class),
            @ApiResponse(code = 201, message = "Created successfully - payment not required",   response = Model_Product.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result product_create(){
        try{

            terminal_logger.debug("product_create: Creating new product");

            // Zpracování Json
            final Form<Swagger_Product_New> form = Form.form(Swagger_Product_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Product_New help = form.get();

            Model_Tariff tariff = Model_Tariff.find.byId(help.tariff_id);
            if(tariff == null) return GlobalResult.result_BadRequest("Tariff identifier id: {" + help.tariff_id  + "} not found or not supported now! Use only supported");

            Model_Person person;

            if(help.person_id == null) person = Controller_Security.get_person();
            else person = Model_Person.get_byId(help.person_id);

            if (person == null) return GlobalResult.notFoundObject("Person not found");

            if(Model_Product.get_byNameAndOwner(help.name, person.id) != null) return GlobalResult.result_BadRequest("You cannot use same Product name twice!");

            Model_Product product   = new Model_Product();
            product.tariff          = tariff;
            product.name            = help.name;
            product.active          = true;
            product.mode            = Enum_Payment_mode.free;
            product.method          = Enum_Payment_method.free;
            product.business_model  = Enum_BusinessModel.saas;

            Model_PaymentDetails payment_details = new Model_PaymentDetails();
            payment_details.person = person;
            payment_details.company_account = false;

            if (help.full_name != null) payment_details.full_name = help.full_name;
            else payment_details.full_name = person.full_name;

            payment_details.street          = help.street;
            payment_details.street_number   = help.street_number;
            payment_details.city            = help.city;
            payment_details.zip_code        = help.zip_code;
            payment_details.country         = help.country;
            payment_details.invoice_email   = help.invoice_email;

            if(tariff.company_details_required){

                if(help.registration_no == null && help.vat_number == null) return GlobalResult.result_BadRequest("company_registration_no or vat_number is required with this tariff");
                if(help.company_name == null)               return GlobalResult.result_BadRequest("company_name is required with this tariff");
                if(help.company_authorized_email == null)   return GlobalResult.result_BadRequest("company_authorized_email is required with this tariff");
                if(help.company_authorized_phone == null)   return GlobalResult.result_BadRequest("company_authorized_phone is required with this tariff");
                if(help.company_web == null)                return GlobalResult.result_BadRequest("company_web is required with this tariff");

                try {
                    new URL(help.company_web);
                } catch (MalformedURLException malformedURLException) {
                    return GlobalResult.result_BadRequest("company_web invalid value");
                }

                if(help.vat_number != null) payment_details.company_vat_number = help.vat_number;

                if(help.registration_no != null) payment_details.company_registration_no = help.registration_no;

                payment_details.company_account = true;
                payment_details.company_name             = help.company_name;
                payment_details.company_authorized_email = help.company_authorized_email;
                payment_details.company_authorized_phone = help.company_authorized_phone;
                payment_details.company_web              = help.company_web;
            }

            terminal_logger.debug("product_create: Payment details are done");

            if(tariff.payment_mode_required) {

                terminal_logger.debug("product_create: Payment mode Required");

                if(help.payment_mode == null) return GlobalResult.result_BadRequest("Payment_mode is required!");

                product.mode = help.payment_mode;
            }

            if(tariff.payment_method_required) {

                terminal_logger.debug("product_create: Payment method Required");

                if(help.payment_method == null) return GlobalResult.result_BadRequest("payment_method is required with this tariff");

                product.method = help.payment_method;
            }

            payment_details.save();
            product.save();

            payment_details.product = product;
            payment_details.update();

            product.payment_details = payment_details;
            product.credit = tariff.credit_for_beginning;
            product.update();

            // Přidám ty, co vybral uživatel
            if(help.extension_ids.size() > 0) {

                for (Model_ProductExtension ext : Model_ProductExtension.find.where().in("id", help.extension_ids).eq("tariff_optional.id", tariff.id).findList()){

                    if(ext.active) {

                        Model_ProductExtension extension = Model_ProductExtension.copyExtension(ext);
                        extension.product = product;

                        if (!extension.create_permission()) return GlobalResult.forbidden_Permission();

                        extension.save();
                    }
                }
            }

            // Okopíruji všechny aktivní, které má Tarrif už v sobě
            for (Model_ProductExtension ext : tariff.extensions_included){

                if(ext.active) {

                    Model_ProductExtension extension = Model_ProductExtension.copyExtension(ext);
                    extension.product = product;

                    if (!extension.create_permission()) return GlobalResult.forbidden_Permission();

                    extension.save();
                }
            }

            product.refresh();

            if(!tariff.payment_required) {
                terminal_logger.debug("product_create:: Payment is not required!");
                return GlobalResult.created(Json.toJson(product));
            }

            terminal_logger.debug("product_create:: Creating invoice");

            Model_Invoice invoice = new Model_Invoice();
            invoice.method = product.method;
            invoice.product = product;

            Model_InvoiceItem invoice_item = new Model_InvoiceItem();
            invoice_item.name = product.tariff.name + " in Mode(" + product.mode.name() + ")";
            invoice_item.unit_price = product.price() * 30;
            invoice_item.quantity = (long) 1;
            invoice_item.unit_name = "Currency";
            invoice_item.currency = Enum_Currency.USD;

            invoice.invoice_items.add(invoice_item);


            terminal_logger.debug("product_create:: Saving invoice");

            terminal_logger.debug("product_create::  Creating Proforma in fakturoid");
            invoice = Utilities_Fakturoid_Controller.create_proforma(invoice);
            if (invoice == null) return GlobalResult.result_BadRequest("Failed to make an invoice, check your provided payment information");
            terminal_logger.debug("product_create::  Proforma done");

            invoice = Utilities_GoPay_Controller.singlePayment("First Payment", product, invoice);

            return Controller.ok(Json.toJson(invoice));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all Products with all information's",
            tags = {"Price & Invoice & Tariffs"},
            notes = "",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of users Products",    response =  Model_Product.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error", response = Result_InternalServerError.class)
    })
    public Result product_getAll(){
        try{

            // Kontrola objektu
            List<Model_Product> products = Model_Product.get_byOwner(Controller_Security.get_person_id());

            // Vrácení seznamu
            return GlobalResult.result_ok(Json.toJson(products));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Product details",
            tags = {"Price & Invoice & Tariffs"},
            notes = "edit basic details of Product",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Product_Edit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated",      response = Model_Product.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_update(@ApiParam(value = "product_id String query", required = true) String product_id){
        try{

            // Vytvoření pomocného Objektu
            final Form<Swagger_Product_Edit> form = Form.form(Swagger_Product_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Product_Edit help = form.get();

            // Kontrola Objektu
            Model_Product product = Model_Product.get_byId(product_id);
            if(product == null) return GlobalResult.notFoundObject("Product product_id not found");

            // Oprávnění operace
            if(!product.edit_permission()) return GlobalResult.forbidden_Permission();

            // úpravy objektu
            product.name = help.name;

            // Updatování do databáze
            product.update();

            // Vrácení objektu
            return  GlobalResult.result_ok(Json.toJson(product));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }

    }

    @ApiOperation(value = "deactivate Product Tariff",
            tags = {"Price & Invoice & Tariffs"},
            notes = "deactivate product and deactivate all stuff under it",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Deactivating was successful",   response = Model_Product.class),
            @ApiResponse(code = 400, message = "Something is wrong",            response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",          response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",      response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Not found object",              response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",             response = Result_InternalServerError.class)
    })
    public Result product_deactivate(@ApiParam(value = "product_id String query", required = true) String product_id){
        try{

            // Kontrola objektu
            Model_Product product = Model_Product.get_byId(product_id);
            if(product == null) return GlobalResult.notFoundObject("Product product_id not found");

            // Kontorla oprávnění
            if(!product.act_deactivate_permission()) return GlobalResult.forbidden_Permission();

            if (!product.active) return GlobalResult.result_BadRequest("Product is already deactivated");

            // Deaktivování (vyřazení všech funkcionalit produktu
            product.active = false;
            product.update();

            // Vrácení potvrzení
            return GlobalResult.result_ok(Json.toJson(product));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "activate Product Tariff",
            tags = {"Price & Invoice & Tariffs"},
            notes = "activate product and deactivate all staff around that",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Activating was successful", response = Model_Product.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_activate(@ApiParam(value = "product_id String query", required = true) String product_id){
        try{

            // Kontrola objektu
            Model_Product product = Model_Product.get_byId(product_id);
            if(product == null) return GlobalResult.notFoundObject("Product product_id not found");

            // Kontrola oprávnění
            if(!product.act_deactivate_permission()) return GlobalResult.forbidden_Permission();

            if (product.active) return GlobalResult.result_BadRequest("Product is already activated");

            // Aktivování
            product.active = true;
            product.update();

            // Vrácení potvrzení
            return GlobalResult.result_ok(Json.toJson(product));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "buy credit for given product",
            tags = {"Price & Invoice & Tariffs"},
            notes = "creates invoice - credit will be added after payment if payment method is bank transfer or if getting money from credit card is successful",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Product_Credit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",                 response = Model_Invoice.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_credit(@ApiParam(value = "product_id String query", required = true) String product_id){
        try{

            // Binding Json with help object
            final Form<Swagger_Product_Credit> form = Form.form(Swagger_Product_Credit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Product_Credit help = form.get();

            if (!(help.credit > 0)) return GlobalResult.result_BadRequest("Credit must be positive double number");

            // Find object
            Model_Product product = Model_Product.get_byId(product_id);
            if(product == null) return GlobalResult.notFoundObject("Product not found");

            // Check permission
            if(!product.edit_permission()) return GlobalResult.forbidden_Permission();

            Model_Invoice invoice = new Model_Invoice();
            invoice.product = product;
            invoice.method = product.method;

            Model_InvoiceItem invoice_item = new Model_InvoiceItem();
            invoice_item.name = product.product_type() + " in Mode(" + product.mode.name() + ")";
            invoice_item.unit_price = (long) (help.credit * 1000);
            invoice_item.quantity = (long) 1;
            invoice_item.unit_name = "Currency";
            invoice_item.currency = Enum_Currency.USD;

            invoice.invoice_items.add(invoice_item);

            invoice = Utilities_Fakturoid_Controller.create_proforma(invoice);
            if (invoice == null) return GlobalResult.result_BadRequest("Failed to make an invoice, check your provided payment information");

            switch (product.method){

                case bank_transfer:{

                    // TODO

                    break;
                }

                case credit_card:{

                    invoice = Utilities_GoPay_Controller.singlePayment("Credit upload payment", product, invoice);
                    // TODO

                    break;
                }

                default: return GlobalResult.result_BadRequest("Payment method is undefined");
            }

            // Return serialized object
            return  GlobalResult.result_ok(Json.toJson(invoice));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }

    }

    @ApiOperation(value = "delete Product Tariff", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    public Result product_delete(String product_id){
        try{

            // URČENO POUZE PRO ADMINISTRÁTORY S OPRÁVNĚNÍM MAZAT!

            // Kontrola objektu
            Model_Product product = Model_Product.get_byId(product_id);
            if(product == null) return GlobalResult.notFoundObject("Product product_id not found");

            // Kontorla oprávnění
            if(!product.delete_permission()) return GlobalResult.forbidden_Permission();

            // Trvalé odstranění produktu!
            product.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Product payment details",
            tags = {"Price & Invoice & Tariffs"},
            notes = "edit payments details in Product",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_PaymentDetails_Edit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated",      response = Model_PaymentDetails.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result paymentDetails_update(@ApiParam(value = "payment_details_id Long query", required = true) Long payment_details_id){
        try{

            // Vytvoření pomocného Objektu
            final Form<Swagger_PaymentDetails_Edit> form = Form.form(Swagger_PaymentDetails_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_PaymentDetails_Edit help = form.get();

            // Kontrola Objektu
            Model_PaymentDetails payment_details = Model_PaymentDetails.find.byId(payment_details_id);
            if(payment_details == null) return GlobalResult.notFoundObject("PaymentDetails not found");

            // Oprávnění operace
            if(!payment_details.edit_permission()) return GlobalResult.forbidden_Permission();

            // úpravy objektu
            payment_details.street        = help.street;
            payment_details.street_number = help.street_number;
            payment_details.city          = help.city;
            payment_details.zip_code      = help.zip_code;
            payment_details.country       = help.country;
            payment_details.invoice_email = help.invoice_email;

            // Pokud se změní nastavení na true (tedy jde o business účet změní se i objekt v databázi
            if (help.company_account && !payment_details.company_account){
                payment_details.company_account = true;
            }

            if (!help.company_account && payment_details.company_account){
                payment_details.company_account          = false;
                payment_details.company_registration_no  = null;
                payment_details.company_name             = null;
                payment_details.company_authorized_email = null;
                payment_details.company_authorized_phone = null;
                payment_details.company_web              = null;
                payment_details.invoice_email            = null;
            }

            // Pokud je účet business - jsou vyžadovány následující informace
            if(payment_details.company_account) {

                if (help.registration_no == null)           return GlobalResult.result_BadRequest("company_registration_no is required with this tariff");
                if (help.company_name == null)              return GlobalResult.result_BadRequest("company_name is required with this tariff");
                if (help.company_authorized_email == null)  return GlobalResult.result_BadRequest("company_authorized_email is required with this tariff");
                if (help.company_authorized_phone == null)  return GlobalResult.result_BadRequest("company_authorized_phone is required with this tariff");
                if (help.company_web == null)               return GlobalResult.result_BadRequest("company_web is required with this tariff");

                if (help.vat_number != null) {
                    if (!Model_PaymentDetails.control_vat_number(help.vat_number))
                        return GlobalResult.result_BadRequest("Prefix code in VatNumber is not valid");
                    payment_details.company_vat_number   = help.vat_number;
                }

                payment_details.company_registration_no  = help.registration_no;
                payment_details.company_name             = help.company_name;
                payment_details.company_authorized_email = help.company_authorized_email;
                payment_details.company_authorized_phone = help.company_authorized_phone;
                payment_details.company_web              = help.company_web;
            }

            if (payment_details.product.fakturoid_subject_id == null) {


                payment_details.product.fakturoid_subject_id = Utilities_Fakturoid_Controller.create_subject(payment_details);
                if (payment_details.product.fakturoid_subject_id == null) return GlobalResult.result_BadRequest("Unable to update your payment details, check provided information.");

                payment_details.update();
            }

            if (!Utilities_Fakturoid_Controller.update_subject(payment_details))
                return GlobalResult.result_BadRequest("Unable to update your payment details, check provided information.");

            // Vrácení objektu
            return  GlobalResult.result_ok(Json.toJson(payment_details));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all active products that the User can use",
            tags = {"Price & Invoice & Tariffs"},
            notes = "get all the products that the user can use when creating new projects",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Product_Active.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_getActive(){
        try{

            List<Swagger_Product_Active> products = new ArrayList<>();

            for(Model_Product product : Model_Product.get_applicableByOwner(Controller_Security.get_person_id())){
                Swagger_Product_Active help = new Swagger_Product_Active();
                help.id = product.id;
                help.name = product.name;
                help.tariff = product.tariff.name;

                products.add(help);
            }

            // Vrácení objektu
            return GlobalResult.result_ok( Json.toJson(products));

        }catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "terminate on demand",
            tags = {"Price & Invoice & Tariffs"},
            notes = "cancel automatic payments in Product",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated",      response = Result_ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_terminateOnDemand(@ApiParam(value = "product_id String query", required = true) String product_id){
        try{

            // Kontrola objektu
            Model_Product product = Model_Product.get_byId(product_id);
            if(product == null) return GlobalResult.notFoundObject("Product not found");

            // Oprávnění operace
            if(!product.edit_permission()) return GlobalResult.forbidden_Permission();

            if(product.gopay_id == null) return GlobalResult.result_BadRequest("Product has on demand payments turned off.");

            // Zrušení automatického strhávání z kreditní karty
            if (product.terminateOnDemand()) return GlobalResult.result_ok("Successfully terminated on demand payment.");

            return GlobalResult.result_BadRequest("Request was unsuccessful.");

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

// INVOICE #############################################################################################################

    @ApiOperation(value = "get Invoice with all details",
            tags = {"Price & Invoice & Tariffs"},
            notes = "get summary information from invoice",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Swagger_Invoice_FullDetails.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error", response = Result_InternalServerError.class)
    })
    public Result invoice_get(String invoice_id){
        try{

            // Kontrola objektu
            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);
            if(invoice == null) return GlobalResult.notFoundObject("Invoice invoice_id not found");

            if(!invoice.read_permission()) return GlobalResult.forbidden_Permission();
            Swagger_Invoice_FullDetails help = new Swagger_Invoice_FullDetails();
            help.invoice = invoice;
            help.invoice_items = Model_InvoiceItem.find.where().eq("invoice.id", invoice_id).findList();

            return GlobalResult.result_ok(Json.toJson(help));

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "re-send Invoice to specific email",
            tags = {"Price & Invoice & Tariffs"},
            notes = "re-send Invoice to specific email",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Resend_Email",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values - values in Json is not requierd"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Result_ok.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error", response = Result_InternalServerError.class)
    })
    public Result invoice_resend(String invoice_id){
        try{

            // Vytvoření pomocného Objektu
            final Form<Swagger_Resend_Email> form = Form.form(Swagger_Resend_Email.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Resend_Email help = form.get();


            // Kontrola objektu
            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);
            if(invoice == null) return GlobalResult.notFoundObject("Invoice invoice_id not found");
            if(!invoice.read_permission()) return GlobalResult.forbidden_Permission();

            Utilities_Fakturoid_Controller.sendInvoiceEmail(invoice, help.mail);

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "reimbursement of an unpaid invoice",
            tags = {"Price & Invoice & Tariffs"},
            notes = "reimbursement of an unpaid invoice - with settings from creating product before",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Invoice.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })  /**  Uživatel může zaplatit neúspěšně zaplacenou fakturu (službu) */
    public Result invoice_reimbursement(String invoice_id) {
        try {

            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);
            if(invoice == null) return GlobalResult.notFoundObject("Invoice invoice_id not found");

            if(!invoice.read_permission()) return GlobalResult.forbidden_Permission();
            if( invoice.status.equals(Enum_Payment_status.paid)) return GlobalResult.result_BadRequest("Invoice is already paid");

            // vyvolání nové platby ale bez vytváření faktury nebo promofaktury
            invoice = Utilities_GoPay_Controller.singlePayment("First Payment", invoice.product, invoice);

            // Vrácení ID s možností uhrazení
            return GlobalResult.result_ok(Json.toJson(invoice));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Invoice PDF file",
            tags = {"Price & Invoice & Tariffs"},
            notes = "get PDF invoice file",
            produces = "multipartFormData",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public Result invoice_getPdf(String kind, String invoice_id){
        try {

            if (!kind.equals("proforma") && !kind.equals("invoice")) return GlobalResult.result_BadRequest("kind should be 'proforma' or 'invoice'");

            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);
            if(invoice == null) return GlobalResult.notFoundObject("Invoice not found");

            if (kind.equals("proforma") && invoice.proforma_pdf_url == null) return GlobalResult.result_BadRequest("Proforma PDF is unavailable");

            if(!invoice.read_permission()) return GlobalResult.forbidden_Permission();

            byte[] pdf_in_array = Utilities_Fakturoid_Controller.download_PDF_invoice(kind, invoice);

            return GlobalResult.result_pdf_file(pdf_in_array, kind.equals("proforma") ? "proforma_" + invoice.invoice_number + ".pdf" : invoice.invoice_number + ".pdf");

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result invoice_reminder(String invoice_id){
        try{

            // Kontrola objektu
            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);
            if(invoice == null) return GlobalResult.notFoundObject("Invoice not found");

            if(!invoice.remind_permission()) return GlobalResult.forbidden_Permission();
            Utilities_Fakturoid_Controller.sendInvoiceReminderEmail(invoice,"You have pending unpaid invoice.");

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result invoice_delete(String invoice_id){
        try{

            // Kontrola objektu
            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);
            if(invoice == null) return GlobalResult.notFoundObject("Invoice invoice_id not found");

            // Kontrola oprávnění
            if(!invoice.delete_permission()) return GlobalResult.forbidden_Permission();

            // TODO - Chybí navázání na fakturoid - smazání faktury (nějaký proces?)

            // Vykonání operace
            invoice.delete();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result invoice_connectProduct(String product_id, String fakturoid_reference_number){
        return TODO;
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result synchonize_invoice_with_fakutoid(String invoice_id){
        return TODO;
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result invoice_set_as_paid(String invoice_id){
        try{

            //TODO
            List<Model_Invoice> invoices = Model_Invoice.find.all();
            return GlobalResult.result_ok(Json.toJson(invoices) );

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }
}
