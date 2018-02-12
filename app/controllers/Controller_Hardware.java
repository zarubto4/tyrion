package controllers;

import com.google.inject.Inject;
import io.ebean.Ebean;
import io.ebean.Query;
import io.swagger.annotations.*;
import models.*;
import org.mindrot.jbcrypt.BCrypt;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.*;
import responses.*;
import utilities.authentication.Authentication;
import utilities.document_db.document_objects.DM_Board_Bootloader_DefaultConfig;
import utilities.hardware_registration_auhtority.Hardware_Registration_Authority;
import utilities.lablel_printer_service.Printer_Api;
import utilities.lablel_printer_service.labels.Label_62_mm_package;
import utilities.enums.*;
import utilities.lablel_printer_service.labels.Label_62_split_mm_Details;
import utilities.logger.Logger;
import utilities.swagger.input.*;
import utilities.swagger.output.*;
import utilities.swagger.output.filter_results.Swagger_Hardware_List;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_change_server;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_set_settings;
import websocket.messages.homer_hardware_with_tyrion.helps_objects.WS_Help_Hardware_Pair;

import java.nio.charset.IllegalCharsetNameException;
import java.util.*;


@Api(value = "Not Documented API - InProgress or Stuck")  // Záměrně takto zapsané - Aby ve swaggru nezdokumentované API byly v jedné sekci
@Security.Authenticated(Authentication.class)
public class Controller_Hardware extends BaseController {

// LOGGER ##############################################################################################################
    
    private static final Logger logger = new Logger(Controller_Hardware.class);
    
///###################################################################################################################*/


    private FormFactory formFactory;

    @Inject
    public Controller_Hardware(FormFactory formFactory) {
        this.formFactory = formFactory;
    }


///###################################################################################################################*/

    @ApiOperation(value = "create Processor",
            tags = {"Admin-Processor"},
            notes = "If you want create new Processor. Send required json values and server respond with new object",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Processor_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_Processor.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result processor_create() {
        try {

            // Zpracování Json
            final Form<Swagger_Processor_New> form = formFactory.form(Swagger_Processor_New.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_Processor_New help = form.get();

            // Vytvářím objekt
            Model_Processor processor = new Model_Processor();
            processor.name           = help.name;
            processor.description    = help.description;
            processor.processor_code = help.processor_code;
            processor.speed          = help.speed;

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if (!processor.create_permission()) return forbidden();

            // Ukládám objekt
            processor.save();

            // Vracím objekt
            return created(Json.toJson(processor));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get Processor",
            tags = {"Processor"},
            notes = "If you get Processor by query processor_id.",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",         response = Model_Processor.class),
            @ApiResponse(code = 404, message = "Object not found",  response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error", response = Result_InternalServerError.class)
    })
    public Result processor_get(@ApiParam(value = "processor_id String query", required = true) String processor_id) {
        try {

            //Zkontroluji validitu
            Model_Processor processor = Model_Processor.getById(processor_id);
            if (processor == null) return notFound("Processor processor_id not found");

            // Vracím objekt
            return ok(Json.toJson(processor));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get Processor All",
            tags = {"Processor"},
            notes = "Get list of all Processor by query",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",         response = Model_Processor.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Server side Error", response = Result_InternalServerError.class)
    })
    public Result processor_getAll() {
        try {

            //Vyhledám objekty
           List<Model_Processor> processors = Model_Processor.find.query().where().eq("deleted", false).order().asc("name").findList();

            // Vracím seznam objektů
           return ok(Json.toJson(processors));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit Processor",
            tags = {"Processor"},
            notes = "If you want update Processor.id by query = processor_id . Send required json values and server respond with update object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "Processor_edit" ),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Processor_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Processor.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error", response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result processor_edit(@ApiParam(value = "processor_id String query", required = true) String processor_id) {
        try {

            // Zpracování Json
            Form<Swagger_Processor_New> form = formFactory.form(Swagger_Processor_New.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_Processor_New help = form.get();

            // Kontroluji validitu
            Model_Processor processor = Model_Processor.getById(processor_id);
            if (processor == null) return notFound("Processor processor_id not found");

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if (!processor.edit_permission())  return forbidden();

            // Upravuji objekt
            processor.description    = help.description;
            processor.processor_code = help.processor_code;
            processor.name           = help.name;
            processor.speed          = help.speed;

            // Ukládám do databáze
            processor.update();

            // Vracím upravený objekt
            return ok(Json.toJson(processor));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete Processor",
            tags = {"Admin-Processor"},
            notes = "If you want delete Processor by query processor_id.",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "Processor_delete" ),
                    })
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result processor_delete(@ApiParam(value = "processor_id String query", required = true) String processor_id) {
        try {

            // Kontroluji validitu
            Model_Processor processor = Model_Processor.getById(processor_id);
            if (processor == null) return notFound("Processor not found");

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if (!processor.delete_permission()) return forbidden();

            if (processor.hardware_types.size() > 0) return badRequest("Processor is assigned to some type of board, so cannot be deleted");

            // Mažu z databáze
            processor.delete();

            return ok();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

///###################################################################################################################*/

    @ApiOperation(value = "get Bootloader FileRecord",
            tags = {"File", "Garfield"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_File_Content.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result fileRecord_get_bootLoader(@ApiParam(value = "file_record_id String query", required = true)  String bootloader_id) {
        try {

            Model_BootLoader boot_loader = Model_BootLoader.getById(bootloader_id);
            if (boot_loader == null) return notFound("BootLoader not found");

            if (!boot_loader.read_permission()) return forbidden();

            // Swagger_File_Content - Zástupný dokumentační objekt
            Swagger_File_Content content = new Swagger_File_Content();
            content.file_in_base64 = boot_loader.file.get_fileRecord_from_Azure_inString();

            // Vracím content
            return ok(Json.toJson(content));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get CProgram_Version FileRecord",
            tags = { "File" , "Garfield"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_File_Content.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result fileRecord_get_firmware(@ApiParam(value = "file_record_id String query", required = true)  String version_id) {
        try {

            // Kontrola validity objektu
            Model_Version version = Model_Version.getById(version_id);
            if (version == null) return notFound("Version not found");

            // Swagger_File_Content - Zástupný dokumentační objekt
            if (version.get_c_program() == null) return badRequest();

            // Kontrola oprávnění
            if (!version.get_c_program().read_permission()) return badRequest();

            // Swagger_File_Content - Zástupný dokumentační objekt
            Swagger_File_Content content = new Swagger_File_Content();
            content.file_in_base64 = version.compilation.blob.get_fileRecord_from_Azure_inString();

            // Vracím content
            return ok(Json.toJson(content));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

///###################################################################################################################*/

    @ApiOperation(value = "create Producer",
            tags = {"Admin-Producer"},
            notes = "if you want create new Producer. Its company owned physical hardware and we used that for filtering",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "Producer_create" ),
                    })
            }
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
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_Producer.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result producer_create() {
        try {

            // Zpracování Json
            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_NameAndDescription help = form.get();

            //Vytvářím objekt
            Model_Producer producer = new Model_Producer();
            producer.name = help.name;
            producer.description = help.description;

            // Kontorluji oprávnění těsně před uložením
            if (!producer.create_permission()) return forbidden();

            //Ukládám objekt
            producer.save();

            // Vracím objekt
            return created(Json.toJson(producer));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit Producer",
            tags = {"Admin-Producer"},
            notes = "if you want edit information about Producer. Its company owned physical hardware and we used that for filtering",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Producer.edit_permission", value =  "true" ),
                            @ExtensionProperty(name = "Static Permission key", value =  "Producer_edit" ),
                    })
            }
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Producer.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result producer_update(String producer_id) {
        try {

            // Zpracování Json
            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_NameAndDescription help = form.get();

            // Kontrola objektu
            Model_Producer producer = Model_Producer.getById(producer_id);
            if (producer == null) return notFound("Producer producer_id not found");

            // Kontorluji oprávnění těsně před uložením
            if (!producer.edit_permission()) return forbidden();

            // Úprava objektu
            producer.name = help.name;
            producer.description = help.description;

            // Uložení změn objektu
            producer.update();

            // Vrácení objektu
            return ok(Json.toJson(producer));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get Producers All",
            tags = {"Producer"},
            notes = "if you want get list of Producers. Its list of companies owned physical hardware and we used that for filtering",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Producer.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",         response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result producer_getAll() {
        try {

            // Získání seznamu
            List<Model_Producer> producers = Model_Producer.find.query().where().eq("deleted", false).order().asc("name").findList();

            // Vrácení seznamu
            return ok(Json.toJson(producers));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get Producer",
            tags = {"Producer"},
            notes = "if you want get Producer. Its company owned physical hardware and we used that for filtering",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Producer.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result producer_get( String producer_id) {
        try {

            // Kontrola objektu
            Model_Producer producer = Model_Producer.getById(producer_id);
            if (producer == null) return notFound("Producer producer_id not found");

            // Vrácneí objektu
            return ok(Json.toJson(producer));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete Producer",
            tags = {"Admin-Producer"},
            notes = "if you want delete Producer",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Producer.delete_permission", value =  "true" ),
                            @ExtensionProperty(name = "Static Permission key", value =  "Producer_delete" ),
                    })
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result producer_delete(String producer_id) {
        try {

            // Kontrola objektu
            Model_Producer producer = Model_Producer.getById(producer_id);
            if (producer == null) return notFound("Producer not found");

            // Kontorluji oprávnění
            if (!producer.delete_permission()) return forbidden();

            if (producer.hardware_types.size() > 0 || producer.blocks.size() > 0 || producer.widgets.size() > 0)
                return badRequest("Producer is assigned to some objects, so cannot be deleted.");

            // Smazání objektu
            producer.delete();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

///###################################################################################################################*/

    @ApiOperation(value = "create HardwareType",
            tags = { "HardwareType"},
            notes = "The HardwareType is category for IoT. Like Raspberry2, Arduino-Uno etc. \n\n" +
                    "We using that for compilation, sorting libraries, filters and more..",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_HardwareType_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_HardwareType.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardwareType_create() {
        try {

            // Zpracování Json
            final Form<Swagger_HardwareType_New> form = formFactory.form(Swagger_HardwareType_New.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_HardwareType_New help = form.get();

            // Kontrola objektu
            Model_Producer producer = Model_Producer.getById(help.producer_id);
            if (producer == null) return notFound("Producer not found");

            // Kontrola objektu
            Model_Processor processor = Model_Processor.getById(help.processor_id);
            if (processor == null) return notFound("Processor not found");

            // Tvorba objektu
            Model_HardwareType hardwareType = new Model_HardwareType();
            hardwareType.name = help.name;
            hardwareType.description = help.description;
            hardwareType.compiler_target_name = help.compiler_target_name;
            hardwareType.processor = processor;
            hardwareType.producer = producer;
            hardwareType.connectible_to_internet = help.connectible_to_internet;

            // Kontorluji oprávnění
            if (!hardwareType.create_permission()) return forbidden();

            // Uložení objektu do DB
            hardwareType.save();

            // Vytvoříme defaultní C_Program pro snížení počtu kroků pro nastavení desky
            Model_CProgram c_program = new Model_CProgram();
            c_program.name =  hardwareType.name + " default program";
            c_program.description = "Default program for this device type";
            c_program.hardware_type_default = hardwareType;
            c_program.hardware_type =  hardwareType;
            c_program.publish_type  = ProgramType.DEFAULT_MAIN;
            c_program.save();

            hardwareType.refresh();

            // Vytvoříme testovací C_Program pro snížení počtu kroků pro nastavení desky
            Model_CProgram c_program_test = new Model_CProgram();
            c_program_test.name =  hardwareType.name + " test program";
            c_program_test.description = "Test program for this device type";
            c_program_test.hardware_type_test = hardwareType;
            c_program_test.hardware_type =  hardwareType;
            c_program_test.publish_type  = ProgramType.DEFAULT_TEST;
            c_program_test.save();

            hardwareType.refresh();

            // TODO přidat do cache

            return created(hardwareType.json());

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit HardwareType",
            tags = { "HardwareType"},
            notes = "if you want edit base HardwareType information",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_HardwareType_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_HardwareType.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardwareType_update(String hardware_type_id) {
        try {

            // Zpracování Json
            final Form<Swagger_HardwareType_New> form = formFactory.form(Swagger_HardwareType_New.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_HardwareType_New help = form.get();

            // Kontrola objektu
            Model_HardwareType hardwareType = Model_HardwareType.getById(hardware_type_id);
            if (hardwareType == null) return notFound("HardwareType not found");

            // Kontrola objektu
            Model_Producer producer = Model_Producer.getById(help.producer_id);
            if (producer == null) return notFound("Producer producer_id not found");

            // Kontrola objektu
            Model_Processor processor = Model_Processor.getById(help.processor_id);
            if (processor == null) return notFound("Processor processor_id not found");

            // Kontorluji oprávnění
            if (!hardwareType.edit_permission()) return forbidden();

            // Uprava objektu
            hardwareType.name = help.name;
            hardwareType.description = help.description;
            hardwareType.compiler_target_name = help.compiler_target_name;
            hardwareType.processor = processor;
            hardwareType.producer = producer;
            hardwareType.connectible_to_internet = help.connectible_to_internet;

            // Uložení do DB
            hardwareType.update();

            // Vrácení změny
            return ok(hardwareType.json());

        } catch (Exception e) {
            return internalServerError(e);
        }

    }

    @ApiOperation(value = "delete HardwareType",
            tags = { "HardwareType"},
            notes = "if you want delete HardwareType object by query = hardware_type_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result hardwareType_delete( String hardware_type_id) {
        try {

            // Kontrola objektu
            Model_HardwareType hardwareType = Model_HardwareType.getById(hardware_type_id);
            if (hardwareType == null) return notFound("HardwareType not found") ;

            // Kontorluji oprávnění
            if (!hardwareType.delete_permission()) return forbidden();

            // Smazání objektu
            hardwareType.delete();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get HardwareTypes All",
            tags = { "HardwareType"},
            notes = "if you want get all HardwareType objects",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_HardwareType.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result hardwareType_getAll() {
        try {

            // TODO dá se cachovat - Pozor stejný seznam se nachází i Job_CheckCompilationLibraries
            // Získání seznamu
            // To co jsem tady napsal jen filtruje tahá ručně desky z cache pojendom - možná by šlo někde mít statické pole ID třeba
            // přímo v objektu Model_HardwareType DB ignor a to používat a aktualizovat a statické pole nechat na samotné jave, aby si ji uchavaala v pam,ěti
            List<Model_HardwareType> hardwareTypes_not_cached = Model_HardwareType.find.query().where().orderBy("UPPER(name) ASC").select("id").findList();

            List<Model_HardwareType> hardwareTypes = new ArrayList<>();

            for (Model_HardwareType hardwareType : hardwareTypes_not_cached) {
                hardwareTypes.add(Model_HardwareType.getById(hardwareType.id));
            }

            // Vrácení seznamu
            return ok(Json.toJson(hardwareTypes));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get HardwareType",
            tags = { "HardwareType"},
            notes = "if you want get HardwareType object by query = hardware_type_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_HardwareType.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result hardwareType_get(String hardware_type_id) {
        try {

            // Kontrola validity objektu
            Model_HardwareType hardwareType = Model_HardwareType.getById(hardware_type_id);
            if (hardwareType == null) return notFound("HardwareType not found");

            // Kontorluji oprávnění
            if (!hardwareType.read_permission()) return forbidden();

            // Vrácení validity objektu
            return ok(hardwareType.json());

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "upload HardwareType picture",
            tags = {"HardwareType"},
            notes = "Upload HardwareType picture",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_BASE64_FILE",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(value = BodyParser.Json.class)//, TODO maxLength = 1024 * 1024 * 10)
    public Result hardwareType_uploadPicture(String hardware_type_id) {
        try {

            // Získání JSON
            final Form<Swagger_BASE64_FILE> form = formFactory.form(Swagger_BASE64_FILE.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_BASE64_FILE help = form.get();

            Model_HardwareType hardwareType = Model_HardwareType.getById(hardware_type_id);
            if (hardwareType == null) return notFound("HardwareType not found");


            if (!hardwareType.edit_permission()) return forbidden();

            logger.debug("hardwareType_uploadPicture - update picture");

            hardwareType.cache_picture_link = null;

            // Odebrání předchozího obrázku
            if (hardwareType.picture != null) {

                logger.debug("hardwareType_uploadPicture - picture is already there - system remove previous photo");
                Model_Blob blob = hardwareType.picture;
                hardwareType.picture = null;
                hardwareType.update();
                blob.delete();
            }

            //  data:image/png;base64,
            String[] parts = help.file.split(",");
            String[] type = parts[0].split(":");
            String[] dataType = type[1].split(";");

            logger.debug("hardwareType_uploadPicture - Type     :: " + dataType[0]);
            logger.debug("hardwareType_uploadPicture - Data     :: " + parts[1].substring(0, 10) + "......");

            String file_name =  UUID.randomUUID().toString() + ".png";
            String file_path =  hardwareType.get_Container().getName() + "/" + file_name;

            logger.debug("hardwareType_uploadPicture - File Name:: " + file_name );
            logger.debug("hardwareType_uploadPicture - File Path:: " + file_path );

            hardwareType.picture  = Model_Blob.uploadAzure_File( parts[1], dataType[0], file_name , file_path);
            hardwareType.update();


            return ok("Picture successfully uploaded");
        } catch (Exception e) {
            return internalServerError(e);
        }
    }

// Type Of Board - Batch ###############################################################################################

    @ApiOperation(value = "create HardwareBatch",
            tags = { "HardwareType"},
            notes = "Create new Production Batch for Hardware Type",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_HardwareBatch_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_HardwareBatch.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardwareBatch_create(String hardware_type_id) {
        try {

            // Zpracování Json
            final Form<Swagger_HardwareBatch_New> form = formFactory.form(Swagger_HardwareBatch_New.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_HardwareBatch_New help = form.get();

            // Kontrola objektu
            Model_HardwareType hardwareType = Model_HardwareType.getById(hardware_type_id);
            if (hardwareType == null) return notFound("HardwareType not found");

            // Tvorba objektu
            Model_HardwareBatch batch = new Model_HardwareBatch();
            batch.hardware_type = hardwareType;

            batch.revision = help.revision;
            batch.production_batch = help.production_batch;

            batch.assembled = help.date_of_assembly;

            batch.pcb_manufacture_name = help.pcb_manufacture_name;
            batch.pcb_manufacture_id = help.pcb_manufacture_id;

            batch.assembly_manufacture_name = help.assembly_manufacture_name;
            batch.assembly_manufacture_id = help.assembly_manufacture_id;

            batch.customer_product_name = help.customer_product_name;

            batch.customer_company_name = help.customer_company_name;
            batch.customer_company_made_description = help.customer_company_made_description;

            batch.mac_address_start = help.mac_address_start;
            batch.mac_address_end = help.mac_address_end;

            batch.ean_number = help.ean_number;

            batch.description = help.description;

            // Kontorluji oprávnění
            if (!batch.create_permission()) return forbidden();

            // Uložení objektu do DB
            batch.save();

            return created(Json.toJson(batch));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete HardwareBatch",
            tags = { "HardwareType"},
            notes = "if you want delete Hardware Batch object by query = hardware_type_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result hardwareBatch_delete( String batch_id) {
        try {

            // Kontrola objektu
            Model_HardwareBatch batch = Model_HardwareBatch.getById(batch_id);
            if (batch == null) return notFound("HardwareBatch not found") ;

            // Kontorluji oprávnění
            if (!batch.delete_permission()) return forbidden();

            // Smazání objektu
            batch.delete();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit HardwareBatch",
            tags = { "HardwareType"},
            notes = "Create new Production Batch for Type Of Board",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_HardwareBatch_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_HardwareBatch.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardwareBatch_edit(String batch_id) {
        try {

            // Zpracování Json
            final Form<Swagger_HardwareBatch_New> form = formFactory.form(Swagger_HardwareBatch_New.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_HardwareBatch_New help = form.get();

            // Kontrola objektu
            Model_HardwareBatch batch = Model_HardwareBatch.getById(batch_id);
            if (batch == null) return notFound("HardwareBatch not found");

            // Tvorba objektu
            batch.revision = help.revision;
            batch.production_batch = help.production_batch;

            batch.assembled = help.date_of_assembly;

            batch.pcb_manufacture_name = help.pcb_manufacture_name;
            batch.pcb_manufacture_id = help.pcb_manufacture_id;

            batch.assembly_manufacture_name = help.assembly_manufacture_name;
            batch.assembly_manufacture_id = help.assembly_manufacture_id;

            batch.customer_product_name = help.customer_product_name;

            batch.customer_company_name = help.customer_company_name;
            batch.customer_company_made_description = help.customer_company_made_description;

            batch.mac_address_start = help.mac_address_start;
            batch.mac_address_end = help.mac_address_end;

            batch.ean_number = help.ean_number;

            batch.description = help.description;

            // Kontorluji oprávnění
            if (!batch.create_permission()) return forbidden();

            // Uložení objektu do DB
            batch.save();

            return ok(batch.json());

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

// BootLoader ##########################################################################################################

    @ApiOperation(value = "create Bootloader",
            tags = { "HardwareType"},
            notes = "Create bootloader for HardwareType",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_BootLoader_New",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BootLoader.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",         response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bootLoader_create(@ApiParam(value = "hardware_type_id", required = true) String hardware_type_id) {
        try {

            // Zpracování Json
            final Form<Swagger_BootLoader_New> form = formFactory.form(Swagger_BootLoader_New.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_BootLoader_New help = form.get();

            Model_HardwareType hardwareType = Model_HardwareType.getById(hardware_type_id);
            if (hardwareType == null) return notFound("HardwareType not found");

            String identifier = help.version_identifier.replaceAll("\\s+", "");

            if (Model_BootLoader.find.query().where().eq("version_identifier", identifier).eq("hardware_type.id", hardwareType.id).findOne() != null)
                return badRequest("Version format is not unique!");

            Model_BootLoader boot_loader = new Model_BootLoader();
            boot_loader.name = help.name;
            boot_loader.changing_note =  help.changing_note;
            boot_loader.description = help.description;
            boot_loader.version_identifier = identifier;
            boot_loader.hardware_type = hardwareType;

            if (!boot_loader.create_permission()) return forbidden();
            boot_loader.save();

            // Vracím seznam
            return ok(Json.toJson(boot_loader));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit Bootloader",
            tags = { "HardwareType"},
            notes = "Edit bootloader for HardwareType",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_BootLoader_New",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BootLoader.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",         response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bootLoader_update(@ApiParam(value = "boot_loader_id", required = true) String boot_loader_id) {
        try {

            // Zpracování Json
            final Form<Swagger_BootLoader_New> form = formFactory.form(Swagger_BootLoader_New.class).bindFromRequest();
            if (form.hasErrors())return invalidBody(form.errorsAsJson());
            Swagger_BootLoader_New help = form.get();

            Model_BootLoader boot_loader = Model_BootLoader.getById(boot_loader_id);
            if (boot_loader == null) return notFound("BootLoader not found");

            if (!boot_loader.edit_permission()) return forbidden();

            boot_loader.name = help.name;
            boot_loader.changing_note = help.changing_note;
            boot_loader.description = help.description;
            boot_loader.version_identifier = help.version_identifier;

            boot_loader.update();

            return ok(Json.toJson(boot_loader));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete Bootloader",
            tags = { "HardwareType"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",         response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bootLoader_delete(String boot_loader_id) {
        try {

            Model_BootLoader boot_loader = Model_BootLoader.getById(boot_loader_id);
            if (boot_loader == null) return notFound("BootLoader not found");

            if (!boot_loader.delete_permission()) return forbidden();

            if (!boot_loader.hardware.isEmpty()) return badRequest("Bootloader is already used on some Board. Cannot be deleted.");

            boot_loader.delete();

            return ok(Json.toJson(boot_loader));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "upload Bootloader File",
            tags = {"Admin-Bootloader"},
            notes = "",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_BASE64_FILE",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(value = BodyParser.Json.class) // TODO , maxLength = 1024 * 1024 * 5)
    public Result bootLoader_uploadFile(String boot_loader_id) {
        try {

            // Získání JSON
            final Form<Swagger_BASE64_FILE> form = formFactory.form(Swagger_BASE64_FILE.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_BASE64_FILE help = form.get();

            Model_BootLoader boot_loader = Model_BootLoader.getById(boot_loader_id);
            if (boot_loader == null) return notFound("BootLoader boot_loader_id not found");

            if (!boot_loader.edit_permission()) return forbidden();

            //  data:image/png;base64,
            String[] parts = help.file.split(",");
            String[] type = parts[0].split(":");
            String[] content_type = type[1].split(";");
            String dataType = content_type[0].split("/")[1];

            logger.debug("bootLoader_uploadFile:: Cont Type:" + content_type[0]);
            logger.debug("bootLoader_uploadFile:: Data Type:" + dataType);
            logger.debug("bootLoader_uploadFile:: Data: " + parts[1].substring(0, 10) + "......");

            if (boot_loader.file != null) {
                boot_loader.file.delete();
            }

            String file_name =  UUID.randomUUID().toString() + "." + "bin";
            String file_path =  boot_loader.get_Container().getName() + "/" +file_name;

            logger.debug("bootLoader_uploadFile::  File Name " + file_name );
            logger.debug("bootLoader_uploadFile::  File Path " + file_path );

            boot_loader.file = Model_Blob.uploadAzure_File( parts[1], content_type[0], file_name, file_path);
            boot_loader.update();

            // Nefungovalo to korektně občas - tak se to ukládá oboustraně!
            boot_loader.file.boot_loader = boot_loader;
            boot_loader.file.update();

            boot_loader.refresh();

            // Vracím seznam
            return ok(boot_loader.json());

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit Bootloader Set as Main",
                tags = {"Admin-Bootloader"},
                notes = "List of Hardware Id for update on latest bootloader version (system used latest bootloader for type of hardware)",
                produces = "application/json",
                protocols = "https"

            )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",         response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Empty.class)
    public Result bootLoader_markAsMain(String boot_loader_id) {
        try {

            Model_BootLoader boot_loader = Model_BootLoader.getById(boot_loader_id);
            if (boot_loader == null) return notFound("BootLoader boot_loader_id not found");

            if (!boot_loader.edit_permission()) return forbidden();
            if (boot_loader.file == null) return badRequest("Required bootloader object with file");

            if (boot_loader.getMainHardwareType() != null) return badRequest("Bootloader is Already Main");

            Model_BootLoader old_main_not_cached = Model_BootLoader.find.query().where().eq("main_hardware_type.id", boot_loader.hardware_type.id).select("id").findOne();

            if (old_main_not_cached != null) {
                Model_BootLoader old_main = Model_BootLoader.getById(old_main_not_cached.id.toString());
                if (old_main != null) {
                    old_main.main_hardware_type = null;
                    old_main.cache_main_hardware_type_id = null;
                    old_main.update();
                }
            }

            boot_loader.main_hardware_type = boot_loader.getHardwareType();
            boot_loader.cache_main_hardware_type_id =  boot_loader.main_hardware_type.id;
            boot_loader.update();

            // Update Chache
            boot_loader.getHardwareType().cache_main_bootloader_id = boot_loader.id;

            // Vymažu Device Cache
            Model_Hardware.cache.clear();


            // Vracím Json
            return ok(boot_loader.json());

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "update Hardware Bootloader",
            tags = {"Bootloader"},
            notes = "List of Hardware Id for update on latest bootloader version (system used latest bootloader for type of hardware)",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Board_Bootloader_Update",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bootLoader_manualUpdate() {
        try {

            // Zpracování Json
            final Form<Swagger_Board_Bootloader_Update > form = formFactory.form(Swagger_Board_Bootloader_Update.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_Board_Bootloader_Update help = form.get();

            List<Model_Hardware> boards = Model_Hardware.find.query().where().in("id", help.device_ids).findList();
            if (boards.isEmpty()) return notFound("Hardware not found");

            List<WS_Help_Hardware_Pair> hardware_for_update = new ArrayList<>();

            for (Model_Hardware hardware : boards) {

                if (!hardware.read_permission()) return forbidden("You have no permission for Device " + hardware.id);

                WS_Help_Hardware_Pair pair = new WS_Help_Hardware_Pair();
                pair.hardware = hardware;

                if (help.bootloader_id != null) {

                    pair.bootLoader = Model_BootLoader.getById(help.bootloader_id);
                    if (pair.bootLoader == null) return notFound("BootLoader not found");

                } else {
                    pair.bootLoader = Model_BootLoader.find.query().where().eq("main_hardware_type.hardware.id", hardware.id).findOne();
                }

                hardware_for_update.add(pair);
            }

            if (!hardware_for_update.isEmpty()) {
                new Thread(() -> {
                    try {

                        Model_UpdateProcedure procedure = Model_Hardware.create_update_procedure(FirmwareType.BOOTLOADER, UpdateType.MANUALLY_BY_USER_INDIVIDUAL, hardware_for_update);
                        procedure.execute_update_procedure();

                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }).start();
            }


            // Vracím Json
            return ok();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    ///###################################################################################################################*/

    @ApiOperation(value = "create Hardware manual",
            tags = { "Admin-Board"},
            notes = "This Api is using only for developing mode, for registration of our Board - in future it will be used only by machine in factory or " +
                    "hardware themselves with \"registration procedure\". Hardware is not allowed to delete! Only deactivate. Classic User can only register that to own " +
                    "project or own to account",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Board_New_Manual",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_Hardware.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardware_create_manual() {
        try {

            // Zpracování Json
            final Form<Swagger_Board_New_Manual> form = formFactory.form(Swagger_Board_New_Manual.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_Board_New_Manual help = form.get();

            // Kotrola objektu
            if (Model_Hardware.getByFullId(help.full_id) != null) return badRequest("Hardware is already registered");

            // Kotrola objektu
            Model_HardwareType hardwareType = Model_HardwareType.getById( help.hardware_type_id);
            if (hardwareType == null) return notFound("HardwareType not found");

            // Kontorluji oprávnění
            if (!hardwareType.register_new_device_permission()) return forbidden();

            Model_Hardware hardware = new Model_Hardware();
            hardware.full_id = help.full_id;
            hardware.is_active = false;
            hardware.hardware_type = hardwareType;
            hardware.registration_hash = Model_Hardware.generate_hash();

            // Uložení desky do DB
            hardware.save();

            // Vracím seznam zařízení k registraci
            return created(hardware.json());

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "create Board automatic Garfield",
            tags = { "Admin-Board"},
            notes = "This Api is using for Board automatic registration adn Testing. Hardware is not allowed to delete! Only deactivate. Classic User can only register that to own " +
                    "project or own to account",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Board_New_Garfield",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Swagger_Hardware_New_Settings_Result.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardware_create_garfield() {
        try {

            // Zpracování Json
            final Form<Swagger_Board_New_Garfield> form = formFactory.form(Swagger_Board_New_Garfield.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_Board_New_Garfield help = form.get();

            // Kotrola objektu
            Model_HardwareType hardwareType = Model_HardwareType.getById(help.hardware_type_id);
            if (hardwareType == null) return notFound("HardwareType not found");

            // Kontorluji oprávnění
            if (!hardwareType.register_new_device_permission()) return forbidden();

            Model_HardwareBatch batch = Model_HardwareBatch.getById(help.batch_id);
            if (batch == null) return notFound("Batch not found");

            // Kontrola Objektu
            Model_Garfield garfield = Model_Garfield.getById(help.garfield_station_id);
            if (garfield == null) return notFound("Garfield Station not found");

            String mqtt_password_not_hashed = UUID.randomUUID().toString();
            String mqtt_username_not_hashed = UUID.randomUUID().toString();

            Model_Hardware hardware = Model_Hardware.getByFullId(help.full_id);
            if (hardware == null) {

                logger.warn("hardware_create_garfield - device not found in local DB, registering hardware, id: {}", help.full_id);

                // Try to Find it on Registration Authority
                if (Hardware_Registration_Authority.check_if_value_is_registered(help.full_id, "hardware_id")) {
                    logger.error("Device is already Registred ID: {}", help.full_id);
                    return badRequest("Device is already Registred ID: " + help.full_id);
                }
                if (Hardware_Registration_Authority.check_if_value_is_registered(batch.get_nextMacAddress_just_for_check(), "mac_address")) {
                    logger.error("Next Mac Address fot this device is already registered. Check It. Mac Address:: {}", help.full_id);
                    return badRequest("Next Mac Address fot this device is already registered. Check It Mac Address:: " +  help.full_id);
                }

                hardware = new Model_Hardware();
                hardware.full_id = help.full_id;
                hardware.is_active = false;
                hardware.hardware_type = hardwareType;
                hardware.batch_id = batch.id.toString();
                hardware.mac_address = batch.get_new_MacAddress();
                hardware.mqtt_username = BCrypt.hashpw(mqtt_username_not_hashed, BCrypt.gensalt());
                hardware.mqtt_password = BCrypt.hashpw(mqtt_password_not_hashed, BCrypt.gensalt());
                hardware.registration_hash = Model_Hardware.generate_hash();

                if (Hardware_Registration_Authority.register_device(hardware, hardwareType, batch)) {
                    hardware.save();
                } else {
                    Model_Hardware hardware_repair_from_authority = Model_Hardware.getByFullId(help.full_id);
                    if (hardware_repair_from_authority != null) {
                        hardware = hardware_repair_from_authority;
                    } else {
                       return notFound("Registration Authority Fail!!");
                    }
                }

                hardware.refresh();
            } else {
                hardware.mqtt_username = BCrypt.hashpw(mqtt_username_not_hashed, BCrypt.gensalt());
                hardware.mqtt_password = BCrypt.hashpw(mqtt_password_not_hashed, BCrypt.gensalt());
                hardware.update();
            }

            // Vytisknu štítky

            Printer_Api api = new Printer_Api();

            // Label 62 mm
            try {
                // Test for creating - Controlling all prerequisites and requirements
                new Label_62_mm_package(hardware, batch, garfield);
            } catch (IllegalArgumentException e) {
                return badRequest("Something is wrong: " + e.getMessage());
            }

            Label_62_mm_package label_62_mmPackage = new Label_62_mm_package(hardware, batch, garfield);
            api.printFile(garfield.print_sticker_id, 1, "Garfield Print Label", label_62_mmPackage.get_label(), null);

            // Label qith QR kode on Ethernet connector
            Label_62_split_mm_Details label_12_mm_details = new Label_62_split_mm_Details(hardware);
            api.printFile(garfield.print_label_id_1, 1, "Garfield Print QR Hash", label_12_mm_details.get_label(), null);

            if (hardwareType.connectible_to_internet) {

                // Najdu backup_server
                Model_HomerServer backup_server = Model_HomerServer.find.query().where().eq("server_type", HomerType.BACKUP).findOne();
                if (backup_server == null) return notFound("Backup server not found!!!");

                // Najdu Main_server
                Model_HomerServer main_server = Model_HomerServer.find.query().where().eq("server_type", HomerType.MAIN).findOne();
                if (main_server == null) return notFound("Main server not found!!!");

                DM_Board_Bootloader_DefaultConfig conf = hardware.bootloader_core_configuration();

                Swagger_Hardware_New_Settings_Result_Configuration configuration = new Swagger_Hardware_New_Settings_Result_Configuration();
                configuration.normal_mqtt_hostname = main_server.server_url;
                configuration.normal_mqtt_port = main_server.mqtt_port;
                configuration.mqtt_username = mqtt_password_not_hashed;
                configuration.mqtt_password = mqtt_username_not_hashed;
                configuration.backup_mqtt_hostname = backup_server.server_url;
                configuration.backup_mqtt_port = backup_server.mqtt_port;
                configuration.mac = hardware.mac_address;
                configuration.autobackup = conf.autobackup;
                configuration.blreport = conf.blreport;
                configuration.wdenable = conf.wdenable;
                configuration.netsource = conf.netsource;
                configuration.webview = conf.webview;
                configuration.webport = conf.webport;
                configuration.timeoffset = conf.timeoffset;
                configuration.lowpanbr = conf.lowpanbr;
                configuration.autojump = conf.autojump;
                configuration.wdtime = conf.wdtime;

                Swagger_Hardware_New_Settings_Result result = new Swagger_Hardware_New_Settings_Result();
                result.full_id = hardware.full_id;
                result.configuration = configuration;

                return created(Json.toJson(result));
            }

            // Vracím seznam zařízení k registraci
            return created(Json.toJson(hardware));
        } catch (IllegalCharsetNameException e) {
            return badRequest("All Mac Address used");
        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get Boards for Ide Operation",
            tags = { "Hardware"},
            notes = "List of hardware under Project for fast upload of Firmware to Board from Web IDE",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Board_for_fast_upload_detail.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result hardware_getForFastUpload( String project_id) {
        try {

            // Kotrola objektu
            Model_Project project = Model_Project.getById(project_id);
            if (project == null) return notFound("Project not found");

            // Kontrola oprávnění
            if (!project.edit_permission()) return forbidden();

            // Vyhledání seznamu desek na které lze nahrát firmware - okamžitě
            List<Model_Hardware> hw = Model_Hardware.find.query().where().eq("hardware_type.connectible_to_internet", true).eq("registration.project.id", project_id).findList();

            List<Swagger_Board_for_fast_upload_detail> list = new ArrayList<>();

            for (Model_Hardware hardware : hw ) {
                list.add(hardware.getHardwareForUpdate());
            }

            // Vrácení upravenéh objektu
            return ok(Json.toJson(list));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit Board personal description",
            tags = { "Hardware"},
            notes = "Used for add descriptions by owners. \"Persons\" who registred \"Board\" to own \"Project\" ",
            produces = "application/json",
            protocols = "https",
            code = 200
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Hardware.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardware_update_description( String hardware_id) {
        try {

            // Zpracování Json
            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_NameAndDescription help = form.get();

            // Kotrola objektu
            Model_Hardware hardware = Model_Hardware.getById(hardware_id);
            if (hardware == null) return notFound("Hardware not found");

            // Kontrola oprávnění
            if (!hardware.edit_permission()) return forbidden();

            // Uprava desky
            hardware.name = help.name;
            hardware.description = help.description;

            // Uprava objektu v databázi
            hardware.update();

            // Synchronizace s Homer serverem
            hardware.set_alias(hardware.name);

            // Vrácení upravenéh objektu
            return ok(Json.toJson(hardware));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit Board developers parameters",
            tags = { "Hardware"},
            notes = "Edit Developers parameters [developer_kit, database_synchronize, web_view, web_port]",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Board_Developer_parameters",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Hardware.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardware_update_parameters( String hardware_id) {
        try {

            // Zpracování Json
            final Form<Swagger_Board_Developer_parameters> form = formFactory.form(Swagger_Board_Developer_parameters.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_Board_Developer_parameters help = form.get();

            // Kotrola objektu
            Model_Hardware board = Model_Hardware.getById(hardware_id);
            if (board == null) return notFound("Board hardware_id not found");

            // Kontrola oprávnění
            if (!board.edit_permission()) return forbidden();

            DM_Board_Bootloader_DefaultConfig config = board.bootloader_core_configuration();

            decision: switch (help.parameter_type.toLowerCase()) {

                case "developer_kit" :{

                    // Synchronizace s Homer serverem a databází
                    board.developer_kit = help.boolean_value;
                    board.update();
                    break;
                }

                case "alias" :{
                    // Synchronizace s Homer serverem a databází
                    board.set_alias(help.string_value);
                    break;
                }

                case "database_synchronize" :{
                    // Synchronizace s Homer serverem a databází
                    board.set_database_synchronize(help.boolean_value);
                    break;
                }

                default: {

                    try {
                        WS_Message_Hardware_set_settings settings =  board.set_hardware_configuration_parameter(help);
                        return ok(Json.toJson(board));
                    } catch (IllegalArgumentException e) {
                        System.out.println("IllegalArgumentException" + e.getMessage());
                        return badRequest(e.getMessage());
                    } catch (Exception e) {
                        System.out.println("Exception" + e.getMessage());
                        return badRequest(e.getMessage());
                    }
                }

            }

            // Vrácení upravenéh objektu
            return ok(Json.toJson(board));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "upload C_Program into Hardware",
            tags = {"C_Program", "Board", "Actualization"},
            notes = "Upload compilation to list of hardware. Compilation is on Version oc C_Program. And before uplouding compilation, you must succesfuly compile required version before! " +
                    "Result (HTML code) will be every time 200. - Its because upload, restart, etc.. operation need more than ++30 second " +
                    "There is also problem / chance that Tyrion didn't find where Embedded hardware is. So you have to listening Server Sent Events (SSE) and show \"future\" message to the user!",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_DeployFirmware",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardware_updateFirmware() {
        try {

            // Zpracování Json
            Form<Swagger_DeployFirmware> form = formFactory.form(Swagger_DeployFirmware.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_DeployFirmware help = form.get();


            List<WS_Help_Hardware_Pair> b_pairs = new ArrayList<>();

            if (help.hardware_pairs.isEmpty()) return badRequest("List is Empty");

            for (Swagger_Board_CProgram_Pair hardware_update_pair : help.hardware_pairs) {

                // Ověření objektu
                Model_Version c_program_version = Model_Version.getById(hardware_update_pair.c_program_version_id);
                if (c_program_version == null) return notFound("Version not found");

                //Zkontroluji validitu Verze zda sedí k C_Programu
                if (c_program_version.get_c_program() == null) return badRequest("Version is not version of C_Program");

                // Zkontroluji oprávnění
                if (!c_program_version.get_c_program().read_permission()) return forbidden();

                //Zkontroluji validitu Verze zda sedí k C_Programu
                if (c_program_version.compilation == null) return badRequest("Version is not version of C_Program - Missing compilation File");

                // Ověření zda je kompilovatelná verze a nebo zda kompilace stále neběží
                if (c_program_version.compilation.status != CompilationStatus.SUCCESS) return badRequest("You cannot upload code in state:: " + c_program_version.compilation.status.name());

                //Zkontroluji zda byla verze už zkompilována
                if (!c_program_version.compilation.status.name().equals(CompilationStatus.SUCCESS.name())) return badRequest("The program is not yet compiled & Restored");

                // Kotrola objektu
                Model_Hardware hardware = Model_Hardware.getById(hardware_update_pair.hardware_id);
                if (hardware == null) return notFound("Hardware not found");

                // Kontrola oprávnění
                if (!hardware.edit_permission()) return forbidden();


                WS_Help_Hardware_Pair b_pair = new WS_Help_Hardware_Pair();
                b_pair.hardware = hardware;
                b_pair.c_program_version = c_program_version;

                b_pairs.add(b_pair);

            }

            if (!b_pairs.isEmpty()) {
                new Thread(() -> {
                    try {

                        Model_UpdateProcedure procedure = Model_Hardware.create_update_procedure(FirmwareType.FIRMWARE, UpdateType.MANUALLY_BY_USER_INDIVIDUAL, b_pairs);
                        procedure.execute_update_procedure();

                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }).start();
            }


            // Vracím odpověď
            return ok();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "update Board Backup",
            tags = { "Hardware"},
            notes = "",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_HardwareBackupSettings",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardware_updateBackup() {
        try {

            // Zpracování Json
            final Form<Swagger_HardwareBackupSettings> form = formFactory.form(Swagger_HardwareBackupSettings.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_HardwareBackupSettings help = form.get();

            if (help.hardware_backup_pairs.isEmpty()) return badRequest("List is Empty");

            // Seznam Hardwaru k updatu
            List<WS_Help_Hardware_Pair> hardware_pairs = new ArrayList<>();

            for (Swagger_HardwareBackupSettings.HardwareBackupPair hardware_backup_pair : help.hardware_backup_pairs) {

                // Kotrola objektu
                Model_Hardware board = Model_Hardware.getById(hardware_backup_pair.hardware_id);
                if (board == null) return notFound("Board hardware_id not found");

                // Kontrola oprávnění
                if (!board.edit_permission()) return forbidden();

                // Pokud je nastaven autobackup na true
                if (hardware_backup_pair.backup_mode) {

                    // Na devicu byla nastavená statická - Proto je potřeba jí odstranit a nahradit autobackupem
                    if (!board.backup_mode) {

                        DM_Board_Bootloader_DefaultConfig config = board.bootloader_core_configuration();
                        config.autobackup = hardware_backup_pair.backup_mode;
                        board.update_bootloader_configuration(config);

                        logger.debug("hardware_updateBackup - To TRUE:: Board Id: {} has own Static Backup - Removing static backup procedure required", hardware_backup_pair.hardware_id);

                        board.actual_backup_c_program_version = null;
                        board.backup_mode = true;
                        board.update();

                        WS_Message_Hardware_set_settings result = board.set_auto_backup();

                    // Na devicu už autobackup zapnutý byl - nic nedělám jen překokontroluji???
                    } else {

                        logger.debug("hardware_updateBackup - To TRUE:: Board Id: {} has already sat as a dynamic Backup", hardware_backup_pair.hardware_id);

                        WS_Message_Hardware_set_settings result = board.set_auto_backup();
                        if (result.status.equals("success")) {
                            logger.debug("hardware_updateBackup - To TRUE:: Board Id: {} Success of setting of dynamic backup", hardware_backup_pair.hardware_id);

                            // Toto je pro výjmečné případy - kdy při průběhu updatu padne tyrion a transakce není komplentí
                            if ( board.actual_backup_c_program_version != null) {
                                board.actual_backup_c_program_version = null;
                                board.update();
                            }
                        }
                    }

                // Autobacku je statický
                } else {

                    if (hardware_backup_pair.c_program_version_id == null) return badRequest("If backup_mode is set to false, c_program_version_id is required");

                    logger.debug("hardware_updateBackup - To FALSE:: Board Id: {} has dynamic Backup or already set static backup", hardware_backup_pair.hardware_id);

                    // Uprava desky na statický backup
                    Model_Version c_program_version = Model_Version.getById(hardware_backup_pair.c_program_version_id);
                    if (c_program_version == null) return notFound("Version not found");

                    //Zkontroluji validitu Verze zda sedí k C_Programu
                    if (c_program_version.get_c_program() == null) return badRequest("Version is not version of C_Program");

                    // Zkontroluji oprávnění
                    if (!c_program_version.get_c_program().read_permission()) return forbidden();

                    //Zkontroluji validitu Verze zda sedí k C_Programu
                    if (c_program_version.compilation == null) return badRequest("Version is not version of C_Program - Missing compilation File");

                    // Ověření zda je kompilovatelná verze a nebo zda kompilace stále neběží
                    if (c_program_version.compilation.status != CompilationStatus.SUCCESS) return badRequest("You cannot upload code in state:: " + c_program_version.compilation.status.name());

                    //Zkontroluji zda byla verze už zkompilována
                    if (!c_program_version.compilation.status.name().equals(CompilationStatus.SUCCESS.name())) return badRequest("The program is not yet compiled & Restored");

                    WS_Help_Hardware_Pair b_pair = new WS_Help_Hardware_Pair();
                    b_pair.hardware = board;
                    b_pair.c_program_version = c_program_version;

                    hardware_pairs.add(b_pair);

                    if (!board.backup_mode) {
                        board.actual_backup_c_program_version = null;
                        board.backup_mode = false;
                        board.update();
                    }

                    DM_Board_Bootloader_DefaultConfig config = board.bootloader_core_configuration();
                    config.autobackup = hardware_backup_pair.backup_mode;
                    board.update_bootloader_configuration(config);
                }
            }

            if (!hardware_pairs.isEmpty()) {
                new Thread(() -> {

                    try {
                        Model_UpdateProcedure procedure = Model_Hardware.create_update_procedure(FirmwareType.BACKUP, UpdateType.MANUALLY_BY_USER_INDIVIDUAL, hardware_pairs);
                        procedure.execute_update_procedure();

                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }).start();
            }

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get Boards with filter parameters",
            tags = { "Hardware"},
            notes = "Get List of hardware. According to permission - system return only hardware from project, where is user owner or" +
                    " all hardware if user have static Permission key",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_Board_Filter",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Hardware_List.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardware_getByFilter(Integer page_number) {
        try {

            // Zpracování Json
            final Form<Swagger_Board_Filter> form = formFactory.form(Swagger_Board_Filter.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_Board_Filter help = form.get();

            // Tvorba parametru dotazu
            Query<Model_Hardware> query = Ebean.find(Model_Hardware.class);

            if (help.hardware_type_ids != null && !help.hardware_type_ids.isEmpty()) {
                query.where().in("hardware_type.id", help.hardware_type_ids);
            }

            // If contains confirms
            if (help.active != null) {
                query.where().eq("is_active", help.active.equals("true"));
            }

            if (help.projects != null && !help.projects.isEmpty()) {
                query.where().in("registration.project.id", help.projects);
            }

            if (help.producers != null) {
                query.where().in("hardware_type.producer.id", help.producers);
            }

            if (help.processors != null) {
                query.where().in("hardware_type.processor.id", help.processors);
            }

            // From date
            if (help.start_time != null) {
                query.where().ge("created", help.start_time);
            }

            // To date
            if (help.end_time != null) {
                query.where().le("created", help.end_time);
            }

            // Vytvářím seznam podle stránky
            Swagger_Hardware_List result = new Swagger_Hardware_List(query, page_number);

            // Vracím seznam
            return ok(Json.toJson(result));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "uploadPicture Hardware",
            tags = { "Hardware"},
            notes = "Upload Board file",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_BASE64_FILE",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Hardware.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(value = BodyParser.Json.class)
    public Result hardware_uploadPicture(String hardware_registration_id) {
        try {

            // Získání JSON
            final Form<Swagger_BASE64_FILE> form = formFactory.form(Swagger_BASE64_FILE.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_BASE64_FILE help = form.get();

            Model_HardwareRegistration hardware = Model_HardwareRegistration.getById(hardware_registration_id);
            if (hardware == null) return notFound("HardwareRegistration not found");

            if (!hardware.edit_permission()) return forbidden();

            // Odebrání předchozího obrázku
            if (hardware.picture != null) {
                logger.debug("hardware_uploadPicture - removing previous picture");
                Model_Blob blob = hardware.picture;
                hardware.picture = null;
                hardware.update();
                blob.delete();
            }

            //  data:image/png;base64,
            String[] parts = help.file.split(",");
            String[] type = parts[0].split(":");
            String[] dataType = type[1].split(";");

            logger.debug("hardware_uploadPicture - type:" + dataType[0]);
            logger.debug("hardware_uploadPicture - data:" + parts[1].substring(0, 10));

            String file_name = UUID.randomUUID().toString() + ".png";
            String file_path = hardware.getPath() + "/" + file_name;

            hardware.picture = Model_Blob.uploadAzure_File(parts[1], dataType[0], file_name , file_path);
            hardware.update();

            return ok(hardware.json());
        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "generate_mqtt_password Board",
            tags = {"Board"},
            notes = "Generate new connection password for Hardware",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",               response = Swagger_Hardware_New_Password.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result hardware_generate_new_password(String hardware_id) {
        try {

            Model_Hardware board = Model_Hardware.getById(hardware_id);
            if (board == null) return notFound("Board hardware_id not found");
            if (!board.edit_permission()) return forbidden();


            String mqtt_password_not_hashed = UUID.randomUUID().toString();
            String mqtt_username_not_hashed = UUID.randomUUID().toString();

            board.mqtt_username = BCrypt.hashpw(mqtt_username_not_hashed, BCrypt.gensalt());
            board.mqtt_password = BCrypt.hashpw(mqtt_password_not_hashed, BCrypt.gensalt());
            board.update();

            Swagger_Hardware_New_Password pss = new Swagger_Hardware_New_Password();
            pss.mqtt_password = mqtt_password_not_hashed;
            pss.mqtt_username = mqtt_username_not_hashed;

            return ok(Json.toJson(pss));
        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "change_server Board",
            tags = { "Hardware"},
            notes = "Redirect Board to another server (Change Server)",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_Board_Server_Redirect",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardware_redirect_to_server(String hardware_id) {
        try {

            // Získání JSON
            final Form<Swagger_Board_Server_Redirect> form = formFactory.form(Swagger_Board_Server_Redirect.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_Board_Server_Redirect help = form.get();

            System.out.println("hardware_redirect_to_server:: Příjem zprávy:: " + Json.toJson(help));

            Model_Hardware board = Model_Hardware.getById(hardware_id);
            if (board == null) return notFound("Board does not exist");
            if (!board.edit_permission()) {
                System.out.println(" board.edit_permission - false!");
                return forbidden();
            }

            // Jedná se o přesměrování na server v rámci stejné hierarchie - na server co mám v DB
            if (help.server_id != null) {

                System.out.println("Bude se přesměrovávat z databáze");

                Model_HomerServer server = Model_HomerServer.getById(help.server_id);
                if (server == null) return notFound("Board does not exist");
                if (!server.read_permission()) {
                    System.out.println("!server.read_permission() - false!");
                    return forbidden();
                }

                board.device_relocate_server(server);

            // Jedná se o server mimo náš svět - například z dev na stage, nebo z produkce na dev
            } else {
                if (help.server_port == null || help.server_url == null) {
                    return badRequest("its required send server_id  or server_url + server_port ");
                }


                WS_Message_Hardware_change_server response = board.device_relocate_server(help.server_url, help.server_port);
                if (response.status.equals("success")) {
                    return ok();
                } else {
                    return badRequest("Cloud Device Execution Error: " + response.error_message);
                }

            }

            return ok();
        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "command Board execution",
            tags = {"Board"},
            notes = "Removes picture of logged person",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_Board_Command",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardware_command_execution() {
        try {

            // Získání JSON
            final Form<Swagger_Board_Command> form = formFactory.form(Swagger_Board_Command.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_Board_Command help = form.get();


            Model_Hardware board = Model_Hardware.getById(help.hardware_id);
            if (board == null) return notFound("Board hardware_id not found");
            if (!board.edit_permission()) return forbidden();

            if (help.command == null) return notFound("Board command not recognized");
            board.execute_command(help.command, true);

            return ok();
        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete Board picture",
            tags = {"Board"},
            notes = "Removes picture of logged person",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result hardware_removePicture(String hardware_registration_id) {
        try {

            Model_HardwareRegistration hardware = Model_HardwareRegistration.getById(hardware_registration_id);
            if (hardware == null) return notFound("HardwareRegistration not found");

            if (!hardware.edit_permission()) return forbidden();

            if (hardware.picture != null) {
                hardware.picture.delete();
                hardware.picture = null;
                hardware.update();
            } else {
                return badRequest("There is no picture to remove.");
            }

            return ok();
        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "deactivate Board",
            tags = { "Hardware"},
            notes = "Permanent exclusion from the system - for some reason it is not allowed to remove the Board from database",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Hardware_update"),
                    }),
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Hardware.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result hardware_deactivate( String hardware_id) {
        try {

            // Kotrola objektu
            Model_Hardware board = Model_Hardware.getById(hardware_id);
            if (board == null) return notFound("Board hardware_id not found");

            // Kontrola oprávnění
            if (board.update_permission()) return forbidden();

            // Úprava stavu
            board.is_active = false;
            board.cache_project_id = null;

            // Uložení do databáze
            board.update();

            // Vrácení objektu
            return ok(Json.toJson(board));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get Board",
            tags = { "Hardware"},
            notes = "if you want get Board object by query = hardware_id. User can get only hardware from project, whitch " +
                    "user owning or user need Permission key \"Board_rea\".",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Hardware_read"),
                    }),
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Hardware.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result hardware_get(String hardware_id) {
        try {

            // Kotrola objektu
            Model_Hardware board = Model_Hardware.getById(hardware_id);
            if (board == null) return notFound("Board hardware_id not found");

            // Kontrola oprávnění
            if (!board.read_permission()) return forbidden();

            // vrácení objektu
            return ok(Json.toJson(board));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "check Board registration status",
            tags = {"Board"},
            notes = "Check Board state for new Registration. Types of responses in JSON state value" +
                    "[CAN_REGISTER, NOT_EXIST, ALREADY_REGISTERED_IN_YOUR_ACCOUNT, ALREADY_REGISTERED, PERMANENTLY_DISABLED, BROKEN_DEVICE]... \n " +
                    "PERMANENTLY_DISABLED - device was removed by Byzance. \n" +
                    "BROKEN_DEVICE - device exist - but its not possible to registered that. Damaged during manufacturing. ",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Board_Registration_Status.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result hardware_check(String hash_for_adding) {
        try {

            Swagger_Board_Registration_Status status = new Swagger_Board_Registration_Status();

            // Kotrola objektu
            Model_Hardware hardware_not_cached = Model_Hardware.find.query().where().eq("registration_hash", hash_for_adding).select("id").findOne();
            if (hardware_not_cached == null) {
                status.status = BoardRegistrationStatus.NOT_EXIST;
                return ok(Json.toJson(status));
            }

            Model_Hardware board = Model_Hardware.getById(hardware_not_cached.id);

            if (board == null) {
                status.status = BoardRegistrationStatus.NOT_EXIST;
            } else if (board.project_id() == null) {
                status.status = BoardRegistrationStatus.CAN_REGISTER;
            } else if (board.project_id() != null && board.read_permission()) {
                status.status = BoardRegistrationStatus.ALREADY_REGISTERED_IN_YOUR_ACCOUNT;
            } else {
                status.status = BoardRegistrationStatus.ALREADY_REGISTERED;
            }

            return ok(Json.toJson(status));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get B_Program all details for integration",
            tags = {"Blocko", "B_Program"},
            notes = "get all hardware that user can integrate to Blocko program",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Boards_For_Blocko.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result hardware_allDetailsForBlocko(String project_id) {
        try {
            /* // TODO
            // Kontrola objektu
            Model_Project project = Model_Project.getById(project_id);
            if (project == null) return notFound("Project project_id not found");

            // Kontrola oprávnění
            if (!project.read_permission()) return forbidden();

            // Získání objektu
            Swagger_Boards_For_Blocko boards_for_blocko = new Swagger_Boards_For_Blocko();
            boards_for_blocko.add_M_Projects(project.getGridProjects());
            boards_for_blocko.add_C_Programs(project.getCPrograms());

            boards_for_blocko.hardware.addAll(project.getHardware());


            boards_for_blocko.hardware_types = Model_HardwareType.find.query().where().eq("hardware.project.id", project.id).findList();


            // Vrácení objektu
            return ok(Json.toJson(boards_for_blocko));*/

            return ok("TODO");

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "Remove Hardware from Database - Only for Administrators", hidden = true)
    public Result hardware_delete(String hardware_id) {
        try {

            // Kontrola objektu
            Model_Hardware hardware = Model_Hardware.getById(hardware_id);
            if (hardware == null) return notFound("Hardware not found");

            // Kontrola oprávnění
            if (!hardware.delete_permission()) return forbidden();

            if (hardware.registration != null)
                return badRequest("Board is already in use.");

            hardware.delete();

            return ok();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

// HARDWARE REGISTRATION ###################################################################################################################

    @ApiOperation(value = "tag HardwareRegistration",
            tags = {"HardwareRegistration"},
            notes = "",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_Tags",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_HardwareRegistration.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardwareRegistration_addTags() {
        try {

            // Zpracování Json
            final Form<Swagger_Tags> form = formFactory.form(Swagger_Tags.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_Tags help = form.get();

            Model_HardwareRegistration registration = Model_HardwareRegistration.getById(help.object_id);
            if (registration == null) return notFound("HardwareRegistration not found");

            // Kontrola oprávnění těsně před uložením
            if (!registration.edit_permission()) return forbidden();

            registration.addTags(help.tags);

            // Vrácení objektu
            return ok(registration.json());

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "untag HardwareRegistration",
            tags = {"HardwareRegistration"},
            notes = "",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_Tags",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_HardwareRegistration.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardwareRegistration_removeTags() {
        try {

            // Zpracování Json
            final Form<Swagger_Tags> form = formFactory.form(Swagger_Tags.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_Tags help = form.get();

            Model_HardwareRegistration registration = Model_HardwareRegistration.getById(help.object_id);
            if (registration == null) return notFound("HardwareRegistration not found");

            // Kontrola oprávnění těsně před uložením
            if (!registration.edit_permission()) return forbidden();

            registration.removeTags(help.tags);

            // Vrácení objektu
            return ok(registration.json());

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

///###################################################################################################################*/

    @ApiOperation(value = "create HardwareGroup",
            tags = { "HardwareGroup"},
            notes = "Create HardwareGroup",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_NameAndDesc_ProjectIdRequired",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_HardwareGroup.class),
            @ApiResponse(code = 401, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardwareGroup_create() {
        try {

            // Zpracování Json
            final Form<Swagger_NameAndDesc_ProjectIdRequired> form = formFactory.form(Swagger_NameAndDesc_ProjectIdRequired.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_NameAndDesc_ProjectIdRequired help = form.get();

            Model_Project project = Model_Project.getById(help.project_id);
            if (project == null) return notFound("Project not found");

            if (Model_HardwareGroup.find.query().where().eq("name", help.name).eq("project.id", project.id).findOne() != null) {
                return badRequest("Group name must be a unique!");
            }

            Model_HardwareGroup group = new Model_HardwareGroup();
            group.name = help.name;
            group.description = help.description;
            group.project = project;

            if (!group.create_permission()) return forbidden();
            group.save();

            // Vracím seznam
            return ok(Json.toJson(group));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit HardwareGroup",
            tags = { "HardwareGroup"},
            notes = "update HardwareGroup",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_NameAndDescription",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_HardwareGroup.class),
            @ApiResponse(code = 401, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardwareGroup_update(String hardware_group_id) {
        try {

            // Zpracování Json
            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors())return invalidBody(form.errorsAsJson());
            Swagger_NameAndDescription help = form.get();

            Model_HardwareGroup group = Model_HardwareGroup.getById(hardware_group_id);
            if (group == null) return notFound("HardwareGroup not found");

            if (!group.edit_permission()) return forbidden();

            group.name = help.name;
            group.description = help.description;

            group.update();

            return ok(Json.toJson(group));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "addHW HardwareGroup",
            tags = { "HardwareGroup"},
            notes = "update HardwareGroup add devices",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_HardwareGroup_Edit",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_HardwareGroup.class),
            @ApiResponse(code = 401, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardwareGroup_addHardware() {
        try {
            
            // Zpracování Json
            final Form<Swagger_HardwareGroup_Edit> form = formFactory.form(Swagger_HardwareGroup_Edit.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_HardwareGroup_Edit help = form.get();

            Model_HardwareGroup group = Model_HardwareGroup.getById(help.group_id);
            if (group == null) return notFound("HardwareGroup not found");
            if (!group.update_permission()) return forbidden();
            
            List<UUID> inGroup = group.getHardwareIds();

            for (UUID hardware_id : help.hardware_ids) {
                
                if (inGroup.contains(hardware_id)) continue;

                Model_HardwareRegistration hardware = Model_HardwareRegistration.getById(hardware_id);
                if (hardware == null) return notFound("HardwareRegistration not found");
                if (!hardware.update_permission()) return forbidden();

                logger.debug("hardwareGroup_addHardware - hardware: {}", hardware.hardware.full_id);
                
                hardware.group = group;
                hardware.save();
            }

            group.refresh();
            
            return ok(group.json());

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "removeHW HardwareGroup",
            tags = { "HardwareGroup"},
            notes = "update HardwareGroup remove devices",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_HardwareGroup_Edit",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_HardwareGroup.class),
            @ApiResponse(code = 401, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardwareGroup_removeHardware() {
        try {

            // Zpracování Json
            final Form<Swagger_HardwareGroup_Edit> form = formFactory.form(Swagger_HardwareGroup_Edit.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_HardwareGroup_Edit help = form.get();

            Model_HardwareGroup group = Model_HardwareGroup.getById(help.group_id);
            if (group == null) return notFound("HardwareGroup not found");
            if (!group.update_permission()) return forbidden();

            List<UUID> inGroup = group.getHardwareIds();

            for (UUID hardware_id : help.hardware_ids) {

                if (inGroup.contains(hardware_id)) continue;

                Model_HardwareRegistration hardware = Model_HardwareRegistration.getById(hardware_id);
                if (hardware == null) return notFound("HardwareRegistration not found");
                if (!hardware.update_permission()) return forbidden();

                logger.debug("hardwareGroup_removeHardware - hardware: {}", hardware.hardware.full_id);

                hardware.group = null;
                hardware.save();
            }

            group.refresh();

            return ok(group.json());

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete HardwareGroup",
            tags = { "HardwareGroup"},
            notes = "delete BoardGroup",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result hardwareGroup_delete(String hardware_group_id) {
        try {

            Model_HardwareGroup group = Model_HardwareGroup.getById(hardware_group_id);
            if (group == null) return notFound("HardwareGroup not found");

            if (!group.delete_permission()) return forbidden();

            group.delete();

            return ok();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "getByProject HardwareGroup",
            tags = { "HardwareGroup"},
            notes = "get List of BoardGroup from Project",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_HardwareGroup.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result hardwareGroup_getByProject(String project_id) {
        try {

            // Kontrola validity objektu
            Model_Project project = Model_Project.getById(project_id);
            if (project == null) return notFound("Project project_id not found");

            // Kontorluji oprávnění
            if (!project.read_permission()) return forbidden();

            // Vrácení validity objektu
            return ok(Json.toJson(project.getHardwareGroups()));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get HardwareGroup",
            tags = { "HardwareGroup"},
            notes = "get List of BoardGroup from Project",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_HardwareGroup.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result hardwareGroup_get(String group_id) {
        try {

            // Kontrola validity objektu
            Model_HardwareGroup group = Model_HardwareGroup.getById(group_id);
            if (group == null) return notFound("HardwareGroup not found");

            if (!group.read_permission()) return forbidden();

            // Vrácení validity objektu
            return ok(group.json());

        } catch (Exception e) {
            return internalServerError(e);
        }
    }
}
