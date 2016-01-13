package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.compiler.Board;
import models.compiler.LibraryGroup;
import models.compiler.Processor;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.EclipseProject.EclipseProject;
import utilities.GlobalResult;
import utilities.UtilTools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
            return GlobalResult.badRequestResult(e);
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


    public Result newLibraryGroup() {
        try {
            JsonNode json = request().body().asJson();
            LibraryGroup libraryGroup = new LibraryGroup();

            libraryGroup.description = json.get("description").asText();
            libraryGroup.groupName = json.get("groupName").asText();

            libraryGroup.save();

            return GlobalResult.okResultWithId(libraryGroup.id);
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e, "description - TEXT", "groupName - String");
        }
    }

    public  Result libraryGroupUpload(String id) {
        try {

            File path = new File("files/blablabla.txt");

            File file = request().body().asRaw().asFile();
            file.renameTo(path);

            file.createNewFile();

           // File.renameTo("files/test.txt");



            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("deasa");
            bw.close();


            file.mkdir();

            return ok("File uploaded");

            /*
            play.mvc.Http.MultipartFormData body = request().body().asMultipartFormData();
            play.mvc.Http.MultipartFormData.FilePart file = body.getFile("file");

            if (file != null) {
                String fileName = file.getFilename();
                String contentType = file.getContentType();
                java.io.File f = file.getFile();
                return ok("File uploaded");
            } else {
                flash("error", "Missing file");
                return badRequest();
            }*/

        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }


    public Result uploudLibraryToLibraryGroup() {

        return null;
    }

    public Result getLibraryGroup(String id) {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getLibraryGroupAll() {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result updateLibraryGroup(String id) {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getLibraryGroupDescription(String id) {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getLibraryGroupProcessors(String id) {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getLibraryGroupLibraries(String id) {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result newLibrary() {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getLibrary(String id) {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getLibraryAll() {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getLibraryFilter() {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result updateLibrary(String id) {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result deleteLibrary(String id) {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getLibraryDescription(String id) {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getLibraryContent(String id) {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result getLibraryLibraryGroups(String id) {
        try {
            JsonNode json = request().body().asJson();

            return GlobalResult.okResult();
        } catch (Exception e) {
            return GlobalResult.badRequestResult(e);
        }
    }

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
