package controllers;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Pattern;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.OrderBy;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import io.swagger.annotations.*;
import models.compiler.*;
import models.project.c_program.C_Program;
import models.project.global.Project;
import play.Logger;
import play.libs.Json;
import play.mvc.*;
import utilities.a_main_utils.Description;
import utilities.a_main_utils.GlobalValue;
import utilities.a_main_utils.UtilTools;
import utilities.loginEntities.Secured;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.*;

import javax.websocket.server.PathParam;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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


@Api(value = "Neroztříděný píčoviny",
     description = "Compilation operation (C_Program, Processor, Libraries, TypeOfBoard...",
     authorizations = { @Authorization(value="logged_in", scopes = {} )}
    )
@Security.Authenticated(Secured.class)
public class CompilationLibrariesController extends Controller {


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
                            dataType = "utilities.swagger.documentationClass.Swagger_C_program_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @BodyParser.Of(BodyParser.Json.class)
    @Dynamic("project.owner")
    public Result create_C_Program(@ApiParam(value = "project_id String query", required = true) @PathParam("project_id") String project_id) {
        try {
            Logger.info("Creating new C_Program on project_id " + project_id);

            JsonNode json = request().body().asJson();

            // Tvorba programu
            C_Program c_program             = new C_Program();
            c_program.programName           = json.get("programName").asText();
            c_program.programDescription    = json.get("programDescription").asText();
            c_program.azurePackageLink      = "personal-program";
            c_program.project               = Project.find.byId(json.get(project_id).asText());
            c_program.dateOfCreate          = new Date();
            c_program.setUniqueAzureStorageLink();

            c_program.save();

            Logger.info("C_Program created c_program.id " + c_program.id);

            return GlobalResult.created(Json.toJson(c_program));

        } catch (NullPointerException e) {
            Logger.warn("Missing Json value in " + Thread.currentThread().getStackTrace());
            return GlobalResult.nullPointerResult(e, "programDescription", "programName");
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
    @Dynamic("project.c_program_owner")
    public Result get_C_Program(@ApiParam(value = "c_program_id String query", required = true) @PathParam("c_program_id") String c_program_id) {
        try {

            C_Program c_program = C_Program.find.byId(c_program_id);
            return GlobalResult.okResult(Json.toJson(c_program));

        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - getCProgram ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get All C_program",
            tags = {"C_Program"},
            notes = "get all C_program from project by query = project_id",
            produces = "application/json",
            response =  Swagger_C_program_list.class,
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_C_program_list.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Dynamic("project.owner")
    public Result get_C_Program_All_from_Project(@ApiParam(value = "project_id String query", required = true) @PathParam("project_id") String project_id) {
        try {

            Project project = Project.find.byId(project_id);
            return GlobalResult.okResult(Json.toJson(project.c_programs));

        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - gellAllProgramFromProject ERROR");
            Logger.error(request().body().asJson().toString());
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
    @Dynamic("project.c_program_owner")
    public Result edit_C_Program_Description(@ApiParam(value = "c_program_id String query", required = true) @PathParam("c_program_id") String c_program_id) {
        try {

            JsonNode json = request().body().asJson();

            C_Program program = C_Program.find.byId(c_program_id);

            program.programName = json.get("programName").asText();
            program.programDescription = json.get("programDescription").asText();

            program.update();

            return GlobalResult.okResult(Json.toJson(program));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "programName", "programDescription" );
        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - gellAllProgramFromProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "update C_Program",
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
                            dataType = "utilities.swagger.documentationClass.Swagger_Version",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @Dynamic("project.c_program_owner")
    @BodyParser.Of(BodyParser.Json.class)
    public Result update_C_Program(@ApiParam(value = "c_program_id String query", required = true) @PathParam("c_program_id") String c_program_id){
        try{

            JsonNode json = request().body().asJson();

            C_Program c_program = C_Program.find.byId(c_program_id);

            // První nová Verze
            Version_Object version_object     = new Version_Object();
            version_object.version_name = json.get("version_name").asText();
            version_object.versionDescription  = json.get("version_description").asText();

            if(c_program.versionObjects.isEmpty() ) version_object.azureLinkVersion = 1;
            else version_object.azureLinkVersion    = ++c_program.versionObjects.get(0).azureLinkVersion; // Zvednu verzi o jednu

            version_object.dateOfCreate        = new Date();
            version_object.c_program           = c_program;
            version_object.save();

            c_program.versionObjects.add(version_object);
            c_program.update();

            // Nahraje do Azure a připojí do verze soubor (lze dělat i cyklem - ale název souboru musí být vždy jiný)

            for (final JsonNode objNode : json.get("files")){
                UtilTools.uploadAzure_Version("c-program", objNode.get("content").asText(), objNode.get("fileName").asText(), c_program.azureStorageLink, c_program.azurePackageLink, version_object);
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
    @Dynamic("project.c_program_owner")
    public Result delete_C_Program_Version(@ApiParam(value = "c_program_id String query", required = true) @PathParam("c_program_id") String c_program_id, @ApiParam(value = "version_id String query",   required = true) @PathParam("version_id")   String version_id){
        try{

            Version_Object versionObjectObject = Version_Object.find.byId(version_id);
            if (versionObjectObject == null) return GlobalResult.notFoundObject();

            C_Program c_program = C_Program.find.byId(c_program_id);

            UtilTools.azureDelete(GlobalValue.blobClient.getContainerReference("c-program"), c_program.azurePackageLink + "/" + c_program.azureStorageLink + "/" + versionObjectObject.azureLinkVersion);

            versionObjectObject.delete();

            return GlobalResult.okResult();

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
    @Dynamic("project.c_program_owner")
    public Result delete_C_Program(@ApiParam(value = "c_program_id String query", required = true) @PathParam("c_program_id") String c_program_id){
        try{

            C_Program c_program = C_Program.find.byId(c_program_id);

            UtilTools.azureDelete(GlobalValue.blobClient.getContainerReference("c-program"), c_program.azurePackageLink + "/" + c_program.azureStorageLink);

            c_program.delete();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - deleteCProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }

///###################################################################################################################*/

    //TODO
    @BodyParser.Of(BodyParser.Json.class)
    public Result compileCProgram(){
        return GlobalResult.okResult("Compiled!"); //TODO
    }

    //TODO
    public Result generateProjectForEclipse() {
       // EclipseProject.createFullnewProject();
        return GlobalResult.okResult("In TODO"); //TODO
    }

    //TODO
    public Result uploadBinaryFileToBoard(String id) {
        try{

            Board board = Board.find.byId(id);
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

    //TODO
    public Result uploadCompilationToBoard(String id, String boardId) {

        Board board = Board.find.byId(boardId);
        if(board == null ) return GlobalResult.notFoundObject();

        C_Program c_program = C_Program.find.byId(id);
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
                            dataType = "utilities.swagger.documentationClass.Swagger_Processor",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @BodyParser.Of(BodyParser.Json.class)
    @Pattern("processor.create")
    public Result new_Processor() {
        try {
            JsonNode json = request().body().asJson();

            Processor processor = new Processor();

            processor.description    = json.get("description").asText();
            processor.processor_code = json.get("processor_code").asText();
            processor.processor_name = json.get("processor_name").asText();
            processor.speed          = json.get("speed").asInt();

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
    @Pattern("processor.read")
    public Result get_Processor(@ApiParam(value = "processor_id String query", required = true) @PathParam("processor_id") String processor_id) {
        try {

            Processor processor = Processor.find.byId(processor_id);
            if(processor == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(processor));

        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get all Processors",
            tags = {"Processor"},
            notes = "If you want get Processor by query processor_id.",
            produces = "application/json",
            response =  Swagger_Processor_list.class,
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_Processor_list.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Pattern("processor.read")
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
                            dataType = "utilities.swagger.documentationClass.Swagger_Processor",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @BodyParser.Of(BodyParser.Json.class)
    @Pattern("processor.edit")
    public Result update_Processor(@ApiParam(value = "processor_id String query", required = true) @PathParam("processor_id") String processor_id) {
        try {
            JsonNode json = request().body().asJson();

            Processor processor = Processor.find.byId(processor_id);
            if(processor == null ) return GlobalResult.notFoundObject();


            processor.description       = json.get("description").asText();
            processor.processor_code    = json.get("processor_code").asText();
            processor.processor_name    = json.get("processor_name").asText();
            processor.speed             = json.get("speed").asInt();

            processor.libraryGroups.clear();

            List<String> libraryGroups = UtilTools.getListFromJson(json, "libraryGroups");

            for (String Lid : libraryGroups) {
                try {
                    processor.libraryGroups.add(LibraryGroup.find.byId(Lid));
                } catch (Exception e) {/**nothing*/}
            }

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
    @Pattern("processor.delete")
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
    @Pattern("processor.read")
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
            response =  Swagger_LibraryGroup_list.class,
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_LibraryGroup_list.class),
            @ApiResponse(code = 400, message = "Object Not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Pattern("processor.read")
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

    @ApiOperation(value = "get Processor.SingleLibraries",
            tags = {"Processor", "SingleLibrary"},
            notes = "If you want get all SingleLibraries from Processor by query processor_id.",
            produces = "application/json",
            response =  Swagger_LibraryGroup_list.class,
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_LibraryGroup_list.class),
            @ApiResponse(code = 400, message = "Object Not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Pattern("processor.read")
    public Result getProcessorSingleLibraries( @ApiParam(value = "processor_id String query", required = true) @PathParam("processor_id")String processor_id) {
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
    @Pattern("processor.edit")
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
    @Pattern("processor.edit")
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
    @Pattern("processor.edit")
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
    @Pattern("processor.edit")
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
    @Pattern("libraryGroup.create")
    public Result new_LibraryGroup() {
        try {
            JsonNode json = request().body().asJson();

            LibraryGroup libraryGroup = new LibraryGroup();
            libraryGroup.description = json.get("description").asText();
            libraryGroup.group_name = json.get("group_name").asText();
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
            response =  Swagger_LibraryGroup_Version.class,
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
            @ApiResponse(code = 201, message = "Successful created",      response = Swagger_LibraryGroup_Version.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Version",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @Pattern("libraryGroup.edit")
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_LibraryGroup_Version(@ApiParam(value = "libraryGroup_id String query", required = true) @PathParam("libraryGroup_id") String libraryGroup_id){
        try {
            JsonNode json = request().body().asJson();

            LibraryGroup libraryGroup = LibraryGroup.find.byId(libraryGroup_id);
            if(libraryGroup == null) return GlobalResult.notFoundObject();

            Version_Object versionObjectObject     = new Version_Object();

            if(libraryGroup.versionObjects.isEmpty() ) versionObjectObject.azureLinkVersion = 1;
            else versionObjectObject.azureLinkVersion    = ++libraryGroup.versionObjects.get(0).azureLinkVersion; // Zvednu verzi o jednu

            versionObjectObject.dateOfCreate        = new Date();
            versionObjectObject.version_name        = json.get("version_name").asText();
            versionObjectObject.versionDescription  = json.get("description").asText();
            versionObjectObject.libraryGroup        = libraryGroup;
            versionObjectObject.save();


            libraryGroup.versionObjects.add(versionObjectObject);
            libraryGroup.update();

            return GlobalResult.created(Json.toJson(versionObjectObject));
        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "description", "version_name");
        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - new_Processor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get all Versions from LibraryGroup",
            tags = {"LibraryGroup"},
            notes = "If you want create new versinon in LibraryGroup query = libraryGroup_id. Send required json values and server respond with new object",
            produces = "application/json",
            response =  Swagger_LibraryGroup_Version_List.class,
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
            @ApiResponse(code = 200, message = "Ok result",      response = Swagger_LibraryGroup_Version_List.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Pattern("libraryGroup.read")
    public Result get_LibraryGroup_Version(@ApiParam(value = "libraryGroup_id String query", required = true) @PathParam("libraryGroup_id") String libraryGroup_id){
        try {

            LibraryGroup libraryGroup = LibraryGroup.find.byId(libraryGroup_id);
            if(libraryGroup == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(libraryGroup.versionObjects));

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
            response =  Swagger_LibraryGroup_Version_List.class,
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
            @ApiResponse(code = 200, message = "Ok result",      response = Swagger_LibraryGroup_Version_List.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Pattern("libraryGroup.edit")
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

                Version_Object versionObjectObject = Version_Object.find.where().in("libraryGroup.id", libraryGroup.id).eq("id", version_id).setMaxRows(1).findUnique();
                if (versionObjectObject == null) return GlobalResult.notFoundObject();

                // Control lenght of name
                String fileName = file.getFilename();
                if (fileName.length() < 5) GlobalResult.forbidden_Global("Too short file name");

                // Ještě kontrola souboru zda už tam není - > Version_Object a knihovny
                FileRecord fileRecord = FileRecord.find.where().in("versionObjects.id", versionObjectObject.id).eq("fileName", fileName).setMaxRows(1).findUnique();
                if (fileRecord != null)
                    return GlobalResult.nullPointerResult("File exist in this version -> " + fileName + " please, create new version!");

                // Mám soubor
                File libraryFile = file.getFile();

                // Připojuji se a tvořím cestu souboru
                CloudBlobContainer container = GlobalValue.blobClient.getContainerReference("libraries");

                String azurePath = libraryGroup.azurePackageLink + "/" + libraryGroup.azureStorageLink + "/" + versionObjectObject.azureLinkVersion + "/" + fileName;

                CloudBlockBlob blob = container.getBlockBlobReference(azurePath);

                blob.upload(new FileInputStream(libraryFile), libraryFile.length());

                fileRecord = new FileRecord();
                fileRecord.fileName = fileName;
                fileRecord.save();


                versionObjectObject.files.add(fileRecord);
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
    @Pattern("libraryGroup.read")
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
    @Pattern("libraryGroup.delete")
    public Result delete_LibraryGroup(@ApiParam(value = "libraryGroup_id String query", required = true) @PathParam("libraryGroup_id") String libraryGroup_id) {
        try {
            LibraryGroup libraryGroup = LibraryGroup.find.byId(libraryGroup_id);
            if(libraryGroup == null) return GlobalResult.notFoundObject();

            UtilTools.azureDelete(GlobalValue.blobClient.getContainerReference("libraries"), libraryGroup.azurePackageLink+"/"+libraryGroup.azureStorageLink);

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
    @Pattern("libraryGroup.edit")
    public Result editLibraryGroup(@ApiParam(value = "libraryGroup_id String query", required = true) @PathParam("libraryGroup_id") String libraryGroup_id) {
        try {

            LibraryGroup libraryGroup = LibraryGroup.find.byId(libraryGroup_id);
            if(libraryGroup == null) return GlobalResult.notFoundObject();

            JsonNode json = request().body().asJson();

            libraryGroup.description = json.get("description").asText();
            libraryGroup.group_name = json.get("group_name").asText();

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
    @Pattern("libraryGroup.read")
    public Result get_LibraryGroup_Description(@ApiParam(value = "libraryGroup_id String query", required = true) @PathParam("libraryGroup_id") String libraryGroup_id) {
        try {
            LibraryGroup libraryGroup = LibraryGroup.find.byId(libraryGroup_id);
            if(libraryGroup == null) return GlobalResult.notFoundObject();

            Description description = new Description();
            description.description = libraryGroup.description;

            return GlobalResult.okResult(Json.toJson(description));

        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - get_LibraryGroup_Description ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get LibraryGroup Processors",
            tags = {"LibraryGroup", "Processor"},
            notes = "If you want get Processors from LibraryGroup by query = libraryGroup_id",
            produces = "application/json",
            response =  Swagger_LibraryGroup_Processor_List.class,
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
            @ApiResponse(code = 200, message = "Ok result",      response = Swagger_LibraryGroup_Processor_List.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Pattern("processor.read")
    public Result get_LibraryGroup_Processors(@ApiParam(value = "libraryGroup_id String query", required = true) @PathParam("libraryGroup_id") String libraryGroup_id) {
        try {
            LibraryGroup libraryGroup = LibraryGroup.find.byId(libraryGroup_id);
            if(libraryGroup == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(libraryGroup.processors));

        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - get_LibraryGroup_Processors ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get Libraries from LibraryGroup",
            tags = {"LibraryGroup"},
            notes = "If you want get Processors from LibraryGroup by query = libraryGroup_id",
            produces = "application/json",
            response =  Swagger_LibraryGroup_Libraries.class,
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
            @ApiResponse(code = 200, message = "Ok result",      response = Swagger_LibraryGroup_Libraries.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Pattern("libraryGroup.read")
    public Result get_LibraryGroup_Libraries(@ApiParam(value = "libraryGroup_id String query", required = true) @PathParam("libraryGroup_id") String libraryGroup_id, @ApiParam(required = true) @PathParam("version_id") String version_id) {
        try {

            Version_Object versionObjectObject = Version_Object.find.where().in("libraryGroup.id", libraryGroup_id).eq("id",version_id).setMaxRows(1).findUnique();
            if(versionObjectObject == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(versionObjectObject.files));
        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - get_LibraryGroup_Libraries ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get Libraries from LibraryGroup Version",
            tags = {"LibraryGroup"},
            notes = "If you want get Libraries from LibraryGroup.Version by query = version_id",
            produces = "application/json",
            response =  Swagger_LibraryGroup_Libraries.class,
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
            @ApiResponse(code = 200, message = "Ok result",      response = Swagger_LibraryGroup_Libraries.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Pattern("libraryGroup.read")
    public Result get_LibraryGroup_Version_Libraries(@ApiParam(required = true) @PathParam("version_id") String version_id){
        try {
            Version_Object versionObject = Version_Object.find.byId(version_id);
            if(versionObject == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(versionObject.files));
        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - get_LibraryGroup_Version_Libraries ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get LibraryGroup with Filter parameters",
            tags = {"LibraryGroup"},
            notes = "If you want get all or only some LibraryGroups you can use filter parameters in Json. But EveryTime i will return maximal 25 objects \n\n" +
                    "so, you have to used that limit for frontend pagination -> first round (0,25), second round (26, 50) etc... \n ",
            produces = "application/json",
            response =  Swagger_LibraryGroup_Libraries.class,
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
            @ApiResponse(code = 200, message = "Ok result",               response = Swagger_LibraryGroup_list.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Pattern("libraryGroup.read")
    public Result get_LibraryGroup_Filter() {
        try {
            JsonNode json = request().body().asJson();

            Query<LibraryGroup> query = Ebean.find(LibraryGroup.class);

            // If contains confirms
            if (json.has("processors_id")) {
                List<String> list = UtilTools.getListFromJson(json, "processors_id");
                Set<String> set = new HashSet<String>(list);
                query.where().in("processors.id", set);
            }

            if (json.has("group_name")) {
                String group_name = json.get("group_name").asText();
                query.where().ieq("group_name", group_name);
            }

            if (json.has("count_from")) {
                Integer countFrom = json.get("count_from").asInt();
                query.setFirstRow(countFrom);
            }

            if (json.has("count_to")) {
                Integer countTo = json.get("count_to").asInt();
                query.setMaxRows(countTo);
            }

            if (json.has("order_by")) {
                JsonNode rdb = json.get("order_by");

                String order = rdb.get("order").asText();
                String value = rdb.get("value").asText();

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
            result.put("fileName", fileRecord.fileName);
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
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Pattern("library.create")
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_SingleLibrary() {
        try {
            JsonNode json = request().body().asJson();

            SingleLibrary singleLibrary = new SingleLibrary();
            singleLibrary.library_name = json.get("library_name").asText();
            singleLibrary.description = json.get("description").asText();
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
            response =  Swagger_SingleLibrary_Version.class,
            protocols = "https",
            code = 201,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "library.edit", description = "For crating Libraries")}
                    )
            }
    )
    @ApiResponses(value = {

            @ApiResponse(code = 201, message = "Successful created",      response = Swagger_SingleLibrary_Version.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Pattern("library.edit")
    public Result new_SingleLibrary_Version(@ApiParam(value = "library_id String query", required = true) @PathParam("library_id")  String library_id){
        try {
            JsonNode json = request().body().asJson();

            SingleLibrary singleLibrary = SingleLibrary.find.byId(library_id);
            if(singleLibrary == null)  return GlobalResult.notFoundObject();

            Version_Object versionObjectObject = new Version_Object();

            if(singleLibrary.versionObjects.isEmpty() ) versionObjectObject.azureLinkVersion = 1;
            else versionObjectObject.azureLinkVersion    = ++singleLibrary.versionObjects.get(0).azureLinkVersion; // Zvednu verzi o jednu

            versionObjectObject.dateOfCreate = new Date();
            versionObjectObject.version_name = json.get("version_name").asText();
            versionObjectObject.versionDescription = json.get("description").asText();
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
            response =  Swagger_SingleLibrary_Version_List.class,
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
            @ApiResponse(code = 200, message = "Ok result",               response = Swagger_SingleLibrary_Version_List.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Pattern("library.read")
    public Result get_SingleLibrary_Versions(@ApiParam(value = "library_id String query", required = true) @PathParam("library_id")  String library_id) {
        try {

            SingleLibrary singleLibrary = SingleLibrary.find.byId(library_id);
            if(singleLibrary == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(singleLibrary.versionObjects));

        } catch (Exception e) {
            return GlobalResult.nullPointerResult(e);
        }
    }

    public Result upload_SingleLibrary_Version(@ApiParam(value = "library_id String query", required = true) @PathParam("library_id")  String library_id, @ApiParam(required = true) @PathParam("version_id") String version_id){
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
            fileRecord.fileName = fileName;
            fileRecord.save();

            CloudBlobContainer container = GlobalValue.blobClient.getContainerReference("libraries");
            String azurePath = singleLibrary.azurePackageLink + "/" + singleLibrary.azureStorageLink + "/"+ versionObjectObject.azureLinkVersion  +"/" + fileRecord.fileName;
            CloudBlockBlob blob = container.getBlockBlobReference(azurePath);

            blob.upload(new FileInputStream(libraryFile), libraryFile.length());

            versionObjectObject.files.add(fileRecord);
            versionObjectObject.dateOfCreate = new Date();
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
    @Pattern("library.read")
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

    @ApiOperation(value = "get Library with Filter parameters",
            tags = {"SingleLibrary"},
            notes = "If you want get all or only some SingleLibraries you can use filter parameters in Json. But EveryTime i will return maximal 25 objects \n\n" +
                    "so, you have to used that limit for frontend pagination -> first round (0,25), second round (26, 50) etc... I will give you also" +
                    "information how many results you can show \n ",
            produces = "application/json",
            response =  Swagger_SingleLibraries_list.class,
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
            @ApiResponse(code = 200, message = "Ok result",               response = Swagger_SingleLibraries_list.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Pattern("libraryGroup.read")
    public Result get_SingleLibrary_Filter() {
        try {
            JsonNode json = request().body().asJson();

            //TODO

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.nullPointerResult(e);
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
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Pattern("library.edit")
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_SingleLibrary(@ApiParam(value = "library_id String query", required = true) @PathParam("library_id") String library_id) {
        try {
            JsonNode json = request().body().asJson();

            SingleLibrary singleLibrary = SingleLibrary.find.byId(library_id);
            if(singleLibrary == null) return GlobalResult.notFoundObject();

            singleLibrary.library_name = json.get("library_name").asText();
            singleLibrary.description = json.get("description").asText();


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
    @Pattern("library.delete")
    public Result delete_SingleLibrary(@ApiParam(value = "library_id String query", required = true) @PathParam("library_id") String library_id) {
        try {

            SingleLibrary singleLibrary = SingleLibrary.find.byId(library_id);
            if(singleLibrary == null) return GlobalResult.notFoundObject();

            UtilTools.azureDelete(GlobalValue.blobClient.getContainerReference("libraries"), singleLibrary.azurePackageLink+"/"+singleLibrary.azureStorageLink);

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
                            dataType = "utilities.swagger.documentationClass.Swagger_Board_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = Producer.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Pattern("producer.create")
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_Producers() {
        try {
            JsonNode json = request().body().asJson();

            Producer producer = new Producer();
            producer.name = json.get("name").asText();
            producer.description = json.get("description").asText();

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
                            dataType = "utilities.swagger.documentationClass.Swagger_Board_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Producer.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Pattern("producer.edit")
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateProducers(@ApiParam(required = true) @PathParam("producer_id") String producer_id) {
        try {
            JsonNode json = request().body().asJson();

            Producer producer = Producer.find.byId(producer_id);
            if(producer == null ) return GlobalResult.notFoundObject();

            producer.name = json.get("name").asText();
            producer.description = json.get("description").asText();

            producer.update();

            return GlobalResult.okResult(Json.toJson(producer));
        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "description - TEXT", "name - String");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - updateProducers ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get all Producers",
            tags = {"Producer"},
            notes = "if you want get list of Producers. Its list of companyes owned physical boards and we used that for filtering",
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
            @ApiResponse(code = 200, message = "Ok Result",      response = Producer.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Pattern("producer.read")
    public Result get_Producers() {
        try {
            List<Producer> producers = Producer.find.all();

            return GlobalResult.okResult(Json.toJson(producers));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - get_Producers ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get Producer",
            tags = {"Producer"},
            notes = "if you want get Producer. Its company owned physical boards and we used that for filtering",
            produces = "application/json",
            response =  Swagger_Producer_list.class,
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_Producer_list.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Pattern("producer.read")
    public Result getProducer(@ApiParam(required = true) @PathParam("producer_id") String producer_id) {
        try {
            Producer producer = Producer.find.byId(producer_id);

            if(producer == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(producer));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - getProducer ERROR");
            Logger.error(request().body().asJson().toString());
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
    @Pattern("producer.read")
    public Result getProducerDescription(@ApiParam(required = true) @PathParam("producer_id") String producer_id) {
        try {
            Producer producer = Producer.find.byId(producer_id);

            if(producer == null ) return GlobalResult.notFoundObject();

            Description description = new Description();
            description.description = producer.description;

            return GlobalResult.okResult(Json.toJson(description));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - getProducerDescription ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get TypeOfBoard from Producer",
            tags = {"Producer", "TypeOfBoard"},
            notes = "if you want get TypeOfBoard from Producer. Its a list of Boards types.",
            produces = "application/json",
            response =  Swagger_TypeOfBoards_List.class,
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
            @ApiResponse(code = 200, message = "Ok Result",               response = Swagger_TypeOfBoards_List.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result getProducerTypeOfBoards(@ApiParam(required = true) @PathParam("producer_id") String producer_id) {
        try {
            Producer producer = Producer.find.byId(producer_id);
            if(producer == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(producer.typeOfBoards));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - getProducerTypeOfBoards ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

///###################################################################################################################*/

    @BodyParser.Of(BodyParser.Json.class)
    public Result newTypeOfBoard() {
        try {
            JsonNode json = request().body().asJson();

            Producer producer = Producer.find.byId(json.get("producerId").asText());
            if(producer == null ) return GlobalResult.notFoundObject();

            Processor processor = Processor.find.byId(json.get("processorId").asText());
            if(processor == null ) return GlobalResult.notFoundObject();


            TypeOfBoard typeOfBoard = new TypeOfBoard();
            typeOfBoard.name = json.get("name").asText();
            typeOfBoard.description = json.get("description").asText();
            typeOfBoard.processor = processor;
            typeOfBoard.producer = producer;

            typeOfBoard.save();

            return GlobalResult.okResult(Json.toJson(typeOfBoard));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "description - TEXT","name - String", "processorId - String", "producerId - String");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - newTypeOfBoard ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result updateTypeOfBoard(String id) {
        try {
            JsonNode json = request().body().asJson();

            TypeOfBoard typeOfBoard = TypeOfBoard.find.byId(id);
            if(typeOfBoard == null ) return GlobalResult.notFoundObject();

            typeOfBoard.name = json.get("name").asText();
            typeOfBoard.description = json.get("description").asText();
            typeOfBoard.update();

            return GlobalResult.okResult(Json.toJson(typeOfBoard));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "description - TEXT","name - String", "processorId - String", "producerId - String");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - updateTypeOfBoard ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getTypeOfBoards() {
        try {

            List<TypeOfBoard> typeOfBoards = TypeOfBoard.find.all();

            return  GlobalResult.okResult(Json.toJson(typeOfBoards));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - getTypeOfBoards ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getTypeOfBoard(String id) {
        try {

            TypeOfBoard typeOfBoard = TypeOfBoard.find.byId(id);
            if(typeOfBoard == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(typeOfBoard));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - getProducerTypeOfBoards ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getTypeOfBoardDescription(String id) {
        try {

            TypeOfBoard typeOfBoard = TypeOfBoard.find.byId(id);
            if(typeOfBoard == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(typeOfBoard.description));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - getProducerTypeOfBoards ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getTypeOfBoardAllBoards(String id) {
        try {

            TypeOfBoard typeOfBoard = TypeOfBoard.find.byId(id);
            if(typeOfBoard == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(typeOfBoard.boards));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - getProducerTypeOfBoards ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    ///###################################################################################################################*/
    @BodyParser.Of(BodyParser.Json.class)
    public Result newBoard() {
        try {
            JsonNode json = request().body().asJson();
            if (Board.find.byId(json.get("hwName").asText()) != null) GlobalResult.forbidden_Global("Duplicate database value");

            TypeOfBoard typeOfBoard = TypeOfBoard.find.byId(json.get("typeOfBoard").asText());
            if(typeOfBoard == null ) return GlobalResult.notFoundObject();

            Board board = new Board();
            board.id = json.get("hwName").asText();
            board.isActive = false;
            board.typeOfBoard = typeOfBoard;

            board.save();

            return GlobalResult.okResult(Json.toJson(board));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "hwName - String(Unique)", "typeOfBoard - String(Id)");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - newBoard ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result addUserDescription(String id){
        try {
            JsonNode json = request().body().asJson();

            Board board = Board.find.byId(id);
            if(board == null ) return GlobalResult.notFoundObject();

            board.userDescription = json.get("userDescription").asText();
            board.update();

            return GlobalResult.okResult(Json.toJson(board));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "generalDescription - Text", "typeOfBoard - String(Id)");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - newBoard ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result getBoardByFilter() {
        try {
            JsonNode json = request().body().asJson();

            Query<Board> query = Ebean.find(Board.class);

            // If contains HashTags
            if(json.has("typeOfBoards") ){
                List<String> stringList = UtilTools.getListFromJson( json, "typeOfBoards" );
                Set<String> stringListSet = new HashSet<String>(stringList);
                query.where().in("typeOfBoard.id", stringListSet);

            }

            // If contains confirms
            if(json.has("active") ){
                Boolean isActive = json.get("active").asBoolean();
                query.where().eq("isActive", isActive);
            }

            // From date
            if(json.has("projects")){
                List<String> stringList = UtilTools.getListFromJson( json, "projects" );
                Set<String> stringListSet = new HashSet<String>(stringList);
                query.where().in("projects.projectId", stringListSet);
            }


            if(json.has("producers")){
                List<String> stringList = UtilTools.getListFromJson( json, "producers" );
                Set<String> stringListSet = new HashSet<String>(stringList);
                query.where().in("typeOfBoard.producer.id", stringListSet);
            }

            if(json.has("processor")){
                List<String> stringList = UtilTools.getListFromJson( json, "processor" );
                Set<String> stringListSet = new HashSet<String>(stringList);
                query.where().in("typeOfBoard.processor.id", stringListSet);
            }

            List<Board> list = query.findList();

            return GlobalResult.okResult(Json.toJson(list));




        } catch (Exception e){
            e.printStackTrace();
            return GlobalResult.nullPointerResult(e);
        }



    }

    public Result deactivateBoard(String id) {
        try {
            Board board = Board.find.byId(id);
            if(board == null ) return GlobalResult.notFoundObject();

            board.isActive = false;
            board.update();

            return GlobalResult.okResult(Json.toJson(board));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - deactivateBoard ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }

    }

    public Result getBoard(String id) {
        try {
            Board board = Board.find.byId(id);
            if(board == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(board));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - getBoard ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getBoardgeneralDescription(String id) {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.nullPointerResult(e);
        }
    }

    public Result getUserDescription(String id) {
        try {
            JsonNode json = request().body().asJson();

            Board board = Board.find.byId(id);
            if(board == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(board.userDescription));
        } catch (Exception e) {
            return GlobalResult.nullPointerResult(e);
        }
    }

    public Result connectBoardWthProject(String id, String pr){
        try {
            Board board = Board.find.byId(id);
            if(board == null ) return GlobalResult.notFoundObject();

            Project project = Project.find.byId(pr);
            if(project == null) return GlobalResult.notFoundObject();

            if( board.projects.contains(project)) return  GlobalResult.okResult("is already connected");
            board.projects.add(project);

            board.update();
            return GlobalResult.okResult(Json.toJson(board));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - getBoard ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public Result disconnectBoardWthProject(String id, String pr){
        try {
            Board board = Board.find.byId(id);
            if(board == null ) return GlobalResult.notFoundObject();

            Project project = Project.find.byId(pr);
            if(project == null) return GlobalResult.notFoundObject();

            if( !board.projects.contains(project)) return  GlobalResult.okResult("is already disconnected");
            board.projects.remove(project);

            board.update();
            return GlobalResult.okResult(Json.toJson(board));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - getBoard ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public Result getBoardProjects(String id){
        try {
            Board board = Board.find.byId(id);
            if(board == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(board.projects));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - getBoard ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public Result getBoardsFromProject(String id) {
        try {

            Project project = Project.find.byId(id);
            if (project == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(project.boards));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - deleteCProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }

}
