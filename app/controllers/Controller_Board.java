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
import utilities.swagger.output.filter_results.Swagger_Board_List;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_change_server;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_set_settings;
import websocket.messages.homer_hardware_with_tyrion.helps_objects.WS_Help_Hardware_Pair;

import java.nio.charset.IllegalCharsetNameException;
import java.util.*;


@Api(value = "Not Documented API - InProgress or Stuck")  // Záměrně takto zapsané - Aby ve swaggru nezdokumentované API byly v jedné sekci
@Security.Authenticated(Authentication.class)
public class Controller_Board extends BaseController {

// LOGGER ##############################################################################################################
    
    private static final Logger logger = new Logger(Controller_Board.class);
    
///###################################################################################################################*/


    private FormFactory formFactory;

    @Inject
    public Controller_Board(FormFactory formFactory) {
        this.formFactory = formFactory;
    }


///###################################################################################################################*/

    @ApiOperation(value = "create Processor",
            tags = {"Admin-Processor"},
            notes = "If you want create new Processor. Send required json values and server respond with new object",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Static Permission key", value =  "Processor_create" ),
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
    @ApiResponses(value = {
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
            processor.description    = help.description;
            processor.processor_code = help.processor_code;
            processor.name           = help.name;
            processor.speed          = help.speed;

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if (!processor.create_permission()) return forbiddenEmpty();

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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",         response = Model_Processor.class),
            @ApiResponse(code = 404, message = "Object not found",  response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error", response = Result_InternalServerError.class)
    })
    public Result processor_get(@ApiParam(value = "processor_id String query", required = true) String processor_id) {
        try {

            //Zkontroluji validitu
            Model_Processor processor = Model_Processor.getById(processor_id);
            if (processor == null ) return notFound("Processor processor_id not found");

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
    @ApiResponses(value = {
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
    @ApiResponses(value = {
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
            if (processor == null ) return notFound("Processor processor_id not found");

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if (! processor.edit_permission())  return forbiddenEmpty();

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
    @ApiResponses(value = {
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
            if (processor == null ) return notFound("Processor not found");

            // Ověření oprávnění těsně před uložením (aby se mohlo ověřit oprávnění nad projektem)
            if (!processor.delete_permission()) return forbiddenEmpty();

            if (processor.type_of_boards.size() > 0) return badRequest("Processor is assigned to some type of board, so cannot be deleted");

            // Mažu z databáze
            processor.delete();

            return okEmpty();

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
    @ApiResponses(value = {
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

            if (!boot_loader.read_permission()) return forbiddenEmpty();

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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_File_Content.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result fileRecord_get_firmware(@ApiParam(value = "file_record_id String query", required = true)  String version_id) {
        try {

            // Kontrola validity objektu
            Model_Version versionObject = Model_Version.getById(version_id);
            if (versionObject == null) return notFound("FileRecord file_record_id not found");

            // Swagger_File_Content - Zástupný dokumentační objekt
            if (versionObject.get_c_program() == null) return badRequestEmpty();

            // Kontrola oprávnění
            if (!versionObject.get_c_program().read_permission()) return badRequestEmpty();

            // Swagger_File_Content - Zástupný dokumentační objekt
            Swagger_File_Content content = new Swagger_File_Content();
            content.file_in_base64 = versionObject.compilation.blob.get_fileRecord_from_Azure_inString();

            // Vracím content
            return ok(Json.toJson(content));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

///###################################################################################################################*/

    @ApiOperation(value = "create Producer",
            tags = {"Admin-Producer"},
            notes = "if you want create new Producer. Its company owned physical boards and we used that for filtering",
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
    @ApiResponses(value = {
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
            if (! producer.create_permission()) return forbiddenEmpty();

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
            notes = "if you want edit information about Producer. Its company owned physical boards and we used that for filtering",
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
    @ApiResponses(value = {
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
            if (producer == null ) return notFound("Producer producer_id not found");

            // Kontorluji oprávnění těsně před uložením
            if (! producer.edit_permission()) return forbiddenEmpty();

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
            notes = "if you want get list of Producers. Its list of companies owned physical boards and we used that for filtering",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Producer.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Objects not found",         response = Result_NotFound.class),
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
            notes = "if you want get Producer. Its company owned physical boards and we used that for filtering",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
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
            if (producer == null ) return notFound("Producer producer_id not found");

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
    @ApiResponses(value = {
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
            if (!producer.delete_permission()) return forbiddenEmpty();

            if (producer.type_of_boards.size() > 0 || producer.blocks.size() > 0 || producer.widgets.size() > 0)
                return badRequest("Producer is assigned to some objects, so cannot be deleted.");

            // Smazání objektu
            producer.delete();

            // Vrácení potvrzení
            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

///###################################################################################################################*/

    @ApiOperation(value = "create TypeOfBoard",
            tags = { "Type-Of-Board"},
            notes = "The TypeOfBoard is category for IoT. Like Raspberry2, Arduino-Uno etc. \n\n" +
                    "We using that for compilation, sorting libraries, filtres and more..",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_TypeOfBoard_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_TypeOfBoard.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result typeOfBoard_create() {
        try {

            // Zpracování Json
            final Form<Swagger_TypeOfBoard_New> form = formFactory.form(Swagger_TypeOfBoard_New.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_TypeOfBoard_New help = form.get();

            // Kontrola objektu
            Model_Producer producer = Model_Producer.getById(help.producer_id);
            if (producer == null ) return notFound("Producer producer_id not found");

            // Kontrola objektu
            Model_Processor processor = Model_Processor.getById(help.processor_id);
            if (processor == null ) return notFound("Processor processor_id not found");

            // Tvorba objektu
            Model_TypeOfBoard typeOfBoard = new Model_TypeOfBoard();
            typeOfBoard.name = help.name;
            typeOfBoard.description = help.description;
            typeOfBoard.compiler_target_name = help.compiler_target_name;
            typeOfBoard.processor = processor;
            typeOfBoard.producer = producer;
            typeOfBoard.connectible_to_internet = help.connectible_to_internet;

            // Kontorluji oprávnění
            if (!typeOfBoard.create_permission()) return forbiddenEmpty();

            // Uložení objektu do DB
            typeOfBoard.save();

            // Vytvoříme defaultní C_Program pro snížení počtu kroků pro nastavení desky
            Model_CProgram c_program = new Model_CProgram();
            c_program.name =  typeOfBoard.name + " default program";
            c_program.description = "Default program for this device type";
            c_program.type_of_board_default = typeOfBoard;
            c_program.type_of_board =  typeOfBoard;
            c_program.publish_type  = ProgramType.DEFAULT_MAIN;
            c_program.save();

            typeOfBoard.refresh();

            // Vytvoříme testovací C_Program pro snížení počtu kroků pro nastavení desky
            Model_CProgram c_program_test = new Model_CProgram();
            c_program_test.name =  typeOfBoard.name + " test program";
            c_program_test.description = "Test program for this device type";
            c_program_test.type_of_board_test = typeOfBoard;
            c_program_test.type_of_board =  typeOfBoard;
            c_program_test.publish_type  = ProgramType.DEFAULT_TEST;
            c_program_test.save();

            typeOfBoard.refresh();

            // TODO přidat do cache

            return created(Json.toJson(typeOfBoard));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit TypeOfBoard",
            tags = { "Type-Of-Board"},
            notes = "if you want edit base TypeOfBoard information",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfBoard.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "TypeOfBoard_edit"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_TypeOfBoard_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_TypeOfBoard.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result typeOfBoard_update( String type_of_board_id) {
        try {

            // Zpracování Json
            final Form<Swagger_TypeOfBoard_New> form = formFactory.form(Swagger_TypeOfBoard_New.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_TypeOfBoard_New help = form.get();

            // Kontrola objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.getById(type_of_board_id);
            if (typeOfBoard == null) return notFound("TypeOfBoard type_of_board_id not found");

            // Kontrola objektu
            Model_Producer producer = Model_Producer.getById(help.producer_id);
            if (producer == null ) return notFound("Producer producer_id not found");

            // Kontrola objektu
            Model_Processor processor = Model_Processor.getById(help.processor_id);
            if (processor == null ) return notFound("Processor processor_id not found");

            // Kontorluji oprávnění
            if (! typeOfBoard.edit_permission()) return forbiddenEmpty();

            // Uprava objektu
            typeOfBoard.name = help.name;
            typeOfBoard.description = help.description;
            typeOfBoard.compiler_target_name = help.compiler_target_name;
            typeOfBoard.processor = processor;
            typeOfBoard.producer = producer;
            typeOfBoard.connectible_to_internet = help.connectible_to_internet;

            // Uložení do DB
            typeOfBoard.update();

            // Vrácení změny
            return ok(Json.toJson(typeOfBoard));

        } catch (Exception e) {
            return internalServerError(e);
        }

    }

    @ApiOperation(value = "delete TypeOfBoard",
            tags = { "Type-Of-Board"},
            notes = "if you want delete TypeOfBoard object by query = type_of_board_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfBoard.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "TypeOfBoard_delete"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result typeOfBoard_delete( String type_of_board_id) {
        try {

            // Kontrola objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.getById(type_of_board_id);
            if (typeOfBoard == null ) return notFound("TypeOfBoard type_of_board_id not found") ;

            // Kontorluji oprávnění
            if (! typeOfBoard.delete_permission()) return forbiddenEmpty();

            // Smazání objektu
            typeOfBoard.delete();

            // Vrácení potvrzení
            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get TypeOfBoards All",
            tags = { "Type-Of-Board"},
            notes = "if you want get all TypeOfBoard objects",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_TypeOfBoard.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result typeOfBoard_getAll() {
        try {

            // TODO dá se cachovat - Pozor stejný seznam se nachází i Job_CheckCompilationLibraries
            // Získání seznamu
            // To co jsem tady napsal jen filtruje tahá ručně desky z cache pojendom - možná by šlo někde mít statické pole ID třeba
            // přímo v objektu Model_TypeOfBoard DB ignor a to používat a aktualizovat a statické pole nechat na samotné jave, aby si ji uchavaala v pam,ěti
            List<Model_TypeOfBoard> typeOfBoards_not_cached = Model_TypeOfBoard.find.query().where().eq("deleted", false).orderBy("UPPER(name) ASC").select("id").findList();

            List<Model_TypeOfBoard> typeOfBoards = new ArrayList<>();

            for (Model_TypeOfBoard typeOfBoard_not_cached : typeOfBoards_not_cached ) {
                typeOfBoards.add(Model_TypeOfBoard.getById(typeOfBoard_not_cached.id));
            }


            // Vrácení seznamu
            return  ok(Json.toJson(typeOfBoards));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get TypeOfBoard",
            tags = { "Type-Of-Board"},
            notes = "if you want get TypeOfBoard object by query = type_of_board_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_TypeOfBoard.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result typeOfBoard_get( String type_of_board_id) {
        try {

            // Kontrola validity objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.getById(type_of_board_id);
            if (typeOfBoard == null ) return notFound("TypeOfBoard type_of_board_id not found");

            // Kontorluji oprávnění
            if (! typeOfBoard.read_permission()) return forbiddenEmpty();

            // Vrácení validity objektu
            return ok(Json.toJson(typeOfBoard));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "upload TypeOfBoard picture",
            tags = { "Admin-Type-Of-Board"},
            notes = "Upload TypeOfBoard picture",
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(value = BodyParser.Json.class)//, TODO maxLength = 1024 * 1024 * 10)
    public Result typeOfBoard_uploadPicture(String type_of_board_id) {
        try {

            // Získání JSON
            final Form<Swagger_BASE64_FILE> form = formFactory.form(Swagger_BASE64_FILE.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_BASE64_FILE help = form.get();

            Model_TypeOfBoard type_of_board = Model_TypeOfBoard.getById(type_of_board_id);
            if (type_of_board == null) return notFound("Type of board does not exist");


            if (!type_of_board.edit_permission()) return forbiddenEmpty();

            logger.debug("typeOfBoard_uploadPicture update picture ");

            type_of_board.cache_picture_link = null;

            // Odebrání předchozího obrázku
            if (!(type_of_board.picture == null)) {

                logger.debug("typeOfBoard_uploadPicture picture is already there - system remove previous photo");
                Model_Blob fileRecord = type_of_board.picture;
                type_of_board.picture = null;
                type_of_board.update();
                fileRecord.delete();
            }

            //  data:image/png;base64,
            String[] parts = help.file.split(",");
            String[] type = parts[0].split(":");
            String[] dataType = type[1].split(";");

            logger.debug("typeOfBoard_uploadPicture:: Type     :: " + dataType[0]);
            logger.debug("typeOfBoard_uploadPicture:: Data     :: " + parts[1].substring(0, 10) + "......");

            String file_name =  UUID.randomUUID().toString() + ".png";
            String file_path =  type_of_board.get_Container().getName() + "/" + file_name;

            logger.debug("typeOfBoard_uploadPicture:: File Name:: " + file_name );
            logger.debug("typeOfBoard_uploadPicture:: File Path:: " + file_path );

            type_of_board.picture  = Model_Blob.uploadAzure_File( parts[1], dataType[0], file_name , file_path);
            type_of_board.update();


            return ok("Picture successfully uploaded");
        } catch (Exception e) {
            return internalServerError(e);
        }
    }

// Type Of Board - Batch ###############################################################################################

    @ApiOperation(value = "create TypeOfBoardBatch",
            tags = { "Type-Of-Board"},
            notes = "Create new Production Batch for Type Of Board",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_TypeOfBoardBatch_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_TypeOfBoard_Batch.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result typeOfBoardBatch_create(String type_of_board_id) {
        try {

            // Zpracování Json
            final Form<Swagger_TypeOfBoardBatch_New> form = formFactory.form(Swagger_TypeOfBoardBatch_New.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_TypeOfBoardBatch_New help = form.get();

            // Kontrola objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.getById(type_of_board_id);
            if (typeOfBoard == null ) return notFound("Model_TypeOfBoard type_of_board_id not found");

            // Tvorba objektu
            Model_TypeOfBoard_Batch batch = new Model_TypeOfBoard_Batch();
            batch.type_of_board = typeOfBoard;

            batch.revision = help.revision;
            batch.production_batch = help.production_batch;

            batch.date_of_assembly = help.date_of_assembly;

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
            if (!batch.create_permission()) return forbiddenEmpty();

            // Uložení objektu do DB
            batch.save();

            return created(Json.toJson(batch));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete TypeOfBoardBatch",
            tags = { "Type-Of-Board"},
            notes = "if you want delete TypeOfBoard Batch object by query = type_of_board_id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension(name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfBoard.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "TypeOfBoard_delete"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result typeOfBoardBatch_delete( String type_of_board_batch_id) {
        try {

            // Kontrola objektu
            Model_TypeOfBoard_Batch batch = Model_TypeOfBoard_Batch.getById(type_of_board_batch_id);
            if (batch == null ) return notFound("Model_TypeOfBoard_Batch type_of_board_batch_id not found") ;

            // Kontorluji oprávnění
            if (! batch.delete_permission()) return forbiddenEmpty();

            // Smazání objektu
            batch.delete();

            // Vrácení potvrzení
            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit TypeOfBoardBatch",
            tags = { "Type-Of-Board"},
            notes = "Create new Production Batch for Type Of Board",
            produces = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_TypeOfBoardBatch_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_TypeOfBoard_Batch.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result typeOfBoardBatch_edit(String type_of_board_id) {
        try {

            // Zpracování Json
            final Form<Swagger_TypeOfBoardBatch_New> form = formFactory.form(Swagger_TypeOfBoardBatch_New.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_TypeOfBoardBatch_New help = form.get();

            // Kontrola objektu
            Model_TypeOfBoard_Batch batch = Model_TypeOfBoard_Batch.getById(type_of_board_id);
            if (batch == null ) return notFound("Model_TypeOfBoard type_of_board_id not found");

            // Tvorba objektu
            batch.revision = help.revision;
            batch.production_batch = help.production_batch;

            batch.date_of_assembly = help.date_of_assembly;

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
            if (!batch.create_permission()) return forbiddenEmpty();

            // Uložení objektu do DB
            batch.save();

            return created(Json.toJson(batch));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

// BootLoader ##########################################################################################################

    @ApiOperation(value = "create Bootloader",
            tags = { "Admin-Type-Of-Board"},
            notes = "Create picture from TypeOfBoard",
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_BootLoader.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bootLoader_create(@ApiParam(value = "type_of_board_id", required = true) String type_of_board_id) {
        try {

            // Zpracování Json
            final Form<Swagger_BootLoader_New> form = formFactory.form(Swagger_BootLoader_New.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_BootLoader_New help = form.get();

            Model_TypeOfBoard type_of_board = Model_TypeOfBoard.getById(type_of_board_id);
            if (type_of_board == null) return notFound("Type_of_board_not_found");

            String identifier = help.version_identifier.replaceAll("\\s+", "");

            if (Model_BootLoader.find.query().where().eq("version_identifier", identifier).eq("type_of_board.id", type_of_board.id).findOne() != null)
                return badRequest("Version format is not unique!");

            Model_BootLoader boot_loader = new Model_BootLoader();
            boot_loader.name = help.name;
            boot_loader.changing_note =  help.changing_note;
            boot_loader.description = help.description;
            boot_loader.version_identifier = identifier;
            boot_loader.type_of_board = type_of_board;

            if (!boot_loader.create_permission()) return forbiddenEmpty();
            boot_loader.save();

            // Vracím seznam
            return ok(Json.toJson(boot_loader));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit Bootloader",
            tags = { "Admin-Type-Of-Board"},
            notes = "Create picture from TypeOfBoard",
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_BootLoader.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
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

            if (!boot_loader.edit_permission()) return forbiddenEmpty();

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
            tags = { "Admin-Type-Of-Board"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bootLoader_delete(String boot_loader_id) {
        try {

            Model_BootLoader boot_loader = Model_BootLoader.getById(boot_loader_id);
            if (boot_loader == null) return notFound("BootLoader not found");

            if (!boot_loader.delete_permission()) return forbiddenEmpty();

            if (!boot_loader.boards.isEmpty()) return badRequest("Bootloader is already used on some Board. Cannot be deleted.");

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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
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

            if (!boot_loader.edit_permission()) return forbiddenEmpty();

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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Objects not found",         response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Empty.class)
    public Result bootLoader_markAsMain(String boot_loader_id) {
        try {

            Model_BootLoader boot_loader = Model_BootLoader.getById(boot_loader_id);
            if (boot_loader == null) return notFound("BootLoader boot_loader_id not found");

            if (!boot_loader.edit_permission()) return forbiddenEmpty();
            if (boot_loader.file == null) return badRequest("Required bootloader object with file");

            if (boot_loader.get_main_type_of_board() != null) return badRequest("Bootloader is Already Main");

            Model_BootLoader old_main_not_cached = Model_BootLoader.find.query().where().eq("main_type_of_board.id", boot_loader.type_of_board.id).select("id").findOne();

            if (old_main_not_cached != null) {
                Model_BootLoader old_main = Model_BootLoader.getById(old_main_not_cached.id.toString());
                if (old_main != null) {
                    old_main.main_type_of_board = null;
                    old_main.cache_main_type_of_board_id = null;
                    old_main.update();
                }
            }

            boot_loader.main_type_of_board = boot_loader.get_type_of_board();
            boot_loader.cache_main_type_of_board_id =  boot_loader.main_type_of_board.id;
            boot_loader.update();

            // Update Chache
            boot_loader.get_type_of_board().cache_main_bootloader_id = boot_loader.id;

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
    @ApiResponses(value = {
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
            if (boards.isEmpty()) return notFound("Board not found");



            List<WS_Help_Hardware_Pair> board_for_update = new ArrayList<>();

            for (Model_Hardware hardware : boards) {

                if (!hardware.read_permission()) return forbidden("You have no permission for Device " + hardware.id);

                WS_Help_Hardware_Pair pair = new WS_Help_Hardware_Pair();
                pair.hardware = hardware;

                if (help.bootloader_id != null) {

                    pair.bootLoader = Model_BootLoader.getById(help.bootloader_id);
                    if (pair.bootLoader == null) return notFound("BootLoader not found");

                } else {
                    pair.bootLoader = Model_BootLoader.find.query().where().eq("main_type_of_board.boards.id", hardware.id).findOne();
                }

                board_for_update.add(pair);
            }

            if (!board_for_update.isEmpty()) {
                new Thread( () -> {
                    try {

                        Model_ActualizationProcedure procedure = Model_Hardware.create_update_procedure(Enum_Firmware_type.BOOTLOADER, UpdateType.MANUALLY_BY_USER_INDIVIDUAL, board_for_update);
                        procedure.execute_update_procedure();

                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }).start();
            }


            // Vracím Json
            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    ///###################################################################################################################*/

    @ApiOperation(value = "create Board manual Registration",
            tags = { "Admin-Board"},
            notes = "This Api is using only for developing mode, for registration of our Board - in future it will be used only by machine in factory or " +
                    "boards themselves with \"registration procedure\". Hardware is not allowed to delete! Only deactivate. Classic User can only register that to own " +
                    "project or own to account",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                 @Extension( name = "permission_required", properties = {
                         @ExtensionProperty(name = "TypeOfBoard.register_new_device_permission", value = "true"),
                         @ExtensionProperty(name = "Static Permission key", value = "Board_create"),
                 }),
            }
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
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_Hardware.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_create_manual() {
        try {

            // Zpracování Json
            final Form<Swagger_Board_New_Manual> form = formFactory.form(Swagger_Board_New_Manual.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_Board_New_Manual help = form.get();

            // Kotrola objektu
            if (Model_Hardware.getByFullId(help.full_id) != null) return badRequest("Board is already registered");

            // Kotrola objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.getById( help.type_of_board_id  );
            if (typeOfBoard == null ) return notFound("TypeOfBoard type_of_board_id not found");

            // Kontorluji oprávnění
            if (!typeOfBoard.register_new_device_permission()) return forbiddenEmpty();

            Model_Hardware board = new Model_Hardware();
            board.full_id = help.full_id;
            board.is_active = false;
            board.type_of_board = typeOfBoard;
            board.hash_for_adding = Model_Hardware.generate_hash();

            // Uložení desky do DB
            board.save();

            // Vracím seznam zařízení k registraci
            return created(Json.toJson(board));

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
            code = 201,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfBoard.register_new_device_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Board_create"),
                    }),
            }
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
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Swagger_Hardware_New_Settings_Result.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_create_garfield() {
        try {

            // Zpracování Json
            final Form<Swagger_Board_New_Garfield> form = formFactory.form(Swagger_Board_New_Garfield.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_Board_New_Garfield help = form.get();

            // Kotrola objektu
            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.getById(help.type_of_board_id);
            if (typeOfBoard == null) return notFound("TypeOfBoard type_of_board_id not found");

            // Kontorluji oprávnění
            if (!typeOfBoard.register_new_device_permission()) return forbiddenEmpty();

            Model_TypeOfBoard_Batch batch = Model_TypeOfBoard_Batch.getById(help.type_of_board_batch_id);
            if (batch == null) return notFound("TypeOfBoard_Batch type_of_board_batch_id not found");

            // Kontrola Objektu
            Model_Garfield garfield = Model_Garfield.getById(help.garfield_station_id);
            if (garfield == null) return notFound("Garfield Station not found");

            String mqtt_password_not_hashed = UUID.randomUUID().toString();
            String mqtt_username_not_hashed = UUID.randomUUID().toString();

            Model_Hardware board = Model_Hardware.getByFullId(help.full_id);
            if (board == null) {

                logger.warn("board_create_garfield - device not found in local DB, registering board, id: {}", help.full_id);

                // Try to Find it on Registration Authority
                if (Hardware_Registration_Authority.check_if_value_is_registered(help.full_id, "board_id")) {
                    logger.error("Device is already Registred ID: {}", help.full_id);
                    return badRequest("Device is already Registred ID: " + help.full_id);
                }
                if (Hardware_Registration_Authority.check_if_value_is_registered(batch.get_nextMacAddress_just_for_check(), "mac_address")) {
                    logger.error("Next Mac Address fot this device is already registered. Check It. Mac Address:: {}", help.full_id);
                    return badRequest("Next Mac Address fot this device is already registered. Check It Mac Address:: " +  help.full_id);
                }

                board = new Model_Hardware();
                board.full_id = help.full_id;
                board.is_active = false;
                board.type_of_board = typeOfBoard;
                board.batch_id = batch.id.toString();
                board.mac_address = batch.get_new_MacAddress();
                board.mqtt_username = BCrypt.hashpw(mqtt_username_not_hashed, BCrypt.gensalt());
                board.mqtt_password = BCrypt.hashpw(mqtt_password_not_hashed, BCrypt.gensalt());
                board.hash_for_adding = Model_Hardware.generate_hash();

                if (Hardware_Registration_Authority.register_device(board, typeOfBoard, batch)) {
                    board.save();
                } else {
                    Model_Hardware board_repair_from_authority = Model_Hardware.getByFullId(help.full_id);
                    if (board_repair_from_authority != null) {
                        board = board_repair_from_authority;
                    } else {
                       return notFound("Registration Authority Fail!!");
                    }
                }

                board.refresh();
            } else {
                board.mqtt_username = BCrypt.hashpw(mqtt_username_not_hashed, BCrypt.gensalt());
                board.mqtt_password = BCrypt.hashpw(mqtt_password_not_hashed, BCrypt.gensalt());
                board.update();
            }

            // Vytisknu štítky

            Printer_Api api = new Printer_Api();

            // Label 62 mm
            try {
                // Test for creating - Controlling all prerequisites and requirements
                new Label_62_mm_package(board, batch, garfield);
            } catch (IllegalArgumentException e) {
                return badRequest("Something is wrong: " + e.getMessage());
            }

            Label_62_mm_package label_62_mmPackage = new Label_62_mm_package(board, batch, garfield);
            api.printFile(garfield.print_sticker_id, 1, "Garfield Print Label", label_62_mmPackage.get_label(), null);

            // Label qith QR kode on Ethernet connector
            Label_62_split_mm_Details label_12_mm_details = new Label_62_split_mm_Details(board);
            api.printFile(garfield.print_label_id_1, 1, "Garfield Print QR Hash", label_12_mm_details.get_label(), null);

            if (typeOfBoard.connectible_to_internet) {

                // Najdu backup_server
                Model_HomerServer backup_server = Model_HomerServer.find.query().where().eq("server_type", HomerType.BACKUP).findOne();
                if (backup_server == null) return notFound("Backup server not found!!!");

                // Najdu Main_server
                Model_HomerServer main_server = Model_HomerServer.find.query().where().eq("server_type", HomerType.MAIN).findOne();
                if (main_server == null) return notFound("Main server not found!!!");

                DM_Board_Bootloader_DefaultConfig conf = board.bootloader_core_configuration();

                Swagger_Hardware_New_Settings_Result_Configuration configuration = new Swagger_Hardware_New_Settings_Result_Configuration();
                configuration.normal_mqtt_hostname = main_server.server_url;
                configuration.normal_mqtt_port = main_server.mqtt_port;
                configuration.mqtt_username = mqtt_password_not_hashed;
                configuration.mqtt_password = mqtt_username_not_hashed;
                configuration.backup_mqtt_hostname = backup_server.server_url;
                configuration.backup_mqtt_port = backup_server.mqtt_port;
                configuration.mac = board.mac_address;
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
                result.full_id = board.full_id;
                result.configuration = configuration;

                return created(Json.toJson(result));
            }

            // Vracím seznam zařízení k registraci
            return created(Json.toJson(board));
        } catch (IllegalCharsetNameException e) {
            return badRequest("All Mac Address used");
        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get Boards for Ide Operation",
            tags = { "Board"},
            notes = "List of boards under Project for fast upload of Firmware to Board from Web IDE",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Board_for_fast_upload_detail.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result board_getForFastUpload( String project_id) {
        try {

            // Kotrola objektu
            Model_Project project = Model_Project.getById(project_id);
            if (project == null ) return notFound("Project project not found");

            // Kontrola oprávnění
            if (!project.edit_permission()) return forbiddenEmpty();

            // Vyhledání seznamu desek na které lze nahrát firmware - okamžitě
            List<Model_Hardware> boards = Model_Hardware.find.query().where().eq("type_of_board.connectible_to_internet", true).eq("project.id", project_id).findList();

            List<Swagger_Board_for_fast_upload_detail> list = new ArrayList<>();

            for (Model_Hardware board : boards ) {
                list.add(board.get_short_board_for_fast_upload());
            }


            // Vrácení upravenéh objektu
            return ok(Json.toJson(list));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit Board personal description",
            tags = { "Board"},
            notes = "Used for add descriptions by owners. \"Persons\" who registred \"Board\" to own \"Projec\" ",
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Hardware.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_update_description( String board_id) {
        try {

            // Zpracování Json
            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_NameAndDescription help = form.get();

            // Kotrola objektu
            Model_Hardware board = Model_Hardware.getById(board_id);
            if (board == null ) return notFound("Board not found");

            // Kontrola oprávnění
            if (!board.edit_permission()) return forbiddenEmpty();

            // Uprava desky
            board.name = help.name;
            board.description = help.description;

            // Uprava objektu v databázi
            board.update();

            // Synchronizace s Homer serverem
            board.set_alias(board.name);

            // Vrácení upravenéh objektu
            return ok(Json.toJson(board));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit Board developers parameters",
            tags = { "Board"},
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Hardware.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_update_parameters( String board_id) {
        try {

            // Zpracování Json
            final Form<Swagger_Board_Developer_parameters> form = formFactory.form(Swagger_Board_Developer_parameters.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_Board_Developer_parameters help = form.get();

            // Kotrola objektu
            Model_Hardware board = Model_Hardware.getById(board_id);
            if (board == null ) return notFound("Board board_id not found");

            // Kontrola oprávnění
            if (!board.edit_permission()) return forbiddenEmpty();

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
                            dataType = "utilities.swagger.input.Swagger_UploadBinaryFileToBoard",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result uploadCompilationToBoard() {
        try {

            // Zpracování Json
            Form<Swagger_UploadBinaryFileToBoard> form = formFactory.form(Swagger_UploadBinaryFileToBoard.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_UploadBinaryFileToBoard help = form.get();


            List<WS_Help_Hardware_Pair> b_pairs = new ArrayList<>();

            if (help.board_pairs.isEmpty()) return badRequest("List is Empty");

            for (Swagger_Board_CProgram_Pair board_update_pair : help.board_pairs) {

                // Ověření objektu
                Model_Version c_program_version = Model_Version.getById(board_update_pair.c_program_version_id);
                if (c_program_version == null) return notFound("Version_Object version_id not found");

                //Zkontroluji validitu Verze zda sedí k C_Programu
                if (c_program_version.get_c_program() == null) return badRequest("Version_Object its not version of C_Program");

                // Zkontroluji oprávnění
                if (!c_program_version.get_c_program().read_permission()) return forbiddenEmpty();

                //Zkontroluji validitu Verze zda sedí k C_Programu
                if (c_program_version.compilation == null) return badRequest("Version_Object its not version of C_Program - Missing compilation File");

                // Ověření zda je kompilovatelná verze a nebo zda kompilace stále neběží
                if (c_program_version.compilation.status != CompilationStatus.SUCCESS) return badRequest("You cannot upload code in state:: " + c_program_version.compilation.status.name());

                //Zkontroluji zda byla verze už zkompilována
                if (!c_program_version.compilation.status.name().equals(CompilationStatus.SUCCESS.name())) return badRequest("The program is not yet compiled & Restored");

                // Kotrola objektu
                Model_Hardware board = Model_Hardware.getById(board_update_pair.board_id);
                if (board == null) return notFound("Board board_id not found");

                // Kontrola oprávnění
                if (!board.edit_permission()) return forbiddenEmpty();


                WS_Help_Hardware_Pair b_pair = new WS_Help_Hardware_Pair();
                b_pair.hardware = board;
                b_pair.c_program_version = c_program_version;

                b_pairs.add(b_pair);

            }

            if (!b_pairs.isEmpty()) {
                new Thread( () -> {
                    try {

                        Model_ActualizationProcedure procedure = Model_Hardware.create_update_procedure(Enum_Firmware_type.FIRMWARE, UpdateType.MANUALLY_BY_USER_INDIVIDUAL, b_pairs);
                        procedure.execute_update_procedure();

                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }).start();
            }


            // Vracím odpověď
            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "update Board Backup",
            tags = { "Board"},
            notes = "",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Board_Backup_settings",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_updateBackup() {
        try {

            // Zpracování Json
            final Form<Swagger_Board_Backup_settings> form = formFactory.form(Swagger_Board_Backup_settings.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_Board_Backup_settings help = form.get();

            if (help.board_backup_pair_list.isEmpty()) return notFound("List is Empty");


            // Seznam Hardwaru k updatu
            List<WS_Help_Hardware_Pair> board_pairs = new ArrayList<>();

            for (Swagger_Board_Backup_settings.Board_backup_pair board_backup_pair : help.board_backup_pair_list) {

                // Kotrola objektu
                Model_Hardware board = Model_Hardware.getById(board_backup_pair.board_id);
                if (board == null) return notFound("Board board_id not found");

                // Kontrola oprávnění
                if (!board.edit_permission()) return forbiddenEmpty();

                // Pokud je nastaven autobackup na true
                if (board_backup_pair.backup_mode) {

                    // Na devicu byla nastavená statická - Proto je potřeba jí odstranit a nahradit autobackupem
                    if (!board.backup_mode) {

                        DM_Board_Bootloader_DefaultConfig config = board.bootloader_core_configuration();
                        config.autobackup = board_backup_pair.backup_mode;
                        board.update_bootloader_configuration(config);

                        logger.debug("Controller_Board:: board_update_backup:: To TRUE:: Board Id: {} has own Static Backup - Removing static backup procedure required", board_backup_pair.board_id);

                        board.actual_backup_c_program_version = null;
                        board.backup_mode = true;
                        board.update();

                        WS_Message_Hardware_set_settings result = board.set_auto_backup();

                    // Na devicu už autobackup zapnutý byl - nic nedělám jen překokontroluji???
                    } else {

                        logger.debug("Controller_Board:: board_update_backup:: To TRUE:: Board Id: {} has already sat as a dynamic Backup", board_backup_pair.board_id);

                        WS_Message_Hardware_set_settings result = board.set_auto_backup();
                        if (result.status.equals("success")) {
                            logger.debug("Controller_Board:: board_update_backup:: To TRUE:: Board Id: {} Success of setting of dynamic backup", board_backup_pair.board_id);

                            // Toto je pro výjmečné případy - kdy při průběhu updatu padne tyrion a transakce není komplentí
                            if ( board.actual_backup_c_program_version != null) {
                                board.actual_backup_c_program_version = null;
                                board.update();
                            }
                        }
                    }

                // Autobacku je statický
                } else {

                    if (board_backup_pair.c_program_version_id == null || board_backup_pair.c_program_version_id.equals("")) return badRequest("If backup_mode is set to false, c_program_version_id is required");

                    logger.debug("Controller_Board:: board_update_backup:: To FALSE:: Board Id: {} has dynamic Backup or already set static backup", board_backup_pair.board_id);

                    // Uprava desky na statický backup
                    Model_Version c_program_version = Model_Version.getById(board_backup_pair.c_program_version_id);
                    if (c_program_version == null) return notFound("Version_Object c_program_version_id not found");

                    //Zkontroluji validitu Verze zda sedí k C_Programu
                    if (c_program_version.get_c_program() == null) return badRequest("Version_Object its not version of C_Program");

                    // Zkontroluji oprávnění
                    if (!c_program_version.get_c_program().read_permission()) return forbiddenEmpty();

                    //Zkontroluji validitu Verze zda sedí k C_Programu
                    if (c_program_version.compilation == null) return badRequest("Version_Object its not version of C_Program - Missing compilation File");

                    // Ověření zda je kompilovatelná verze a nebo zda kompilace stále neběží
                    if (c_program_version.compilation.status != CompilationStatus.SUCCESS) return badRequest("You cannot upload code in state:: " + c_program_version.compilation.status.name());

                    //Zkontroluji zda byla verze už zkompilována
                    if (!c_program_version.compilation.status.name().equals(CompilationStatus.SUCCESS.name())) return badRequest("The program is not yet compiled & Restored");

                    WS_Help_Hardware_Pair b_pair = new WS_Help_Hardware_Pair();
                    b_pair.hardware = board;
                    b_pair.c_program_version = c_program_version;

                    board_pairs.add(b_pair);

                    if (!board.backup_mode) {
                        board.actual_backup_c_program_version = null;
                        board.backup_mode = false;
                        board.update();
                    }

                    DM_Board_Bootloader_DefaultConfig config = board.bootloader_core_configuration();
                    config.autobackup = board_backup_pair.backup_mode;
                    board.update_bootloader_configuration(config);
                }

            }

            if (!board_pairs.isEmpty()) {
                new Thread( () -> {

                    try {
                        Model_ActualizationProcedure procedure = Model_Hardware.create_update_procedure(Enum_Firmware_type.BACKUP, UpdateType.MANUALLY_BY_USER_INDIVIDUAL, board_pairs);
                        procedure.execute_update_procedure();

                    } catch (Exception e) {
                        logger.internalServerError(e);
                    }
                }).start();
            }


            // Vrácení potvrzení
            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get Boards with filter parameters",
            tags = { "Board"},
            notes = "Get List of boards. Acording by permission - system return only hardware from project, where is user owner or" +
                    " all boards if user have static Permission key",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Hardware_read"),
                    }),
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Board_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Board_List.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_getByFilter(Integer page_number) {
        try {

            // Zpracování Json
            final Form<Swagger_Board_Filter> form = formFactory.form(Swagger_Board_Filter.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_Board_Filter help = form.get();

            // Tvorba parametru dotazu
            Query<Model_Hardware> query = Ebean.find(Model_Hardware.class);


            // If Json contains TypeOfBoards list of id's
            if (help.type_of_board_ids != null && !help.type_of_board_ids.isEmpty()) {
                query.where().in("type_of_board.id", help.type_of_board_ids);
            }

            // If contains confirms
            if (help.active != null) {
                query.where().eq("is_active", help.active.equals("true"));
            }

            if (help.projects != null && !help.projects.isEmpty()) {
                query.where().in("project.id", help.projects);
            }

            if (help.producers != null) {
                query.where().in("type_of_board.producer.id", help.producers);
            }

            if (help.processors != null) {
                query.where().in("type_of_board.processor.id", help.processors);
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
            Swagger_Board_List result = new Swagger_Board_List(query, page_number);

            // Vracím seznam
            return ok(Json.toJson(result));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "upload Board picture",
            tags = { "Board"},
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_Hardware.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(value = BodyParser.Json.class) // TODO , maxLength = 1024 * 1024 * 10)
    public Result board_uploadPicture(String board_id) {
        try {

            // Získání JSON
            final Form<Swagger_BASE64_FILE> form = formFactory.form(Swagger_BASE64_FILE.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_BASE64_FILE help = form.get();

            Model_Hardware board = Model_Hardware.getById(board_id);
            if (board == null) return notFound("Board does not exist");

            if (!board.edit_permission()) return forbiddenEmpty();

            if (board.get_project() == null ) return badRequest("Hardware is not in project!");


            // Odebrání předchozího obrázku
            if (board.picture != null) {
                logger.debug("person_uploadPicture:: Removing previous picture");
                Model_Blob fileRecord = board.picture;
                board.picture = null;
                board.update();
                fileRecord.delete();
            }

            //  data:image/png;base64,
            String[] parts = help.file.split(",");
            String[] type = parts[0].split(":");
            String[] dataType = type[1].split(";");

            logger.debug("person_uploadPicture:: Data Type  :: " + dataType[0] + ":::");
            logger.debug("person_uploadPicture:: Data       :: " + parts[1].substring(0, 10) + "......");

            String file_name =  UUID.randomUUID().toString() + ".png";
            String file_path =  board.get_path() + "/" + file_name;


            board.picture =  Model_Blob.uploadAzure_File( parts[1], dataType[0], file_name , file_path);
            board.update();

            return ok(Json.toJson(board));
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",               response = Swagger_Hardware_New_Password.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result board_generate_new_password(String board_id) {
        try {

            Model_Hardware board = Model_Hardware.getById(board_id);
            if (board == null ) return notFound("Board board_id not found");
            if (!board.edit_permission()) return forbiddenEmpty();


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
            tags = { "Board"},
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message",    response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_redirect_to_server(String board_id) {
        try {

            // Získání JSON
            final Form<Swagger_Board_Server_Redirect> form = formFactory.form(Swagger_Board_Server_Redirect.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_Board_Server_Redirect help = form.get();

            System.out.println("board_redirect_to_server:: Příjem zprávy:: " + Json.toJson(help));

            Model_Hardware board = Model_Hardware.getById(board_id);
            if (board == null) return notFound("Board does not exist");
            if (!board.edit_permission()) {
                System.out.println(" board.edit_permission - false!");
                return forbiddenEmpty();
            }

            // Jedná se o přesměrování na server v rámci stejné hierarchie - na server co mám v DB
            if (help.server_id != null) {

                System.out.println("Bude se přesměrovávat z databáze");

                Model_HomerServer server = Model_HomerServer.getById(help.server_id);
                if (server == null) return notFound("Board does not exist");
                if (!server.read_permission()) {
                    System.out.println("!server.read_permission() - false!");
                    return forbiddenEmpty();
                }

                board.device_relocate_server(server);

            // Jedná se o server mimo náš svět - například z dev na stage, nebo z produkce na dev
            } else {
                if (help.server_port == null || help.server_url == null) {
                    return badRequest("its required send server_id  or server_url + server_port ");
                }


                WS_Message_Hardware_change_server response = board.device_relocate_server(help.server_url, help.server_port);
                if (response.status.equals("success")) {
                    return okEmpty();
                } else {
                    return badRequest("Cloud Device Execution Error: " + response.error_message);
                }

            }

            return okEmpty();
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_command_execution() {
        try {

            // Získání JSON
            final Form<Swagger_Board_Command> form = formFactory.form(Swagger_Board_Command.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_Board_Command help = form.get();


            Model_Hardware board = Model_Hardware.getById(help.board_id);
            if (board == null ) return notFound("Board board_id not found");
            if (!board.edit_permission()) return forbiddenEmpty();

            if (help.command == null ) return notFound("Board command not recognized");
            board.execute_command(help.command, true);

            return okEmpty();
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result board_removePicture(String board_id) {
        try {

            Model_Hardware board = Model_Hardware.getById(board_id);
            if (board == null ) return notFound("Board board_id not found");

            if (!(board.picture == null)) {
                board.picture.delete();
                board.picture = null;
                board.update();
            } else {
                return badRequest("There is no picture to remove.");
            }

            return ok("Picture successfully removed");
        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "deactivate Board",
            tags = { "Board"},
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Hardware.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result board_deactivate( String board_id) {
        try {

            // Kotrola objektu
            Model_Hardware board = Model_Hardware.getById(board_id);
            if (board == null ) return notFound("Board board_id not found");

            // Kontrola oprávnění
            if (board.update_permission()) return forbiddenEmpty();

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
            tags = { "Board"},
            notes = "if you want get Board object by query = board_id. User can get only boards from project, whitch " +
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Hardware.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result board_get(String board_id) {
        try {

            // Kotrola objektu
            Model_Hardware board = Model_Hardware.getById(board_id);
            if (board == null ) return notFound("Board board_id not found");

            // Kontrola oprávnění
            if (!board.read_permission()) return forbiddenEmpty();

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
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Hardware_read"),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Board_Registration_Status.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result board_check(String hash_for_adding) {
        try {

            Swagger_Board_Registration_Status status = new Swagger_Board_Registration_Status();

            // Kotrola objektu
            Model_Hardware board_not_cached = Model_Hardware.find.query().where().eq("hash_for_adding", hash_for_adding).select("id").findOne();
            if (board_not_cached == null) {
                status.status = BoardRegistrationStatus.NOT_EXIST;
                return ok(Json.toJson(status));
            }

            Model_Hardware board = Model_Hardware.getById(board_not_cached.id);

            if (board == null ) {
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

    @ApiOperation(value = "connect Board with Project",
            tags = { "Board"},
            notes = "This Api is used by Users for connection of Board with their Project",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_Board_Registration_To_Project",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Hardware.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_connectProject() {
        try {

            // Zpracování Json
            final Form<Swagger_Board_Registration_To_Project> form = formFactory.form(Swagger_Board_Registration_To_Project.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_Board_Registration_To_Project help = form.get();

            logger.debug("board_connectProject: registering new device with hash: {}", help.hash_for_adding);

            // Kotrola objektu - NAjdu v Databázi
            Model_Hardware board_not_cache = Model_Hardware.find.query().where().eq("hash_for_adding", help.hash_for_adding).select("id").findOne();
            if (board_not_cache == null) return notFound("Board board_id not found");

            //Vytáhnu přes Cache Manager
            Model_Hardware board = Model_Hardware.getById(board_not_cache.id);
            if (board == null) return notFound("Board not found");
            if (!board.first_connect_permission()) return badRequest("Board is already registered");

            // Kotrola objektu
            Model_Project project = Model_Project.getById(help.project_id);
            if (project == null) return notFound("Project not found");
            if (!project.update_permission()) return forbiddenEmpty();

            // Pouze získání aktuálního stavu do Cache paměti ID listu
            if (board.cache_hardware_group_ids == null) {
                board.get_hardware_groups();
            }

            board.date_of_user_registration = new Date();
            board.cache_project_id = project.id;
            board.project = project;
            board.update();

            project.cache_hardware_ids.add(board.id);

            if (!help.group_ids.isEmpty()) {

                for (String board_group_id : help.group_ids) {
                    UUID id = UUID.fromString(board_group_id);
                    Model_HardwareGroup group = Model_HardwareGroup.getById(id);
                    if (group == null) return notFound("BoardGroup not found");
                    if (!group.update_permission()) return forbiddenEmpty();

                    // Přidám všechny, které nejsou už součásti cache_hardware_group_ids
                    if (!board.cache_hardware_group_ids.contains(id)) {

                        board.cache_hardware_group_ids.add(id);
                        board.board_groups.add(group);
                        group.cache_group_size += 1;
                    }
                }
            }

            project.cache_hardware_ids.add(board.id);
            board.update();

            // vrácení objektu
            return ok(Json.toJson(board));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "disconnect Board from Project",
            tags = { "Board"},
            notes = "This Api is used by Users for disconnection of Board from their Project, its not meaning that Board is removed from system, only disconnect " +
                    "and another user can registred that (connect that with different account/project etc..)",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "Board_Disconnection", value = Model_Hardware.disconnection_permission_docs),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value = "Hardware_update"),
                    }),
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Hardware.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result board_disconnectProject(  String board_id) {
        try {

            // Kontrola objektu
            // !!! pozor vyjímka!!!!
            Model_Hardware board = Model_Hardware.getById(board_id);
            if (board == null ) return notFound("Board board_id not found");

            // Kontrola oprávnění
            if (!board.update_permission()) return forbiddenEmpty();

            if (board.get_project() == null) {
                return notFound("Board already removed");
            }

            Model_Project project = board.get_project();
            project.cache_hardware_ids.remove(board_id);

            // Odstraním vazbu
            board.project = null;

            // uložím do databáze
            board.update();

            project.refresh();

            // vracím upravenou hodnotu
            return ok(Json.toJson(board));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get B_Program all details for integration",
            tags = {"Blocko", "B_Program"},
            notes = "get all boards that user can integrate to Blocko program",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Project_read_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Boards_For_Blocko.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result board_allDetailsForBlocko(  String project_id) {
        try {

            // Kontrola objektu
            Model_Project project = Model_Project.getById(project_id);
            if (project == null) return notFound("Project project_id not found");

            // Kontrola oprávnění
            if (! project.read_permission()) return forbiddenEmpty();

            // Získání objektu
            Swagger_Boards_For_Blocko boards_for_blocko = new Swagger_Boards_For_Blocko();
            boards_for_blocko.add_M_Projects(project.get_m_projects_not_deleted());
            boards_for_blocko.add_C_Programs(project.get_c_programs_not_deleted());

            boards_for_blocko.boards.addAll(project.get_project_boards_not_deleted());


            boards_for_blocko.type_of_boards = Model_TypeOfBoard.find.query().where().eq("boards.project.id", project.id).findList();


            // Vrácení objektu
            return ok(Json.toJson(boards_for_blocko));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "Remove Hardware from Database - Only for Administrators", hidden = true)
    public Result board_delete(String board_id) {
        try {

            // Kontrola objektu
            Model_Hardware board = Model_Hardware.getById(board_id);
            if (board == null ) return notFound("Board not found");

            // Kontrola oprávnění
            if (!board.delete_permission()) return forbiddenEmpty();

            if (board.project != null || board.date_of_user_registration != null)
                return badRequest("Board is already in use.");

            board.delete();

            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

///###################################################################################################################*/

    @ApiOperation(value = "create BoardGroup",
            tags = { "BoardGroup"},
            notes = "Create Board Group",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_HardwareGroup.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_group_create() {
        try {

            // Zpracování Json
            final Form<Swagger_NameAndDesc_ProjectIdRequired> form = formFactory.form(Swagger_NameAndDesc_ProjectIdRequired.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_NameAndDesc_ProjectIdRequired help = form.get();

            Model_Project project = Model_Project.getById(help.project_id);
            if (project == null) return notFound("Model_Project not found");

            if (Model_HardwareGroup.find.query().where().eq("name", help.name).eq("project.id", project.id).findOne() != null) {
                return badRequest("Group name must be a unique!");
            }

            Model_HardwareGroup group = new Model_HardwareGroup();
            group.name = help.name;
            group.description = help.description;
            group.project = project;

            if (!group.create_permission()) return forbiddenEmpty();
            group.save();

            // Vracím seznam
            return ok(Json.toJson(group));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "edit BoardGroup",
            tags = { "BoardGroup"},
            notes = "update BoardGroup",
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Model_HardwareGroup.class),
            @ApiResponse(code = 404, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_group_update(String board_group_id) {
        try {

            // Zpracování Json
            final Form<Swagger_NameAndDescription> form = formFactory.form(Swagger_NameAndDescription.class).bindFromRequest();
            if (form.hasErrors())return invalidBody(form.errorsAsJson());
            Swagger_NameAndDescription help = form.get();

            Model_HardwareGroup group = Model_HardwareGroup.getById(board_group_id);
            if (group == null) return notFound("HardwareGroup not found");

            if (!group.edit_permission()) return forbiddenEmpty();

            group.name = help.name;
            group.description = help.description;

            group.update();

            return ok(Json.toJson(group));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "update BoardGroup Device List",
            tags = { "BoardGroup"},
            notes = "update BoardGroup add or remove device list",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_Hardware_Group_DeviceListEdit",
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Result_Ok.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result board_group_update_device_list() {
        try {

            // Zpracování Json
            final Form<Swagger_Hardware_Group_DeviceListEdit> form = formFactory.form(Swagger_Hardware_Group_DeviceListEdit.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_Hardware_Group_DeviceListEdit help = form.get();

            if (help.device_synchro != null) {

                Model_Hardware board = Model_Hardware.getById(help.device_synchro.device_id);
                if (board == null) return notFound("Board ID not found");
                if (!board.update_permission()) return forbiddenEmpty();

                logger.debug("board_group_update_device_list - board: {}", board.id);


                logger.debug("board_group_update_device_list - cached groups: {}", Json.toJson(board.get_hardware_groups_ids()));

                List<UUID> hw_list = board.get_hardware_groups_ids();
                // Cyklus pro přidávání
                for (UUID board_group_id: help.device_synchro.group_ids) {

                    // Přidám všechny, které nejsou už součásti cache_hardware_group_ids
                    if (!hw_list.contains(board_group_id)) {

                        logger.debug("board_group_update_device_list - adding group {}", board_group_id );

                        Model_HardwareGroup group = Model_HardwareGroup.getById(board_group_id);
                        if (group == null) return notFound("BoardGroup not found");
                        if (!group.update_permission()) return forbiddenEmpty();

                        board.get_hardware_groups_ids().add(board_group_id);
                        board.board_groups.add(group);
                        group.cache_group_size +=1;
                    }
                }

                // Cyklus pro mazání java.util.ConcurrentModificationException
                for (Iterator<UUID> it = board.cache_hardware_group_ids.iterator(); it.hasNext(); ) {

                    UUID board_group_id = it.next();

                    // Není a tak mažu
                    if (!help.device_synchro.group_ids.contains(board_group_id.toString())) {

                        logger.debug("board_group_update_device_list - removing group {}", board_group_id );

                        Model_HardwareGroup group = Model_HardwareGroup.getById(board_group_id);
                        if (group == null) return notFound("BoardGroup not found");
                        if (!group.update_permission()) return forbiddenEmpty();

                        board.board_groups.remove(group);
                        group.cache_group_size -=1;
                        it.remove();
                    }
                }

                board.set_hardware_groups_on_hardware(board.get_hardware_groups_ids(),  Enum_type_of_command.SET);
                board.update();
            }

            if (help.group_synchro != null) {

                Model_HardwareGroup group = Model_HardwareGroup.getById(help.group_synchro.group_id);
                if (!group.update_permission()) return forbiddenEmpty();

                for (String board_id: help.group_synchro.device_ids) {
                    Model_Hardware board = Model_Hardware.getById(board_id);
                    if (!board.update_permission()) return forbiddenEmpty();

                    board.cache_hardware_group_ids.add(UUID.fromString(help.group_synchro.group_id));
                    board.board_groups.add(group);
                }

                group.refresh();
            }

            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "delete BoardGroup",
            tags = { "BoardGroup"},
            notes = "delete BoardGroup",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Result_Ok.class),
            @ApiResponse(code = 404, message = "Objects not found - details in message", response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result board_group_delete(String board_group_id) {
        try {

            Model_HardwareGroup group = Model_HardwareGroup.getById(board_group_id);
            if (group == null) return notFound("BootLoader not found");

            if (!group.delete_permission()) return forbiddenEmpty();

            group.delete();

            return okEmpty();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get_List BoardGroup From Project",
            tags = { "Type-Of-Board"},
            notes = "get List of BoardGroup from Project",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Model_HardwareGroup.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error", response = Result_InternalServerError.class)
    })
    public Result board_group_get_list_project(String project_id) {
        try {

            // Kontrola validity objektu
            Model_Project project = Model_Project.getById(project_id);
            if (project == null ) return notFound("Project project_id not found");

            // Kontorluji oprávnění
            if (! project.read_permission()) return forbiddenEmpty();

            // Vrácení validity objektu
            return ok(Json.toJson(project.get_hardware_groups_not_deleted()));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "get BoardGroup",
            tags = { "Type-Of-Board"},
            notes = "get List of BoardGroup from Project",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Model_HardwareGroup.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error", response = Result_InternalServerError.class)
    })
    public Result board_group_get(String board_group_id) {
        try {

            // Kontrola validity objektu
            Model_HardwareGroup group = Model_HardwareGroup.getById(board_group_id);
            if (group == null) return notFound("BoardGroupLoader not found");

            if (!group.read_permission()) return forbiddenEmpty();

            // Vrácení validity objektu
            return ok(Json.toJson(group));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

}
