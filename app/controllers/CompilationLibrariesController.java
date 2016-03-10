package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.OrderBy;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import io.swagger.annotations.*;
import models.compiler.*;
import models.persons.PersonPermission;
import models.project.c_program.C_Program;
import models.project.global.Project;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.*;
import utilities.Server;
import utilities.UtilTools;
import utilities.loginEntities.Secured;
import utilities.permission.InterfaceDynamic;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Description;

import javax.websocket.server.PathParam;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * Controller se zabívá správou knihoven, procesorů, desek (hardware), typů desek a jejich výrobce.
 * To z důvodu, aby se to dalo filtrovat a jednodušeji spravovat. Dále je zde část, která s nejvyšší pravděpodobností
 * bude mít vlastní controller a to správce C_programu (A další služby na kompilaci programu)
 *
 * Knihovny se dělí na skupiny knihoven (group Library) a na samostatné knihovny - Single libraries.
 * Nad knihovnami i c_programy je postaveno nucené verzování.
 *
 * Při updatu programu se vytvoří nová verze (Version_Object).
 *
 */


@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured.class)
public class CompilationLibrariesController extends Controller {


// SYSTEM PERMISSION for this controller
    public static void set_System_Permission(){

        new PersonPermission("processor.read", "description");
        new PersonPermission("processor.edit", "description");
        new PersonPermission("processor.create", "description");
        new PersonPermission("processor.delete", "description");

        new PersonPermission("producer.read", "description");
        new PersonPermission("producer.edit", "description");
        new PersonPermission("producer.create", "description");
        new PersonPermission("producer.delete", "description");

        new PersonPermission("type_of_board.read", "description");
        new PersonPermission("type_of_board.edit", "description");
        new PersonPermission("type_of_board.create", "description");
        new PersonPermission("type_of_board.delete", "description");

        new PersonPermission("board.read", "description");
        new PersonPermission("board.edit", "description");
        new PersonPermission("board.create", "description");
        new PersonPermission("board.delete", "description");

    }

    static {
        Server.handlers.put("project.pepek", Optional.of(new InterfaceDynamic() { public boolean check_dynamic (final String name) {

            System.out.println("Jsem v metodě project.owner");
            return true;
        }}));


        Server.handlers.put("project.kokot",Optional.of(new InterfaceDynamic() { public boolean check_dynamic (final String name) {

            System.out.println("Jsem v metodě project.creator");
            return true;

        }}));
    }

///###################################################################################################################*/

    @ApiOperation(value = "Create new C_Program",
                  tags = {"C_Program"},
                  notes = "If you want create new C_program in project.id = {project_id}. Send required json values and server respond with new object",
                  produces = "application/json",
                  response =  C_Program.class,
                  protocols = "https",
                  code = 201,
                  authorizations = {
                        @Authorization(
                                value="permission",
                                scopes = { @AuthorizationScope(scope = "project.owner", description = "For create new C_program, you have to own project"),
                                           @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                        )
                  }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_C_program_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = C_Program.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result create_C_Program(@ApiParam(value = "project_id String query", required = true) @PathParam("project_id") String project_id) {
        try {


            final Form<Swagger_C_program_New> form = Form.form(Swagger_C_program_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_C_program_New help = form.get();

            // Tvorba programu
            C_Program c_program             = new C_Program();
            c_program.program_name          = help.program_name;
            c_program.program_description   = help.program_description;
            c_program.azurePackageLink      = "personal-program";
            c_program.project               = Project.find.byId(project_id);
            c_program.dateOfCreate          = new Date();
            c_program.setUniqueAzureStorageLink();

            c_program.save();


            return GlobalResult.created(Json.toJson(c_program));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get C_program",
            tags = {"C_Program"},
            notes = "get C_program by query = c_program_id",
            produces = "application/json",
            response =  C_Program.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For create new C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = C_Program.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
  //  @Dynamic("project.c_program_owner")
    public Result get_C_Program(@ApiParam(value = "c_program_id String query", required = true) @PathParam("c_program_id") String c_program_id) {
        try {


            C_Program c_program = C_Program.find.byId(c_program_id);
            return GlobalResult.okResult(Json.toJson(c_program));

        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - getCProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get All C_program",
            tags = {"C_Program"},
            notes = "get all C_program from project by query = project_id",
            produces = "application/json",
            response =  C_Program.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For create new C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = C_Program.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Dynamic("project.owner")
    public Result get_C_Program_All_from_Project(@ApiParam(value = "project_id String query", required = true) @PathParam("project_id") String project_id) {
        try {

            Project project = Project.find.byId(project_id);
            return GlobalResult.okResult(Json.toJson(project.c_programs));

        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - gellAllProgramFromProject ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "Edit C_Program",
            tags = {"C_Program"},
            notes = "If you want edit base information about C_program by  query = c_program_id. Send required json values and server respond with new object",
            produces = "application/json",
            response =  C_Program.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For create new C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = C_Program.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_C_program_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @BodyParser.Of(BodyParser.Json.class)
    // @Dynamic("project.c_program_owner")
    public Result edit_C_Program_Description(@ApiParam(value = "c_program_id String query", required = true) @PathParam("c_program_id") String c_program_id) {
        try {

            final Form<Swagger_C_program_New> form = Form.form(Swagger_C_program_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_C_program_New help = form.get();

            C_Program program = C_Program.find.byId(c_program_id);

            program.program_name = help.program_name;
            program.program_description = help.program_description;

            program.update();

            return GlobalResult.okResult(Json.toJson(program));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "program_name", "program_description" );
        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - gellAllProgramFromProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "new Version of C_Program",
            tags = {"C_Program"},
            notes = "If you want add new code to C_program by query = c_program_id. Send required json values and server respond with new object",
            produces = "application/json",
            response =  Version_Object.class,
            protocols = "https",
            code = 201,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For create new C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = C_Program.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_C_Program_Version",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    // @Dynamic("project.c_program_owner")
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_C_Program_Version(@ApiParam(value = "c_program_id String query", required = true) @PathParam("c_program_id") String c_program_id){
        try{

            final Form<Swagger_C_Program_Version> form = Form.form(Swagger_C_Program_Version.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_C_Program_Version help = form.get();

            C_Program c_program = C_Program.find.byId(c_program_id);

            // První nová Verze
            Version_Object version_object     = new Version_Object();
            version_object.version_name = help.version_name;
            version_object.version_description = help.version_description;

            if(c_program.version_objects.isEmpty() ) version_object.azureLinkVersion = 1;
            else version_object.azureLinkVersion    = ++c_program.version_objects.get(0).azureLinkVersion; // Zvednu verzi o jednu

            version_object.date_of_create = new Date();
            version_object.c_program           = c_program;
            version_object.save();

            c_program.version_objects.add(version_object);
            c_program.update();

            // Nahraje do Azure a připojí do verze soubor (lze dělat i cyklem - ale název souboru musí být vždy jiný)

            for (final Swagger_C_Program_Version.VersionFiles file : help.files){
                UtilTools.uploadAzure_Version("c-program", file.content, file.file_name, c_program.azureStorageLink, c_program.azurePackageLink, version_object);
            }

            return GlobalResult.created(Json.toJson(version_object));

        } catch (NullPointerException e) {
            e.printStackTrace(); //TODO
            return GlobalResult.nullPointerResult(e, "version_description", "version_name", "files {}");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "delete Version in C_program",
            tags = {"C_Program"},
            notes = "delete Version.id = version_id in C_program by query = c_program_id, query = version_id",
            produces = "application/json",
            protocols = "https",
            response =  Result_ok.class,
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    //  @Dynamic("project.c_program_owner")
    public Result delete_C_Program_Version(@ApiParam(value = "c_program_id String query", required = true) @PathParam("c_program_id") String c_program_id, @ApiParam(value = "version_id String query",   required = true) @PathParam("version_id")   String version_id){
        try{

            Version_Object versionObjectObject = Version_Object.find.byId(version_id);
            if (versionObjectObject == null) return GlobalResult.notFoundObject();

            C_Program c_program = C_Program.find.byId(c_program_id);

            UtilTools.azureDelete(Server.blobClient.getContainerReference("c-program"), c_program.azurePackageLink + "/" + c_program.azureStorageLink + "/" + versionObjectObject.azureLinkVersion);

            versionObjectObject.delete();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - deleteVersionOfCProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "update basic information in Version of C_program",
            tags = {"C_Program"},
            notes = "For update basic (name and description) information in Version of C_program. If you want update code. You have to create new version. " +
                    "And after that you can delete previous version",
            produces = "application/json",
            protocols = "https",
            response =  Result_ok.class,
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_C_Program_Version_Edit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Version_Object.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    //  @Dynamic("project.c_program_owner")
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_C_Program_version( @ApiParam(value = "version_id String query",   required = true) @PathParam("version_id") String version_id){
        try{

            final Form<Swagger_C_Program_Version_Edit> form = Form.form(Swagger_C_Program_Version_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_C_Program_Version_Edit help = form.get();


            Version_Object version_object= Version_Object.find.byId(version_id);
            if (version_object == null) return GlobalResult.notFoundObject();

            version_object.version_name = help.version_name;
            version_object.version_description = help.version_description;

            return GlobalResult.okResult(Json.toJson(version_object));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - deleteVersionOfCProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "delete C_program",
            tags = {"C_Program"},
            notes = "delete C_program by query = c_program_id, query = version_id",
            produces = "application/json",
            protocols = "https",
            response =  Result_ok.class,
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For delete C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Dynamic("project.c_program_owner")
    public Result delete_C_Program(@ApiParam(value = "c_program_id String query", required = true) @PathParam("c_program_id") String c_program_id){
        try{

            C_Program c_program = C_Program.find.byId(c_program_id);

            UtilTools.azureDelete(Server.blobClient.getContainerReference("c-program"), c_program.azurePackageLink + "/" + c_program.azureStorageLink);

            c_program.delete();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - deleteCProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }

///###################################################################################################################*/

    //TODO swagger Documentation
    @BodyParser.Of(BodyParser.Json.class)
    public Result compileCProgram(){
        return GlobalResult.okResult("Compiled!"); //TODO
    }

    //TODO swagger Documentation
    public Result generateProjectForEclipse(String c_program_id) {
       // EclipseProject.createFullnewProject();
        return GlobalResult.okResult("In TODO"); //TODO
    }

    //TODO swagger Documentation
    public Result uploadBinaryFileToBoard(String board_id) {
        try{

            Board board = Board.find.byId(board_id);
            if(board == null ) return GlobalResult.notFoundObject();

            // Přijmu soubor
            Http.MultipartFormData body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart file = body.getFile("file");

            // Its file not null
            if (file == null) return GlobalResult.notFound("File not found");

            return GlobalResult.okResult("Vše v pořádku další operace in In TODO"); //TODO

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - deleteCProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }

    //TODO swagger Documentation
    public Result uploadCompilationToBoard(String c_program_id, String boardId) {

        Board board = Board.find.byId(boardId);
        if(board == null ) return GlobalResult.notFoundObject();

        C_Program c_program = C_Program.find.byId(c_program_id);
        if (c_program == null) return GlobalResult.notFoundObject();

        //TODO Chybí kompilování atd... tohle bude mega metoda!!!
        return GlobalResult.okResult();
    }

///###################################################################################################################*/

    @ApiOperation(value = "Create new Processor",
            tags = {"Processor"},
            notes = "If you want create new Processor. Send required json values and server respond with new object",
            produces = "application/json",
            response =  Processor.class,
            protocols = "https",
            code = 201,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "processor.create", description = "For create new Processor")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = Processor.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Processor_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @BodyParser.Of(BodyParser.Json.class)
    //  @Pattern("processor.create")
    public Result new_Processor() {
        try {

            final Form<Swagger_Processor_New> form = Form.form(Swagger_Processor_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Processor_New help = form.get();


            Processor processor = new Processor();

            processor.description    = help.description;
            processor.processor_code = help.processor_code;
            processor.processor_name = help.processor_name;
            processor.speed          = help.speed;

            processor.save();
            return GlobalResult.created(Json.toJson(processor));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "description", "processor_code", "processor_name ", "speed");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get Processor",
            tags = {"Processor"},
            notes = "If you get Processor by query processor_id.",
            produces = "application/json",
            response =  Processor.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "processor.read", description = "For read  Procesor, you have to own project")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Processor.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("processor.read")
    public Result get_Processor(@ApiParam(value = "processor_id String query", required = true) @PathParam("processor_id") String processor_id) {
        try {

            Processor processor = Processor.find.byId(processor_id);
            if(processor == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(processor));

        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get all Processors",
            tags = {"Processor"},
            notes = "If you want get Processor by query processor_id.",
            produces = "application/json",
            response =  Processor.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "processor.read", description = "For read Processors")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Processor.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    //  @Pattern("processor.read")
    public Result get_Processor_All() {
        try {

           List<Processor> processors = Processor.find.all();
            return GlobalResult.okResult(Json.toJson(processors));

        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "update Processor",
            tags = {"Processor"},
            notes = "If you want update Processor.id by query = processor_id . Send required json values and server respond with update object",
            produces = "application/json",
            response =  Processor.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.edit", description = "For create new C_program, you have to own project")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Processor.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Processor_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @BodyParser.Of(BodyParser.Json.class)
    // @Pattern("processor.edit")
    public Result update_Processor(@ApiParam(value = "processor_id String query", required = true) @PathParam("processor_id") String processor_id) {
        try {

            final Form<Swagger_Processor_New> form = Form.form(Swagger_Processor_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Processor_New help = form.get();


            Processor processor = Processor.find.byId(processor_id);
            if(processor == null ) return GlobalResult.notFoundObject();

            processor.description    = help.description;
            processor.processor_code = help.processor_code;
            processor.processor_name = help.processor_name;
            processor.speed          = help.speed;


            processor.update();

            return GlobalResult.update(Json.toJson(processor));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "description - TEXT", "processor_code - String", "processor_name - String", "speed - Integer", "libraryGroups [Id,Id..]");
        } catch (Exception e) {
            Logger.error("Error", e.getMessage());
            Logger.error("CompilationLibrariesController - update_Processor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "delete Processor",
            tags = {"Processor"},
            notes = "If you want delete Processor by query processor_id.",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "processor.read", description = "For deleting Processor")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("processor.delete")
    public Result delete_Processor(@ApiParam(value = "processor_id String query", required = true) @PathParam("processor_id")String processor_id) {
        try {

            Processor processor = Processor.find.byId(processor_id);
            if(processor == null ) return GlobalResult.notFoundObject();

            processor.delete();

            return GlobalResult.okResult();
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get Processor description",
            tags = {"Processor"},
            notes = "If you get Processor by query processor_id.",
            produces = "application/json",
            response =  Description.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "processor.read", description = "For read  Procesor, you have to own project")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Description.class),
            @ApiResponse(code = 400, message = "Object Not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    //@Pattern("processor.read")
    public Result get_Processor_Description(@ApiParam(value = "processor_id String query", required = true) @PathParam("processor_id")String processor_id) {
        try {

            Processor processor = Processor.find.byId(processor_id);
            if(processor == null ) return GlobalResult.notFoundObject();

            Description description = new Description();
            description.description = processor.description;

            return GlobalResult.okResult(Json.toJson(description));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - get_Processor_Description ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get Processor.LibraryGroups",
            tags = {"Processor", "LibraryGroup"},
            notes = "If you want get all LibraryGroups from Processor by query processor_id.",
            produces = "application/json",
            response =  LibraryGroup.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "processor.read", description = "For read  Procesor, you have to own project")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = LibraryGroup.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Object Not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("processor.read")
    public Result getProcessorLibraryGroups( @ApiParam(value = "processor_id String query", required = true) @PathParam("processor_id")String processor_id) {
        try {
            Processor processor = Processor.find.byId(processor_id);
            if(processor == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(processor.libraryGroups));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - getProcessorLibraryGroups ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get SingleLibraries from Processor object",
            tags = {"Processor", "SingleLibrary"},
            notes = "If you want get all SingleLibraries from Processor by query processor_id.",
            produces = "application/json",
            response =  SingleLibrary.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "processor.read", description = "For read  Processor, you have to own project")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = SingleLibrary.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Object Not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("singleLibraries.read")
    public Result getProcessorSingleLibraries( @ApiParam(value = "processor_id String query", required = true) @PathParam("processor_id") String processor_id) {
        try {
            Processor processor = Processor.find.byId(processor_id);
            if(processor == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(processor.singleLibraries));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - getProcessorSingleLibraries ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "connect Processor with SingleLibraries",
            tags = {"Processor", "SingleLibrary"},
            notes = "If you want connect SingleLibraries with Processor by query processor_id and library_id.",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "processor.edit", description = "For editing Processor")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result"),
            @ApiResponse(code = 400, message = "Object Not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    //  @Pattern("processor.edit")
    public Result connectProcessorWithLibrary( @ApiParam(value = "processor_id String query", required = true) @PathParam("processor_id")String processor_id,  @ApiParam(required = true) @PathParam("library_id") String library_id) {
        try {
            Processor processor = Processor.find.byId(processor_id);
            if(processor == null ) return GlobalResult.notFoundObject();

            SingleLibrary singleLibrary = SingleLibrary.find.byId(library_id);
            if(singleLibrary == null ) return GlobalResult.notFoundObject();


            processor.singleLibraries.add(singleLibrary);
            processor.update();

            return GlobalResult.okResult();
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - connectProcessorWithLibrary ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "connect Processor with LibraryGroup",
            tags = {"Processor", "LibraryGroup"},
            notes = "If you want  connect LibraryGroup with Processor by query processor_id and library_group_id.",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "processor.edit", description = "For editing Processor")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result"),
            @ApiResponse(code = 400, message = "Object Not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    //  @Pattern("processor.edit")
    public Result connectProcessorWithLibraryGroup( @ApiParam(value = "processor_id String query", required = true) @PathParam("processor_id")String processor_id,  @ApiParam(required = true) @PathParam("library_group_id") String library_group_id) {
        try {
            Processor processor = Processor.find.byId(processor_id);
            if(processor == null ) return GlobalResult.notFoundObject();

            LibraryGroup libraryGroup = LibraryGroup.find.byId(library_group_id);
            if(libraryGroup == null ) return GlobalResult.notFoundObject();


            processor.libraryGroups.add(libraryGroup);
            processor.update();

            return GlobalResult.okResult();
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - connectProcessorWithLibraryGroup ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "disconnect Processor with SingleLibraries",
            tags = {"Processor", "SingleLibrary"},
            notes = "If you want disconnect SingleLibraries from Processor by query processor_id and library_id.",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "processor.edit", description = "For editing Processor")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result"),
            @ApiResponse(code = 400, message = "Object Not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("processor.edit")
    public Result disconnectProcessorWithLibrary( @ApiParam(value = "processor_id String query", required = true) @PathParam("processor_id")String processor_id,  @ApiParam(required = true) @PathParam("library_id") String library_id) {
        try {
            Processor processor = Processor.find.byId(processor_id);
            if(processor == null ) return GlobalResult.notFoundObject();

            SingleLibrary singleLibrary = SingleLibrary.find.byId(library_id);
            if(singleLibrary == null ) return GlobalResult.notFoundObject();


            processor.singleLibraries.remove(singleLibrary);
            processor.update();

            return GlobalResult.okResult();
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - connectProcessorWithLibrary ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "disconnect Processor with LibraryGroup",
            tags = {"Processor", "LibraryGroup"},
            notes = "If you want disconnect LibraryGroup from Processor by query processor_id and library_group_id.",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "processor.edit", description = "For editing Processor")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result"),
            @ApiResponse(code = 400, message = "Object Not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("processor.edit")
    public Result disconnectProcessorWithLibraryGroup( @ApiParam(value = "processor_id String query", required = true) @PathParam("processor_id")String processor_id,  @ApiParam(required = true) @PathParam("library_group_id")  String library_group_id) {
        try {
            Processor processor = Processor.find.byId(processor_id);
            if(processor == null ) return GlobalResult.notFoundObject();

            LibraryGroup libraryGroup = LibraryGroup.find.byId(library_group_id);
            if(libraryGroup == null ) return GlobalResult.notFoundObject();


            processor.libraryGroups.remove(libraryGroup);
            processor.update();

            return GlobalResult.okResult();
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - connectProcessorWithLibraryGroup ERROR");
            return GlobalResult.internalServerError();
        }
    }


///###################################################################################################################*/

    @ApiOperation(value = "Create new LibraryGroup",
            tags = {"LibraryGroup"},
            notes = "If you want create new LibraryGroup. Send required json values and server respond with new object",
            produces = "application/json",
            response =  LibraryGroup.class,
            protocols = "https",
            code = 201,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "libraryGroup.create", description = "For create new Processor")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = LibraryGroup.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_LibraryGroup_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @BodyParser.Of(BodyParser.Json.class)
    // @Pattern("libraryGroup.create")
    public Result new_LibraryGroup() {
        try {

            final Form<Swagger_LibraryGroup_New> form = Form.form(Swagger_LibraryGroup_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_LibraryGroup_New help = form.get();

            LibraryGroup libraryGroup = new LibraryGroup();
            libraryGroup.description = help.description;
            libraryGroup.group_name = help.group_name;
            libraryGroup.azurePackageLink = "libraryGroup"; // TODO? -> Nějaké třídění ??? (Private, Public,.. etc?)
            libraryGroup.setUniqueAzureStorageLink();
            libraryGroup.save();

            return GlobalResult.created(Json.toJson(libraryGroup));
        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "description", "group_name");
        }catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - connectProcessorWithLibraryGroup ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "Create new Version in LibraryGroup",
            tags = {"LibraryGroup"},
            notes = "If you want create new versinon in LibraryGroup query = libraryGroup_id. Send required json values and server respond with new object",
            produces = "application/json",
            response =  Version_Object.class,
            protocols = "https",
            code = 201,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "libraryGroup.edit", description = "For create new Processor")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = Version_Object.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_LibraryGroup_Version",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    //  @Pattern("libraryGroup.edit")
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_LibraryGroup_Version(@ApiParam(value = "libraryGroup_id String query", required = true) @PathParam("libraryGroup_id") String libraryGroup_id){
        try {

            final Form<Swagger_LibraryGroup_Version> form = Form.form(Swagger_LibraryGroup_Version.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_LibraryGroup_Version help = form.get();

            LibraryGroup libraryGroup = LibraryGroup.find.byId(libraryGroup_id);
            if(libraryGroup == null) return GlobalResult.notFoundObject();

            Version_Object versionObjectObject     = new Version_Object();

            if(libraryGroup.version_objects.isEmpty() ) versionObjectObject.azureLinkVersion = 1;
            else versionObjectObject.azureLinkVersion    = ++libraryGroup.version_objects.get(0).azureLinkVersion; // Zvednu verzi o jednu

            versionObjectObject.date_of_create = new Date();
            versionObjectObject.version_name        = help.version_name;
            versionObjectObject.version_description = help.version_description;
            versionObjectObject.libraryGroup        = libraryGroup;
            versionObjectObject.save();


            libraryGroup.version_objects.add(versionObjectObject);
            libraryGroup.update();

            return GlobalResult.created(Json.toJson(versionObjectObject));
        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "version_description", "version_name");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get all Versions from LibraryGroup",
            tags = {"LibraryGroup"},
            notes = "If you want create new versinon in LibraryGroup query = libraryGroup_id. Send required json values and server respond with new object",
            produces = "application/json",
            response =  Version_Object.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "libraryGroup.edit", description = "For create new Processor")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok result",      response = Version_Object.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("libraryGroup.read")
    public Result get_LibraryGroup_Version(@ApiParam(value = "libraryGroup_id String query", required = true) @PathParam("libraryGroup_id") String libraryGroup_id){
        try {

            LibraryGroup libraryGroup = LibraryGroup.find.byId(libraryGroup_id);
            if(libraryGroup == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(libraryGroup.version_objects));

        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("CompilationLibrariesController - getVersionLibraryGroup ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "upload files to Version in LibraryGroup",
            tags = {"LibraryGroup"},
            notes = "Its not possible now describe uploud file in Swagger. But file name must be longer than 5 chars." +
                    "in body of html content is \"files\"",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "libraryGroup.edit", description = "For create new Processor")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok result"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("libraryGroup.edit")
    public Result upload_Library_To_LibraryGroup(@ApiParam(value = "libraryGroup_id String query", required = true) @PathParam("libraryGroup_id") String libraryGroup_id, @ApiParam(required = true) @PathParam("version_id") String version_id) {
        try {

            // Přijmu soubor
            Http.MultipartFormData body = request().body().asMultipartFormData();
            List<Http.MultipartFormData.FilePart> files = body.getFiles();

            for( Http.MultipartFormData.FilePart file :  files ) {
                System.out.println("Nahrávám soubor: " + file.getFilename());

                // If fileRecord group is not null
                LibraryGroup libraryGroup = LibraryGroup.find.byId(libraryGroup_id);
                if (libraryGroup == null) return GlobalResult.notFoundObject();

                Version_Object versionObjectObject = Version_Object.find.where().in("libraryGroup.id", libraryGroup.id).where().eq("id", version_id).findUnique();
                if (versionObjectObject == null) return GlobalResult.notFoundObject();

                // Control lenght of name
                String fileName = file.getFilename();
                if (fileName.length() < 5) GlobalResult.forbidden_Global("Too short file name");

                // Ještě kontrola souboru zda už tam není - > Version_Object a knihovny
                FileRecord fileRecord = FileRecord.find.where().in("version_object.id", versionObjectObject.id).ieq("file_name", fileName).findUnique();
                if (fileRecord != null) return GlobalResult.nullPointerResult("File exist in this version -> " + fileName + " please, create new version!");

                // Mám soubor
                File libraryFile = file.getFile();

                // Připojuji se a tvořím cestu souboru
                CloudBlobContainer container = Server.blobClient.getContainerReference("libraries");

                String azurePath = libraryGroup.azurePackageLink + "/" + libraryGroup.azureStorageLink + "/" + versionObjectObject.azureLinkVersion + "/" + fileName;

                CloudBlockBlob blob = container.getBlockBlobReference(azurePath);

                blob.upload(new FileInputStream(libraryFile), libraryFile.length());

                fileRecord = new FileRecord();
                fileRecord.file_name = fileName;
                fileRecord.version_object = versionObjectObject;
                fileRecord.save();

                versionObjectObject.save();
            }

            return GlobalResult.okResult();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("CompilationLibrariesController - upload_Library_To_LibraryGroup ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get LibraryGroup",
            tags = {"LibraryGroup"},
            notes = "If you want get LibraryGroup by query = libraryGroup_id",
            produces = "application/json",
            response =  LibraryGroup.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "libraryGroup.read", description = "For get LibraryGroup")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok result",      response = LibraryGroup.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    //  @Pattern("libraryGroup.read")
    public Result get_LibraryGroup(@ApiParam(value = "libraryGroup_id String query", required = true) @PathParam("libraryGroup_id") String libraryGroup_id) {
        try {

            LibraryGroup libraryGroup = LibraryGroup.find.byId(libraryGroup_id);
            if(libraryGroup == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(libraryGroup));
        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - getLibraryGroup ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "delete LibraryGroup",
            tags = {"LibraryGroup"},
            notes = "If you want delete LibraryGroup by query = libraryGroup_id",
            produces = "application/json",
            response =  Result_ok.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "libraryGroup.delete", description = "For delete LibraryGroup")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok result",      response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("libraryGroup.delete")
    public Result delete_LibraryGroup(@ApiParam(value = "libraryGroup_id String query", required = true) @PathParam("libraryGroup_id") String libraryGroup_id) {
        try {
            LibraryGroup libraryGroup = LibraryGroup.find.byId(libraryGroup_id);
            if(libraryGroup == null) return GlobalResult.notFoundObject();

            UtilTools.azureDelete(Server.blobClient.getContainerReference("libraries"), libraryGroup.azurePackageLink+"/"+libraryGroup.azureStorageLink);

            libraryGroup.delete();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - delete_LibraryGroup ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "edit LibraryGroup",
            tags = {"LibraryGroup"},
            notes = "If you want edit LibraryGroup by query libraryGroup_id. Send required json values and server respond with new object",
            produces = "application/json",
            response =  LibraryGroup.class,
            protocols = "https",
            code = 201,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "processor.create", description = "For create new Processor")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = LibraryGroup.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_LibraryGroup_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    //  @Pattern("libraryGroup.edit")
    @BodyParser.Of(BodyParser.Json.class)
    public Result editLibraryGroup(@ApiParam(value = "libraryGroup_id String query", required = true) @PathParam("libraryGroup_id") String libraryGroup_id) {
        try {

            final Form<Swagger_LibraryGroup_New> form = Form.form(Swagger_LibraryGroup_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_LibraryGroup_New help = form.get();

            LibraryGroup libraryGroup = LibraryGroup.find.byId(libraryGroup_id);
            if(libraryGroup == null) return GlobalResult.notFoundObject();


            libraryGroup.description = help.description;
            libraryGroup.group_name = help.group_name;

            libraryGroup.save();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - updateLibraryGroup ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get LibraryGroup description",
            tags = {"LibraryGroup"},
            notes = "If you want get description from LibraryGroup by query = libraryGroup_id",
            produces = "application/json",
            response =  Description.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "libraryGroup.read", description = "For get LibraryGroup")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok result",      response = Description.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    //  @Pattern("libraryGroup.read")
    public Result get_LibraryGroup_Description(@ApiParam(value = "libraryGroup_id String query", required = true) @PathParam("libraryGroup_id") String libraryGroup_id) {
        try {
            LibraryGroup libraryGroup = LibraryGroup.find.byId(libraryGroup_id);
            if(libraryGroup == null) return GlobalResult.notFoundObject();

            Description description = new Description();
            description.description = libraryGroup.description;

            return GlobalResult.okResult(Json.toJson(description));

        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - get_LibraryGroup_Description ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get LibraryGroup Processors",
            tags = {"LibraryGroup", "Processor"},
            notes = "If you want get Processors from LibraryGroup by query = libraryGroup_id",
            produces = "application/json",
            response =  Processor.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "processor.read", description = "For get Processors")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok result",      response = Processor.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("processor.read")
    public Result get_LibraryGroup_Processors(@ApiParam(value = "libraryGroup_id String query", required = true) @PathParam("libraryGroup_id") String libraryGroup_id) {
        try {
            LibraryGroup libraryGroup = LibraryGroup.find.byId(libraryGroup_id);
            if(libraryGroup == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(libraryGroup.processors));

        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - get_LibraryGroup_Processors ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get Libraries from LibraryGroup",
            tags = {"LibraryGroup"},
            notes = "If you want get Processors from LibraryGroup by query = libraryGroup_id",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "processor.read", description = "For get Processors")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok result",      response = FileRecord.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("libraryGroup.read")
    public Result get_LibraryGroup_Libraries(@ApiParam(value = "libraryGroup_id String query", required = true) @PathParam("libraryGroup_id") String libraryGroup_id, @ApiParam(required = true) @PathParam("version_id") String version_id) {
        try {

            Version_Object versionObjectObject = Version_Object.find.where().in("libraryGroup.id", libraryGroup_id).eq("id",version_id).setMaxRows(1).findUnique();
            if(versionObjectObject == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(versionObjectObject.files));
        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - get_LibraryGroup_Libraries ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get Libraries from LibraryGroup Version",
            tags = {"LibraryGroup"},
            notes = "If you want get Libraries from LibraryGroup.Version by query = version_id",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "libraryGroup.read", description = "For get Libraries")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok result",  response = FileRecord.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("libraryGroup.read")
    public Result get_LibraryGroup_Version_Libraries(@ApiParam(required = true) @PathParam("version_id") String version_id){
        try {
            Version_Object versionObject = Version_Object.find.byId(version_id);
            if(versionObject == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(versionObject.files));
        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - get_LibraryGroup_Version_Libraries ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get LibraryGroup with Filters parameters",
            tags = {"LibraryGroup"},
            notes = "If you want get all or only some LibraryGroups you can use filter parameters in Json. But EveryTime i will return maximal 25 objects \n\n" +
                    "so, you have to used that limit for frontend pagination -> first round (0,25), second round (26, 50) etc... \n ",
            produces = "application/json",
            response =  LibraryGroup.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "libraryGroup.read", description = "For get Libraries")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_LibraryGroup_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok result",               response = LibraryGroup.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    //   @Pattern("libraryGroup.read")
    @BodyParser.Of(BodyParser.Json.class)
    public Result get_LibraryGroup_Filter() {
        try {

            final Form<Swagger_LibraryGroup_Filter> form = Form.form(Swagger_LibraryGroup_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_LibraryGroup_Filter help = form.get();

            System.out.println("JSem aspoň zde");
            Query<LibraryGroup> query = Ebean.find(LibraryGroup.class);


            // If contains confirms
            if (help.processors_id != null) {
                List<String> list = help.processors_id;
                Set<String> set = new HashSet<>(list);
                query.where().in("processors.id", set);
            }

            if (help.group_name != null) {
                String group_name = help.group_name;
                query.where().ieq("group_name", group_name);
            }

            if (help.count_from != null) {
                Integer countFrom = help.count_from;
                query.setFirstRow(countFrom);
            }

            if (help.count_to !=null) {
                Integer countTo = help.count_to;
                query.setMaxRows(countTo);
            }

            if (help.order != null) {

                String order = help.order;
                String value = help.value;

                OrderBy<LibraryGroup> orderBy = new OrderBy<>();

                if (order.equals("asc")) orderBy.asc(value);
                else if (order.equals("desc")) orderBy.desc(value);

                query.setOrder(orderBy);
            }


            List<LibraryGroup> list = query.findList();


            return GlobalResult.okResult(Json.toJson(list));

        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - get_LibraryGroup_Version_Libraries ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

///###################################################################################################################*/

    public Result fileRecord(String id){

        try {
            FileRecord fileRecord = FileRecord.find.byId(id);
            if (fileRecord == null) return GlobalResult.notFoundObject();

            ObjectNode result = Json.newObject();
            result.put("file_name", fileRecord.file_name);
            result.put("content", fileRecord.get_fileRecord_from_Azure_inString());

            return GlobalResult.okResult(Json.toJson(result));

        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - get_LibraryGroup_Version_Libraries ERROR");
            e.printStackTrace();
            return GlobalResult.internalServerError();
        }
    }

///###################################################################################################################*/

    @ApiOperation(value = "create new SingleLibrary",
            tags = {"SingleLibrary"},
            notes = "if you want create new SingleLibrary for C_program compilation",
            produces = "application/json",
            response =  SingleLibrary.class,
            protocols = "https",
            code = 201,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "library.create", description = "For crating Libraries")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_SingleLibrary_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = SingleLibrary.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    //  @Pattern("library.create")
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_SingleLibrary() {
        try {

            final Form<Swagger_SingleLibrary_New> form = Form.form(Swagger_SingleLibrary_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_SingleLibrary_New help = form.get();


            SingleLibrary singleLibrary = new SingleLibrary();
            singleLibrary.library_name = help.library_name;
            singleLibrary.description = help.description;
            singleLibrary.azurePackageLink = "singleLibraries";
            singleLibrary.setUniqueAzureStorageLink();

            singleLibrary.save();

            return GlobalResult.created(Json.toJson(singleLibrary));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "library_name", "description");
        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - get_LibraryGroup_Version_Libraries ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "create new SingleLibrary",
            tags = {"SingleLibrary"},
            notes = "if you want create new SingleLibrary for C_program compilation",
            produces = "application/json",
            response =  Version_Object.class,
            protocols = "https",
            code = 201,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "library.edit", description = "For crating Libraries")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_SingleLibrary_Version",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {

            @ApiResponse(code = 201, message = "Successful created",      response = Version_Object.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("library.edit")
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_SingleLibrary_Version(@ApiParam(value = "library_id String query", required = true) @PathParam("library_id")  String library_id){
        try {
            final Form<Swagger_SingleLibrary_Version> form = Form.form(Swagger_SingleLibrary_Version.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_SingleLibrary_Version help = form.get();


            SingleLibrary singleLibrary = SingleLibrary.find.byId(library_id);
            if(singleLibrary == null)  return GlobalResult.notFoundObject();

            Version_Object versionObjectObject = new Version_Object();

            if(singleLibrary.version_objects.isEmpty() ) versionObjectObject.azureLinkVersion = 1;
            else versionObjectObject.azureLinkVersion    = ++singleLibrary.version_objects.get(0).azureLinkVersion; // Zvednu verzi o jednu

            versionObjectObject.date_of_create = new Date();
            versionObjectObject.version_name = help.version_name;
            versionObjectObject.version_description = help.version_description;
            versionObjectObject.singleLibrary = singleLibrary;
            versionObjectObject.save();

            return GlobalResult.created(Json.toJson(versionObjectObject));

        } catch (NullPointerException a) {
            return GlobalResult.nullPointerResult(a, "description", "version_name - String");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get SingleLibrary versions",
            tags = {"SingleLibrary"},
            notes = "if you want create new SingleLibrary for C_program compilation",
            produces = "application/json",
            response =  Version_Object.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "library.read", description = "For crating Libraries")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok result",               response = Version_Object.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("library.read")
    public Result get_SingleLibrary_Versions(@ApiParam(value = "library_id String query", required = true) @PathParam("library_id")  String library_id) {
        try {

            SingleLibrary singleLibrary = SingleLibrary.find.byId(library_id);
            if(singleLibrary == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(singleLibrary.version_objects));

        } catch (Exception e) {
            return GlobalResult.nullPointerResult(e);
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result upload_SingleLibrary_Version(@ApiParam(value = "library_id String query", required = true) @PathParam("library_id")  String library_id, @ApiParam(required = true) @PathParam("version_id") String version_id){
        // TODO dokumentace
        try{
            Http.MultipartFormData body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart file = body.getFile("file");

            SingleLibrary singleLibrary = SingleLibrary.find.byId(library_id);
            if(singleLibrary == null ) return GlobalResult.notFoundObject();

            // If fileRecord group is not null
            Version_Object versionObjectObject = Version_Object.find.where().in("singleLibrary.id", library_id).eq("azureLinkVersion",version_id).setMaxRows(1).findUnique();
            if(versionObjectObject == null ) return GlobalResult.badRequest("Version_Object in library not Exist: -> " +version_id);

            if (versionObjectObject.files.size() > 0) return GlobalResult.badRequest("Version_Object has file already.. Create new Version_Object ");

            // Control lenght of name
            String fileName = file.getFilename();
            if(fileName.length()< 5 )return GlobalResult.badRequest("Too short FileName -> " + fileName);

            File libraryFile = file.getFile();

            FileRecord fileRecord =  new FileRecord();
            fileRecord.file_name = fileName;
            fileRecord.save();

            CloudBlobContainer container = Server.blobClient.getContainerReference("libraries");
            String azurePath = singleLibrary.azurePackageLink + "/" + singleLibrary.azureStorageLink + "/"+ versionObjectObject.azureLinkVersion  +"/" + fileRecord.file_name;
            CloudBlockBlob blob = container.getBlockBlobReference(azurePath);

            blob.upload(new FileInputStream(libraryFile), libraryFile.length());

            versionObjectObject.files.add(fileRecord);
            versionObjectObject.date_of_create = new Date();
            versionObjectObject.update();

            return GlobalResult.okResult(Json.toJson(versionObjectObject));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get SingleLibrary",
            tags = {"SingleLibrary"},
            notes = "if you want get SingleLibrary by query = library_id",
            produces = "application/json",
            response =  SingleLibrary.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "library.read", description = "For crating Libraries")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok result",                response = SingleLibrary.class),
            @ApiResponse(code = 401, message = "Unauthorized request",     response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("library.read")
    public Result get_SingleLibrary(@ApiParam(value = "library_id String query", required = true) @PathParam("library_id")  String library_id) {
        try {

            SingleLibrary singleLibrary = SingleLibrary.find.byId(library_id);
            if(singleLibrary == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(singleLibrary));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get Library with Filters parameters",
            tags = {"SingleLibrary"},
            notes = "If you want get all or only some SingleLibraries you can use filter parameters in Json. But EveryTime i will return maximal 25 objects \n\n" +
                    "so, you have to used that limit for frontend pagination -> first round (0,25), second round (26, 50) etc... I will give you also" +
                    "information how many results you can show \n ",
            produces = "application/json",
            response =  SingleLibrary.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "libraryGroup.read", description = "For get Libraries")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_SingleLibrary_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok result",               response = SingleLibrary.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("libraryGroup.read")
    @BodyParser.Of(BodyParser.Json.class)
    public Result get_SingleLibrary_Filter() {
        try {

            final Form<Swagger_SingleLibrary_Filter> form = Form.form(Swagger_SingleLibrary_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_SingleLibrary_Filter help = form.get();

            Query<SingleLibrary> query = Ebean.find(SingleLibrary.class);

            // If contains confirms
            if (help.processors_id != null) {
                List<String> list = help.processors_id;
                Set<String> set = new HashSet<>(list);
                query.where().in("processors.id", set);
            }

            if (help.library_name != null) {
                String group_name = help.library_name;
                query.where().ieq("library_name", group_name);
            }

            if (help.count_from != null) {
                Integer countFrom = help.count_from;
                query.setFirstRow(countFrom);
            }

            if (help.count_to != null) {
                Integer count_to = help.count_to;
                query.setMaxRows(count_to);
            }

            if (help.order != null) {

                String order = help.order;
                String value = help.value;

                OrderBy<SingleLibrary> orderBy = new OrderBy<>();

                if (order.equals("asc")) orderBy.asc(value);
                else if (order.equals("desc")) orderBy.desc(value);

                query.setOrder(orderBy);
            }


            List<SingleLibrary> list = query.findList();


            return GlobalResult.okResult(Json.toJson(list));
        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - get_LibraryGroup_Version_Libraries ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "edit SingleLibrary",
            tags = {"SingleLibrary"},
            notes = "if you want edit name or description of SingleLibrary by query = library_id",
            produces = "application/json",
            response =  SingleLibrary.class,
            protocols = "https",
            code = 201,
            authorizations = {
                    @Authorization(
                            value="library.edit",
                            scopes = { @AuthorizationScope(scope = "library.create", description = "For crating Libraries")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_SingleLibrary_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok result",               response = SingleLibrary.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("library.edit")
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_SingleLibrary(@ApiParam(value = "library_id String query", required = true) @PathParam("library_id") String library_id) {
        try {

            final Form<Swagger_SingleLibrary_New> form = Form.form(Swagger_SingleLibrary_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_SingleLibrary_New help = form.get();

            SingleLibrary singleLibrary = SingleLibrary.find.byId(library_id);
            if(singleLibrary == null) return GlobalResult.notFoundObject();

            singleLibrary.library_name = help.library_name;
            singleLibrary.description = help.description;


            singleLibrary.update();

            return GlobalResult.okResult();
        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "library_name", "description");
        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - get_LibraryGroup_Version_Libraries ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "delete SingleLibrary",
            tags = {"SingleLibrary"},
            notes = "If you want delete SingleLibrary by query = library_id",
            produces = "application/json",
            response =  Result_ok.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "library.delete", description = "For delete SingleLibrary")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok result",               response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    //  @Pattern("library.delete")
    public Result delete_SingleLibrary(@ApiParam(value = "library_id String query", required = true) @PathParam("library_id") String library_id) {
        try {

            SingleLibrary singleLibrary = SingleLibrary.find.byId(library_id);
            if(singleLibrary == null) return GlobalResult.notFoundObject();

            UtilTools.azureDelete(Server.blobClient.getContainerReference("libraries"), singleLibrary.azurePackageLink+"/"+singleLibrary.azureStorageLink);

            singleLibrary.delete();
            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - get_LibraryGroup_Version_Libraries ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

///###################################################################################################################*/

    @ApiOperation(value = "create new Producer",
            tags = {"Producer"},
            notes = "if you want create new Producer. Its company owned physical boards and we used that for filtering",
            produces = "application/json",
            response =  Producer.class,
            protocols = "https",
            code = 201,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "producer.create", description = "Person need this permission"),
                                    @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Producer_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = Producer.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("producer.create")
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_Producer() {
        try {
            final Form<Swagger_Producer_New> form = Form.form(Swagger_Producer_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Producer_New help = form.get();

            Producer producer = new Producer();
            producer.name = help.name;
            producer.description = help.description;

            producer.save();

            return GlobalResult.created(Json.toJson(producer));
        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "description", "name");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "edit Producer",
            tags = {"Producer"},
            notes = "if you want edit information about Producer. Its company owned physical boards and we used that for filtering",
            produces = "application/json",
            response =  Producer.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "producer.edit", description = "Person need this permission"),
                                    @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Producer_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Producer.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("producer.edit")
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_Producer(@ApiParam(required = true) @PathParam("producer_id") String producer_id) {
        try {
            final Form<Swagger_Producer_New> form = Form.form(Swagger_Producer_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Producer_New help = form.get();


            Producer producer = Producer.find.byId(producer_id);
            if(producer == null ) return GlobalResult.notFoundObject();

            producer.name = help.name;
            producer.description = help.description;

            producer.update();

            return GlobalResult.okResult(Json.toJson(producer));
        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "description", "name");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - edit_Producer ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get all Producers",
            tags = {"Producer"},
            notes = "if you want get list of Producers. Its list of companies owned physical boards and we used that for filtering",
            produces = "application/json",
            response =  Producer.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "producer.edit", description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Producer.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("producer.read")
    public Result get_Producers() {
        try {

            List<Producer> producers = Producer.find.all();

            return GlobalResult.okResult(Json.toJson(producers));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - get_Producers ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get Producer",
            tags = {"Producer"},
            notes = "if you want get Producer. Its company owned physical boards and we used that for filtering",
            produces = "application/json",
            response =  Producer.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "producer.read", description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Producer.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    //  @Pattern("producer.read")
    public Result get_Producer(@ApiParam(required = true) @PathParam("producer_id") String producer_id) {
        try {
            Producer producer = Producer.find.byId(producer_id);

            if(producer == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(producer));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - get_Producer ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "delete Producer",
            tags = {"Producer"},
            notes = "if you want delete Producer",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "producer.read", description = "Person need this permission"),
                                    @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("producer.delete")
    public Result delete_Producer(@ApiParam(required = true) @PathParam("producer_id") String producer_id) {
        try {
            Producer producer = Producer.find.byId(producer_id);

            if(producer == null ) return GlobalResult.notFoundObject();

            producer.delete();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - get_Producer ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get Producer description",
            tags = {"Producer"},
            notes = "if you get Producer object his description is hiding under this link",
            produces = "application/json",
            response =  Description.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "producer.read", description = "Person need this permission"),
                                    @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Description.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("producer.read")
    public Result get_Producer_Description(@ApiParam(required = true) @PathParam("producer_id") String producer_id) {
        try {
            Producer producer = Producer.find.byId(producer_id);

            if(producer == null ) return GlobalResult.notFoundObject();

            Description description = new Description();
            description.description = producer.description;

            return GlobalResult.okResult(Json.toJson(description));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - get_Producer_Description ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get TypeOfBoard from Producer",
            tags = {"Producer", "Type-Of-Board"},
            notes = "if you want get TypeOfBoard from Producer. Its a list of Boards types.",
            produces = "application/json",
            response =  TypeOfBoard.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "type_of_board.read", description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")
                            }
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = TypeOfBoard.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("type_of_board.read")
    public Result get_Producer_TypeOfBoards(@ApiParam(required = true) @PathParam("producer_id") String producer_id) {
        try {
            Producer producer = Producer.find.byId(producer_id);
            if(producer == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(producer.type_of_boards));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - get_Producer_TypeOfBoards ERROR");
            return GlobalResult.internalServerError();
        }
    }



///###################################################################################################################*/

    @ApiOperation(value = "create TypeOfBoard",
            tags = { "Type-Of-Board"},
            notes = "The TypeOfBoard is category for IoT. Like Raspberry2, Arduino-Uno etc. \n\n" +
                    "We using that for compilation, sorting libraries, filtres and more..",
            produces = "application/json",
            response =  TypeOfBoard.class,
            protocols = "https",
            code = 201,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "type_of_board.create", description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfBoard_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = TypeOfBoard.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_TypeOfBoard() {
        try {
            final Form<Swagger_TypeOfBoard_New> form = Form.form(Swagger_TypeOfBoard_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TypeOfBoard_New help = form.get();

            Producer producer = Producer.find.byId(help.producer_id);
            if(producer == null ) return GlobalResult.notFoundObject();

            Processor processor = Processor.find.byId(help.processor_id);
            if(processor == null ) return GlobalResult.notFoundObject();

            TypeOfBoard typeOfBoard = new TypeOfBoard();
            typeOfBoard.name = help.name;
            typeOfBoard.description = help.description;
            typeOfBoard.processor = processor;
            typeOfBoard.producer = producer;

            typeOfBoard.save();

            return GlobalResult.okResult(Json.toJson(typeOfBoard));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "description","name", "processor_id", "producer_id");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - new_TypeOfBoard ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "edit TypeOfBoard",
            tags = { "Type-Of-Board"},
            notes = "if you want edit base TypeOfBoard information",
            produces = "application/json",
            response =  TypeOfBoard.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "type_of_board.edit", description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfBoard_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = TypeOfBoard.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    // @Pattern("type_of_board.edit")
    public Result edit_TypeOfBoard(@ApiParam(required = true) @PathParam("type_of_board_id") String type_of_board_id) {
        try {
            final Form<Swagger_TypeOfBoard_New> form = Form.form(Swagger_TypeOfBoard_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TypeOfBoard_New help = form.get();

            TypeOfBoard typeOfBoard = TypeOfBoard.find.byId(type_of_board_id);
            if (typeOfBoard == null) return GlobalResult.notFoundObject();

            Producer producer = Producer.find.byId(help.producer_id);
            if(producer == null ) return GlobalResult.notFoundObject();

            Processor processor = Processor.find.byId(help.processor_id);
            if(processor == null ) return GlobalResult.notFoundObject();


            typeOfBoard.name = help.name;
            typeOfBoard.description = help.description;
            typeOfBoard.processor = processor;
            typeOfBoard.producer = producer;

            typeOfBoard.update();

            return GlobalResult.okResult(Json.toJson(typeOfBoard));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "description", "name");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - edit_TypeOfBoard ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }

    }

    @ApiOperation(value = "delete TypeOfBoard",
            tags = { "Type-Of-Board"},
            notes = "if you want delete TypeOfBoard object by query = type_of_board_id",
            produces = "application/json",
            response =  Result_ok.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "type_of_board.delete", description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin",         description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    //TODO dokumentace Issue TYRION-88 (http://youtrack.byzance.cz/youtrack/issue/TYRION-88)
    // @Pattern("type_of_board.delete")
    public Result delete_TypeOfBoard(@ApiParam(required = true) @PathParam("type_of_board_id") String type_of_board_id) {
        try {

            TypeOfBoard typeOfBoard = TypeOfBoard.find.byId(type_of_board_id);
            if(typeOfBoard == null ) return GlobalResult.notFoundObject();

            typeOfBoard.delete();

            return GlobalResult.okResult();

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "description","name");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - edit_TypeOfBoard ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get list of all TypeOfBoard",
            tags = { "Type-Of-Board"},
            notes = "if you want get all TypeOfBoard objects",
            produces = "application/json",
            response =  TypeOfBoard.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "type_of_board.read", description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin",       description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = TypeOfBoard.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    //  @Pattern("type_of_board.read")
    public Result get_TypeOfBoard_all() {
        try {

            List<TypeOfBoard> typeOfBoards = TypeOfBoard.find.all();
            return  GlobalResult.okResult(Json.toJson(typeOfBoards));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - get_TypeOfBoard_all ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get TypeOfBoard",
            tags = { "Type-Of-Board"},
            notes = "if you want get TypeOfBoard object by query = type_of_board_id",
            produces = "application/json",
            response =  TypeOfBoard.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "type_of_board.read", description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin",       description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = TypeOfBoard.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    //  @Pattern("type_of_board.read")
    public Result get_TypeOfBoard(@ApiParam(required = true) @PathParam("type_of_board_id") String type_of_board_id) {
        try {

            TypeOfBoard typeOfBoard = TypeOfBoard.find.byId(type_of_board_id);
            if(typeOfBoard == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(typeOfBoard));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - get_Producer_TypeOfBoards ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get TypeOfBoard description",
            tags = { "Type-Of-Board"},
            notes = "if you want get description of TypeOfBoard object by query = type_of_board_id",
            produces = "application/json",
            response =  TypeOfBoard.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "type_of_board.read", description = "Person need this permission"),
                                    @AuthorizationScope(scope = "SuperAdmin",       description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = TypeOfBoard.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    //  @Pattern("type_of_board.read")
    public Result get_TypeOfBoard_Description(@ApiParam(required = true) @PathParam("type_of_board_id") String type_of_board_id) {
        try {

            TypeOfBoard typeOfBoard = TypeOfBoard.find.byId(type_of_board_id);
            if(typeOfBoard == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(typeOfBoard.description));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - get_Producer_TypeOfBoards ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get all Boards from TypeOfBoard",
            tags = { "Type-Of-Board"},
            notes = "if you want get physics Boards from TypeOfBoard  by query = type_of_board_id",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "type_of_board.read", description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin",       description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Board.class, responseContainer = "List"), //TODO list
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result getTypeOfBoardAllBoards(@ApiParam(required = true) @PathParam("type_of_board_id") String type_of_board_id) {
        try {

            TypeOfBoard typeOfBoard = TypeOfBoard.find.byId(type_of_board_id);
            if(typeOfBoard == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(typeOfBoard.boards));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - get_Producer_TypeOfBoards ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get TypeOfBoard by Filter",
            tags = { "Type-Of-Board"},
            notes = "get List of TypeOfBoard by filter",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "type_of_board.edit", description = "Person need this permission"),
                                    @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfBoard_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = TypeOfBoard.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result get_TypeOfBoard_Filter(){
        try {

            final Form<Swagger_TypeOfBoard_Filter> form = Form.form(Swagger_TypeOfBoard_Filter.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TypeOfBoard_Filter help = form.get();


            Query<Swagger_TypeOfBoard_Filter> query = Ebean.find(Swagger_TypeOfBoard_Filter.class);


            if(help.producer_name != null){
                query.where().eq("producer.name", help.producer_name);
            }

            if(help.processor_name != null){
                query.where().eq("processor.processor_name", help.processor_name);
            }

            if(help.count_from != null){
                query.setFirstRow(help.count_from);
            }

            if(help.count_to != null){
                query.setMaxRows(help.count_to);
            }

            if(help.order != null){

                String order = help.order;
                String value = help.value;

                OrderBy<Swagger_TypeOfBoard_Filter> orderBy = new OrderBy<>();

                if(order.equals("asc")) orderBy.asc(value);
                else if (order.equals("desc")) orderBy.desc(value);

                query.setOrder(orderBy);
            }



            List<Swagger_TypeOfBoard_Filter> list = query.findList();



            return GlobalResult.okResult(Json.toJson(list));




        } catch (Exception e) {
            Logger.error("Error", e);
            return GlobalResult.internalServerError();
        }
    }

    ///###################################################################################################################*/

    @ApiOperation(value = "create Board",
            tags = { "Board"},
            notes = "This Api is using only for developing mode, for registration of our Board - in future it will be used only by machine in factory or " +
                    "boards themselves with \"registration procedure\". Its not allowed to delete that! Only deactivate. Classic User can registed that to own " +
                    "project or own account",
            produces = "application/json",
            response =  Board.class,
            protocols = "https",
            code = 201,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "type_of_board.create", description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Board_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = Board.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_Board() {
        try {

            final Form<Swagger_Board_New> form = Form.form(Swagger_Board_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Board_New help = form.get();


            if (Board.find.byId( help.hardware_unique_id ) != null) return GlobalResult.badRequest("Duplicate database value");

            TypeOfBoard typeOfBoard = TypeOfBoard.find.byId( help.type_of_board_id  );
            if(typeOfBoard == null ) return GlobalResult.notFoundObject();

            Board board = new Board();
            board.id =  help.hardware_unique_id;
            board.isActive = false;
         // TODO   board.type_of_board = typeOfBoard;

            board.save();

            return GlobalResult.okResult(Json.toJson(board));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - newBoard ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "edit Board - update personal description",
            tags = { "Board"},
            notes = "Used for add descriptions by owners. \"Persons\" who registred \"Board\" to own \"Projec\" ",
            produces = "application/json",
            response =  Board.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "type_of_board.create", description = "Person need this permission"),
                                    @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Board_Personal",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Board.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_Board_User_Description(@ApiParam(required = true) @PathParam("board_id") String board_id){
        try {

            final Form<Swagger_Board_Personal> form = Form.form(Swagger_Board_Personal.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Board_Personal help = form.get();

            Board board = Board.find.byId(board_id);
            if(board == null ) return GlobalResult.notFoundObject();

            board.personal_description = help.personal_description;
            board.update();

            return GlobalResult.okResult(Json.toJson(board));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "personal_description");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - newBoard ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get Boards with filter parameters",
            tags = { "Board"},
            notes = "Get List of boards ",
            produces = "application/json",
            response =  Board.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "board.read", description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Board_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Board.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    // @Pattern("board.read")
    public Result get_Board_Filter() {
        try {

            final Form<Swagger_Board_Filter> form = Form.form(Swagger_Board_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Board_Filter help = form.get();


            Query<Board> query = Ebean.find(Board.class);

            // If contains HashTags
            if(help.typeOfBoards != null ){
                List<String> stringList = help.typeOfBoards;
                Set<String> stringListSet = new HashSet<>(stringList);
                query.where().in("type_of_board.id", stringListSet);

            }

            // If contains confirms
            if(help.active != null){
                Boolean isActive = help.active.equals("true");
                query.where().eq("isActive", isActive);
            }

            // From date
            if(help.projects != null){
                List<String> stringList = help.projects;
                Set<String> stringListSet = new HashSet<>(stringList);
                query.where().in("projects.id", stringListSet);
            }


            if(help.producers != null){
                List<String> stringList = help.producers;
                Set<String> stringListSet = new HashSet<>(stringList);
                query.where().in("type_of_board.producer.id", stringListSet);
            }

            if(help.processors != null){
                List<String> stringList = help.processors;
                Set<String> stringListSet = new HashSet<>(stringList);
                query.where().in("type_of_board.processor.id", stringListSet);
            }

            List<Board> list = query.findList();



            return GlobalResult.okResult(Json.toJson(list));




        } catch (Exception e){
            e.printStackTrace();
            return GlobalResult.nullPointerResult(e);
        }



    }

    @ApiOperation(value = "deactivate Board",
            tags = { "Board"},
            notes = "Permanent exclusion from the system - for some reason it is not allowed to remove the Board from database",
            produces = "application/json",
            response =  Board.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "type_of_board.create", description = "Person need this permission"),
                                    @AuthorizationScope(scope = "SuperAdmin", description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Board.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    //  @Pattern("board.deactivate")
    public Result deactivate_Board(@ApiParam(required = true) @PathParam("board_id")  String board_id) {
        try {
            Board board = Board.find.byId(board_id);
            if(board == null ) return GlobalResult.notFoundObject();

            board.isActive = false;
            board.update();

            return GlobalResult.okResult(Json.toJson(board));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - deactivate_Board ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }

    }

    @ApiOperation(value = "get Board",
            tags = { "Board"},
            notes = "if you want get Board object by query = board_id",
            produces = "application/json",
            response =  Board.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "Board Owner", description = "Person who owned this Board"),
                                       @AuthorizationScope(scope = "board.read",  description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin",  description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Board.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    // @Pattern("board.read")
    public Result get_Board(@ApiParam(required = true) @PathParam("board_id")  String board_id) {
        try {
            Board board = Board.find.byId(board_id);
            if(board == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(board));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - get_Board ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "connect Board with Project",
            tags = { "Board"},
            notes = "This Api is used by Users for connection of Board with their Project",
            produces = "application/json",
            response =  Board.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "Board Owner & Project Owner", description = "Person who owned this Board and Project"),
                                       @AuthorizationScope(scope = "board.edit",  description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin",  description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Board.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result connect_Board_with_Project(@ApiParam(required = true) @PathParam("board_id")  String board_id, @ApiParam(required = true) @PathParam("project_id")  String project_id){
        try {
            Board board = Board.find.byId(board_id);
            if(board == null ) return GlobalResult.notFoundObject();

            Project project = Project.find.byId(project_id);
            if(project == null) return GlobalResult.notFoundObject();

            if( board.projects.contains(project)) return  GlobalResult.okResult(Json.toJson(board));
            board.projects.add(project);

            board.update();

            return GlobalResult.okResult(Json.toJson(board));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - get_Board ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "disconnect Board from Project",
            tags = { "Board"},
            notes = "This Api is used by Users for disconnection of Board from their Project, its not meaning that Board is removed from system, only disconnect " +
                    "and another user can registred that (connect that with different account/project etc..)",
            produces = "application/json",
            response =  Board.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "Board Owner & Project Owner", description = "Person who owned this Board and Project"),
                                       @AuthorizationScope(scope = "board.edit",  description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin",  description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Board.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result disconnect_Board_from_Project(@ApiParam(required = true) @PathParam("board_id")  String board_id, @ApiParam(required = true) @PathParam("project_id")  String project_id){
        try {
            Board board = Board.find.byId(board_id);
            if(board == null ) return GlobalResult.notFoundObject();

            Project project = Project.find.byId(project_id);
            if(project == null) return GlobalResult.notFoundObject();

            if( !board.projects.contains(project)) return  GlobalResult.okResult(Json.toJson(board));
            board.projects.remove(project);

            board.update();
            return GlobalResult.okResult(Json.toJson(board));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - get_Board ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get Project from Board",
            tags = { "Board", "Project"},
            notes = "Design pattern from Api-GitHub says that from every object you can get another, which belongs to it",
            produces = "application/json",
            response =  Board.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "Board Owner & Project Owner", description = "Person who owned this Board and Project"),
                                       @AuthorizationScope(scope = "board.edit",  description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin",  description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Board.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result getBoardProjects(@ApiParam(required = true) @PathParam("board_id")  String board_id){
        try {
            Board board = Board.find.byId(board_id);
            if(board == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(board.projects));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - get_Board ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get all Boards from Project",
            tags = { "Board", "Project"},
            notes = "Get all boards which are connected with Project",
            produces = "application/json",
            response =  Board.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "Board Owner & Project Owner", description = "Person who owned this Board and Project"),
                                       @AuthorizationScope(scope = "board.edit",  description = "Person need this permission"),
                                       @AuthorizationScope(scope = "SuperAdmin",  description = "Or person must be SuperAdmin role")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Board.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Boards_from_Project(@ApiParam(required = true) @PathParam("project_id")  String project_id) {
        try {

            Project project = Project.find.byId(project_id);
            if (project == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(project.boards));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - deleteCProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }



}
