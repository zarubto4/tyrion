package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.blocko.*;
import models.compiler.Version_Object;
import models.persons.Person;
import models.project.b_program.B_Program;
import models.project.b_program.B_Program_Cloud;
import models.project.global.Homer;
import models.project.global.Project;
import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.a_main_utils.UtilTools;
import utilities.loginEntities.Secured;
import utilities.response.GlobalResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Security.Authenticated(Secured.class)
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
            return GlobalResult.nullPointerResult(e, "projectName - String", "projectDescription - TEXT");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - postNewProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public  Result getProjectsByUserAccount(){
        try {

            List<Project> projects = SecurityController.getPerson().owningProjects;

            return GlobalResult.okResult(Json.toJson( projects ));

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



            if (    Project.find.where().eq("ownersOfProject.mail", SecurityController.getPerson().mail).eq("projectId", id).findUnique() == null ) return GlobalResult.forbidden_Global();

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

            if (!project.ownersOfProject.contains( SecurityController.getPerson() ) ) return GlobalResult.forbidden_Global();

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

            if (!project.ownersOfProject.contains( SecurityController.getPerson() ) ) return GlobalResult.forbidden_Global();

            project.projectName = json.get("projectName").asText();
            project.projectDescription = json.get("projectDescription").asText();
            project.update();

            return GlobalResult.okResult(Json.toJson(project));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "projectName - String", "projectDescription - TEXT");
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


            List<Person> persons = new ArrayList<>();

            // NEJDŘÍVE KONTROLA VŠECH UŽIVATELŮ ZDA EXISTUJÍ
            for (final JsonNode objNode : json.get("persons")) {

                Person person = Person.find.byId(objNode.asText());
                if(person == null) return GlobalResult.nullPointerResult("User " + objNode.asText() + " not exist");
                persons.add(person);
            }

            // POTÉ PŘIDÁVÁNÍ DO projektu
            for (Person person : persons) {
                if (!person.owningProjects.contains(project)) {
                    project.ownersOfProject.add(person);
                    person.owningProjects.add(project);
                    person.update();
                }
            }

            project.update();

            return GlobalResult.okResult(Json.toJson(project));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "persons - [ids]");
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


            List<Person> persons = new ArrayList<>();

            // NEJDŘÍVE KONTROLA VŠECH UŽIVATELŮ ZDA EXISTUJÍ
            for (final JsonNode objNode : json.get("persons")) {

                Person person = Person.find.byId(objNode.asText());

                if(person == null) return GlobalResult.nullPointerResult("User " + objNode.asText() + " not exist");

                persons.add(person);
            }

            // POTÉ PŘIDÁVÁNÍ DO projektu
            for (Person person : persons) {
                if (person.owningProjects.contains(project)) {
                    project.ownersOfProject.remove(person);
                    person.owningProjects.remove(project);
                    person.update();
                }
            }

            project.update();

            return GlobalResult.okResult(Json.toJson(project));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "persons - [ids]");
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
            return GlobalResult.nullPointerResult(e, "homerId - String", "typeOfDevice - String");
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

            List<Homer> intersection = new ArrayList<>();

            for( Homer homer : project.homerList){
                if(WebSocketController_Incoming.isConnected(homer)) intersection.add(homer);
            }

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
            return GlobalResult.nullPointerResult(e, "homerId - String", "projectId - String");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - connectHomerWithProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public  Result disconnectHomerWithProject(){
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
            return GlobalResult.nullPointerResult(e, "homerId - String", "projectId - String");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - unConnectHomerWithProject ERROR");
            return GlobalResult.internalServerError();
        }
    }

/// ###################################################################################################################*/

    @BodyParser.Of(BodyParser.Json.class)
    public  Result postNewBProgram(){
        try{
            JsonNode json = request().body().asJson();

            // Ověřím program
            Project project = Project.find.byId(json.get("projectId").asText());
            if (project == null) return GlobalResult.notFoundObject();


            // Tvorba programu
            B_Program b_program             = new B_Program();
            b_program.azurePackageLink      = "personal-program";
            b_program.dateOfCreate          = new Date();
            b_program.programDescription    = json.get("programDescription").asText();
            b_program.programName           = json.get("programName").asText();
            b_program.project               = project;
            b_program.setUniqueAzureStorageLink();


            b_program.save();

            return GlobalResult.okResult(Json.toJson(b_program));
        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult("Some value in Json missing");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - postNewBProgram ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public  Result getProgram(String id){
        try{

            B_Program program = B_Program.find.byId(id);
            if (program == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(program));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public  Result getProgramInString(String version_id){
        try{
            Version_Object versionObject  = Version_Object.find.byId(version_id);
            if (versionObject == null) return GlobalResult.notFoundObject();

            String text = versionObject.files.get(0).get_fileRecord_from_Azure_inString();

            return GlobalResult.okResult( Json.parse(text) );
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getProgramInJson ERROR");
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

            B_Program program  = B_Program.find.byId(id);
            if (program == null) return GlobalResult.notFoundObject();

            program.programDescription = json.get("programDescription").asText();
            program.programName = json.get("programName").asText();

            program.update();
            return GlobalResult.okResult(Json.toJson(program));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "programName - String", "programDescription - TEXT");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - editProgram ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public  Result update_b_program(String id){
        try{
            JsonNode json = request().body().asJson();

            // Program který budu ukládat do data Storage v Azure
            String file_content =  json.get("program").toString();

            // Ověřím program
            B_Program b_program = B_Program.find.byId(id);
            if (b_program == null) return GlobalResult.notFoundObject();

            // První nová Verze
            Version_Object versionObjectObject      = new Version_Object();
            versionObjectObject.version_name = json.get("version_name").asText();
            versionObjectObject.versionDescription  = json.get("version_description").asText();

            if(b_program.versionObjects.isEmpty() ) versionObjectObject.azureLinkVersion = 1;
            else versionObjectObject.azureLinkVersion    = ++b_program.versionObjects.get(0).azureLinkVersion; // Zvednu verzi o jednu

             versionObjectObject.dateOfCreate        = new Date();
            versionObjectObject.b_program           = b_program;
            versionObjectObject.save();

            b_program.versionObjects.add(versionObjectObject);
            b_program.update();

            // Nahraje do Azure a připojí do verze soubor (lze dělat i cyklem - ale název souboru musí být vždy jiný)
            UtilTools.uploadAzure_Version("b-program", file_content, "b-program-file", b_program.azureStorageLink, b_program.azurePackageLink, versionObjectObject);

            return GlobalResult.okResult(Json.toJson(b_program));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "programName - String", "programDescription - TEXT");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - editProgram ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public  Result remove_b_Program(String id){
        try{

            B_Program program  = B_Program.find.byId(id);
            if (program == null) return GlobalResult.notFoundObject();


            program.delete();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - removeProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public  Result getAll_b_Programs(String id){
        try {

            Project project = Project.find.byId(id);
            if(project == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson( project.b_programs));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - removeProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public  Result getAll_c_Programs(String id){
        try {

            Project project = Project.find.byId(id);
            if(project == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson( project.c_programs));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - removeProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public  Result getAll_m_Projects(String id){
        try {

            Project project = Project.find.byId(id);
            if(project == null) return GlobalResult.notFoundObject();


            return GlobalResult.okResult(Json.toJson( project.m_projects));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - removeProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public  Result uploadProgramToHomer_Immediately(String homerId, String version_id){
        try {
            JsonNode json = request().body().asJson();

            Homer homer = Homer.find.byId(json.get("homerId").asText());
            if (homer == null) return GlobalResult.notFoundObject();

            Version_Object versionObject  = Version_Object.find.byId(version_id);
            if (versionObject == null) return GlobalResult.notFoundObject();


           String b_program_in_String = versionObject.files.get(0).get_fileRecord_from_Azure_inString();
           // TODO nahrát tento string na homera - inspirace nahrátí do cloudu

            return GlobalResult.okResult("Program was upload To Homer succesfuly and started");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - uploadProgramToHomer_Immediately ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public  Result uploadProgramToCloud(String program_id, String version_id){
        try {

            // B program, který chci nahrát do Cloudu na Blocko server
            B_Program b_program = B_Program.find.byId(program_id);
            if (b_program == null) return GlobalResult.notFoundObject();

            // Verze B programu kterou budu nahrávat do cloudu
            Version_Object version_object = Version_Object.find.byId(version_id);
            if (version_object == null) return GlobalResult.notFoundObject();

            // Pokud už nějaká instance běžela, tak jí zabiju a z databáze odstraním vazbu na běžící instanci b programu
            if( b_program.b_program_cloud != null ) {

               WebSocketController_OutComing.blockoServerKillInstance(b_program.b_program_cloud.blocko_server_name,  b_program.b_program_cloud.blocko_instance_name);

               B_Program_Cloud b_program_cloud = b_program.b_program_cloud;
               b_program_cloud.delete();
            }

            // Vytvářím nový záznam v databázi pro běžící instanci b programu na blocko serveru
            B_Program_Cloud program_cloud       = new B_Program_Cloud();
            program_cloud.b_program             = b_program;
            program_cloud.running_from          = new Date();
            program_cloud.version_object        = version_object;
            program_cloud.blocko_server_name    = Configuration.root().getString("Servers.blocko.server1.name");
            program_cloud.setUnique_blocko_instance_name();

            // if(WebSocketController_OutComing.servers.containsKey( Configuration.root().getString("Servers.blocko.server1.name")) && WebSocketController_OutComing.servers.get(Configuration.root().getString("Servers.blocko.server1.name") ).session.isOpen()) {

            // Vytvářím instanci na serveru
            WebSocketController_OutComing.blockoServerCreateInstance( program_cloud.blocko_server_name,  program_cloud.blocko_instance_name);

            // Nahrávám do instance program
            WebSocketController_OutComing.blockoServerUploadProgram(  program_cloud.blocko_server_name,  program_cloud.blocko_instance_name, program_cloud.version_object.files.get(0).get_fileRecord_from_Azure_inString());

            // Ukládám po úspěšné nastartvoání programu v cloudu jeho databázový ekvivalent
            program_cloud.save();

            return GlobalResult.okResult();
        } catch (NullPointerException a) {
            return GlobalResult.nullPointerResult("Server není nastartován");
         } catch (TimeoutException a) {
            return GlobalResult.nullPointerResult("Nepodařilo se včas nahrát na server");
         } catch (InterruptedException a) {
            return GlobalResult.nullPointerResult("Vlákno nahrávání bylo přerušeno ");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - removeProgram ERROR");
            return GlobalResult.internalServerError();
        }
    }

    //TODO
    public Result listOfUploadedHomers(String id) {
        //Na projectId B_Program vezmu všechny Houmry na kterých je program nahrán
        return GlobalResult.ok("Nutné dodělat - listOfUploadedHomers");
    }

    //TODO
    public Result listOfHomersWaitingForUpload(String id){
        //Na projectId B_Program vezmu všechny Houmry na které jsem program ještě nenahrál
        return GlobalResult.ok("Nutné dodělat - listOfHomersWaitingForUpload");
    }

///###################################################################################################################*/

    @BodyParser.Of(BodyParser.Json.class)
    public Result newTypeOfBlock(){
        try{
            JsonNode json = request().body().asJson();

            TypeOfBlock typeOfBlock = new TypeOfBlock();
            typeOfBlock.generalDescription  = json.get("generalDescription").asText();
            typeOfBlock.name                = json.get("name").asText();

            typeOfBlock.save();

            return GlobalResult.created( Json.toJson(typeOfBlock));
        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "name - String", "generalDescription - TEXT");
        } catch (Exception e) {
            Logger.error("Error", e);
            return GlobalResult.internalServerError();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result editTypeOfBlock(String id){
        try{
            JsonNode json = request().body().asJson();

            TypeOfBlock typeOfBlock = TypeOfBlock.find.byId(id);
            if(typeOfBlock == null) return GlobalResult.notFoundObject();

            typeOfBlock.generalDescription  = json.get("generalDescription").asText();
            typeOfBlock.name                = json.get("name").asText();

            typeOfBlock.update();

            return GlobalResult.update( Json.toJson(typeOfBlock));
        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "name - String", "generalDescription - TEXT");
        } catch (Exception e) {
            Logger.error("Error", e);
            return GlobalResult.internalServerError();
        }
    }

    public Result deleteTypeOfBlock(String id){
        try{

            TypeOfBlock typeOfBlock = TypeOfBlock.find.byId(id);
            if(typeOfBlock == null) return GlobalResult.notFoundObject();

            typeOfBlock.delete();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("Error", e);
            return GlobalResult.internalServerError();
        }
    }

    public Result getAllTypeOfBlocks(){
        try {
            List<TypeOfBlock> typeOfBlocks = TypeOfBlock.find.all();
            return GlobalResult.okResult(Json.toJson(typeOfBlocks));
        } catch (Exception e) {
            Logger.error("Error", e);
            return GlobalResult.internalServerError();
        }
    }
///###################################################################################################################*/

    @BodyParser.Of(BodyParser.Json.class)
    public Result newBlock(){
       try{
            JsonNode json = request().body().asJson();


            BlockoBlock blockoBlock = new BlockoBlock();
            blockoBlock.generalDescription  = json.get("generalDescription").asText();
            blockoBlock.name                = json.get("name").asText();
            blockoBlock.author              = SecurityController.getPerson();


           TypeOfBlock typeOfBlock = TypeOfBlock.find.byId( json.get("typeOfBlockId").asText());
           if(typeOfBlock == null) return GlobalResult.notFoundObject();

           blockoBlock.typeOfBlock = typeOfBlock;
           blockoBlock.save();


            return GlobalResult.okResult( Json.toJson(blockoBlock) );
       } catch (NullPointerException e) {
           return GlobalResult.nullPointerResult(e, "name - String", "generalDescription - TEXT", "typeOfBlockId - String");
       } catch (Exception e) {
           Logger.error("Error", e);
           Logger.error("ProgramingPackageController - newBlock ERROR");
           return GlobalResult.internalServerError();
       }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result editBlock(String id){
        try {
            JsonNode json = request().body().asJson();

                BlockoBlock blockoBlock = BlockoBlock.find.byId(id);
                if (blockoBlock == null) return GlobalResult.notFoundObject();

                blockoBlock.generalDescription  = json.get("generalDescription").asText();
                blockoBlock.name                = json.get("name").asText();

                TypeOfBlock typeOfBlock = TypeOfBlock.find.byId( json.get("typeOfBlockId").asText());
                if(typeOfBlock == null) return GlobalResult.notFoundObject();

                blockoBlock.typeOfBlock = typeOfBlock;

                blockoBlock.update();

                return GlobalResult.okResult(Json.toJson(blockoBlock));
        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "name - String", "versionDescription - TEXT", "typeOfBlockId - String");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getBlockLast ERROR");
            return GlobalResult.internalServerError();
        }

    }

    public Result getBlockVersions(String id){
        try {
                BlockoBlock blockoBlock = BlockoBlock.find.byId(id);
                if(blockoBlock == null) return GlobalResult.notFoundObject();

                return GlobalResult.okResult(Json.toJson(blockoBlock.contentBlocks));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getBlockVersion ERROR");
            return GlobalResult.internalServerError();
        }

    }

    public Result getBlockBlock(String id){
        try {
            BlockoBlock blockoBlock = BlockoBlock.find.byId(id);
            if(blockoBlock == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(blockoBlock));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getBlockVersion ERROR");
            return GlobalResult.internalServerError();
        }

    }


    public Result getByCategory(){

        try {

            ObjectNode result = Json.newObject();
            List<TypeOfBlock> typeOfBlocks = TypeOfBlock.find.all();

            for (TypeOfBlock typeOfBlock : typeOfBlocks) {

                ObjectNode blocks = Json.newObject();

                blocks.set("typeOfBlock", Json.toJson(typeOfBlock));
                blocks.set("Blocks", Json.toJson(typeOfBlock.blockoBlocks));

                result.set(typeOfBlock.name, blocks);

            }

            return GlobalResult.okResult(result);

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - getBlockLast ERROR");
            return GlobalResult.internalServerError();
        }


    }

    public Result deleteBlock(String id){
        try {
            BlockoBlock blockoBlock = BlockoBlock.find.byId(id);
            if(blockoBlock == null) return GlobalResult.notFoundObject();
            blockoBlock.delete();

            return GlobalResult.okResult();
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - shareProjectWithUsers ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public Result deleteBlockVersion(String id){
        try {

            BlockoContentBlock blockoContentBlock = BlockoContentBlock.find.byId(id);
            if(blockoContentBlock == null) return GlobalResult.notFoundObject();

            blockoContentBlock.delete();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - shareProjectWithUsers ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result updateOfBlock(String id){
        try {
            JsonNode json = request().body().asJson();
            BlockoBlock blockoBlock = BlockoBlock.find.byId(id);

            BlockoContentBlock contentBlock = new BlockoContentBlock();
            contentBlock.dateOfCreate = new Date();

            contentBlock.versionName = json.findValue("version_name").asText();
            contentBlock.versionDescription = json.findValue("versionDescription").asText();
            contentBlock.designJson = json.findValue("designJson").toString();
            contentBlock.logicJson = json.findValue("logicJson").toString();
            contentBlock.blockoBlock = blockoBlock;
            contentBlock.save();

            //blockoBlock.contentBlocks.add(contentBlock);
            return GlobalResult.okResult(Json.toJson(blockoBlock));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "version_name - String", "versionDescription - TEXT", "designJson - TEXT", "logicJson - TEXT");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - shareProjectWithUsers ERROR");
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

///###################################################################################################################*/

}
