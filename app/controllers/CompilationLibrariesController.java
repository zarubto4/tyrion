package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import models.compiler.*;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utilities.A_GlobalValue;
import utilities.EclipseProject.EclipseProject;
import utilities.GlobalResult;
import utilities.UtilTools;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;


public class CompilationLibrariesController extends Controller {

    public Result newProcessor() {
        try {
            JsonNode json = request().body().asJson();

            Processor processor = new Processor();

            processor.description   = json.get("description").asText();
            processor.processorCode = json.get("processorCode").asText();
            processor.processorName = json.get("processorName").asText();
            processor.speed         = json.get("speed").asInt();

            List<String> libraryGroups = UtilTools.getListFromJson(json, "libraryGroups");

            for (String id : libraryGroups) {
                try {
                    processor.libraryGroups.add(LibraryGroup.find.byId(id));
                } catch (Exception e) {/**nothing*/}
            }

            processor.save();

            return GlobalResult.okResultWithId(processor.id);
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e, "description - TEXT", "processorCode - String", "processorName - String" , "speed - Integer", "libraryGroups [Id,Id..]");
        }
    }

    public Result getProcessor(String id) {
        try {
            Processor processor = Processor.find.byId(id);
            if(processor == null ) throw new Exception("Processor not Exist");

            return GlobalResult.okResult(Json.toJson(processor));
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getProcessorAll() {
        try {
           List<Processor> processors = Processor.find.all();

            return GlobalResult.okResult(Json.toJson(processors));
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result updateProcessor(String id) {
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

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result deleteProcessor(String id) {
        try {

            Processor processor = Processor.find.byId(id);
            if(processor == null ) throw new Exception("Processor not Exist");

            processor.delete();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getProcessorDescription(String id) {
        try {
            Processor processor = Processor.find.byId(id);
            if(processor == null ) throw new Exception("Processor not Exist");

            return GlobalResult.okResult(Json.toJson(processor.description));
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getProcessorLibraryGroups(String id) {
        try {
            Processor processor = Processor.find.byId(id);
            if(processor == null ) throw new Exception("Processor not Exist");

            return GlobalResult.okResult(Json.toJson(processor.libraryGroups));
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }


/**###################################################################################################################*/

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

            return GlobalResult.okResultWithId(libraryGroup.id);
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e, "description - TEXT", "groupName - String");
        }
    }

    public Result createNewVersionLibraryGroup(String id){
        try {
            JsonNode json = request().body().asJson();

            LibraryGroup libraryGroup = LibraryGroup.find.byId(id);
            if(libraryGroup == null) throw new Exception("LibraryRecord Not found");

            Version versionObject = new Version();
            versionObject.azureLinkVersion = json.get("version").asDouble();
            versionObject.dateOfCreate = new Date();
            versionObject.versionName = json.get("versionName").asText();
            versionObject.versionDescription = json.get("description").asText();

            // Kontrola nové verze jestli je vyšší číslo než u té předchozí!
            Version versionHelp = Version.find.where().in("libraryGroup.id", libraryGroup.id).setOrderBy("azureLinkVersion").setMaxRows(1).findUnique();
            if(versionHelp == null) throw new Exception("Error který se nikdy neměl stát - knihovna nemá žádnou verzi!!!");
            if(versionHelp.azureLinkVersion > versionObject.azureLinkVersion) throw new Exception("You can create new minimal version with " + (versionHelp.azureLinkVersion + 0.01) + " only");

            versionObject.libraryGroup = libraryGroup;
            versionObject.save();


            return GlobalResult.okResultWithId(versionObject.id);
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e, "description - TEXT", "versionName - String", "version - Double");
        }
    }

    public Result getVersionLibraryGroup(String id){
        try {
            LibraryGroup libraryGroup = LibraryGroup.find.byId(id);
            if(libraryGroup == null) throw new Exception("LibraryRecord Not found");



            return GlobalResult.okResult(Json.toJson(libraryGroup.versions));
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e, "description - TEXT", "versionName - String", "version - Double");
        }
    }

    public Result uploudLibraryToLibraryGroup(String libraryId, Double versionId) {
        try {

            // Přijmu soubor
            Http.MultipartFormData body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart file = body.getFile("file");

            // If libraryRecord group is not null
            LibraryGroup libraryGroup = LibraryGroup.find.byId(libraryId);
            if(libraryGroup == null ) throw new Exception("libraryGroup not Exist: -> " +libraryId);

            Version versionObject = Version.find.where().in("libraryGroup.id", libraryGroup.id).eq("azureLinkVersion",versionId).setMaxRows(1).findUnique();
            if(versionObject == null ) throw new Exception("Version not Exist: -> " +versionId);

            // Its file not null
            if (file == null) throw new Exception("No File");

            // Control lenght of name
            String fileName = file.getFilename();
            if(fileName.length()< 5 ) throw new Exception("Too short FileName -> " + fileName);

            // Ještě kontrola souboru zda už tam není - > Version a knihovny
            LibraryRecord libraryRecord = LibraryRecord.find.where().in("versions.id", versionObject.id).eq("filename", fileName).setMaxRows(1).findUnique();
            if(libraryRecord != null) throw new Exception("File exist in this version -> " + fileName + " please, create new version!");


            // Mám soubor
            File libraryFile = file.getFile();

            // Připojuji se a tvořím cestu souboru
            CloudBlobContainer container = A_GlobalValue.blobClient.getContainerReference("libraries");

            String azurePath = libraryGroup.azurePackageLink+"/"+libraryGroup.azureStorageLink+ "/"+ versionObject.azureLinkVersion +"/"+fileName;

            CloudBlockBlob blob = container.getBlockBlobReference(azurePath);

            blob.upload(new FileInputStream(libraryFile), libraryFile.length());

            libraryRecord = new LibraryRecord();
            libraryRecord.filename = fileName;
            libraryRecord.save();


            versionObject.records.add(libraryRecord);
            versionObject.save();

            return GlobalResult.okResult();
        } catch (Exception e) {
            e.printStackTrace();
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getLibraryGroup(String id) {
        try {
            LibraryGroup libraryGroup = LibraryGroup.find.byId(id);
            if(libraryGroup == null) throw new Exception("Not found");

            return GlobalResult.okResult(Json.toJson(libraryGroup));
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result deleteLibraryGroup(String id) {
        try {
            LibraryGroup libraryGroup = LibraryGroup.find.byId(id);
            if(libraryGroup == null) throw new Exception("Not found");

            UtilTools.azureDelete(A_GlobalValue.blobClient.getContainerReference("libraries"), libraryGroup.azurePackageLink+"/"+libraryGroup.azureStorageLink);

            libraryGroup.delete();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getLibraryGroupAll() {
        try {

            List<LibraryGroup> libraryGroups = LibraryGroup.find.all();
            return GlobalResult.okResult(Json.toJson(libraryGroups));

        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result updateLibraryGroup(String id) {
        try {

            LibraryGroup libraryGroup = LibraryGroup.find.byId(id);
            if(libraryGroup == null) throw new Exception("Not found");

            JsonNode json = request().body().asJson();

            libraryGroup.description = json.get("description").asText();
            libraryGroup.groupName = json.get("groupName").asText();

            libraryGroup.save();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e, "description - TEXT", "groupName - String");
        }
    }

    public Result getLibraryGroupDescription(String id) {
        try {
            LibraryGroup libraryGroup = LibraryGroup.find.byId(id);
            if(libraryGroup == null) throw new Exception("Not found");

            return GlobalResult.okResult(Json.toJson(libraryGroup.description));
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getLibraryGroupProcessors(String id) {
        try {
            LibraryGroup libraryGroup = LibraryGroup.find.byId(id);
            if(libraryGroup == null) throw new Exception("Not found");

            return GlobalResult.okResult(Json.toJson(libraryGroup.processors));
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getLibraryGroupLibraries(String libraryId, String versionId) {
        try {

            Version versionObject= Version.find.where().in("libraryGroup.id", libraryId).eq("id",versionId).setMaxRows(1).findUnique();
            if(versionObject == null ) throw new Exception("Version in library not Exist: -> " +versionId);

            return GlobalResult.okResult(Json.toJson(versionObject.records));
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result listOfFilesInVersion(String id){
        try {
            Version version = Version.find.byId(id);
            if(version == null) throw new Exception("Version Not found");

            return GlobalResult.okResult(Json.toJson(version.records));
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    // TODO - IDEA?
    public Result fileRecord(String id){
        try {
            LibraryRecord libraryRecord = LibraryRecord.find.byId(id);
            if(libraryRecord == null) throw new Exception("Version Not found");

            return GlobalResult.okResult("Zatím neimplementováno... TODO .. tom youtrack.byzance.cz@36 ");
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    // TODO - IDEA?
    public Result getGroupLibraryFilter() {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

/**###################################################################################################################*/

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


            return GlobalResult.okResultWithId(singleLibrary.id);
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e, "libraryName - String", "description - TEXT");
        }
    }

    public Result newVersionSingleLibrary(String id){
        try {
            JsonNode json = request().body().asJson();

            SingleLibrary singleLibrary = SingleLibrary.find.byId(id);
            if(singleLibrary == null) throw new Exception("LibraryRecord Not found");

            Version versionObject = new Version();
            versionObject.azureLinkVersion = json.get("version").asDouble();
            versionObject.dateOfCreate = new Date();
            versionObject.versionName = json.get("versionName").asText();
            versionObject.versionDescription = json.get("description").asText();


            if(versionObject.azureLinkVersion  == null )  throw new Exception("Version doesn't make a sense");
            if(versionObject.azureLinkVersion  <  1.01)   throw new Exception("Version must start at 1.01");


            // Kontrola nové verze jestli je vyšší číslo než u té předchozí!
            Version versionHelp = Version.find.where().in("singleLibrary.id", singleLibrary.id).setOrderBy("azureLinkVersion").setMaxRows(1).findUnique();
            if(versionHelp == null) throw new Exception("Error který se nikdy neměl stát - knihovna nemá žádnou verzi!!!");
            if(versionHelp.azureLinkVersion > versionObject.azureLinkVersion) throw new Exception("You can create new minimal version with " + (versionHelp.azureLinkVersion + 0.01) + " only");

            versionObject.singleLibrary = singleLibrary;
            versionObject.save();


            return GlobalResult.okResultWithId(versionObject.id);
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e, "description - TEXT", "versionName - String", "version - Double");
        }
    }

    public Result uploadSingleLibraryWithVersion(String id, Double version){
        try{

            Http.MultipartFormData body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart file = body.getFile("file");

            SingleLibrary singleLibrary = SingleLibrary.find.byId(id);
            if(singleLibrary == null ) throw new Exception("libraryRecord not Exist: -> " +id);

            // If libraryRecord group is not null
            Version versionObject= Version.find.where().in("singleLibrary.id", id).eq("id",version).setMaxRows(1).findUnique();
            if(versionObject == null ) throw new Exception("Version in library not Exist: -> " +version);

            if (versionObject.records.size() > 0) throw new Exception("Version has file already.. Create new Version ");

            // Control lenght of name
            String fileName = file.getFilename();
            if(fileName.length()< 5 ) throw new Exception("Too short FileName -> " + fileName);

            File libraryFile = file.getFile();

            LibraryRecord libraryRecord =  new LibraryRecord();
            libraryRecord.filename = fileName;
            libraryRecord.save();

            CloudBlobContainer container = A_GlobalValue.blobClient.getContainerReference("records");
            String azurePath = singleLibrary.azurePackageLink + "/" + singleLibrary.azureStorageLink + "/"+ versionObject.azureLinkVersion  +"/" + libraryRecord.filename;
            CloudBlockBlob blob = container.getBlockBlobReference(azurePath);

            blob.upload(new FileInputStream(libraryFile), libraryFile.length());

            versionObject.records.add(libraryRecord);
            versionObject.update();

            return GlobalResult.okResult();
        } catch (Exception e) {
            e.printStackTrace();
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getSingleLibrary(String id) {
        try {

            SingleLibrary singleLibrary = SingleLibrary.find.byId(id);
            if(singleLibrary == null) throw new Exception("LibraryRecord not found");
            return GlobalResult.okResult(Json.toJson(singleLibrary));

        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getSingleLibraryAll() {
        try {

            List<SingleLibrary> singleLibraries = SingleLibrary.find.all();
            return GlobalResult.okResult(Json.toJson(singleLibraries));

        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    // TODO - IDEA?
    public Result getSingleLibraryFilter() {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result updateSingleLibrary(String id) {
        try {
            JsonNode json = request().body().asJson();

            SingleLibrary singleLibrary = SingleLibrary.find.byId(id);
            if(singleLibrary == null) throw new Exception("LibraryRecord not found");

            singleLibrary.libraryName = json.get("libraryName").asText();
            singleLibrary.description = json.get("description").asText();


            singleLibrary.update();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result deleteSingleLibrary(String id) {
        try {

            SingleLibrary singleLibrary = SingleLibrary.find.byId(id);
            if(singleLibrary == null) throw new Exception("LibraryRecord not found");

            UtilTools.azureDelete(A_GlobalValue.blobClient.getContainerReference("libraries"), singleLibrary.azurePackageLink+"/"+singleLibrary.azureStorageLink);

            singleLibrary.delete();
            return GlobalResult.okResult();

        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getSingleLibraryDescription(String id){
        try {
            SingleLibrary singleLibrary = SingleLibrary.find.byId(id);
            if(singleLibrary == null) throw new Exception("LibraryRecord not found");

            return GlobalResult.okResult(singleLibrary.description);
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }


/**###################################################################################################################*/

    public Result newProducers() {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result updateProducers(String id) {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getProducers() {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getProducer(String id) {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getProducerDescription(String id) {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getProducerTypeOfBoards(String id) {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result newTypeOfBoard() {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result updateTypeOfBoard(String id) {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getTypeOfBoards() {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getTypeOfBoard(String id) {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getTypeOfBoardDescription(String id) {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getTypeOfBoardAllBoards(String id) {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }


/**###################################################################################################################*/

    public Result newBoard() {
        try {

            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");

            if (Board.find.byId(json.get("hwName").asText()) != null) throw new Exception("Duplicate database value");

            Board board = new Board();

            board.save();


            return GlobalResult.okResult();

        } catch (Exception e) {
            return GlobalResult.badRequestResult(e, "hwName", "typeOfDevice", "producer", "parameters");
        }

    }

    public Result editBoard(String id) {
        try {

            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");

            Board board = Board.find.byId(id);

            if (board == null) throw new Exception("Id not exist");

            board.update();

            return GlobalResult.okResult();

        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }

    }

    public Result deleteBoard(String id) {
        try {

            Board board = Board.find.byId(id);

            if (board == null) throw new Exception("Id not exist");


            board.delete();

            return GlobalResult.okResult();

        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }

    }

    public Result getBoard(String id) {
        try {
            Board device = Board.find.byId(id);
            if (device == null) throw new Exception("Id not exist");

            return GlobalResult.ok(Json.toJson(device));
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getBoardgeneralDescription(String id) {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getUserDescription(String id) {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result generateProjectForEclipse() {

        EclipseProject.createFullnewProject();

        return ok("Ok");
    }


}
