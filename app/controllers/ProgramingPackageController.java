package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.blocko.*;
import models.login.Person;
import play.Logger;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.UtilTools;
import utilities.response.GlobalResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//@Security.Authenticated(Secured.class)
public class ProgramingPackageController extends Controller {

    @BodyParser.Of(BodyParser.Json.class)
    public  Result postNewProject() {
        try{
            JsonNode json = request().body().asJson();

            Project project  = new Project();
            project.projectName = json.get("projectName").asText();
            project.projectDescription = json.get("projectDescription").asText();
            project.ownersOfProject.add( SecurityController.getPerson() );

            project.save();

            return GlobalResult.okResult( Json.toJson(project) );

        } catch (NullPointerException e) {
            return GlobalResult.badRequest(e, "projectName - String", "projectDescription - TEXT");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - postNewProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public  Result getProjectsByUserAccount(){
        try {

            return GlobalResult.okResult(Json.toJson( SecurityController.getPerson().owningProjects ));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getProjectsByUserAccount ERROR");
            return GlobalResult.internalServerError();
        }

    }

    public  Result getProject(String id){
        try {
            Project project = Project.find.byId(id);
            if (project == null) return GlobalResult.notFoundObject();



            if (    Project.find.where().eq("ownersOfProject.mail", SecurityController.getPerson().mail).eq("projectId", id).findUnique() == null ) return GlobalResult.forbidden();

            return GlobalResult.okResult(Json.toJson(project));

         } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getProjectsByUserAccount ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public  Result deleteProject(String id){
        try {

            Project project = Project.find.byId(id);
            if (project == null) return GlobalResult.notFoundObject();

            if (!project.ownersOfProject.contains( SecurityController.getPerson() ) ) return GlobalResult.forbidden();

            project.delete();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - deleteProject ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public  Result updateProject(String id){
        try {
            JsonNode json = request().body().asJson();

            Project project = Project.find.byId(id);
            if (project == null) return GlobalResult.notFoundObject();

            if (!project.ownersOfProject.contains( SecurityController.getPerson() ) ) return GlobalResult.forbidden();

            project.projectName = json.get("projectName").asText();
            project.projectDescription = json.get("projectDescription").asText();
            project.update();

            return GlobalResult.okResult(Json.toJson(project));

        } catch (NullPointerException e) {
            return GlobalResult.badRequest(e, "projectName - String", "projectDescription - TEXT");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - updateProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getProjectsBoard(String id){
        try {

            Project project = Project.find.byId(id);
            if(project == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(project.boards));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getBoard ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result shareProjectWithUsers(String id){
        try {

            JsonNode json = request().body().asJson();

            Project project = Project.find.byId(id);
            if(project == null) return GlobalResult.notFoundObject();



            for (final JsonNode objNode : json.get("persons")) {

                Person person = Person.find.byId(objNode.asText());


                if(person != null && !person.owningProjects.contains(project)){
                    person.owningProjects.add(project);
                    person.update();
                }
            }

            return GlobalResult.okResult(Json.toJson(project));

        } catch (NullPointerException e) {
            return GlobalResult.badRequest(e, "persons - [ids]");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - shareProjectWithUsers ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result unshareProjectWithUsers(String id){
        try {
            JsonNode json = request().body().asJson();

            Project project = Project.find.byId(id);
            if(project == null) return GlobalResult.notFoundObject();

            for (final JsonNode objNode : json.get("persons")) {

                Person person = Person.find.byId(objNode.asText());

                if(person != null && person.owningProjects.contains(project)){
                    person.owningProjects.remove(project);
                    person.update();
                }
            }

            return GlobalResult.okResult(Json.toJson(project.ownersOfProject));

        } catch (NullPointerException e) {
            return GlobalResult.badRequest(e, "persons - [ids]");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - unshareProjectWithUsers ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getProjectOwners(String id){
        try {

            Project project = Project.find.byId(id);
            if(project == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(project.ownersOfProject));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - shareProjectWithUsers ERROR");
            return GlobalResult.internalServerError();
        }
    }


///###################################################################################################################*/

    @BodyParser.Of(BodyParser.Json.class)
    public  Result newHomer(){
        try{
            JsonNode json = request().body().asJson();

            Homer homer = new Homer();
            homer.homerId = json.get("homerId").asText();
            homer.typeOfDevice = json.get("typeOfDevice").asText();

            homer.save();

            return GlobalResult.okResult(Json.toJson(homer));

        } catch (NullPointerException e) {
            return GlobalResult.badRequest(e, "homerId - String", "typeOfDevice - String");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - newHomer ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public  Result removeHomer(String id){
        try{

           Homer homer = Homer.find.byId(id);
           if(homer == null) return GlobalResult.notFoundObject();

           homer.delete();

           return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - removeHomer ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public  Result getHomer(String id){
        try {
            Homer homer = Homer.find.byId(id);
            if (homer == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult( Json.toJson(homer) );

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - removeHomer ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public  Result getConnectedHomers(String id){
        try {
            Project project = Project.find.byId(id);
            if (project == null) return GlobalResult.notFoundObject();

            List<Homer> projectDevices = project.homerList;
            if(projectDevices.isEmpty()) return GlobalResult.okResult(Json.toJson(projectDevices));

            List<Homer> connectedDevices = WebSocketController.getAllConnectedDevice();

            List<Homer> intersection = new ArrayList<>();

            for( Homer homer :projectDevices) if(connectedDevices.contains(homer)) intersection.add(homer);

            return GlobalResult.okResult(Json.toJson(intersection));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getConnectedHomers ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public  Result getAllHomers(){
        try {
            List<Homer> homers = Homer.find.all();

            return GlobalResult.okResult(Json.toJson(homers));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getAllHomers ERROR");
            return GlobalResult.internalServerError();
        }
    }

// ###################################################################################################################*/

    @BodyParser.Of(BodyParser.Json.class)
    public  Result connectHomerWithProject(){
        try{
            JsonNode json = request().body().asJson();

            Project project = Project.find.byId(json.get("projectId").asText());
            Homer homer = Homer.find.byId(json.get("homerId").asText());

            if(project == null)  return GlobalResult.notFoundObject();
            if(homer == null)  return GlobalResult.notFoundObject();

            homer.project = project;
            homer.update();

            return GlobalResult.okResult(Json.toJson(project));
        } catch (NullPointerException e) {
            return GlobalResult.badRequest(e, "homerId - String", "projectId - String");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - connectHomerWithProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public  Result unConnectHomerWithProject(){
        try{
            JsonNode json = request().body().asJson();

            Project project = Project.find.byId(json.get("projectId").asText());
            Homer homer = Homer.find.byId(json.get("homerId").asText());

            if(project == null)  return GlobalResult.notFoundObject();
            if(homer == null)  return GlobalResult.notFoundObject();


            project.homerList.remove(homer);
            homer.project = null;

            project.update();
            homer.update();

            return GlobalResult.okResult(Json.toJson(project));
        } catch (NullPointerException e) {
            return GlobalResult.badRequest(e, "homerId - String", "projectId - String");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - unConnectHomerWithProject ERROR");
            return GlobalResult.internalServerError();
        }
    }

/// ###################################################################################################################*/

    @BodyParser.Of(BodyParser.Json.class)
    public  Result postNewProgram(){
        try{
            JsonNode json = request().body().asJson();


            Project project = Project.find.byId(json.get("projectId").asText());
            if (project == null) return GlobalResult.notFoundObject();

            HomerProgram program = new HomerProgram();

            program.programInString = json.get("program").toString();

            program.dateOfCreate = new Date();
            program.programDescription = json.get("programDescription").asText();
            program.programName = json.get("programName").asText();

            program.save();

            program.project = project;
            program.update();

            return GlobalResult.okResult(Json.toJson(program));

        } catch (NullPointerException e) {
            return GlobalResult.badRequest(e, "homerId - String", "projectId - String");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - postNewProgram ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public  Result getProgram(String id){
        try{

            HomerProgram program = HomerProgram.find.byId(id);
            if (program == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(program));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public  Result getProgramInJson(String id){
        try{

            HomerProgram program  = HomerProgram.find.byId(id);
            if (program == null) return GlobalResult.notFoundObject();


            return GlobalResult.okResult( Json.parse(program.programInString));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getProgramInJson ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public  Result getProgramPrograms(String id){
        try{

            Project project  = Project.find.byId(id);
            if (project == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(project.programs));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getProgramPrograms ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public  Result getProgramhomerList(String id){
        try{

            Project project  = Project.find.byId(id);
            if (project == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(project.homerList));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getProgramhomerList ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public  Result editProgram(String id){
        try{
            JsonNode json = request().body().asJson();

            HomerProgram program  = HomerProgram.find.byId(id);
            if (program == null) return GlobalResult.notFoundObject();


            program.programInString = json.get("program").asText();
            program.dateOfCreate = new Date();
            program.programDescription = json.get("programDescription").asText();
            program.programName = json.get("programName").asText();

            program.update();

            return GlobalResult.okResult(Json.toJson(program));

        } catch (NullPointerException e) {
            return GlobalResult.badRequest(e, "programName - String", "programDescription - TEXT", "program - TEXT" );
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - editProgram ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public  Result removeProgram(String id){
        try{

            HomerProgram program  = HomerProgram.find.byId(id);
            if (program == null) return GlobalResult.notFoundObject();


            program.delete();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - removeProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public  Result getAllPrograms(String id){
        try {

            Project project = Project.find.byId(id);
            if(project == null) throw new Exception("Project not exist");

           JsonNode json = new ObjectMapper().valueToTree(project.programs);

            return GlobalResult.okResult(json);
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - removeProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public  Result uploadProgramToHomer_Immediately(){
        try {
            JsonNode json = request().body().asJson();

            Homer homer = Homer.find.byId(json.get("homerId").asText());
            if (homer == null) return GlobalResult.notFoundObject();

            HomerProgram program = HomerProgram.find.byId(json.get("programId").asText());
            if (program == null) return GlobalResult.notFoundObject();

            Project project = homer.project;


            if (!project.projectId.equals(program.project.projectId)) GlobalResult.forbidden("Program is not from the same project!");
            if(!WebSocketController.isConnected(homer))           GlobalResult.forbidden("Homer is not connected");


            homer.sendProgramToHomer(program, null, null);

            program.successfullyUploaded.add(homer);
            program.update();

            return GlobalResult.okResult("Program was uploud To Homer succesfuly and started");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - uploadProgramToHomer_Immediately ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public  Result uploadProgramToHomer_AsSoonAsPossible(){
        try {
            JsonNode json = request().body().asJson();

            Homer homer = Homer.find.byId(json.get("homerId").asText());
            if (homer == null) return GlobalResult.notFoundObject();

            HomerProgram program = HomerProgram.find.byId(json.get("programId").asText());
            if (program == null) return GlobalResult.notFoundObject();

            Project project = homer.project;

            if (!project.projectId.equals(program.project.projectId)) GlobalResult.forbidden("Program is not from the same project!");

            //1 Pokud je zařízení přopojené, nahraji okamžitě
            Date until = UtilTools.returnDateFromMillis( json.get("until").asText());
            if(WebSocketController.isConnected(homer))   homer.sendProgramToHomer(program, null, until);

            //2 Pokud není, vytvářím meziobjekt - Mezi Holder
            ForUploadProgram forUploadProgram = new ForUploadProgram();
            forUploadProgram.homer = homer;
            forUploadProgram.program = program;
            forUploadProgram.untilDate =  UtilTools.returnDateFromMillis( json.get("until").asText());
            forUploadProgram.save();

            return GlobalResult.okResult("Homer " + homer.homerId + " is not online. When Homer logs to Cloud Server, Program " + program.programId + " will be upload");


        } catch (NullPointerException e) {
            return GlobalResult.badRequest(e, "homerId - String","programId - String");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - uploadProgramToHomer_Immediately ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public  Result uploadProgramToHomer_GivenTimeAsSoonAsPossible(){
        try {
            JsonNode json = request().body().asJson();

            Homer homer = Homer.find.byId(json.get("homerId").asText());
            if (homer == null) return GlobalResult.notFoundObject();

            HomerProgram program = HomerProgram.find.byId(json.get("programId").asText());
            if (program == null) return GlobalResult.notFoundObject();

            Project project = homer.project;

            if (!project.projectId.equals(program.project.projectId)) GlobalResult.forbidden("Program is not from the same project!");

            Date when = UtilTools.returnDateFromMillis( json.get("when").asText());
            Date until = UtilTools.returnDateFromMillis( json.get("until").asText());

            //1 Pokud je zařízení přopojené, nahraji okamžitě
            if(WebSocketController.isConnected(homer))  homer.sendProgramToHomer(program, when, until);

            //2 Pokud ne -
            ForUploadProgram forUploadProgram = new ForUploadProgram();
            forUploadProgram.homer = homer;
            forUploadProgram.program = program;
            forUploadProgram.untilDate =  until;
            forUploadProgram.whenDate =  when;
            forUploadProgram.save();

            return GlobalResult.okResult("Program was uploud succesfuly ");
        } catch (NullPointerException e) {
            return GlobalResult.badRequest(e, "homerId - String","programId - String");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - uploadProgramToHomer_GivenTimeAsSoonAsPossible ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public  Result uploadProgramToHomer(){
        try {
            JsonNode json = request().body().asJson();

            if( UtilTools.returnIntFromString( json.get("when").asText() ) < 0 ) return uploadProgramToHomer_Immediately();
            if( UtilTools.returnIntFromString( json.get("when").asText() ) == 0 ) return uploadProgramToHomer_AsSoonAsPossible();
            else return uploadProgramToHomer_GivenTimeAsSoonAsPossible();
        } catch (NullPointerException e) {
            return GlobalResult.badRequest(e, "when - String");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - removeProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }


    //TODO
    public Result listOfUploadedHomers(String id) {
        //Na projectId HomerProgram vezmu všechny Houmry na kterých je program nahrán
        return GlobalResult.ok("Nutné dodělat - listOfUploadedHomers");
    }

    //TODO
    public Result listOfHomersWaitingForUpload(String id){
        //Na projectId HomerProgram vezmu všechny Houmry na které jsem program ještě nenahrál
        return GlobalResult.ok("Nutné dodělat - listOfHomersWaitingForUpload");
    }


///###################################################################################################################*/

    @BodyParser.Of(BodyParser.Json.class)
    public Result newBlock(){
       try{
            JsonNode json = request().body().asJson();

            BlockoBlock blockoBlock = new BlockoBlock();
            blockoBlock.generalDescription = json.get("description").asText();
            blockoBlock.name        = json.get("name").asText();
            blockoBlock.author = SecurityController.getPerson();


            BlockoContentBlock contentBlock = new BlockoContentBlock();
            contentBlock.dateOfCreate = new Date();
            contentBlock.designJson   = json.findValue("designJson").toString();
            contentBlock.logicJson    = json.findValue("logicJson").toString();
            contentBlock.version  = 1.01;

            contentBlock.save();
            blockoBlock.save();


            contentBlock.blockoBlock  = blockoBlock;
            blockoBlock.contentBlocks.add(contentBlock);

            contentBlock.update();


            return GlobalResult.okResult( Json.toJson(blockoBlock) );
       } catch (NullPointerException e) {
           return GlobalResult.badRequest(e, "name - String", "designJson - TEXT", "logicJson - TEXT", "description - TEXT");
       } catch (Exception e) {
           Logger.error("Error", e);
           Logger.error("ProgramingPackageController - newBlock ERROR");
           return GlobalResult.internalServerError();
       }
    }

    public Result getBlockLast(String id){
        try {
                BlockoBlock blockoBlock = BlockoBlock.find.byId(id);
                if (blockoBlock == null) return GlobalResult.notFoundObject();

                return GlobalResult.ok(Json.toJson(blockoBlock));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getBlockLast ERROR");
            return GlobalResult.internalServerError();
        }

    }

    public Result getBlockVersion(String id, Double version){
        try {

                BlockoContentBlock block = BlockoContentBlock.find.where().in("blockoBlock.id", id).where().in("version", version).findUnique();
                if (block == null) return GlobalResult.notFoundObject();

                BlockoBlock blockoBlock = BlockoBlock.find.byId(id);
                blockoBlock.setVersion(version);

                return GlobalResult.ok(Json.toJson(blockoBlock));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getBlockVersion ERROR");
            return GlobalResult.internalServerError();
        }

    }

    public Result getByFilter(){
        // Přijmu v JSON nový blok
        return null;
    }

    //TODO
    public Result deleteBlock(String url){
        String[] parts = url.split("/");

        if (parts.length > 2)
            return GlobalResult.badRequest(new Exception("URL " + url + " contains more substrings than one"));


        if (parts.length > 1) {
            BlockoContentBlock block = BlockoContentBlock.find
                    .where().in("blockoBlock.id", parts[0])
                    .eq("version", Double.valueOf(parts[1]))
                    .findUnique();
            block.delete();
            return GlobalResult.ok();

        } else {
            BlockoBlock block = BlockoBlock.find.byId(parts[0]);
            block.delete();
            return GlobalResult.ok();
        }
    }

    public Result newVersionOfBlock(String id){
        try {
            JsonNode json = request().body().asJson();
            BlockoBlock blockoBlock = BlockoBlock.find.byId(id);

            BlockoContentBlock contentBlock = new BlockoContentBlock();
            contentBlock.dateOfCreate = new Date();
            contentBlock.designJson = json.findValue("designJson").toString();
            contentBlock.logicJson = json.findValue("logicJson").toString();

            BlockoContentBlock block = BlockoContentBlock.find
                    .where().in("blockoBlock.id", id)
                    .orderBy("version").setMaxRows(1).findUnique();

            if (json.hasNonNull("version")) {
                Double newVersion = json.get("version").asDouble();
                Double previousVersion = block.version;

                if (newVersion < previousVersion)
                    return GlobalResult.badRequest(new Exception("A new version " + newVersion + " should have a greater number than the previous " + previousVersion));
                contentBlock.version = newVersion;
            } else contentBlock.version = block.version + 0.01;

            contentBlock.blockoBlock = blockoBlock;

            contentBlock.save();
            blockoBlock.contentBlocks.add(contentBlock);
            blockoBlock.update();

            return GlobalResult.okResult();
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - shareProjectWithUsers ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }


    public Result designJsonVersion(String id, Double version){
        try {

             BlockoContentBlock block = BlockoContentBlock.find
                        .where().in("blockoBlock.id", id)
                        .eq("version", version)
                        .findUnique();

            if(block == null) return GlobalResult.notFoundObject();

            return GlobalResult.ok(Json.toJson(block.designJson));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - designJsonDouble ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public Result logicJsonVersion(String id, Double version){
        try {

            BlockoContentBlock block = BlockoContentBlock.find
                    .where().in("blockoBlock.id", id)
                    .eq("version", version)
                    .findUnique();

            if(block == null) return GlobalResult.notFoundObject();

            return GlobalResult.ok(Json.toJson(block.logicJson));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - logicJson ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result designJsonLast(String id){
        try {

            BlockoContentBlock block = BlockoContentBlock.find
                        .where().in("blockoBlock.id", id)
                        .orderBy("dateOfCreate").where()
                        .setMaxRows(1)
                        .findUnique();

            if(block == null) return GlobalResult.notFoundObject();

            return GlobalResult.ok(Json.toJson(block.designJson));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - logicJson ERROR");
            return GlobalResult.internalServerError();
        }
    }


    public Result logicJsonLast(String id){
        try {

            BlockoContentBlock block = BlockoContentBlock.find
                    .where().in("blockoBlock.id", id)
                    .orderBy("dateOfCreate").where()
                    .setMaxRows(1)
                    .findUnique();

            if(block == null) return GlobalResult.notFoundObject();

            return GlobalResult.ok(Json.toJson(block.logicJson));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - logicJson ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result allPrevVersions(String id){
        try {
            BlockoBlock blockoBlock = BlockoBlock.find.byId(id);
            if (blockoBlock == null) return GlobalResult.notFoundObject();
            return GlobalResult.ok(Json.toJson(blockoBlock.contentBlocks));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - allPrevVersions ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public Result generalDescription(String id) {
        try {
            BlockoBlock block = BlockoBlock.find.byId(id);
            if (block == null) return GlobalResult.notFoundObject();
            return GlobalResult.ok(Json.toJson(block.generalDescription));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - generalDescription ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public Result versionDescription(String id) {
        try {
            BlockoContentBlock block = BlockoContentBlock.find.byId(id);
            if (block == null) return GlobalResult.notFoundObject();

            return GlobalResult.ok(Json.toJson(block.versionDescription));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - versionDescription ERROR");
            return GlobalResult.internalServerError();
        }
    }


}
