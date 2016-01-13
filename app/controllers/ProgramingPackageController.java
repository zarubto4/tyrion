package controllers;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.*;
import models.blocko.*;
import models.compiler.Board;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.GlobalResult;
import utilities.Secured;
import utilities.UtilTools;
import webSocket.controllers.SocketCollector;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Security.Authenticated(Secured.class)
public class ProgramingPackageController extends Controller {

    public  Result postNewProject() {
        try{
            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");
            Project project  = Json.fromJson(json, Project.class);

            Person prihlaseny =  SecurityController.getPerson();

            project.ownersOfProject.add( prihlaseny );

            project.save();



            ObjectNode result = Json.newObject();
            result.put("projectId", project.projectId);

            return GlobalResult.okResult( result );

        } catch(Exception e){
           return GlobalResult.badRequestResult(e);
        }
    }

    public  Result getProjectsByUserAccount(){
        try {

            return GlobalResult.okResult(Json.toJson( SecurityController.getPerson().owningProjects ));

        } catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }

    }

    public  Result getProject(String id){
        try {
            Project project = Project.find.byId(id);
            if (project == null) throw new Exception("Project with this ID not exist");


            return GlobalResult.okResult(Json.toJson(project));

        } catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public  Result deleteProject(String id){

        try {
            Project project = Project.find.byId(id);
            if (project == null) throw new Exception("Project with this ID not exist");

            project.delete();

            return GlobalResult.okResult("Deleting was properly performed");
        } catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public  Result updateProject(String id){
        try {
            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");

            Project project = Project.find.byId(id);
            if (project == null) throw new Exception("Project with this ID not exist");

            project.projectName = json.get("projectName").asText();
            project.projectDescription = json.get("projectDescription").asText();
            project.update();

            return GlobalResult.okResult("Updating was properly performed");
        } catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public  Result newHomer(){
        try{
            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");
            Homer help = Json.fromJson(json, Homer.class);

            help.save();

            return GlobalResult.okResult();
        } catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public  Result removeHomer(String id){
        try{
            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");

            Homer.find.byId(id).delete();

            return GlobalResult.okResult();
        } catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public  Result getHomer(String id){
        try {
            Homer device = Homer.find.byId(id);
            if (device == null) throw new Exception("Homer with this macAddress not exist");

            return GlobalResult.okResult( Json.toJson(device) );

        } catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public  Result getConnectedHomers(String id){
        try {
            Project project = Project.find.byId(id);
            if (project == null) throw new Exception("Project with this ID not exist");

            List<Homer> projectDevices = project.homerList;
            if(projectDevices.isEmpty()) return GlobalResult.okResult(Json.toJson(projectDevices));

            List<Homer> connectedDevices = SocketCollector.getAllConnectedDevice();

            List<Homer> intersection = new ArrayList<>();

            for( Homer homer :projectDevices) if(connectedDevices.contains(homer)) intersection.add(homer);

            return GlobalResult.okResult(Json.toJson(intersection));

        } catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public  Result getAllHomers(){
        try {
            List<Homer> devices = Homer.find.all();
            if (devices == null) throw new Exception("Zero homers in database");

            JsonNode json = new ObjectMapper().valueToTree(devices);

            return GlobalResult.okResult(json);

        } catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }


    }

    public  Result postHome(){
        try{
            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");
            Home help = Json.fromJson(json, Home.class);

            help.save();

            return GlobalResult.okResult();

        }catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }



    public  Result connectIoTWithProject(){
        try{

            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");

            Project project = Project.find.byId(json.get("projectId").asText());
            if( project == null) throw new Exception("Project not exist");

            if (json.get("hwName").isArray()) {

                for (final JsonNode objNode : json.get("hwName")) {

                    Board device = Board.find.byId(objNode.asText());
                    if(device == null) throw new Exception("Device with hwName: " + objNode.asText() + " not exist");
                    project.electronicDevicesList.add(device);
                    device.project = project;
                    device.update();
                    project.update();

                }
            }

            project.save();

            return GlobalResult.okResult();

        }catch(Exception e){
            return GlobalResult.badRequestResult(e, "projectId", "hwName - []");
        }

    }

    public  Result unConnectIoTWithProject(){
        try{

            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");

            Project project = Project.find.byId(json.get("projectId").asText());
            if( project == null) throw new Exception("Project not exist");
            if (json.get("hwName").isArray()) {

                for (final JsonNode objNode : json.get("hwName")) {

                    Board device = Board.find.byId(objNode.asText());
                    if(device == null) throw new Exception("Device with hwName: " + objNode.asText() + " not exist");
                    project.electronicDevicesList.remove(device);
                    device.project = null;
                    device.update();
                    project.update();

                }
            }
            project.save();

            return GlobalResult.okResult();

        }catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }

    }

    public  Result connectHomerWithProject(){
        try{
            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");

            Project p = Project.find.byId(json.get("projectId").asText());
            Homer d = Homer.find.byId(json.get("homerId").asText());

            if(p == null)  throw new Exception(" Project doesn't exist");
            if(d == null)  throw new Exception(" Homer doesn't exist");

            d.project = p;
            d.update();

            return GlobalResult.okResult();

        }catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public  Result unConnectHomerWithProject(){
        try{
            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");

            Project p = Project.find.byId(json.get("projectId").asText());
            Homer d = Homer.find.byId(json.get("homerId").asText());

            if(p == null)  throw new Exception(" Project doesn't exist");
            if(d == null)  throw new Exception(" Homer doesn't exist");

            p.homerList.remove(d);
            d.project = null;

            p.update();
            d.update();

            return GlobalResult.okResult();
        }catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public  Result postNewProgram(){
        try{
            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");



            Project project = Project.find.byId(json.get("projectId").asText());
            if (project == null) throw new Exception("Project not exist");

            HomerProgram program = new HomerProgram();


            program.programInString = json.get("program").toString();


            program.dateOfCreate = new Date();
            program.programDescription = json.get("programDescription").asText();
            program.programName = json.get("programName").asText();

            program.save();


            program.project = project;
            program.update();

            ObjectNode result = Json.newObject();
            result.put("projectId", program.programId);

            return GlobalResult.okResult(result);
        }catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public  Result getProgram(String id){
        try{

            HomerProgram program  = HomerProgram.find.byId(id);
            if (program == null) throw new Exception("Program not exist");

            return GlobalResult.okResult(Json.toJson(program));
        }catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public  Result getProgramInJson(String id){
        try{

            HomerProgram program  = HomerProgram.find.byId(id);
            if (program == null) throw new Exception("Program not exist");

            ObjectMapper mapper = new ObjectMapper();
            JsonFactory factory = mapper.getFactory();
            JsonParser jp = factory.createParser(program.programInString);

            return GlobalResult.okResult(Json.toJson(jp));
        }catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public  Result getProgramPrograms(String id){
        try{

            Project project  = Project.find.byId(id);
            if (project == null) throw new Exception("Program not exist");

            return GlobalResult.okResult(Json.toJson(project.programs));
        }catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }
    public  Result getProgramelectronicDevicesList(String id){
        try{

            Project project  = Project.find.byId(id);
            if (project == null) throw new Exception("Program not exist");

            return GlobalResult.okResult(Json.toJson(project.electronicDevicesList));
        }catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }
    public  Result getProgramhomerList(String id){
        try{

            Project project  = Project.find.byId(id);
            if (project == null) throw new Exception("Program not exist");

            return GlobalResult.okResult(Json.toJson(project.homerList));
        }catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public  Result editProgram(String id){
        try{
            JsonNode json = request().body().asJson();

            if (json == null) throw new Exception("Null Json");

            HomerProgram program  = HomerProgram.find.byId(id);
            if (program == null) throw new Exception("Program not exist");


            program.programInString = json.get("program").asText();
            program.dateOfCreate = new Date();
            program.programDescription = json.get("programDescription").asText();
            program.programName = json.get("programName").asText();

            program.update();

            return GlobalResult.okResult();
        }catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public  Result removeProgram(String id){
        try{
            JsonNode json = request().body().asJson();

            if (json == null) throw new Exception("Null Json");

            HomerProgram program  = HomerProgram.find.byId(id);
            if (program == null) throw new Exception("Program not exist");

            program.delete();

            return GlobalResult.okResult();
        }catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public  Result getAllPrograms(String id){
        try {

            Project project = Project.find.byId(id);
            if(project == null) throw new Exception("Project not exist");

           JsonNode json = new ObjectMapper().valueToTree(project.programs);

            return GlobalResult.okResult(json);
        }catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public  Result uploadProgramToHomer_Immediately(){
        try {
            System.out.println("Program nahrávám okamžitě");
            JsonNode json = request().body().asJson();

            Homer homer = Homer.find.byId(json.get("homerId").asText());
            if (homer == null) throw new Exception("Homer with this ID not exist");

            HomerProgram program = HomerProgram.find.byId(json.get("programId").asText());
            if (program == null) throw new Exception("Program with this ID not exist");

            Project project = homer.project;

            if (!project.projectId.equals(program.project.projectId)) throw new Exception("Program is not from the same project!");
            if(!SocketCollector.isConnected(homer.homerId)) throw new Exception("Homer is not connected");

            homer.sendProgramToHomer(program, null, null);

            program.successfullyUploaded.add(homer);
            program.update();

            return GlobalResult.okResult("Program was uploud To Homer succesfuly and started");
        }catch (Exception e){
            return GlobalResult.badRequestResult(e, "homerId - String", "programId - String");
        }
    }

    public  Result uploadProgramToHomer_AsSoonAsPossible(){
        try {
            System.out.println("Program nahrávám AsSoonAsPossible");
            JsonNode json = request().body().asJson();

            Homer homer = Homer.find.byId(json.get("homerId").asText());
            if (homer == null) throw new Exception("Homer with this ID not exist");

            HomerProgram program = HomerProgram.find.byId(json.get("projectId").asText());
            if (program == null) throw new Exception("Program with this ID not exist");

            Project project = homer.project;

            if (!project.projectId.equals(program.project.projectId)) throw new Exception("Program is not from the same project!");


            //1 Pokud je zařízení přopojené, nahraji okamžitě
            Date until = UtilTools.returnDateFromMillis( json.get("until").asText());
            if(SocketCollector.isConnected(homer.homerId)) homer.sendProgramToHomer(program, null, until);

            //2 Pokud není, vytvářím meziobjekt - Mezi Holder
            ForUploadProgram forUploadProgram = new ForUploadProgram();
            forUploadProgram.homer = homer;
            forUploadProgram.program = program;
            forUploadProgram.untilDate =  UtilTools.returnDateFromMillis( json.get("until").asText());
            forUploadProgram.save();

            return GlobalResult.okResult("Homer " + homer.homerId + " is not online. When Homer logs to Cloud Server, Program " + program.programId + " will be upload");
        }catch (Exception e){
            e.printStackTrace();
            return GlobalResult.badRequestResult(e);
        }
    }
    public  Result uploadProgramToHomer_GivenTimeAsSoonAsPossible(){
        try {
            System.out.println("Program nahrávám GivenTime");
            JsonNode json = request().body().asJson();

            Homer homer = Homer.find.byId(json.get("homerId").asText());
            if (homer == null) throw new Exception("Homer with this ID not exist");

            HomerProgram program = HomerProgram.find.byId(json.get("projectId").asText());
            if (program == null) throw new Exception("Program with this ID not exist");

            Project project = homer.project;

            if (!project.projectId.equals(program.project.projectId)) throw new Exception("Program is not from the same project!");

            Date when = UtilTools.returnDateFromMillis( json.get("when").asText());
            Date until = UtilTools.returnDateFromMillis( json.get("until").asText());

            //1 Pokud je zařízení přopojené, nahraji okamžitě
            if(SocketCollector.isConnected(homer.homerId)) homer.sendProgramToHomer(program, when, until);

            //2 Pokud ne -
            ForUploadProgram forUploadProgram = new ForUploadProgram();
            forUploadProgram.homer = homer;
            forUploadProgram.program = program;
            forUploadProgram.untilDate =  until;
            forUploadProgram.whenDate =  when;
            forUploadProgram.save();

            return GlobalResult.okResult("Program was uploud succesfuly");
        }catch (Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public  Result uploadProgramToHomer(){
        try {
            JsonNode json = request().body().asJson();

            if( UtilTools.returnIntFromString( json.get("when").asText() ) < 0 ) return uploadProgramToHomer_Immediately();
            if( UtilTools.returnIntFromString( json.get("when").asText() ) == 0 ) return uploadProgramToHomer_AsSoonAsPossible();
            else return uploadProgramToHomer_GivenTimeAsSoonAsPossible();

        }catch (Exception e){
            return GlobalResult.badRequestResult(e, "when");
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


    public Result newBlock(){
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

        ObjectNode result = Json.newObject();
        result.put("id", blockoBlock.id);

        return GlobalResult.okResult( result );
    }

    public Result getBlock(String url){
        try {
            String[] parts = url.split("/");

            if (parts.length > 2)
                return GlobalResult.badRequestResult(new Exception("URL " + url + " contains more substrings than one"));

            if (parts.length > 1) {

                BlockoContentBlock block = BlockoContentBlock.find.where().in("blockoBlock.id", parts[0]).where().in("version", Double.valueOf(parts[1])).findUnique();
                if (block == null) return GlobalResult.badRequestResult(new Exception("This version doesn't exist"));

                BlockoBlock blockoBlock = BlockoBlock.find.byId(parts[0]);
                blockoBlock.setVersion(Double.parseDouble(parts[1]));

                return GlobalResult.ok(Json.toJson(blockoBlock));

            } else {

                BlockoBlock blockoBlock = BlockoBlock.find.byId(parts[0]);
                if (blockoBlock == null)
                    return GlobalResult.badRequestResult(new Exception("This block doesn't exist"));
                return GlobalResult.ok(Json.toJson(blockoBlock));
            }
        } catch(Exception e){
            return GlobalResult.badRequestResult(e);
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
            return GlobalResult.badRequestResult(new Exception("URL " + url + " contains more substrings than one"));


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
                    return GlobalResult.badRequestResult(new Exception("A new version " + newVersion + " should have a greater number than the previous " + previousVersion));
                contentBlock.version = newVersion;
            } else contentBlock.version = block.version + 0.01;

            contentBlock.blockoBlock = blockoBlock;

            contentBlock.save();
            blockoBlock.contentBlocks.add(contentBlock);
            blockoBlock.update();

            return GlobalResult.okResult();
        } catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result designJson(String url){
        try {

            String[] parts = url.split("/");

            if (parts.length > 2)
                return GlobalResult.badRequestResult(new Exception("URL " + url + " contains more substrings than one"));


            if (parts.length > 1) {
                BlockoContentBlock block = BlockoContentBlock.find
                        .where().in("blockoBlock.id", parts[0])
                        .eq("version", Double.valueOf(parts[1]))
                        .findUnique();
                return GlobalResult.ok(Json.toJson(block.designJson));

            } else {
                BlockoContentBlock block = BlockoContentBlock.find
                        .where().in("blockoBlock.id", parts[0])
                        .orderBy("dateOfCreate").where()
                        .setMaxRows(1)
                        .findUnique();
                return GlobalResult.ok(Json.toJson(block.designJson));
            }

        } catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result logicJson(String url){
        try {

            String[] parts = url.split("/");

            if (parts.length > 2)
                return GlobalResult.badRequestResult(new Exception("URL " + url + " contains more substrings than one"));


            if (parts.length > 1) {
                BlockoContentBlock block = BlockoContentBlock.find
                        .where().in("blockoBlock.id", parts[0])
                        .eq("version", Double.valueOf(parts[1]))
                        .findUnique();
                return GlobalResult.ok(Json.toJson(block.logicJson));

            } else {
                BlockoContentBlock block = BlockoContentBlock.find
                        .where().in("blockoBlock.id", parts[0])
                        .orderBy("dateOfCreate").where()
                        .setMaxRows(1)
                        .findUnique();
                return GlobalResult.ok(Json.toJson(block.logicJson));
            }

        } catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result allPrevVersions(String id){
        try {
            BlockoBlock blockoBlock = BlockoBlock.find.byId(id);
            if (blockoBlock == null) return GlobalResult.badRequestResult(new Exception("Block with this Id not exist"));
            return GlobalResult.ok(Json.toJson(blockoBlock.contentBlocks));
        } catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result generalDescription(String id) {
        try {
            BlockoBlock block = BlockoBlock.find.byId(id);
            if (block == null) return GlobalResult.badRequestResult(new Exception("This block doesn't exist"));
            return GlobalResult.ok(Json.toJson(block.generalDescription));
        } catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result versionDescription(String id) {
        try {
            BlockoContentBlock block = BlockoContentBlock.find.byId(id);
            if (block == null) return GlobalResult.badRequestResult(new Exception("This block doesn't exist"));
            return GlobalResult.ok(Json.toJson(block.versionDescription));
        } catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }


}
