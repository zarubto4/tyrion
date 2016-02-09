package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import models.blocko.Project;
import models.compiler.*;
import play.Logger;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utilities.GlobalValue;
import utilities.UtilTools;
import utilities.response.GlobalResult;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.UUID;


//@Security.Authenticated(Secured.class)
public class CompilationLibrariesController extends Controller {

    @BodyParser.Of(BodyParser.Json.class)
    public Result newCProgram() {
        try {
            JsonNode json = request().body().asJson();

            C_Program c_program = new C_Program();

            c_program.programName           = json.get("programName").asText();
            c_program.programDescription    = json.get("programDescription").asText();
            c_program.azurePackageLink      =  "personal-program";

            Project project = Project.find.byId(json.get("project").asText());
            if(project == null) return GlobalResult.notFoundObject();

            c_program.project = project;

            while(true){ // I need Unique Value
                c_program.azureStorageLink = UUID.randomUUID().toString();
                if (C_Program.find.where().eq("azureStorageLink",c_program.azureStorageLink ).findUnique() == null) break;
            }
            c_program.save();

            Version versionObject = new Version();
            versionObject.azureLinkVersion = 1.01;
            versionObject.dateOfCreate = new Date();
            versionObject.versionName = "Nova verze bla bla bla";
            versionObject.c_program = c_program;
            versionObject.save();

            c_program.versions.add(versionObject);
            c_program.save();

            String temporaryDirectory =  "files/" + c_program.azureStorageLink ;
            String azurePath = c_program.azurePackageLink + "/" + c_program.azureStorageLink + "/" + versionObject.azureLinkVersion;

            UtilTools.c_ProgramAzure(temporaryDirectory, json.get("files"), azurePath, versionObject);

            return GlobalResult.created(Json.toJson(c_program));

        } catch (NullPointerException e) {
            e.printStackTrace(); //TODO
            return GlobalResult.badRequest(e, "programDescription - TEXT", "processorCode - String", "programName - String", "project - id-String", "files {}");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - newProcessor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getCProgram(String id) {
        try {

            C_Program c_program = C_Program.find.byId(id);
            if (c_program == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(c_program));

        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - getCProgram ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result gellAllProgramFromProject(String id) {
        try {

            Project project = Project.find.byId(id);
            if (project == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(project.c_programs));

        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - gellAllProgramFromProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result updateCProgramDescription(String id) {
        try {

            JsonNode json = request().body().asJson();

            C_Program program = C_Program.find.byId(id);
            if (program == null) return GlobalResult.notFoundObject();


            program.programName = json.get("programName").asText();
            program.programDescription = json.get("programDescription").asText();

            return GlobalResult.okResult(Json.toJson(program));

        } catch (NullPointerException e) {
            return GlobalResult.badRequest(e, "programName - String", "programDescription - TEXT" );
        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - gellAllProgramFromProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result newVersionOfCProgram(String id){
        try{
            JsonNode json = request().body().asJson();

            C_Program c_program = C_Program.find.byId(id);
            if (c_program == null) return GlobalResult.notFoundObject();


            Version versionObject = new Version();


            if(json.has("version")) {

                // Verze začíná minimálně na 1.01
                if(json.get("version").asDouble()  < 1.01) return GlobalResult.badRequest("Version must be minimal 1.01");

                // Pokud už nějakou verzi mám musí být nová větší než ta předchozí
                if( !c_program.versions.isEmpty() &&  c_program.versions.get(0).azureLinkVersion >= json.get("version").asDouble() )
                 return  GlobalResult.forbidden ("You can create new minimal version with " + (c_program.versions.get(0).azureLinkVersion  + 0.01) + " only");

                // Pokud nemám používám z Json
                versionObject.azureLinkVersion =  json.get("version").asDouble();

            }else{
                // Json neobsahuje žádnou verzi
                if( !c_program.versions.isEmpty() ) versionObject.azureLinkVersion =  c_program.versions.get(0).azureLinkVersion + 0.01;
                else  versionObject.azureLinkVersion = 1.01;

            }
            versionObject.dateOfCreate = new Date();
            versionObject.versionName = json.get("versionName").asText();
            versionObject.versionDescription = json.get("versionDescription").asText();
            versionObject.c_program = c_program;
            versionObject.save();

            c_program.versions.add(versionObject);
            c_program.save();

            String temporaryDirectory =  "files/" + c_program.azureStorageLink ;
            String azurePath = c_program.azurePackageLink + "/" + c_program.azureStorageLink + "/" + versionObject.azureLinkVersion;


            UtilTools.c_ProgramAzure(temporaryDirectory, json.get("files"), azurePath,versionObject);

            return GlobalResult.created(Json.toJson(c_program));
        } catch (NullPointerException e) {
            e.printStackTrace(); //TODO
            return GlobalResult.badRequest(e, "programDescription - TEXT", "processorCode - String", "programName - String", "project - id-String", "files {}");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - newProcessor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result deleteVersionOfCProgram(String id, String version){
        try{

            Version versionObject = Version.find.byId(version);
            if (versionObject == null) return GlobalResult.notFoundObject();

            C_Program c_program = C_Program.find.byId(id);

            if (c_program == null) return GlobalResult.notFoundObject();

            UtilTools.azureDelete(GlobalValue.blobClient.getContainerReference("c-program"), c_program.azurePackageLink + "/" + c_program.azureStorageLink + "/" + versionObject.azureLinkVersion);

            versionObject.delete();

            return GlobalResult.okResult();
        } catch (NullPointerException e) {
            e.printStackTrace(); //TODO
            return GlobalResult.badRequest(e, "programDescription - TEXT", "processorCode - String", "programName - String", "project - id-String", "files {}");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - deleteVersionOfCProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public Result deleteCProgram(String id){
        try{

            C_Program c_program = C_Program.find.byId(id);

            if (c_program == null) return GlobalResult.notFoundObject();

            UtilTools.azureDelete(GlobalValue.blobClient.getContainerReference("c-program"), c_program.azurePackageLink + "/" + c_program.azureStorageLink);

            c_program.delete();

            return GlobalResult.okResult();
        } catch (NullPointerException e) {
            e.printStackTrace(); //TODO
            return GlobalResult.badRequest(e, "programDescription - TEXT", "processorCode - String", "programName - String", "project - id-String", "files {}");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - deleteCProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }

///###################################################################################################################*/

    @BodyParser.Of(BodyParser.Json.class)
    public Result compileCProgram(){
        return GlobalResult.okResult("Compiled!"); //TODO
    }

    public Result generateProjectForEclipse() {
       // EclipseProject.createFullnewProject();
        return GlobalResult.okResult("In TODO"); //TODO
    }

    public Result uploudBinaryFileToBoard(String id) {
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



    public Result uploudCompilationToBoard(String id, String boardId) {

        Board board = Board.find.byId(boardId);
        if(board == null ) return GlobalResult.notFoundObject();

        C_Program c_program = C_Program.find.byId(id);
        if (c_program == null) return GlobalResult.notFoundObject();

        //TODO Chybí kompilování atd... tohle bude mega metoda!!!
        return GlobalResult.okResult();
    }
///###################################################################################################################*/


    @BodyParser.Of(BodyParser.Json.class)
    public Result newProcessor() {
        try {
            JsonNode json = request().body().asJson();

            Processor processor = new Processor();

            processor.description   = json.get("description").asText();
            processor.processorCode = json.get("processorCode").asText();
            processor.processorName = json.get("processorName").asText();
            processor.speed         = json.get("speed").asInt();

            processor.save();
            return GlobalResult.created(Json.toJson(processor));

        } catch (NullPointerException e) {
            return GlobalResult.badRequest(e, "description - TEXT", "processorCode - String", "processorName - String", "speed - Integer", "libraryGroups [Id,Id..]");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - newProcessor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getProcessor( String id) {
        try {

            Processor processor = Processor.find.byId(id);

            if(processor == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(processor));

        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - newProcessor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getProcessorAll() {
        try {

           List<Processor> processors = Processor.find.all();
            return GlobalResult.okResult(Json.toJson(processors));

        } catch (Exception e) {
            return GlobalResult.badRequest(e);
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result updateProcessor( String id) {
        try {
            JsonNode json = request().body().asJson();

            Processor processor = Processor.find.byId(id);
            if(processor == null ) throw new Exception("Processor not Exist");


            processor.description   = json.get("description").asText();
            processor.processorCode = json.get("processorCode").asText();
            processor.processorName = json.get("processorName").asText();
            processor.speed         = json.get("speed").asInt();

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
            return GlobalResult.badRequest(e, "description - TEXT", "processorCode - String", "processorName - String", "speed - Integer", "libraryGroups [Id,Id..]");
        } catch (Exception e) {
            Logger.error("Error", e.getMessage());
            Logger.error("CompilationLibrariesController - updateProcessor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result deleteProcessor(String id) {
        try {

            Processor processor = Processor.find.byId(id);
            if(processor == null ) return GlobalResult.notFoundObject();

            processor.delete();

            return GlobalResult.okResult();
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - newProcessor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getProcessorDescription(String id) {
        try {
            Processor processor = Processor.find.byId(id);
            if(processor == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(processor.description));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - getProcessorDescription ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getProcessorLibraryGroups(String id) {
        try {
            Processor processor = Processor.find.byId(id);
            if(processor == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(processor.libraryGroups));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - getProcessorLibraryGroups ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getProcessorSingleLibraries(String id) {
        try {
            Processor processor = Processor.find.byId(id);
            if(processor == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(processor.singleLibraries));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - getProcessorSingleLibraries ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result connectProcessorWithLibrary(String id, String lbrId) {
        try {
            Processor processor = Processor.find.byId(id);
            if(processor == null ) return GlobalResult.notFoundObject();

            SingleLibrary singleLibrary = SingleLibrary.find.byId(id);
            if(singleLibrary == null ) return GlobalResult.notFoundObject();


            processor.singleLibraries.add(singleLibrary);
            processor.update();

            return GlobalResult.okResult();
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - connectProcessorWithLibrary ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result connectProcessorWithLibraryGroup(String id, String lbrgId) {
        try {
            Processor processor = Processor.find.byId(id);
            if(processor == null ) return GlobalResult.notFoundObject();

            LibraryGroup libraryGroup = LibraryGroup.find.byId(id);
            if(libraryGroup == null ) return GlobalResult.notFoundObject();


            processor.libraryGroups.add(libraryGroup);
            processor.update();

            return GlobalResult.okResult();
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - connectProcessorWithLibraryGroup ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result unconnectProcessorWithLibrary(String id, String lbrId) {
        try {
            Processor processor = Processor.find.byId(id);
            if(processor == null ) return GlobalResult.notFoundObject();

            SingleLibrary singleLibrary = SingleLibrary.find.byId(id);
            if(singleLibrary == null ) return GlobalResult.notFoundObject();


            processor.singleLibraries.remove(singleLibrary);
            processor.update();

            return GlobalResult.okResult();
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - connectProcessorWithLibrary ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result unconnectProcessorWithLibraryGroup(String id, String lbrgId) {
        try {
            Processor processor = Processor.find.byId(id);
            if(processor == null ) return GlobalResult.notFoundObject();

            LibraryGroup libraryGroup = LibraryGroup.find.byId(id);
            if(libraryGroup == null ) return GlobalResult.notFoundObject();


            processor.libraryGroups.remove(libraryGroup);
            processor.update();

            return GlobalResult.okResult();
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - connectProcessorWithLibraryGroup ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }


///###################################################################################################################*/

    @BodyParser.Of(BodyParser.Json.class)
    public Result newLibraryGroup() {
        try {
            JsonNode json = request().body().asJson();
            LibraryGroup libraryGroup = new LibraryGroup();

            libraryGroup.description = json.get("description").asText();
            libraryGroup.groupName = json.get("groupName").asText();

           // TODO? -> Nějaké třídění ??? (Private, Public,.. etc?)
            libraryGroup.azurePackageLink = "libraryGroup";

            while(true){ // I need Unique Value

                libraryGroup.azureStorageLink = new BigInteger(60, new SecureRandom()).toString(30).toLowerCase();
                if (LibraryGroup.find.where().eq("azureStorageLink",libraryGroup.azureStorageLink ).findUnique() == null) break;

            }
            libraryGroup.save();

            Version versionObject = new Version();
            versionObject.azureLinkVersion = 1.01;
            versionObject.dateOfCreate = new Date();
            versionObject.versionName = "Nova verze Skupiny s názvem " + libraryGroup.groupName;
            versionObject.libraryGroup = libraryGroup;
            versionObject.save();

            return GlobalResult.okResult(Json.toJson(libraryGroup));
        } catch (Exception e) {
            return GlobalResult.badRequest(e, "description - TEXT", "groupName - String");
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result createNewVersionLibraryGroup(String id){
        try {
            JsonNode json = request().body().asJson();

            LibraryGroup libraryGroup = LibraryGroup.find.byId(id);
            if(libraryGroup == null) return GlobalResult.notFoundObject();

            Version versionObject = new Version();
            versionObject.azureLinkVersion   = json.get("version").asDouble();
            versionObject.dateOfCreate       = new Date();
            versionObject.versionName        = json.get("versionName").asText();
            versionObject.versionDescription = json.get("description").asText();

            // Kontrola nové verze jestli je vyšší číslo než u té předchozí!
            Version versionHelp = Version.find.where().in("libraryGroup.id", libraryGroup.id).setOrderBy("azureLinkVersion").setMaxRows(1).findUnique();
            if(versionHelp == null) throw new Exception("Error který se nikdy neměl stát - knihovna nemá žádnou verzi!!!");
            if(versionHelp.azureLinkVersion >= versionObject.azureLinkVersion) throw new Exception("You can create new minimal version with " + (versionHelp.azureLinkVersion + 0.01) + " only");

            versionObject.libraryGroup = libraryGroup;
            versionObject.save();


            return GlobalResult.okResult(Json.toJson(versionObject));
        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - newProcessor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getVersionLibraryGroup(String id){
        try {
            LibraryGroup libraryGroup = LibraryGroup.find.byId(id);
            if(libraryGroup == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(libraryGroup.versions));
        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - getVersionLibraryGroup ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result uploudLibraryToLibraryGroup(String libraryId, Double versionId) {
        try {

            // Přijmu soubor
            Http.MultipartFormData body = request().body().asMultipartFormData();
            List<Http.MultipartFormData.FilePart> files = body.getFiles();

            for( Http.MultipartFormData.FilePart file :  files ) {
                System.out.println("Nahrávám soubor: " + file.getFilename());

                // If fileRecord group is not null
                LibraryGroup libraryGroup = LibraryGroup.find.byId(libraryId);
                if (libraryGroup == null) return GlobalResult.notFoundObject();

                Version versionObject = Version.find.where().in("libraryGroup.id", libraryGroup.id).eq("azureLinkVersion", versionId).setMaxRows(1).findUnique();
                if (versionObject == null) return GlobalResult.notFoundObject();

                // Control lenght of name
                String fileName = file.getFilename();
                if (fileName.length() < 5) GlobalResult.forbidden("Too short file name");

                // Ještě kontrola souboru zda už tam není - > Version a knihovny
                FileRecord fileRecord = FileRecord.find.where().in("versions.id", versionObject.id).eq("filename", fileName).setMaxRows(1).findUnique();
                if (fileRecord != null)
                    return GlobalResult.badRequest("File exist in this version -> " + fileName + " please, create new version!");

                // Mám soubor
                File libraryFile = file.getFile();

                // Připojuji se a tvořím cestu souboru
                CloudBlobContainer container = GlobalValue.blobClient.getContainerReference("libraries");

                String azurePath = libraryGroup.azurePackageLink + "/" + libraryGroup.azureStorageLink + "/" + versionObject.azureLinkVersion + "/" + fileName;

                CloudBlockBlob blob = container.getBlockBlobReference(azurePath);

                blob.upload(new FileInputStream(libraryFile), libraryFile.length());

                fileRecord = new FileRecord();
                fileRecord.filename = fileName;
                fileRecord.save();


                versionObject.files.add(fileRecord);
                versionObject.save();
            }

            return GlobalResult.okResult();
        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - uploudLibraryToLibraryGroup ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public Result getLibraryGroup(String id) {
        try {
            LibraryGroup libraryGroup = LibraryGroup.find.byId(id);
            if(libraryGroup == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(libraryGroup));
        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - getLibraryGroup ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result deleteLibraryGroup(String id) {
        try {
            LibraryGroup libraryGroup = LibraryGroup.find.byId(id);
            if(libraryGroup == null) return GlobalResult.notFoundObject();

            UtilTools.azureDelete(GlobalValue.blobClient.getContainerReference("libraries"), libraryGroup.azurePackageLink+"/"+libraryGroup.azureStorageLink);

            libraryGroup.delete();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - deleteLibraryGroup ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getLibraryGroupAll() {
        try {

            List<LibraryGroup> libraryGroups = LibraryGroup.find.all();
            return GlobalResult.okResult(Json.toJson(libraryGroups));

        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - getLibraryGroupAll ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result updateLibraryGroup(String id) {
        try {

            LibraryGroup libraryGroup = LibraryGroup.find.byId(id);
            if(libraryGroup == null) return GlobalResult.notFoundObject();

            JsonNode json = request().body().asJson();

            libraryGroup.description = json.get("description").asText();
            libraryGroup.groupName = json.get("groupName").asText();

            libraryGroup.save();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - updateLibraryGroup ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getLibraryGroupDescription(String id) {
        try {
            LibraryGroup libraryGroup = LibraryGroup.find.byId(id);
            if(libraryGroup == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(libraryGroup.description));

        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - getLibraryGroupDescription ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getLibraryGroupProcessors(String id) {
        try {
            LibraryGroup libraryGroup = LibraryGroup.find.byId(id);
            if(libraryGroup == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(libraryGroup.processors));
        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - getLibraryGroupProcessors ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getLibraryGroupLibraries(String libraryId, String versionId) {
        try {

            Version versionObject= Version.find.where().in("libraryGroup.id", libraryId).eq("id",versionId).setMaxRows(1).findUnique();
            if(versionObject == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(versionObject.files));
        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - getLibraryGroupLibraries ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result listOfFilesInVersion(String id){
        try {
            Version version = Version.find.byId(id);
            if(version == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(version.files));
        } catch (Exception e) {
            Logger.error("CompilationLibrariesController - listOfFilesInVersion ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    // TODO - IDEA?
    public Result fileRecord(String id){
       return TODO;
    }

    // TODO - IDEA?
    public Result getGroupLibraryFilter() {
        return TODO;
    }

///###################################################################################################################*/

    @BodyParser.Of(BodyParser.Json.class)
    public Result newSingleLibrary() {
        try {
            JsonNode json = request().body().asJson();

            SingleLibrary singleLibrary = new SingleLibrary();
            singleLibrary.libraryName = json.get("libraryName").asText();
            singleLibrary.description = json.get("description").asText();
            singleLibrary.azurePackageLink = "singleLibraries";

            while(true) { // I need Unique Value
                singleLibrary.azureStorageLink = new BigInteger(60, new SecureRandom()).toString(30).toLowerCase();
                if (SingleLibrary.find.where().eq("azureStorageLink", singleLibrary.azureStorageLink).findUnique() == null) break;
            }
            singleLibrary.save();


            Version versionObject = new Version();
            versionObject.azureLinkVersion = 1.01;
            versionObject.dateOfCreate = new Date();
            versionObject.versionName = "Nova verze Skupiny s názvem " + singleLibrary.libraryName;
            versionObject.singleLibrary = singleLibrary;
            versionObject.save();


            return GlobalResult.okResult(Json.toJson(singleLibrary));
        } catch (Exception e) {
            return GlobalResult.badRequest(e, "libraryName - String", "description - TEXT");
        }
    }

    public Result newVersionSingleLibrary(String id){
        try {
            JsonNode json = request().body().asJson();

            SingleLibrary singleLibrary = SingleLibrary.find.byId(id);
            if(singleLibrary == null)  return GlobalResult.badRequest("FileRecord Not found");

            Version versionObject = new Version();
            versionObject.azureLinkVersion = json.get("version").asDouble();
            versionObject.dateOfCreate = new Date();
            versionObject.versionName = json.get("versionName").asText();
            versionObject.versionDescription = json.get("description").asText();


            if(versionObject.azureLinkVersion  == null )  return GlobalResult.badRequest("Version doesn't make a sense");
            if(versionObject.azureLinkVersion  <  1.01)    return GlobalResult.badRequest("Version must start at 1.01");


            // Kontrola nové verze jestli je vyšší číslo než u té předchozí!
            Version versionHelp = Version.find.where().in("singleLibrary.id", singleLibrary.id).setOrderBy("azureLinkVersion").setMaxRows(1).findUnique();
            if(versionHelp == null) return GlobalResult.badRequest("Error který se nikdy neměl stát - knihovna nemá žádnou verzi!!!");
            if(versionHelp.azureLinkVersion >= versionObject.azureLinkVersion) return GlobalResult.badRequest("You can create new minimal version with " + (versionHelp.azureLinkVersion + 0.01) + " only");

            versionObject.singleLibrary = singleLibrary;
            versionObject.save();


            return GlobalResult.okResult(Json.toJson(versionObject));

        } catch (NullPointerException a) {
            return GlobalResult.badRequest(a, "description - TEXT", "versionName - String", "version - Double");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - newProcessor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getAllVersionSingleLibrary(String id) {
        try {

            SingleLibrary singleLibrary = SingleLibrary.find.byId(id);
            if(singleLibrary == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(singleLibrary.versions));

        } catch (Exception e) {
            return GlobalResult.badRequest(e);
        }
    }

    public Result uploadSingleLibraryWithVersion(String id, Double version){
        try{

            Http.MultipartFormData body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart file = body.getFile("file");

            SingleLibrary singleLibrary = SingleLibrary.find.byId(id);
            if(singleLibrary == null ) return GlobalResult.badRequest("fileRecord not Exist: -> " +id);

            // If fileRecord group is not null
            Version versionObject= Version.find.where().in("singleLibrary.id", id).eq("azureLinkVersion",version).setMaxRows(1).findUnique();
            if(versionObject == null ) return GlobalResult.badRequest("Version in library not Exist: -> " +version);

            if (versionObject.files.size() > 0) return GlobalResult.badRequest("Version has file already.. Create new Version ");

            // Control lenght of name
            String fileName = file.getFilename();
            if(fileName.length()< 5 )return GlobalResult.badRequest("Too short FileName -> " + fileName);

            File libraryFile = file.getFile();

            FileRecord fileRecord =  new FileRecord();
            fileRecord.filename = fileName;
            fileRecord.save();

            CloudBlobContainer container = GlobalValue.blobClient.getContainerReference("libraries");
            String azurePath = singleLibrary.azurePackageLink + "/" + singleLibrary.azureStorageLink + "/"+ versionObject.azureLinkVersion  +"/" + fileRecord.filename;
            CloudBlockBlob blob = container.getBlockBlobReference(azurePath);

            blob.upload(new FileInputStream(libraryFile), libraryFile.length());

            versionObject.files.add(fileRecord);
            versionObject.dateOfCreate = new Date();
            versionObject.update();

            return GlobalResult.okResult(Json.toJson(versionObject));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - newProcessor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result uploadSingleLibrary(String id){

        try{
            Http.MultipartFormData body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart file = body.getFile("file");

            SingleLibrary singleLibrary = SingleLibrary.find.byId(id);
            if(singleLibrary == null ) return GlobalResult.notFoundObject();

            // Control lenght of name
            String fileName = file.getFilename();
            if(fileName.length()< 5 ) return GlobalResult.badRequest("Too short FileName -> " + fileName);

            Double versionValue = 1.01;
            Version versionObject = Version.find.where().in("singleLibrary.id", id).setMaxRows(1).findUnique();

            // Pokud žádná verze neexistuje
            if(versionObject != null ) versionValue = versionObject.azureLinkVersion + 0.01;


            Version version = new Version();
            version.azureLinkVersion = versionValue;
            version.singleLibrary = singleLibrary;
            version.dateOfCreate = new Date();
            version.save();

            File libraryFile = file.getFile();

            FileRecord fileRecord =  new FileRecord();
            fileRecord.filename = fileName;
            fileRecord.save();

            CloudBlobContainer container = GlobalValue.blobClient.getContainerReference("libraries");
            String azurePath = singleLibrary.azurePackageLink + "/" + singleLibrary.azureStorageLink + "/"+ version.azureLinkVersion  +"/" + fileRecord.filename;
            CloudBlockBlob blob = container.getBlockBlobReference(azurePath);

            blob.upload(new FileInputStream(libraryFile), libraryFile.length());

            version.files.add(fileRecord);
            version.update();

            return GlobalResult.okResult(Json.toJson(version));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - newProcessor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }

    }


    public Result getSingleLibrary(String id) {
        try {

            SingleLibrary singleLibrary = SingleLibrary.find.byId(id);
            if(singleLibrary == null) throw new Exception("FileRecord not found");
            return GlobalResult.okResult(Json.toJson(singleLibrary));

        } catch (Exception e) {
            return GlobalResult.badRequest(e);
        }
    }

    public Result getSingleLibraryAll() {
        try {

            List<SingleLibrary> singleLibraries = SingleLibrary.find.all();
            return GlobalResult.okResult(Json.toJson(singleLibraries));

        } catch (Exception e) {
            return GlobalResult.badRequest(e);
        }
    }

    // TODO - IDEA?
    public Result getSingleLibraryFilter() {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequest(e);
        }
    }

    public Result updateSingleLibrary(String id) {
        try {
            JsonNode json = request().body().asJson();

            SingleLibrary singleLibrary = SingleLibrary.find.byId(id);
            if(singleLibrary == null) throw new Exception("FileRecord not found");

            singleLibrary.libraryName = json.get("libraryName").asText();
            singleLibrary.description = json.get("description").asText();


            singleLibrary.update();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequest(e);
        }
    }

    public Result deleteSingleLibrary(String id) {
        try {

            SingleLibrary singleLibrary = SingleLibrary.find.byId(id);
            if(singleLibrary == null) throw new Exception("FileRecord not found");

            UtilTools.azureDelete(GlobalValue.blobClient.getContainerReference("libraries"), singleLibrary.azurePackageLink+"/"+singleLibrary.azureStorageLink);

            singleLibrary.delete();
            return GlobalResult.okResult();

        } catch (Exception e) {
            return GlobalResult.badRequest(e);
        }
    }


/**###################################################################################################################*/
    @BodyParser.Of(BodyParser.Json.class)
    public Result newProducers() {
        try {
            JsonNode json = request().body().asJson();

            Producer producer = new Producer();
            producer.name = json.get("name").asText();
            producer.description = json.get("description").asText();

            producer.save();

            return GlobalResult.created(Json.toJson(producer));
        } catch (NullPointerException e) {
            return GlobalResult.badRequest(e, "description - TEXT", "name - String");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - newProcessor ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result updateProducers(String id) {
        try {
            JsonNode json = request().body().asJson();

            Producer producer = Producer.find.byId(id);
            if(producer == null ) return GlobalResult.notFoundObject();

            producer.name = json.get("name").asText();
            producer.description = json.get("description").asText();

            producer.update();

            return GlobalResult.okResult(Json.toJson(producer));
        } catch (NullPointerException e) {
            return GlobalResult.badRequest(e, "description - TEXT", "name - String");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - updateProducers ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getProducers() {
        try {
            List<Producer> producers = Producer.find.all();

            return GlobalResult.okResult(Json.toJson(producers));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - getProducers ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getProducer(String id) {
        try {
            Producer producer = Producer.find.byId(id);

            if(producer == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(producer));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - getProducer ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getProducerDescription(String id) {
        try {
            Producer producer = Producer.find.byId(id);

            if(producer == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(producer.description));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - getProducerDescription ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getProducerTypeOfBoards(String id) {
        try {
            Producer producer = Producer.find.byId(id);

            if(producer == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(producer.typeOfBoards));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - getProducerTypeOfBoards ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

/**###################################################################################################################*/
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
            return GlobalResult.badRequest(e, "description - TEXT","name - String", "processorId - String", "producerId - String");
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
            return GlobalResult.badRequest(e, "description - TEXT","name - String", "processorId - String", "producerId - String");
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

/**###################################################################################################################*/
    @BodyParser.Of(BodyParser.Json.class)
    public Result newBoard() {
        try {
            JsonNode json = request().body().asJson();
            if (Board.find.byId(json.get("hwName").asText()) != null) GlobalResult.forbidden("Duplicate database value");

            TypeOfBoard typeOfBoard = TypeOfBoard.find.byId(json.get("typeOfBoard").asText());
            if(typeOfBoard == null ) return GlobalResult.notFoundObject();

            Board board = new Board();
            board.id = json.get("hwName").asText();
            board.isActive = false;
            board.typeOfBoard = typeOfBoard;

            board.save();

            return GlobalResult.okResult(Json.toJson(board));

        } catch (NullPointerException e) {
            return GlobalResult.badRequest(e, "hwName - String(Unique)", "typeOfBoard - String(Id)");
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
            return GlobalResult.badRequest(e, "generalDescription - Text", "typeOfBoard - String(Id)");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("CompilationLibrariesController - newBoard ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
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
            return GlobalResult.badRequest(e);
        }
    }

    public Result getUserDescription(String id) {
        try {
            JsonNode json = request().body().asJson();

            Board board = Board.find.byId(id);
            if(board == null ) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(board.userDescription));
        } catch (Exception e) {
            return GlobalResult.badRequest(e);
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

    public Result unconnectBoardWthProject(String id, String pr){
        try {
            Board board = Board.find.byId(id);
            if(board == null ) return GlobalResult.notFoundObject();

            Project project = Project.find.byId(pr);
            if(project == null) return GlobalResult.notFoundObject();

            if( !board.projects.contains(project)) return  GlobalResult.okResult("is already unconnected");
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
