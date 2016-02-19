package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.project.global.Project;
import models.project.m_program.M_Program;
import models.project.m_program.M_Project;
import models.grid.Screen_Size_Type;
import play.Logger;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import utilities.loginEntities.Secured;
import utilities.response.GlobalResult;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Security.Authenticated(Secured.class)
public class GridController extends play.mvc.Controller {

    @BodyParser.Of(BodyParser.Json.class)
    public Result new_M_Program() {
        try{
            JsonNode json = request().body().asJson();

            Project project = Project.find.byId( json.get("project_id").asText() );
            if(project == null) return GlobalResult.notFoundObject();

            M_Project m_project = new M_Project();
            m_project.program_description = json.get("program_description").asText();
            m_project.program_name = json.get("program_name").asText();
            m_project.date_of_create = new Date();
            m_project.project = project;

            m_project.save();

            return GlobalResult.created( Json.toJson(m_project));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "project_id - String", "program_name - String", "program_description - TEXT");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - postNewProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result get_M_Program(String id){
        try {
            M_Project m_project = M_Project.find.byId(id);
            if (m_project == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(m_project));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - get_Screen_Size_Type ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_M_Program(String id){
        try{
            JsonNode json = request().body().asJson();

            M_Project m_project = M_Project.find.byId(id);
            if(m_project == null) return GlobalResult.notFoundObject();

            m_project.program_description = json.get("program_description").asText();
            m_project.program_name = json.get("program_name").asText();

            m_project.update();
            return GlobalResult.update( Json.toJson(m_project));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "project_id - String", "program_name - String", "program_description - TEXT");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - postNewProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result remove_M_Program(String id){
        try{
            M_Project m_project = M_Project.find.byId(id);
            if(m_project == null) return GlobalResult.notFoundObject();

            m_project.delete();

            return GlobalResult.okResult();

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "projectName - String", "projectDescription - TEXT");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - postNewProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result getAll_M_Program_fromProject(String id){
        try {
            Project project = Project.find.byId(id);
            if (project == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(project.m_projects));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - get_Screen_Size_Type ERROR");
            return GlobalResult.internalServerError();
        }
    }

//######################################################################################################################

    @BodyParser.Of(BodyParser.Json.class)
    public Result new_M_Program_Screen() {
        try {
            JsonNode json = request().body().asJson();

            M_Project m_project = M_Project.find.byId( json.get("m_program_id").asText() );
            if(m_project == null) return GlobalResult.notFoundObject();

            Screen_Size_Type screen_size_type = Screen_Size_Type.find.byId(json.get("screen_type_id").asText());
            if(screen_size_type == null) return GlobalResult.notFoundObject();

            M_Program m_program_ = new M_Program();
            m_program_.date_of_create      = new Date();
            m_program_.program_description = json.get("program_description").asText();
            m_program_.program_name        = json.get("program_name").asText();
            m_program_.m_project = m_project;
            m_program_.programInString     = json.get("m_code").asText();
            m_program_.screen_size_type    = screen_size_type;
            m_program_.height_lock         = json.get("height_lock").asBoolean();
            m_program_.width_lock          = json.get("width_lock").asBoolean();

            while(true){ // I need Unique Value
                m_program_.token  = UUID.randomUUID().toString();
                if (M_Program.find.where().eq("token", m_program_.token).findUnique() == null) break;
            }

            m_program_.save();

            return GlobalResult.created(Json.toJson(m_program_));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "m_program_id - String", "screen_type_id - String",
                     "program_description - String", "program_name - String", "m_code - String",
                    "height_lock - boolean", "width_lock - boolean");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - postNewProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }

    }

    public Result get_M_Program_Screen(String id) {
        try {
            M_Program m_program_ = M_Program.find.byId(id);
            if (m_program_ == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(m_program_));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - get_Screen_Size_Type ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_M_Program_Screen(String id){
        try {
            JsonNode json = request().body().asJson();

            Screen_Size_Type screen_size_type = Screen_Size_Type.find.byId(json.get("screen_type_id").asText());
            if(screen_size_type == null) return GlobalResult.notFoundObject();

            M_Program m_program_ = M_Program.find.byId(id);
            m_program_.program_description = json.get("program_description").asText();
            m_program_.program_name        = json.get("program_name").asText();
            m_program_.programInString     = json.get("m_code").asText();
            m_program_.screen_size_type    = screen_size_type;
            m_program_.height_lock         = json.get("height_lock").asBoolean();
            m_program_.width_lock          = json.get("width_lock").asBoolean();
            m_program_.last_update         = new Date();

            m_program_.update();

            return GlobalResult.created(Json.toJson(m_program_));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "screen_type_id - String",
                    "program_description - String", "program_name - String", "m_code - String",
                    "height_lock - boolean", "width_lock - boolean");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - postNewProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result remove_M_Program_Screen(String id){
        try {
            M_Program m_program_ = M_Program.find.byId(id);
            if (m_program_ == null) return GlobalResult.notFoundObject();

            m_program_.delete();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - get_Screen_Size_Type ERROR");
            return GlobalResult.internalServerError();
        }
    }

    public Result getAll_M_Program_Screen_fromM_Project(String id){
        try {
            Project project = Project.find.byId(id);
            if (project == null) return GlobalResult.notFoundObject();

            List<M_Program> list = M_Program.find.where().eq("m_project.project.projectId", id).findList();
            return GlobalResult.okResult(Json.toJson(list));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - get_Screen_Size_Type ERROR");
            return GlobalResult.internalServerError();
        }
    }

//######################################################################################################################

    @BodyParser.Of(BodyParser.Json.class)
    public Result new_Screen_Size_Type(){
        try {
            JsonNode json = request().body().asJson();

            Screen_Size_Type screen_size_type = new Screen_Size_Type();
            screen_size_type.name = json.get("name").asText();
            screen_size_type.height = json.get("height").asInt();
            screen_size_type.width = json.get("width").asInt();
            screen_size_type.height_lock = json.get("height_lock").asBoolean();
            screen_size_type.width_lock = json.get("width_lock").asBoolean();
            screen_size_type.touch_screen = json.get("touch_screen").asBoolean();

            if( json.has("project_id")) {
                Project project = Project.find.byId(json.get("project_id").asText());
                if (project == null) return GlobalResult.notFoundObject();

                screen_size_type.project = project;
            }

            screen_size_type.save();

            return GlobalResult.created(Json.toJson(screen_size_type));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "name - String", "height - Integer", "width - Integer", "height_lock - boolean", "width_lock - boolean", "touch_screen boolean");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - postNewProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    public Result get_Screen_Size_Type(String id){
        try {
            Screen_Size_Type screen_size_type = Screen_Size_Type.find.byId(id);
            if (screen_size_type == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(screen_size_type));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - get_Screen_Size_Type ERROR");
            return GlobalResult.internalServerError();
        }
    }


    public Result get_Screen_Size_Type_PublicList(){
        try {

            List<Screen_Size_Type> list = Screen_Size_Type.find.where().eq("project", null).findList();

            return GlobalResult.okResult(Json.toJson(list));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - get_Screen_Size_Type ERROR");
            return GlobalResult.internalServerError();
        }
    }


    public Result get_Screen_Size_Type_Combination(String id){
        try {

            List<Screen_Size_Type> public_list = Screen_Size_Type.find.where().eq("project", null).findList();
            List<Screen_Size_Type> private_list = Screen_Size_Type.find.where().eq("project.projectId", id).findList();


            ObjectNode result = Json.newObject();
            result.set("public", Json.toJson(public_list));
            result.set("private", Json.toJson(private_list));

            return GlobalResult.okResult(result);
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - get_Screen_Size_Type ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_Screen_Size_Type(String id){
        try {
            JsonNode json = request().body().asJson();

            Screen_Size_Type screen_size_type = Screen_Size_Type.find.byId(id);
            if(screen_size_type == null) return GlobalResult.notFoundObject();

            screen_size_type.name           = json.get("name").asText();
            screen_size_type.height         = json.get("height").asInt();
            screen_size_type.width          = json.get("width").asInt();
            screen_size_type.height_lock    = json.get("height_lock").asBoolean();
            screen_size_type.width_lock     = json.get("width_lock").asBoolean();
            screen_size_type.touch_screen   = json.get("touch_screen").asBoolean();


            if( json.has("project_id")) {
                Project project = Project.find.byId(json.get("project_id").asText());
                if (project == null) return GlobalResult.notFoundObject();

                screen_size_type.project = project;
            }

            screen_size_type.save();

            screen_size_type.update();
            return GlobalResult.update(Json.toJson(screen_size_type));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "name - String", "height - Integer", "width - Integer", "height_lock - boolean", "width_lock - boolean", "touch_screen boolean");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - postNewProject ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }


    public Result remove_Screen_Size_Type(String id){
        try {
            Screen_Size_Type screen_size_type = Screen_Size_Type.find.byId(id);
            if(screen_size_type == null) return GlobalResult.notFoundObject();

            screen_size_type.delete();

            return GlobalResult.okResult();
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - get_Screen_Size_Type ERROR");
            return GlobalResult.internalServerError();
        }
    }


}
