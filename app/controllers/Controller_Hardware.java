package controllers;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import exceptions.BadRequestException;
import exceptions.FailedMessageException;
import exceptions.ForbiddenException;
import io.ebean.Ebean;
import io.ebean.Query;
import io.swagger.annotations.*;
import models.*;
import mongo.ModelMongo_Hardware_BatchCollection;
import mongo.ModelMongo_Hardware_RegistrationEntity;
import org.mindrot.jbcrypt.BCrypt;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.*;
import responses.*;
import utilities.authentication.Authentication;
import utilities.document_mongo_db.document_objects.DM_Board_Bootloader_DefaultConfig;
import exceptions.NotFoundException;
import utilities.hardware.HardwareConfigurator;
import utilities.hardware.HardwareInterface;
import utilities.hardware.HardwareService;
import utilities.hardware.update.UpdateService;
import utilities.lablel_printer_service.Printer_Api;
import utilities.lablel_printer_service.labels.Label_62_mm_package;
import utilities.enums.*;
import utilities.lablel_printer_service.labels.Label_62_split_mm_Details;
import utilities.logger.Logger;
import utilities.model.EchoService;
import utilities.notifications.NotificationService;
import utilities.permission.PermissionService;
import utilities.swagger.Picture2Mb;
import utilities.swagger.input.*;
import utilities.swagger.output.*;
import utilities.swagger.output.filter_results.Swagger_HardwareGroup_List;
import utilities.swagger.output.filter_results.Swagger_Hardware_List;
import websocket.messages.homer_hardware_with_tyrion.helps_objects.WS_Help_Hardware_Pair;

import java.io.File;
import java.nio.charset.IllegalCharsetNameException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static play.mvc.Controller.request;

@Api(value = "Not Documented API - InProgress or Stuck")  // Záměrně takto zapsané - Aby ve swaggru nezdokumentované API byly v jedné sekci
@Security.Authenticated(Authentication.class)
public class Controller_Hardware extends _BaseController {

// LOGGER ##############################################################################################################
    
    private static final Logger logger = new Logger(Controller_Hardware.class);

// CONTROLLER CONFIGURATION ############################################################################################

    private final HardwareService hardwareService;
    private final UpdateService updateService;

    @Inject
    public Controller_Hardware(WSClient ws, _BaseFormFactory formFactory, Config config, PermissionService permissionService,
                               NotificationService notificationService, HardwareService hardwareService, UpdateService updateService, EchoService echoService) {
        super(ws, formFactory, config, permissionService, notificationService, echoService);
        this.hardwareService = hardwareService;
        this.updateService = updateService;
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

            // Get and Validate Object
            Swagger_Processor_New help = formFromRequestWithValidation(Swagger_Processor_New.class);

            // Vytvářím objekt
            Model_Processor processor = new Model_Processor();
            processor.name           = help.name;
            processor.description    = help.description;
            processor.processor_code = help.processor_code;
            processor.speed          = help.speed;

            return create(processor);

        } catch (Exception e) {
            return controllerServerError(e);
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Processor.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result processor_get(UUID processor_id) {
        try {
            return ok(Model_Processor.find.byId(processor_id));
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Processor All",
            tags = {"Processor"},
            notes = "Get list of all Processor by query",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Processor.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result processor_getAll() {
        try {

            //Vyhledám objekty
           List<Model_Processor> processors = Model_Processor.find.query().where().eq("deleted", false).order().asc("name").findList();

            // Vracím seznam objektů
           return ok(processors);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Processor",
            tags = {"Processor"},
            notes = "If you want update Processor.id by query = processor_id . Send required json values and server respond with update object",
            produces = "application/json",
            protocols = "https"
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
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result processor_edit(UUID processor_id) {
        try {

            // Get and Validate Object
            Swagger_Processor_New help = formFromRequestWithValidation(Swagger_Processor_New.class);

            // Kontroluji validitu
            Model_Processor processor = Model_Processor.find.byId(processor_id);

            // Upravuji objekt
            processor.description    = help.description;
            processor.processor_code = help.processor_code;
            processor.name           = help.name;
            processor.speed          = help.speed;

            return update(processor);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Processor",
            tags = {"Admin-Processor"},
            notes = "If you want delete Processor by query processor_id.",
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
    public Result processor_delete(UUID processor_id) {
        try {

            // Kontroluji validitu
            Model_Processor processor = Model_Processor.find.byId(processor_id);
           
            if (processor.hardware_types.size() > 0) return badRequest("Processor is assigned to some type of board, so cannot be deleted");

            return delete(processor);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

///###################################################################################################################*/

    @ApiOperation(value = "get Bootloader FileRecord",
            tags = {"File", "Garfield"},
            notes = "",     //TODO
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 303
    )
    @ApiResponses({
            @ApiResponse(code = 303, message = "Automatic Redirect To another URL"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result fileRecord_get_bootLoader(UUID bootloader_id) {
        try {

            Model_BootLoader boot_loader = Model_BootLoader.find.byId(bootloader_id);

            // Swagger_File_Content - Zástupný dokumentační objekt
            Swagger_File_Content content = new Swagger_File_Content();
            content.file_in_base64 = boot_loader.getBlob().downloadString();

            // Vracím content
            return redirect(boot_loader.getBlob().link);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get CProgram_Version FileRecord",
            tags = { "File" , "Garfield"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 303
    )
    @ApiResponses({
            @ApiResponse(code = 303, message = "Automatic Redirect To another URL"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result fileRecord_get_firmware(UUID version_id) {
        try {

            // Kontrola validity objektu
            Model_CProgramVersion version = Model_CProgramVersion.find.byId(version_id);

            // Vracím content
            return redirect(version.getCompilation().getBlob().link);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

///###################################################################################################################*/

    @ApiOperation(value = "create Producer",
            tags = {"Admin-Producer"},
            notes = "if you want create new Producer. Its company owned physical hardware and we used that for filtering",
            produces = "application/json",
            protocols = "https",
            code = 201
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

            // Get and Validate Object
            Swagger_NameAndDescription help = formFromRequestWithValidation(Swagger_NameAndDescription.class);

            //Vytvářím objekt
            Model_Producer producer = new Model_Producer();
            producer.name = help.name;
            producer.description = help.description;

            return create(producer);
            
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Producer",
            tags = {"Admin-Producer"},
            notes = "if you want edit information about Producer. Its company owned physical hardware and we used that for filtering",
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Producer.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result producer_update(UUID producer_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help = formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_Producer producer = Model_Producer.find.byId(producer_id);
          
            // Úprava objektu
            producer.name = help.name;
            producer.description = help.description;

            return update(producer);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Producers All",
            tags = {"Producer"},
            notes = "if you want get list of Producers. Its list of companies owned physical hardware and we used that for filtering",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Producer.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",           response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result producer_getAll() {
        try {

            // Získání seznamu
            List<Model_Producer> producers = Model_Producer.find.query().where().eq("deleted", false).order().asc("name").findList();

            // Vrácení seznamu
            return ok(producers);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Producer",
            tags = {"Producer"},
            notes = "if you want get Producer. Its company owned physical hardware and we used that for filtering",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Producer.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result producer_get(UUID producer_id) {
        try {
            return ok(Model_Producer.find.byId(producer_id));
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Producer",
            tags = {"Admin-Producer"},
            notes = "if you want delete Producer",
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
    public Result producer_delete(UUID producer_id) {
        try {

            // Kontrola objektu
            Model_Producer producer = Model_Producer.find.byId(producer_id);
            
            if (producer.hardware_types.size() > 0 || producer.blocks.size() > 0 || producer.widgets.size() > 0)
                return badRequest("Producer is assigned to some objects, so cannot be deleted.");

            return delete(producer);

        } catch (Exception e) {
            return controllerServerError(e);
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

            // Get and Validate Object
            Swagger_HardwareType_New help = formFromRequestWithValidation(Swagger_HardwareType_New.class);

            // Kontrola objektu
            Model_Producer producer = Model_Producer.find.byId(help.producer_id);
            this.checkReadPermission(producer);
            
            // Kontrola objektu
            Model_Processor processor = Model_Processor.find.byId(help.processor_id);
            this.checkReadPermission(processor);

            // Tvorba objektu
            Model_HardwareType hardwareType = new Model_HardwareType();
            hardwareType.name = help.name;
            hardwareType.description = help.description;
            hardwareType.compiler_target_name = help.compiler_target_name;
            hardwareType.processor = processor;
            hardwareType.producer = producer;
            hardwareType.connectible_to_internet = help.connectible_to_internet;

            this.checkCreatePermission(hardwareType);

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

            return created(hardwareType);

        } catch (Exception e) {
            return controllerServerError(e);
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
    public Result hardwareType_update(UUID hardware_type_id) {
        try {

            // Get and Validate Object
            Swagger_HardwareType_New help = formFromRequestWithValidation(Swagger_HardwareType_New.class);

            // Kontrola objektu
            Model_HardwareType hardwareType = Model_HardwareType.find.byId(hardware_type_id);
        
            // Kontrola objektu
            Model_Producer producer = Model_Producer.find.byId(help.producer_id);
            this.checkReadPermission(producer);
  
            // Kontrola objektu
            Model_Processor processor = Model_Processor.find.byId(help.processor_id);
            this.checkReadPermission(processor);

            // Uprava objektu
            hardwareType.name = help.name;
            hardwareType.description = help.description;
            hardwareType.compiler_target_name = help.compiler_target_name;
            hardwareType.processor = processor;
            hardwareType.producer = producer;
            hardwareType.connectible_to_internet = help.connectible_to_internet;

            return update(hardwareType);

        } catch (Exception e) {
            return controllerServerError(e);
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
    public Result hardwareType_delete(UUID hardware_type_id) {
        try {
            return delete(Model_HardwareType.find.byId(hardware_type_id));
        } catch (Exception e) {
            return controllerServerError(e);
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

            // Získání seznamu
            // To co jsem tady napsal jen filtruje tahá ručně desky z cache pojendom - možná by šlo někde mít statické pole ID třeba
            // přímo v objektu Model_HardwareType DB ignor a to používat a aktualizovat a statické pole nechat na samotné jave, aby si ji uchavaala v pam,ěti
            List<UUID> hardwareTypes_not_cached = Model_HardwareType.find.query().where().orderBy("UPPER(name) ASC").findIds();

            List<Model_HardwareType> hardwareTypes = new ArrayList<>();

            for (UUID id : hardwareTypes_not_cached) {
                hardwareTypes.add(Model_HardwareType.find.byId(id));
            }

            // Vrácení seznamu
            return ok(hardwareTypes);

        } catch (Exception e) {
            return controllerServerError(e);
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
    public Result hardwareType_get(UUID hardware_type_id) {
        try {
            return read(Model_HardwareType.find.byId(hardware_type_id));
        } catch (Exception e) {
            return controllerServerError(e);
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
    @BodyParser.Of(value = Picture2Mb.Json.class)
    public Result hardwareType_uploadPicture(UUID hardware_type_id) {
        try {

            // Get and Validate Object
            Swagger_BASE64_FILE help = formFromRequestWithValidation(Swagger_BASE64_FILE.class);

            // Kontrola objektu
            Model_HardwareType hardwareType = Model_HardwareType.find.byId(hardware_type_id);

            this.checkUpdatePermission(hardwareType);

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


            hardwareType.picture = Model_Blob.upload_picture(help.file , hardwareType.get_path() );
            hardwareType.update();
            
            return ok();
            
        } catch (Exception e) {
            return controllerServerError(e);
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
            @ApiResponse(code = 201, message = "Successfully created",      response = ModelMongo_Hardware_BatchCollection.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardwareBatch_create(UUID hardware_type_id) {
        try {

            // Get and Validate Object
            Swagger_HardwareBatch_New help = formFromRequestWithValidation(Swagger_HardwareBatch_New.class);

            // Kontrola objektu
            Model_HardwareType hardwareType = Model_HardwareType.find.byId(hardware_type_id);

            this.checkUpdatePermission(hardwareType);
           
            // Tvorba objektu
            ModelMongo_Hardware_BatchCollection batch = new ModelMongo_Hardware_BatchCollection();
            batch.compiler_target_name = hardwareType.compiler_target_name;

            batch.revision = help.revision;
            batch.production_batch = help.production_batch;

            batch.date_of_assembly =  LocalDateTime.ofInstant(Instant.ofEpochSecond(help.date_of_assembly), TimeZone.getDefault().toZoneId());

            batch.pcb_manufacture_name = help.pcb_manufacture_name;
            batch.pcb_manufacture_id = help.pcb_manufacture_id;

            batch.assembly_manufacture_name = help.assembly_manufacture_name;
            batch.assembly_manufacture_id = help.assembly_manufacture_id;

            batch.customer_product_name = help.customer_product_name;

            batch.customer_company_name = help.customer_company_name;
            batch.customer_company_made_description = help.customer_company_made_description;

            batch.mac_address_start = help.mac_address_start.toUpperCase();
            batch.mac_address_end = help.mac_address_end.toUpperCase();
            batch.ean_number = help.ean_number;

            batch.description = help.description;


            // Uložení objektu do DB
            batch.save();

            return created(batch);

        } catch (Exception e) {
            return controllerServerError(e);
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
    public Result hardwareBatch_delete(String id) {
        try {

            // Kontrola objektu
            ModelMongo_Hardware_BatchCollection batch = ModelMongo_Hardware_BatchCollection.find.byId(id);

            this.checkUpdatePermission(batch.getHardwareType());
      
            // Smazání objektu
            batch.delete();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = ModelMongo_Hardware_BatchCollection.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardwareBatch_edit(String id) {
        try {

            // Get and Validate Object
            Swagger_HardwareBatch_New help = formFromRequestWithValidation(Swagger_HardwareBatch_New.class);

            // Kontrola objektu
            ModelMongo_Hardware_BatchCollection batch = ModelMongo_Hardware_BatchCollection.find.byId (id);

            this.checkUpdatePermission(batch.getHardwareType());

            // Tvorba objektu
            batch.revision = help.revision;
            batch.production_batch = help.production_batch;

            batch.date_of_assembly = LocalDateTime.ofInstant(Instant.ofEpochSecond(help.date_of_assembly), TimeZone.getDefault().toZoneId());

            batch.pcb_manufacture_name = help.pcb_manufacture_name;
            batch.pcb_manufacture_id = help.pcb_manufacture_id;

            batch.assembly_manufacture_name = help.assembly_manufacture_name;
            batch.assembly_manufacture_id = help.assembly_manufacture_id;

            batch.customer_product_name = help.customer_product_name;

            batch.customer_company_name = help.customer_company_name;
            batch.customer_company_made_description = help.customer_company_made_description;

            batch.mac_address_start = help.mac_address_start.toUpperCase();
            batch.mac_address_end = help.mac_address_end.toUpperCase();
            batch.ean_number = help.ean_number;

            batch.description = help.description;
            
            // Uložení objektu do DB
            batch.update();

            return ok(batch);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// BootLoader ##########################################################################################################

    @ApiOperation(value = "create Bootloader",
            tags = { "HardwareType"},
            notes = "Create bootloader for HardwareType",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
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
    public Result bootLoader_create(UUID hardware_type_id) {
        try {

            // Get and Validate Object
            Swagger_BootLoader_New help = formFromRequestWithValidation(Swagger_BootLoader_New.class);

            // Kontrola objektu
            Model_HardwareType hardwareType = Model_HardwareType.find.byId(hardware_type_id);

            String identifier = help.version_identifier.replaceAll("\\s+", "");

            if (Model_BootLoader.find.query().where().eq("version_identifier", identifier).eq("hardware_type.id", hardwareType.id).findOne() != null) return badRequest("Version format is not unique!");

            Model_BootLoader boot_loader = new Model_BootLoader();
            boot_loader.name = help.name;
            boot_loader.changing_note =  help.changing_note;
            boot_loader.description = help.description;
            boot_loader.version_identifier = identifier;
            boot_loader.hardware_type = hardwareType;

            return create(boot_loader);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Bootloader",
            tags = { "HardwareType"},
            notes = "Edit bootloader for HardwareType",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
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
    public Result bootLoader_update(UUID boot_loader_id) {
        try {

            // Get and Validate Object
            Swagger_BootLoader_New help = formFromRequestWithValidation(Swagger_BootLoader_New.class);

            // Kontrola objektu
            Model_BootLoader boot_loader = Model_BootLoader.find.byId(boot_loader_id);

            boot_loader.name = help.name;
            boot_loader.changing_note = help.changing_note;
            boot_loader.description = help.description;
            boot_loader.version_identifier = help.version_identifier;

            return update(boot_loader);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Bootloader",
            tags = { "HardwareType"},
            notes = "",     //TODO
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
    @BodyParser.Of(BodyParser.Json.class)
    public Result bootLoader_delete(UUID boot_loader_id) {
        try {

            // Kontrola objektu
            Model_BootLoader boot_loader = Model_BootLoader.find.byId(boot_loader_id);

            if (!boot_loader.hardware.isEmpty()) return badRequest("Bootloader is already used on some Board. Cannot be deleted.");

            return delete(boot_loader);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "upload Bootloader File",
            tags = {"Admin-Bootloader"},
            notes = "",     //TODO
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
    @BodyParser.Of(value = BodyParser.Json.class)
    public Result bootLoader_uploadFile(UUID boot_loader_id) {
        try {

            // Get and Validate Object
            Swagger_BASE64_FILE help = formFromRequestWithValidation(Swagger_BASE64_FILE.class);

            // Kontrola objektu
            Model_BootLoader boot_loader = Model_BootLoader.find.byId(boot_loader_id);

            this.checkUpdatePermission(boot_loader);
            
            //  data:image/png;base64,
            String[] parts = help.file.split(",");
            String[] type = parts[0].split(":");
            String[] content_type = type[1].split(";");
            String dataType = content_type[0].split("/")[1];

            logger.debug("bootLoader_uploadFile:: Cont Type:" + content_type[0]);
            logger.debug("bootLoader_uploadFile:: Data Type:" + dataType);
            logger.debug("bootLoader_uploadFile:: Data: " + parts[1].substring(0, 10) + "......");

            try {
                boot_loader.getBlob().delete();
            } catch (NotFoundException e) {
                // Nothing
            }


            boot_loader.file = Model_Blob.upload_bin_file(help.file, boot_loader.get_path());
            boot_loader.update();

            // Vracím seznam
            return ok(boot_loader);

        } catch (Exception e) {
            return controllerServerError(e);
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
    public Result bootLoader_markAsMain(UUID boot_loader_id) {
        try {

            Model_BootLoader boot_loader = Model_BootLoader.find.byId(boot_loader_id);

            this.checkUpdatePermission(boot_loader);

            try {
                boot_loader.getBlob();
            } catch (NotFoundException e) {
                return badRequest("Bootloader is missing binary file");
            }

            if (boot_loader.getMainHardwareType() != null) return badRequest("Bootloader is Already Main");

            Model_HardwareType hardware_type = boot_loader.getHardwareType();

            Model_BootLoader old_main = Model_BootLoader.find.query().nullable().where().eq("main_hardware_type.id", boot_loader.getHardwareTypeId()).findOne();
            if (old_main != null) {
                old_main.main_hardware_type = null;
                old_main.update();
            }

            boot_loader.main_hardware_type = hardware_type;
            boot_loader.update();

            hardware_type.refresh();

            // Vymažu Device Cache
            Model_Hardware.find.getCache().clear();

            // Vracím Json
            return ok(boot_loader);

        } catch (Exception e) {
            return controllerServerError(e);
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

            // Get and Validate Object
            Swagger_Board_Bootloader_Update help = formFromRequestWithValidation(Swagger_Board_Bootloader_Update.class);

            List<Model_Hardware> boards = Model_Hardware.find.query().where().in("id", help.device_ids).findList();
            if (boards.isEmpty()) return badRequest("Hardware not found");

            List<WS_Help_Hardware_Pair> hardware_for_update = new ArrayList<>();

            for (Model_Hardware hardware : boards) {

                this.checkUpdatePermission(hardware);

                WS_Help_Hardware_Pair pair = new WS_Help_Hardware_Pair();
                pair.hardware = hardware;

                if (help.bootloader_id != null) {

                    pair.bootLoader = Model_BootLoader.find.byId(help.bootloader_id);

                } else {
                    pair.bootLoader = Model_BootLoader.find.query().where().eq("main_hardware_type.hardware.id", hardware.id).findOne();
                }

                hardware_for_update.add(pair);
            }

            if (!hardware_for_update.isEmpty()) {
                this.updateService.bulkUpdate(boards, Model_BootLoader.find.byId(help.bootloader_id), FirmwareType.BOOTLOADER, UpdateType.MANUALLY_BY_USER_INDIVIDUAL, null);
            }else {
                logger.error("bootLoader_manualUpdate hardware_for_update is Empty");
            }

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
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

            // Get and Validate Object
            Swagger_Board_New_Manual help = formFromRequestWithValidation(Swagger_Board_New_Manual.class);

            // Kotrola objektu
            // TODO Kontrola vůči Globální autoritě!
            if (Model_Hardware.find.query().where().eq("full_id", help.full_id).findCount() > 0) return badRequest("Hardware is already registered");

            // Kotrola objektu
            Model_HardwareType hardwareType = Model_HardwareType.find.byId(help.hardware_type_id);

            Model_Hardware hardware = new Model_Hardware();
            hardware.full_id = help.full_id;
            hardware.is_active = false;
            hardware.hardware_type = hardwareType;

            return create(hardware);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Hardware hash admin only",
            tags = { "Admin-Board"},
            notes = "This Api is using only with special Admin Permission",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully created",      response = Swagger_Hardware_Registration_Hash.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result hardware_get_registration_hash(String full_id) {
        try {

            if(!isAdmin()) {
                throw new ForbiddenException();
            }

            if(ModelMongo_Hardware_RegistrationEntity.getbyFull_id(full_id) == null) {
                return notFound(Model_Hardware.class);
            }

            ModelMongo_Hardware_RegistrationEntity hw = ModelMongo_Hardware_RegistrationEntity.getbyFull_id(full_id);

            Swagger_Hardware_Registration_Hash hash = new Swagger_Hardware_Registration_Hash();
            hash.hash = hw.hash_for_adding;

            // Vracím seznam zařízení k registraci
            return ok(hash);

        } catch (Exception e) {
            return controllerServerError(e);
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

            if (!isAdmin()) {
                throw new ForbiddenException();
            }

            // Get and Validate Object
            Swagger_Board_New_Garfield help = formFromRequestWithValidation(Swagger_Board_New_Garfield.class);

            // Kotrola objektu
            Model_HardwareType hardwareType = Model_HardwareType.find.byId(help.hardware_type_id);

            // Kontrola Objektu
            ModelMongo_Hardware_BatchCollection batch = ModelMongo_Hardware_BatchCollection.find.byId(help.batch_id);
            
            // Kontrola Objektu
            Model_Garfield garfield = Model_Garfield.find.byId(help.garfield_station_id);

            // Odzkouším -zda už není registrovaný v centárlní autoritě!
            if (ModelMongo_Hardware_RegistrationEntity.getbyFull_id(help.full_id) != null) {
                logger.trace("hardware_create_garfield:: Hardware is already registred in Central authority");
            } else {

            }


            String mqtt_password_not_hashed = UUID.randomUUID().toString();
            String mqtt_username_not_hashed = UUID.randomUUID().toString();

            if(ModelMongo_Hardware_RegistrationEntity.getbyFull_macAddress(batch.get_nextMacAddress_just_for_check()) != null) {
                logger.error("hardware_create_garfield:: Mac Address {} is already used!", batch.get_nextMacAddress_just_for_check());
                return badRequest("hardware_create_garfield:: Mac Address {} is already used!");
             }

            ModelMongo_Hardware_RegistrationEntity registration_of_hardware = ModelMongo_Hardware_RegistrationEntity.getbyFull_id(help.full_id);

            // Pokud je null - zaregistruji hardware jako nový do centrální autority
            if (registration_of_hardware == null) {
                logger.warn("hardware_create_garfield:: Hardware is not regstred in central authority!");
                logger.warn("hardware_create_garfield:: - hardware is not found in centrall database, full_id: {}", help.full_id);
                logger.warn("hardware_create_garfield:: - Creation of new device for central database");

                if (ModelMongo_Hardware_RegistrationEntity.getbyFull_macAddress(batch.get_nextMacAddress_just_for_check()) != null) {
                    logger.error("Next Mac Address fot this device is already registered. Check It. Mac Address:: {}", help.full_id);
                    return badRequest("Next Mac Address fot this device is already registered. Check It Mac Address:: " +  help.full_id);
                }

                registration_of_hardware = new ModelMongo_Hardware_RegistrationEntity();
                registration_of_hardware.full_id = help.full_id;
                registration_of_hardware.mac_address = batch.get_new_MacAddress();
                registration_of_hardware.hardware_type_compiler_target_name =  hardwareType.compiler_target_name;
                registration_of_hardware.production_batch_id = batch.id;
                registration_of_hardware.mqtt_username = BCrypt.hashpw(mqtt_username_not_hashed, BCrypt.gensalt());
                registration_of_hardware.mqtt_password = BCrypt.hashpw(mqtt_password_not_hashed, BCrypt.gensalt());
                registration_of_hardware.save();


            // Pokud nový není - změním jeho oprávnění na mqtt a oprávnění uložím!
            } else {
                logger.warn("hardware_create_garfield:: Hardware is already registered in Central authority");
                logger.warn("hardware_create_garfield:: Changing only basic mqtt_username and mqtt_password");
                registration_of_hardware.mqtt_username = BCrypt.hashpw(mqtt_username_not_hashed, BCrypt.gensalt());
                registration_of_hardware.mqtt_password = BCrypt.hashpw(mqtt_password_not_hashed, BCrypt.gensalt());
                registration_of_hardware.update();
            }

            // Vytisknu štítky
            Printer_Api api = new Printer_Api();

            // Kontrola zda jde štítek vytisknout!
            try {
                // Test for creating - Controlling all prerequisites and requirements
                new Label_62_mm_package(registration_of_hardware, batch, hardwareType, garfield);
            } catch (IllegalArgumentException e) {
                return badRequest("Something is wrong: " + e.getMessage());
            }

            // Vytisknu štítky
            Label_62_mm_package label_62_mmPackage = new Label_62_mm_package(registration_of_hardware, batch, hardwareType, garfield);
            api.printFile(garfield.print_sticker_id, 1, "Garfield Print Label", label_62_mmPackage.get_label(), null);

            // Label qith QR kode on Ethernet connector
            Label_62_split_mm_Details label_12_mm_details = new Label_62_split_mm_Details(registration_of_hardware);
            api.printFile(garfield.print_label_id_1, 1, "Garfield Print QR Hash", label_12_mm_details.get_label(), null);


            // Vytvořím registrační číčoviny pro hardware
            if (hardwareType.connectible_to_internet) {

                // Najdu backup_server
                Model_HomerServer backup_server = Model_HomerServer.find.query().where().eq("server_type", HomerType.BACKUP).findOne();

                // Najdu Main_server
                Model_HomerServer main_server = Model_HomerServer.find.query().where().eq("server_type", HomerType.MAIN).findOne();

                // Vytvořím konfigurační soubor
                DM_Board_Bootloader_DefaultConfig conf = DM_Board_Bootloader_DefaultConfig.generateConfig();

                Swagger_Hardware_New_Settings_Result_Configuration configuration = new Swagger_Hardware_New_Settings_Result_Configuration();
                configuration.normal_mqtt_hostname = main_server.server_url;
                configuration.normal_mqtt_port = main_server.mqtt_port;
                configuration.mqtt_username = mqtt_username_not_hashed;
                configuration.mqtt_password = mqtt_password_not_hashed;
                configuration.backup_mqtt_hostname = backup_server.server_url;
                configuration.backup_mqtt_port = backup_server.mqtt_port;
                configuration.mac = registration_of_hardware.mac_address;
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
                result.full_id = registration_of_hardware.full_id;
                result.configuration = configuration;

                return created(result);
            }else {
                logger.error("hardware_create_garfield:: Error not supported type of board - device is not connectible to internet!");
                return badRequest("hardware_create_garfield:: Error not supported type of board - device is not connectible to internet!");
            }

        } catch (IllegalCharsetNameException e) {
            return badRequest("All Mac Address used");
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "edit Board personal description",
            tags = { "Hardware"},
            notes = "Used for add descriptions by owners. \"Persons\" who registred \"Board\" to own \"Project\" ",
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Hardware.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardware_update_description(UUID hardware_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help = formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kotrola objektu
            Model_Hardware hardware = Model_Hardware.find.byId(hardware_id);

            // Uprava desky
            hardware.name = help.name;
            hardware.description = help.description;

            // Uprava objektu v databázi
            hardware.update();

            // TODO might be async
            this.hardwareService.getConfigurator(hardware).configure("alias", hardware.name);

            hardware.setTags(help.tags);

            // Vrácení upravenéh objektu
            return ok(hardware);

        } catch (Exception e) {
            return controllerServerError(e);
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
    public Result hardware_update_parameters(UUID hardware_id) {
        try {

            // Get and Validate Object
            Swagger_Board_Developer_parameters help = formFromRequestWithValidation(Swagger_Board_Developer_parameters.class);

            // Kotrola objektu
            Model_Hardware hardware = Model_Hardware.find.byId(hardware_id);

            this.checkUpdatePermission(hardware);

            HardwareConfigurator configurator = this.hardwareService.getConfigurator(hardware);

            switch (help.parameter_type.toLowerCase()) {

                case "developer_kit": {
                    hardware.developer_kit = help.boolean_value;
                    hardware.update();
                    break;
                }

                case "alias": {
                    hardware.name = help.string_value;
                    hardware.update();
                    configurator.configure("alias", hardware.name);
                    break;
                }

                case "autobackup": {
                    hardware.backup_mode = help.boolean_value;
                    hardware.update();
                    configurator.configure("autobackup", hardware.backup_mode);
                    break;
                }

                case "database_synchronize": {
                    hardware.database_synchronize = help.boolean_value;
                    hardware.update();
                    configurator.configure("database_synchronize", hardware.database_synchronize);
                    break;
                }

                case "wdtime": {

                    if (help.integer_value  == null) {
                        throw new BadRequestException("wdtime must be integer! And minimal value is 30");
                    }

                    if (help.integer_value < 30) {
                        help.integer_value = 30;
                    }
                    configurator.configure("wdtime", help.integer_value);
                    break;
                }

                case "autojump": {

                    if (help.integer_value  == null) {
                        throw new BadRequestException("autojump must be integer! And minimal value is 30");
                    }

                    if (help.integer_value < 30) {
                        help.integer_value = 30;
                    }
                    configurator.configure("autojump", help.integer_value);
                    break;
                }

                case "imsi": {
                    throw new BadRequestException("imsi IS NOT possible to change!");
                }

                case "iccid": {
                    throw new BadRequestException("iccid IS NOT possible to change!");
                }

                case "netsource": {

                    if (help.string_value == null) {
                        throw new BadRequestException("netsource must be string! Allowed values: 6lowpan, ethernet, gsm");
                    }

                    if (!(help.string_value.equals("ethernet") || help.string_value.equals("6lowpan") || help.string_value.equals("gsm"))) {
                        throw new BadRequestException("netsource must be string! Allowed values: 6lowpan, ethernet, gsm");
                    }

                    configurator.configure("netsource", help.string_value);
                    break;
                }

                default: {

                    try {
                        configurator.configure(help);
                        return ok(hardware);
                    } catch (IllegalArgumentException e) {
                        logger.trace("IllegalArgumentException" + e.getMessage());
                        return badRequest(e.getMessage());
                    } catch (Exception e) {
                        logger.trace("Exception" + e.getMessage());
                        return badRequest(e.getMessage());
                    }
                }
            }

            // Vrácení upravenéh objektu
            return ok(hardware);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }




    @ApiOperation(value = "get Boards List by Filter",
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
    public Result hardware_getByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n. For first call, use 1 (first page of list)", required = true) Integer page_number) {
        try {

            // Get and Validate Object
            Swagger_Board_Filter help = formFromRequestWithValidation(Swagger_Board_Filter.class);

            // Musí být splněna alespoň jedna podmínka, aby mohl být Junction aktivní. V opačném případě by totiž způsobil bychu
            // která vypadá nějak takto:  where t0.deleted = false and and .... KDE máme 2x end!!!!!
            if (!(
                    help.projects != null && !help.projects.isEmpty()
                    || ( help.producers != null && !help.producers.isEmpty() )
                    || ( help.processors != null && !help.processors.isEmpty())
                    || ( help.hardware_groups_id != null && !help.hardware_groups_id.isEmpty())
                ) && !isAdmin()) {
                return ok(new Swagger_Hardware_List());
            }

            // Tvorba parametru dotazu
            Query<Model_Hardware> query = Ebean.find(Model_Hardware.class);

            // not deleted
            query.where().ne("deleted", true);


            if (help.order_by != null) {

                if(help.order_by == Swagger_Board_Filter.Order_by.NAME) {
                    query.where().order("name" + " " + help.order_schema  );
                }

                if(help.order_by == Swagger_Board_Filter.Order_by.FULL_ID) {
                    query.where().order("full_id" + " " + help.order_schema );

                }

                if(help.order_by == Swagger_Board_Filter.Order_by.ID) {
                    query.where().order("id" + " " + help.order_schema );
                }

            }

            if (help.full_id != null && help.full_id.length() > 0) {
                System.out.println("Full ID vyplněno: " + help.full_id + " l: " + help.full_id.length());
                query.where().icontains("full_id", help.full_id);
            }

            if (help.id != null) {
                System.out.println("ID vyplněno: " + help.id);
                query.where().eq("id", help.id);
            }

            if (help.name != null && help.name.length() > 0) {
                System.out.println("name vyplněno: " + help.name + " l: " + help.name.length());
                query.where().icontains("name", help.name);
            }

            if (help.description != null && help.description.length() > 0) {
                System.out.println("description vyplněno: " + help.description + " l: " + help.description.length());
                query.where().icontains("description", help.description);
            }

            if (help.hardware_type_ids != null && !help.hardware_type_ids.isEmpty()) {
                query.where().in("hardware_type.id", help.hardware_type_ids);
            }

            // If contains confirms
            if (help.active != null) {
                query.where().eq("is_active", help.active.equals("true"));
            }

            if (help.projects != null && !help.projects.isEmpty()) {
                query.where().in("project.id", help.projects);
            }

            if (help.producers != null) {
                query.where().in("hardware_type.producer.id", help.producers);
            }

            if (help.processors != null) {
                query.where().in("hardware_type.processor.id", help.processors);
            }

            if (help.instance_snapshot != null) {
                query.where().in("id",  Model_InstanceSnapshot.find.byId(help.instance_snapshot).getHardwareIds());
            }

            if (help.hardware_groups_id != null) {
                query.where().in("hardware_groups.id", help.hardware_groups_id);
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
            Swagger_Hardware_List result = new Swagger_Hardware_List(query, page_number, help);

            // Vracím seznam
            return ok(result);

        } catch (Exception e) {
            return controllerServerError(e);
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
    public Result hardware_uploadPicture(UUID hardware_id) {
        try {

            // Get and Validate Object
            Swagger_BASE64_FILE help = formFromRequestWithValidation(Swagger_BASE64_FILE.class);

            //Kontrola objektu
            Model_Hardware hardware = Model_Hardware.find.byId(hardware_id);

            this.checkUpdatePermission(hardware);

            // Odebrání předchozího obrázku
            if (hardware.picture != null) {
                logger.debug("hardware_uploadPicture - removing previous picture");
                Model_Blob blob = hardware.picture;
                blob.delete();
                hardware.picture = null;
                hardware.update();
            }

            hardware.picture = Model_Blob.upload_picture( help.file, hardware.getPath());
            hardware.update();

            return ok(hardware);
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "generate_mqtt_password Board",
            tags = {"Board"},
            notes = "Generate new connection password for Hardware",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK Result",               response = Swagger_Hardware_New_Password.class),
            @ApiResponse(code = 400, message = "Something is wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    public Result hardware_generate_new_password(UUID hardware_id) {
        try {

            //Kontrola objektu
            Model_Hardware board = Model_Hardware.find.byId(hardware_id);
            String mqtt_password_not_hashed = UUID.randomUUID().toString();
            String mqtt_username_not_hashed = UUID.randomUUID().toString();

            board.mqtt_username = BCrypt.hashpw(mqtt_username_not_hashed, BCrypt.gensalt());
            board.mqtt_password = BCrypt.hashpw(mqtt_password_not_hashed, BCrypt.gensalt());
            board.update();

            Swagger_Hardware_New_Password pss = new Swagger_Hardware_New_Password();
            pss.mqtt_password = mqtt_password_not_hashed;
            pss.mqtt_username = mqtt_username_not_hashed;

            return ok(pss);
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "change_server Board",
            tags = { "Hardware"},
            notes = "Redirect Board to another server (Change Server)",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
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
    public Result hardware_redirect_to_server(UUID hardware_id) {
        try {

            // Get and Validate Object
            Swagger_Board_Server_Redirect help = formFromRequestWithValidation(Swagger_Board_Server_Redirect.class);

            logger.trace("hardware_redirect_to_server:: Příjem zprávy:: " + Json.toJson(help));

            Model_Hardware hardware = Model_Hardware.find.byId(hardware_id);

            HardwareInterface hardwareInterface = this.hardwareService.getInterface(hardware);

            // Jedná se o přesměrování na server v rámci stejné hierarchie - na server co mám v DB
            if (help.server_id != null) {

                logger.trace("hardware_redirect_to_server:: Bude se přesměrovávat z databáze");

                Model_HomerServer server = Model_HomerServer.find.byId(help.server_id);

                hardwareInterface.relocate(server)
                        .whenComplete((message, exception) -> {
                            if (exception != null) {
                                logger.internalServerError(exception);
                            } else {
                                logger.info("hardware_redirect_to_server - successfully redirected");
                            }
                        });

            // Jedná se o server mimo náš svět - například z dev na stage, nebo z produkce na dev
            } else {

                if (help.server_port == null || help.server_url == null) {
                    return badRequest("its required send server_id  or server_url + server_port ");
                }

                hardwareInterface.relocate(help.server_url, help.server_port)
                        .whenComplete((message, exception) -> {
                            if (exception != null) {
                                logger.internalServerError(exception);
                            } else {
                                logger.info("hardware_redirect_to_server - successfully redirected");
                            }
                        });
            }

            return ok();
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "command Board execution",
            tags = {"Board"},
            notes = "Removes picture of logged person",
            produces = "application/json",
            protocols = "https"
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

            // Get and Validate Object
            Swagger_Board_Command help = formFromRequestWithValidation(Swagger_Board_Command.class);

            // Kontrola objektu
            Model_Hardware hardware = Model_Hardware.find.byId(help.hardware_id);
            if (help.command == null) {
                throw new NotFoundException(BoardCommand.class);
            }

            this.checkUpdatePermission(hardware);

            HardwareInterface hardwareInterface = this.hardwareService.getInterface(hardware);
            hardwareInterface.command(help.command, true).whenComplete((message, exception) -> {
                if (exception != null) {
                    logger.internalServerError(exception);
                } else {
                    logger.info("hardware_command_execution - command {} was successful", help.command);
                }
            });

            return ok();
        } catch (Exception e) {
            return controllerServerError(e);
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
    public Result hardware_removePicture(UUID hardware_id) {
        try {

            Model_Hardware hardware = Model_Hardware.find.byId(hardware_id);

            if (hardware.picture != null) {
                hardware.picture.delete();
                hardware.picture = null;
                hardware.update();
            } else {
                return badRequest("There is no picture to remove.");
            }

            return ok();
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "deactivate Board",
            tags = { "Hardware"},
            notes = "Permanent exclusion from the system - for some reason it is not allowed to remove the Board from database",
            produces = "application/json",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Hardware.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result hardware_deactivate( UUID hardware_id) {
        try {

            // Kotrola objektu
            Model_Hardware board = Model_Hardware.find.byId(hardware_id);

            // Úprava stavu
            board.is_active = false;

            board.idCache().removeAll(Model_Project.class);

            // Uložení do databáze
            board.update();

            // Vrácení objektu
            return ok(board);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get Board",
            tags = { "Hardware"},
            notes = "if you want get Board object by query = hardware_id. User can get only hardware from project, whitch " +
                    "user owning or user need Permission key \"Board_rea\".",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Hardware.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result hardware_get(UUID hardware_id) {
        try {
            return read(Model_Hardware.find.byId(hardware_id));
        } catch (Exception e) {
            return controllerServerError(e);
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Entity_Registration_Status.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result hardware_check(String registration_hash, UUID project_id) {
        try {

            logger.trace("hardware_check:: Registration_hash: {} ",  registration_hash);
            logger.trace("hardware_check:: Project_id: {}", project_id.toString());

            Swagger_Entity_Registration_Status status = new Swagger_Entity_Registration_Status();

            // It better to calid only full id (26 chars)
            if(registration_hash.length() != 26){
                status.status = BoardRegistrationStatus.NOT_EXIST;
                return ok(status);
            }

            // Kontrola projektu
            Model_Project.find.byId(project_id);

            // Kotrola objektu
            ModelMongo_Hardware_RegistrationEntity hardware = ModelMongo_Hardware_RegistrationEntity.getbyFull_hash(registration_hash);

            if (hardware == null) {
                status.status = BoardRegistrationStatus.NOT_EXIST;
                return ok(status);
            }

            if(hardware.state != null && hardware.state.equals("PERMANENTLY_DISABLED")) {
                status.status = BoardRegistrationStatus.PERMANENTLY_DISABLED;
                return ok(status);
            }

            if(Model_Hardware.find.query().nullable().where().eq("full_id", hardware.full_id).eq("project.id", project_id).findCount() < 1) {
                status.status = BoardRegistrationStatus.CAN_REGISTER;
                return ok(status);
            }else {
                status.status = BoardRegistrationStatus.ALREADY_REGISTERED_IN_YOUR_ACCOUNT;
                return ok(status);
            }

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "Remove Hardware from Database - Only for Administrators", hidden = true)
    public Result hardware_delete(UUID hardware_id) {
        try {

            // Kontrola objektu
            Model_Hardware hardware = Model_Hardware.find.byId(hardware_id);

            hardware.delete();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

// HARDWARE REGISTRATION ###################################################################################################################

    @ApiOperation(value = "tag HardwareRegistration",
            tags = {"HardwareRegistration"},
            notes = "",     //TODO
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Hardware.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardwareRegistration_addTags() {
        try {

            // Get and Validate Object
            Swagger_Tags help = formFromRequestWithValidation(Swagger_Tags.class);

            // Kontrola objektu
            Model_Hardware hardware = Model_Hardware.find.byId(help.object_id);

            // Add Tags
            hardware.addTags(help.tags);

            // Vrácení objektu
            return ok(hardware);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "untag HardwareRegistration",
            tags = {"HardwareRegistration"},
            notes = "",     //TODO
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Hardware.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardwareRegistration_removeTags() {
        try {

            // Get and Validate Object
            Swagger_Tags help = formFromRequestWithValidation(Swagger_Tags.class);

            // Kontrola Objektu
            Model_Hardware hardware = Model_Hardware.find.byId(help.object_id);

            // Remove Tags
            hardware.removeTags(help.tags);

            // Vrácení objektu
            return ok(hardware);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

///###################################################################################################################*/

    @ApiOperation(value = "create HardwareGroup",
            tags = { "HardwareGroup"},
            notes = "Create HardwareGroup",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https",
            code = 201
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
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_HardwareGroup.class),
            @ApiResponse(code = 401, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardwareGroup_create() {
        try {

            // Get and Validate Object
            Swagger_NameAndDesc_ProjectIdRequired help = formFromRequestWithValidation(Swagger_NameAndDesc_ProjectIdRequired.class);

            // Kontrola objektu
            Model_Project project = Model_Project.find.byId(help.project_id);

            Model_HardwareGroup group = new Model_HardwareGroup();
            group.name = help.name;
            group.description = help.description;
            group.project = project;

            // Vracím seznam
            return create(group);

        } catch (Exception e) {
            return controllerServerError(e);
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
    public Result hardwareGroup_update(UUID hardware_group_id) {
        try {

            // Get and Validate Object
            Swagger_NameAndDescription help = formFromRequestWithValidation(Swagger_NameAndDescription.class);

            // Kontrola objektu
            Model_HardwareGroup group = Model_HardwareGroup.find.byId(hardware_group_id);

            group.name = help.name;
            group.description = help.description;

            return update(group);

        } catch (Exception e) {
            return controllerServerError(e);
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
    public Result hardwareGroup_delete(UUID hardware_group_id) {
        try {
            return delete(Model_HardwareGroup.find.byId(hardware_group_id));
        } catch (Exception e) {
            return controllerServerError(e);
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
            
            // Get and Validate Object
            Swagger_Hardware_Group_DeviceListEdit help = formFromRequestWithValidation(Swagger_Hardware_Group_DeviceListEdit.class);

            if (help.device_synchro != null) {

                Model_Hardware hardware = Model_Hardware.find.byId(help.device_synchro.hardware_id);

                HardwareInterface hardwareInterface = this.hardwareService.getInterface(hardware);
                
                logger.debug("board_group_update_device_list - hardware: {}", hardware.id);


                logger.debug("board_group_update_device_list - cached groups: {}", Json.toJson(hardware.get_hardware_group_ids()));

                List<UUID> group_hardware_ids = hardware.get_hardware_group_ids();

                // Cyklus pro přidávání
                if(help.device_synchro.hardware_group_ids != null) {
                    for (UUID board_group_id : help.device_synchro.hardware_group_ids) {

                        // Přidám všechny, které nejsou už součásti cache_hardware_groups_id
                        if (!group_hardware_ids.contains(board_group_id)) {

                            logger.debug("board_group_update_device_list - adding group {}", board_group_id);

                            if ( Model_Hardware.find.query().where().eq("hardware_groups.id", board_group_id).eq("id", hardware.id).findCount() > 0) {
                                continue;
                            }

                            Model_HardwareGroup group = Model_HardwareGroup.find.byId(board_group_id);

                            hardware.get_hardware_group_ids().add(group.id);
                            hardware.hardware_groups.add(group);
                            group.cache_group_size += 1;

                            if (group.idCache().get(Model_HardwareType.class) == null) {
                                 group.idCache().add(Model_HardwareType.class,  new ArrayList<>());
                            }

                            if(!group.idCache().gets(Model_HardwareType.class).contains(hardware.getHardwareType().getId())){
                                    group.idCache().add(Model_HardwareType.class, hardware.getHardwareType().getId());
                            }

                            hardware.get_hardware_group_ids();
                        }
                    }
                }

                // Cyklus pro mazání java.util.ConcurrentModificationException
                for (Iterator<UUID> it = hardware.get_hardware_group_ids().iterator(); it.hasNext(); ) {

                    UUID board_group_id = it.next();

                    // NEní žádná, tak odstraním všechny
                    if(help.device_synchro.hardware_group_ids == null) {
                        Model_HardwareGroup group = Model_HardwareGroup.find.byId(board_group_id);
                        hardware.hardware_groups.remove(group);
                        group.cache_group_size -= 1;
                        group.idCache().removeAll(Model_HardwareType.class);  // Clean cache
                        it.remove();
                    }

                    if(help.device_synchro.hardware_group_ids != null) {
                        // Není a tak mažu
                        if (!help.device_synchro.hardware_group_ids.contains(board_group_id)) {

                            logger.debug("board_group_update_device_list - removing group {}", board_group_id);

                            Model_HardwareGroup group = Model_HardwareGroup.find.byId(board_group_id);

                            hardware.hardware_groups.remove(group);

                            group.cache_group_size -= 1;
                            group.idCache().removeAll(Model_HardwareType.class);
                            it.remove();
                        }
                    }
                }

                hardwareInterface.setHardwareGroups(hardware.get_hardware_group_ids(), Enum_type_of_command.SET)
                        .whenComplete((message, exception) -> {
                            if (exception != null) {
                                logger.internalServerError(exception);
                            } else {
                                logger.info("board_group_update_device_list - successfully set groups");
                            }
                        });

                hardware.idCache().removeAll(Model_HardwareGroup.class);
                hardware.update();
            }

            if (help.group_synchro != null) {

                Model_HardwareGroup group = Model_HardwareGroup.find.byId(help.group_synchro.group_id);

                this.checkUpdatePermission(group);

                // List of All HW ADDS
                List<UUID> hw_ids_in_group = Model_Hardware.find.query().where().eq("hardware_groups.id", help.group_synchro.group_id).ne("deleted", true).findIds();

                // Cyklus pro přidání

                for (UUID board_id: help.group_synchro.hardware_ids) {

                    // Remove from list and skip it - its already in group
                    if(hw_ids_in_group.contains(board_id)) {
                        hw_ids_in_group.remove(board_id);
                        continue;
                    }

                    // Remove from list
                    hw_ids_in_group.remove(board_id);

                    Model_Hardware board = Model_Hardware.find.byId(board_id);

                    board.idCache().add(Model_HardwareGroup.class, group.id);
                    board.hardware_groups.add(group);
                    board.update();

                    group.cache_group_size += 1;
                    group.idCache().add(Model_Hardware.class, board.id);

                    if (group.idCache().get(Model_HardwareType.class) == null) {
                        group.idCache().add(Model_HardwareType.class,  new ArrayList<>());
                    }

                    if(!group.idCache().gets(Model_HardwareType.class).contains(board.getHardwareType().getId())){
                        group.idCache().add(Model_HardwareType.class, board.getHardwareType().getId());
                    }
                }

                // Cyklus pro smazání
                for(UUID board_id:  hw_ids_in_group) {

                    System.out.println("Kontrola HW_ID: " + board_id);

                    Model_Hardware board = Model_Hardware.find.byId(board_id);
                    board.idCache().remove(Model_HardwareGroup.class, group.id);
                    board.hardware_groups.remove(group);
                    board.update();

                    group.cache_group_size -= 1;
                    group.idCache().remove(Model_Hardware.class, board.id);
                }
            }

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get HardwareGroup List by Filter",
            tags = { "HardwareGroup"},
            notes = "get List of BoardGroup from Project",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_HardwareGroup_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_HardwareGroup_List.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result hardwareGroup_get_filter(@ApiParam(value = "page_number is Integer. 1,2,3...n. For first call, use 1 (first page of list)", required = true)  int page_number) {
        try {
            // Get and Validate Object
            Swagger_HardwareGroup_Filter help = formFromRequestWithValidation(Swagger_HardwareGroup_Filter.class);

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_HardwareGroup> query = Ebean.find(Model_HardwareGroup.class);

            query.where().eq("deleted", false);


            System.out.println("hardwareGroup_get_filter:: help.project_id != null ");

            // Pokud JSON obsahuje project_id filtruji podle projektu
            query.where().eq("project.id", help.project_id);

            if (help.instance_snapshots != null && !help.instance_snapshots.isEmpty()) {

                System.out.println("hardwareGroup_get_filter::instance_snapshots:" + help.instance_snapshots);
                List<UUID> list_ids = new ArrayList<>();
                for (UUID snapshost_ids : help.instance_snapshots) {


                    Model_InstanceSnapshot snapshot = Model_InstanceSnapshot.find.byId(snapshost_ids);
                    System.out.println("hardwareGroup_get_filter::snapshot name: " + snapshot.name);
                    System.out.println("hardwareGroup_get_filter::groupIDs: " + snapshot.getHardwareGroupIds());

                    list_ids.addAll( Model_InstanceSnapshot.find.byId(snapshost_ids).getHardwareGroupIds());
                }

                query.where().in("id", list_ids);
            }

            // Vyvoření odchozího JSON
            Swagger_HardwareGroup_List result = new Swagger_HardwareGroup_List(query, page_number, help);

            // Vrácení výsledku
            return ok(result);


        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "get HardwareGroup",
            tags = { "HardwareGroup"},
            notes = "get List of BoardGroup from Project",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_HardwareGroup.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result hardwareGroup_get(UUID group_id) {
        try {

            // Kontrola validity objektu
            Model_HardwareGroup group = Model_HardwareGroup.find.byId(group_id);

            // Vrácení validity objektu
            return ok(group);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }
}
