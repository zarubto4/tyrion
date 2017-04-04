package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.*;
import models.*;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.enums.Enum_Currency;
import utilities.enums.Enum_Payment_method;
import utilities.enums.Enum_Payment_mode;
import utilities.enums.Enum_Payment_status;
import utilities.fakturoid.Utilities_Fakturoid_Controller;
import utilities.goPay.Utilities_GoPay_Controller;
import utilities.loggy.Loggy;
import utilities.login_entities.Secured_API;
import utilities.login_entities.Secured_Admin;
import utilities.response.CoreResponse;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Swagger_Product_Active;
import utilities.swagger.outboundClass.Swagger_GoPay_Url;
import utilities.swagger.outboundClass.Swagger_Invoice_FullDetails;
import utilities.swagger.outboundClass.Swagger_ProductExtension_Type;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured_API.class)
public class Controller_Finance extends Controller {

    // Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    // ADMIN - TARIFF SETTINGS #########################################################################################

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
            tariff.credit_for_beginning     = help.credit_for_beginning;

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
            return Loggy.result_internalServerError(e, request());
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
            return Loggy.result_internalServerError(e, request());
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
            return Loggy.result_internalServerError(e, request());
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
            return Loggy.result_internalServerError(e, request());
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
            return Loggy.result_internalServerError(e, request());
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
            return Loggy.result_internalServerError(e, request());
        }
    }

    // ADMIN - TARIFF LABEL ############################################################################################

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
            return Loggy.result_internalServerError(e, request());
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
            return Loggy.result_internalServerError(e, request());
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
            return Loggy.result_internalServerError(e, request());
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
            return Loggy.result_internalServerError(e, request());
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
            return Loggy.result_internalServerError(e, request());
        }
    }

    // USER -  EXTENSION PACKAGES ######################################################################################

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
            return Loggy.result_internalServerError(e, request());
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
            return Loggy.result_internalServerError(e, request());
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

            return GlobalResult.result_ok(Json.toJson(Model_ProductExtension.get_byUser(Controller_Security.getPerson().id)));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
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
            return Loggy.result_internalServerError(e, request());
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
            return Loggy.result_internalServerError(e, request());
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
            return Loggy.result_internalServerError(e, request());
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
            return Loggy.result_internalServerError(e, request());
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
            return Loggy.result_internalServerError(e, request());
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
            config.price = help.price;
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
            return Loggy.result_internalServerError(e, request());
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
            config.price = help.price;
            config.count = help.count;

            extension.config = Json.toJson(config).toString();

            if (!extension.edit_permission()) return GlobalResult.forbidden_Permission();

            extension.update();

            return GlobalResult.result_ok(Json.toJson(extension));

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
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
            return Loggy.result_internalServerError(e, request());
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
            return Loggy.result_internalServerError(e, request());
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
            return Loggy.result_internalServerError(e, request());
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
            return Loggy.result_internalServerError(e, request());
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
            return Loggy.result_internalServerError(e, request());
        }
    }

    // USER PRODUCT_TARIFF #############################################################################################

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
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "create Product under Tariff",
            tags = {"Price & Invoice & Tariffs"},
            notes = "It is the base object. Peak of Pyramid :). This Api is used for its creation. You can get two kind of response: \n\n" +
                    "First(201):  System create new Object - Object Product \n\n" +
                    "Second(200): The product requires payment - The server creates an object, but returns the payment details - payment go_url for GoPay Terminal!",
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
            @ApiResponse(code = 200, message = "Created successfully - but payment is required by Credit Card",     response = Swagger_GoPay_Url.class),
            @ApiResponse(code = 201, message = "Created successfully - payment not required",                       response = Model_Product.class),
            @ApiResponse(code = 202, message = "Created successfully - but payment is required by Bank Transfer",   response = Model_Invoice.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Not found object",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result product_create(){
        try{

            logger.debug("Financial_Controller:: product_create:: Creating new product:: ");

            // Zpracování Json
            final Form<Swagger_Product_New> form = Form.form(Swagger_Product_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Product_New help = form.get();


            Model_Tariff tariff = Model_Tariff.find.byId(help.tariff_id);
            if(tariff == null) return GlobalResult.result_BadRequest("Tariff identifier iD: {" + help.tariff_id  + "} not found or not supported now! Use only supported");

            logger.debug("Financial_Controller:: product_create:: On Tariff:: " +  tariff.name);

            if(Model_Product.get_byNameAndOwner(help.name) != null) return GlobalResult.result_BadRequest("You cannot use same Product name twice!");

            Model_Product product = new Model_Product();

            product.tariff =  tariff;
            product.name = help.name;
            product.active = true;  // Produkt jelikož je Aplha je aktivní - Alpha nebo Trial dojedou kvuli omezení času

            product.mode = Enum_Payment_mode.free;
            product.method = Enum_Payment_method.free;

            Model_Person person = Controller_Security.getPerson();

            Model_PaymentDetails payment_details = new Model_PaymentDetails();
            payment_details.person = person;
            payment_details.company_account = false;

            if (help.full_name != null) payment_details.full_name = help.full_name;
            else payment_details.full_name = person.full_name;

            payment_details.street = help.street;
            payment_details.street_number = help.street_number;
            payment_details.city = help.city;
            payment_details.zip_code = help.zip_code;
            payment_details.country = help.country;
            payment_details.invoice_email = help.invoice_email;


            if(tariff.company_details_required){

                if((help.registration_no == null)&&(help.vat_number == null)) return GlobalResult.result_BadRequest("company_registration_no or vat_number is required with this tariff");
                if(help.company_name == null)               return GlobalResult.result_BadRequest("company_name is required with this tariff");

                if(help.company_authorized_email == null)   return GlobalResult.result_BadRequest("company_authorized_email is required with this tariff");
                if(help.company_authorized_phone == null)   return GlobalResult.result_BadRequest("company_authorized_phone is required with this tariff");
                if(help.company_web == null)                return GlobalResult.result_BadRequest("company_web is required with this tariff");

                try {
                    new URL(help.company_web);
                } catch (MalformedURLException malformedURLException) {
                    return GlobalResult.result_BadRequest("company_web invalid value");
                }

                if(help.vat_number != null) {
                    payment_details.company_vat_number = help.vat_number;
                }

                if(help.registration_no != null) {
                    payment_details.company_registration_no  = help.registration_no;
                }

                payment_details.company_account = true;
                payment_details.company_name             = help.company_name;
                payment_details.company_authorized_email = help.company_authorized_email;
                payment_details.company_authorized_phone = help.company_authorized_phone;
                payment_details.company_web              = help.company_web;

            }

            logger.debug("Financial_Controller:: product_create:: Payment details done");

            // payment_mode
            if(tariff.payment_mode_required) {

                logger.debug("Financial_Controller:: product_create:: Payment mode Required");

                if(help.payment_mode == null) return GlobalResult.result_BadRequest("Payment_mode is required!");

                if(help.payment_mode.equals( Enum_Payment_mode.free.name()))              product.mode = Enum_Payment_mode.free;
                else if(help.payment_mode.equals( Enum_Payment_mode.annual.name()))       product.mode = Enum_Payment_mode.annual;
                else if(help.payment_mode.equals( Enum_Payment_mode.monthly.name()))      product.mode = Enum_Payment_mode.monthly;
                else if(help.payment_mode.equals( Enum_Payment_mode.per_credit.name()))   product.mode = Enum_Payment_mode.per_credit;
                else { return GlobalResult.result_BadRequest("payment_mode is invalid. Use only (free, monthly, annual, per_credit)");}
            }

            if(tariff.payment_method_required) {

                logger.debug("Financial_Controller:: product_create:: Payment method Required");

                if(help.payment_method == null) return GlobalResult.result_BadRequest("payment_method is required with this tariff");

                     if(help.payment_method.equals( Enum_Payment_method.bank_transfer.name()))  product.method = Enum_Payment_method.bank_transfer;
                else if(help.payment_method.equals( Enum_Payment_method.credit_card.name()))    product.method = Enum_Payment_method.credit_card;
                else if(help.payment_method.equals( Enum_Payment_method.free.name()))           product.method = Enum_Payment_method.free;
                else { return GlobalResult.result_BadRequest("payment_method is invalid. Use only (bank_transfer, credit_card, free)");}
            }

            payment_details.save();
            product.save();

            payment_details.product = product;
            payment_details.update();

            product.payment_details = payment_details;
            product.remaining_credit += tariff.credit_for_beginning;
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
                logger.debug("Financial_Controller:: product_create:: Payment is not required!");
                return GlobalResult.created(Json.toJson(product));
            }

            logger.debug("Financial_Controller:: product_create:: Creating invoice");

            Model_Invoice invoice = new Model_Invoice();
            invoice.created = new Date();
            invoice.proforma = true;

            if(product.method == Enum_Payment_method.credit_card) {
                invoice.status = Enum_Payment_status.sent;
            }
            if(product.method == Enum_Payment_method.bank_transfer) {
                invoice.status = Enum_Payment_status.created_waited;
            }

            Model_InvoiceItem invoice_item = new Model_InvoiceItem();
            invoice_item.name = product.tariff.name + " in Mode(" + product.mode.name() + ")";
            invoice_item.unit_price = 0.0; // TODO co vrátit, když ještě nevím, kolik bude produkt stát
            invoice_item.quantity = (long) 1;
            invoice_item.unit_name = "Currency";
            invoice_item.currency = Enum_Currency.USD;

            invoice.invoice_items.add(invoice_item);
            invoice.method = product.method;

            invoice.product = product;

            logger.debug("Financial_Controller:: product_create:: Saving invoice");
            invoice.save();

            Model_Invoice test = Model_Invoice.find.byId(invoice.id);
            logger.debug("Financial_Controller:: product_create:: " + Json.toJson(test));


            logger.debug("Financial_Controller:: product_create::  Creating Proforma in fakturoid");
            Utilities_Fakturoid_Controller.create_proforma(product, invoice);
            logger.debug("Financial_Controller:: product_create::  Proforma done");


            if(product.method == Enum_Payment_method.credit_card){

                logger.debug("Financial_Controller:: product_create::  User want pay it with credit card!");

                logger.debug("Financial_Controller:: product_create::  Preparing for providing payment");
                JsonNode result = Utilities_GoPay_Controller.provide_payment("First Payment", product, invoice);


                if(result.has("id")){

                    logger.debug("Set GoPay ID to Invoice");

                    invoice.refresh();
                    invoice.gopay_id = result.get("id").asLong();
                    invoice.gopay_order_number = result.get("order_number").asText();
                    invoice.update();

                    if(product.on_demand) {

                        logger.debug("Set GoPay ID to Product because Product has ON_DEMAND - TRUE");

                        product.gopay_id = invoice.gopay_id;
                        product.update();
                    }

                }else {
                    logger.error("Result from GoPay not contains id for invoice!");
                }

                Swagger_GoPay_Url swagger_goPay_url = new Swagger_GoPay_Url();
                swagger_goPay_url.gw_url = result.get("gw_url").asText();

                return GlobalResult.result_ok(Json.toJson(swagger_goPay_url));

            }else {

                CoreResponse.cors();
                return Controller.status(202, Json.toJson(invoice));
            }

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
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
            List<Model_Product> products = Model_Product.get_byOwner(Controller_Security.getPerson().id);

            // Vrácení seznamu
            return GlobalResult.result_ok(Json.toJson(products));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Tariff general details",
            tags = {"Price & Invoice & Tariffs"},
            notes = "edit basic details on user Tariff",
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
            return Loggy.result_internalServerError(e, request());
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
            return Loggy.result_internalServerError(e, request());
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
            return Loggy.result_internalServerError(e, request());
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
            return Loggy.result_internalServerError(e, request());
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
            @ApiResponse(code = 200, message = "Ok Result- payment is required",  response =  Swagger_GoPay_Url.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error", response = Result_InternalServerError.class)
    })  /**  Uživatel může zaplatit neúspěšně zaplacenou fakturu (službu) */
    public Result pay_send_invoice(String invoice_id) {
        try {

            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);
            if(invoice == null) return GlobalResult.notFoundObject("Invoice invoice_id not found");

            if(!invoice.read_permission()) return GlobalResult.forbidden_Permission();
            if(!invoice.status.equals(Enum_Payment_status.sent)) return GlobalResult.result_BadRequest("Invoice is already paid");


            // vyvolání nové platby ale bez vytváření faktury nebo promofaktury


            JsonNode result = Utilities_GoPay_Controller.provide_payment("First Payment", invoice.product, invoice);

            if(result.has("id")){

                logger.debug("Set GoPay ID to Invoice");

                invoice.refresh();
                invoice.gopay_id = result.get("id").asLong();
                invoice.gopay_order_number = result.get("order_number").asText();
                invoice.update();

                if(invoice.product.on_demand) {

                    logger.debug("Set GoPay ID to Product because Product has ON_DEMAND - TRUE");

                    invoice.product.gopay_id = invoice.gopay_id;
                    invoice.product.update();
                }

            }else {
                logger.error("Result from GoPay not contains id for invoice!");
            }


            // Vytváření pomocného Objektu
            Swagger_GoPay_Url swager_goPay_url = new Swagger_GoPay_Url();
            swager_goPay_url.gw_url = result.get("gw_url").asText();


            // Vrácení ID s možností uhrazení
            return GlobalResult.result_ok(Json.toJson(swager_goPay_url));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Tariff payment details",
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
                        return GlobalResult.badRequest("Prefix code in VatNumber is not valid");
                    payment_details.company_vat_number = help.vat_number;
                }

                payment_details.company_registration_no = help.registration_no;
                payment_details.company_name = help.company_name;
                payment_details.company_authorized_email = help.company_authorized_email;
                payment_details.company_authorized_phone = help.company_authorized_phone;
                payment_details.company_web = help.company_web;
            }

            // Updatování do databáze
            payment_details.update();

            // Vrácení objektu
            return  GlobalResult.result_ok(Json.toJson(payment_details));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
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
            @ApiResponse(code = 200, message = "Ok Result",                 response =  Swagger_Product_Active.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result product_getActive(){
        try{

            List<Swagger_Product_Active> products = new ArrayList<>();

            for(Model_Product product : Model_Product.get_applicableByOwner(Controller_Security.getPerson().id)){
                Swagger_Product_Active help = new Swagger_Product_Active();
                help.id = product.id;
                help.name = product.name;
                help.tariff = product.tariff.name;

                products.add(help);
            }

            // Vrácení objektu
            return GlobalResult.result_ok( Json.toJson(products));

        }catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "cancel automatic payments in Tariff",
            tags = {"Price & Invoice & Tariffs"},
            notes = "edit payments details in Tariff",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated",    response =  Result_ok.class),
            @ApiResponse(code = 400, message = "Something is wrong - details in message ",  response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error", response = Result_InternalServerError.class)
    })
    public Result temrinate_On_Demance_Payment(String product_id){
        try{

            // Kontrola objektu
            Model_Product product = Model_Product.get_byId(product_id);
            if(product == null) return GlobalResult.notFoundObject("Product product_id not found");

            // Oprávnění operace
            if(!product.edit_permission()) return GlobalResult.forbidden_Permission();

            // Zrušení automatického strhávání z kreditní karty
            product.on_demand = false;
            product.update();


            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }


// INVOICE #############################################################################################

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
            return Loggy.result_internalServerError(e, request());
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
    public Result resend_invoice(String invoice_id){
        try{

            // Vytvoření pomocného Objektu
            final Form<Swagger_Resend_Email> form = Form.form(Swagger_Resend_Email.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Resend_Email help = form.get();


            // Kontrola objektu
            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);
            if(invoice == null) return GlobalResult.notFoundObject("Invoice invoice_id not found");
            if(!invoice.read_permission()) return GlobalResult.forbidden_Permission();


            // Email na který se faktura zašle
            String email = null;

            if(help.mail == null || help.mail.length() < 1) email = invoice.product.payment_details.invoice_email;
            else email = help.mail;

            System.out.println("Email na který přeposílám fakturu je:: " + email);

            Utilities_Fakturoid_Controller.send_invoice_to_Email(invoice, email);

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result send_remainder_to_custumer(String invoice_id){
        try{

            // Kontrola objektu
            Model_Invoice invoice = Model_Invoice.find.byId(invoice_id);
            if(invoice == null) return GlobalResult.notFoundObject("Invoice invoice_id not found");

            if(!invoice.send_reminder()) return GlobalResult.forbidden_Permission();
            Utilities_Fakturoid_Controller.send_UnPaidInvoice_to_Email(invoice);

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result invoice_remove(String invoice_id){
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
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion frontend", hidden = true)
    public Result connect_invoice_manualy_to_product(Long product_id, String fakturoid_reference_number){
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
            return Loggy.result_internalServerError(e, request());
        }
    }
}
