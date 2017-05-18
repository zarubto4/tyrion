package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.*;
import models.*;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.emails.Email;
import utilities.enums.Enum_Approval_state;
import utilities.enums.Enum_type_of_command;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.login_entities.Secured_API;
import utilities.login_entities.Secured_Admin;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Filter_List.Swagger_B_Program_List;
import utilities.swagger.outboundClass.Filter_List.Swagger_Blocko_Block_List;
import utilities.swagger.outboundClass.Filter_List.Swagger_Instance_List;
import utilities.swagger.outboundClass.Filter_List.Swagger_Type_Of_Block_List;
import utilities.swagger.outboundClass.Swagger_B_Program_Version;
import utilities.swagger.outboundClass.Swagger_BlockoBlock_Version_scheme;
import utilities.swagger.outboundClass.Swagger_Instance_Short_Detail;
import web_socket.message_objects.homer_instance.*;
import web_socket.message_objects.homerServer_with_tyrion.WS_Message_Destroy_instance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Security.Authenticated(Secured_API.class)
@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Blocko extends Controller{

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Blocko.class);
    
// B PROGRAM ###########################################################################################################

    @ApiOperation(value = "create new B_Program",
            tags = {"B_Program"},
            notes = "create new B_Program",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.update_permission", value = "true"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_B_Program_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_BProgram.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public  Result new_b_Program(String project_id){
        try{

            // Zpracování Json
            final Form<Swagger_B_Program_New> form = Form.form(Swagger_B_Program_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_B_Program_New help = form.get();

            // Kontrola objektu
            Model_Project project = Model_Project.find.byId(project_id);
            if (project == null) return GlobalResult.notFoundObject("Project project_id not found");

            // Kontrola oprávnění
            if (!project.update_permission() ) return GlobalResult.forbidden_Permission();

            // Tvorba programu
            Model_BProgram b_program        = new Model_BProgram();
            b_program.date_of_create        = new Date();
            b_program.description           = help.description;
            b_program.name                  = help.name;
            b_program.project               = project;

            // Kontrola oprávnění těsně před uložením
            if (!b_program.create_permission() ) return GlobalResult.forbidden_Permission();

            // Uložení objektu
            b_program.save();

            // Vrácení objektu
            return GlobalResult.created(Json.toJson(b_program));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get B Program",
            tags = {"B_Program"},
            notes = "get B_Program object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BProgram.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public  Result get_b_Program(@ApiParam(value = "b_program_id String path", required = true)  String b_program_id){
        try{

            // Kontrola objektu
            Model_BProgram b_program = Model_BProgram.find.byId(b_program_id);
            if (b_program == null) return GlobalResult.notFoundObject("B_Program id not found");

            // Kontrola oprávnění
            if (!b_program.read_permission() ) return GlobalResult.forbidden_Permission();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(b_program));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get B Program version",
            tags = {"B_Program"},
            notes = "get B_Program version object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "B_program.read_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_B_Program_Version.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public  Result get_b_Program_version(@ApiParam(value = "version_id String path", required = true)  String version_id){
        try{

            // Kontrola objektu
            Model_VersionObject version_object = Model_VersionObject.find.byId(version_id);
            if (version_object == null) return GlobalResult.notFoundObject("Version_Object version_id not found");

            // Kontrola oprávnění
            if (version_object.b_program == null) return GlobalResult.notFoundObject("Version_Object is not version of B_Program");

            // Kontrola oprávnění
            if (! version_object.b_program.read_permission() ) return GlobalResult.forbidden_Permission();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(version_object.b_program.program_version(version_object)));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit B_Program",
            tags = {"B_Program"},
            notes = "edit basic information in B_Program object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "B_program.edit_permission", value = "true"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_B_Program_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BProgram.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public  Result edit_b_Program(@ApiParam(value = "b_program_id String path", required = true)  String b_program_id){
        try{

            // Zpracování Json
            final Form<Swagger_B_Program_New> form = Form.form(Swagger_B_Program_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_B_Program_New help = form.get();

            // Kontrola objektu
            Model_BProgram b_program = Model_BProgram.find.byId(b_program_id);
            if (b_program == null) return GlobalResult.notFoundObject("B_Program id not found");

            // Kontrola oprávěnní
            if (!b_program.edit_permission()) return GlobalResult.forbidden_Permission();

            // Úprava objektu
            b_program.description = help.description;
            b_program.name        = help.name;

            // Uložení objektu
            b_program.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(b_program));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "create new Version of B Program",
            tags = {"B_Program"},
            notes = "edit Blocko program / new Version in B_Program object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "B_program.update_permission", value = "true"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_B_Program_Version_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_B_Program_Version.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_JsonValueMissing.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public  Result update_b_program_new_version(@ApiParam(value = "b_program_id String path", required = true)  String b_program_id){
        try{

            // Zpracování Json
            final Form<Swagger_B_Program_Version_New> form = Form.form(Swagger_B_Program_Version_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_B_Program_Version_New help = form.get();

            // Program který budu ukládat do data Storage v Azure
            String file_content =  help.program;

            // Ověření programu
            Model_BProgram b_program = Model_BProgram.find.byId(b_program_id);
            if (b_program == null) return GlobalResult.notFoundObject("B_Program id not found");

            // Kontrola oprávnění
            if (! b_program.update_permission() ) return GlobalResult.forbidden_Permission();

            // První nová Verze
            Model_VersionObject version_object     = new Model_VersionObject();
            version_object.version_name            = help.version_name;
            version_object.version_description     = help.version_description;
            version_object.date_of_create          = new Date();
            version_object.b_program               = b_program;
            version_object.author                  = Controller_Security.get_person();

            if(help.m_project_snapshots != null)
                for(Swagger_B_Program_Version_New.M_Project_SnapShot help_m_project_snap : help.m_project_snapshots){

                    Model_MProject m_project = Model_MProject.find.byId(help_m_project_snap.m_project_id);
                    if(m_project == null) return GlobalResult.notFoundObject("M_Project not found");
                    if(!m_project.update_permission()) return GlobalResult.forbidden_Permission();

                    Model_MProjectProgramSnapShot snap = new Model_MProjectProgramSnapShot();
                    snap.m_project = m_project;

                    for(Swagger_B_Program_Version_New.M_Program_SnapShot help_m_program_snap : help_m_project_snap.m_program_snapshots){
                        Model_VersionObject m_program_version = Model_VersionObject.find.where().eq("id", help_m_program_snap.version_object_id ).eq("m_program.id", help_m_program_snap.m_program_id).eq("m_program.m_project.id", m_project.id).findUnique();
                        if(m_program_version == null) return GlobalResult.notFoundObject("M_Program Version id not found");
                        snap.version_objects_program.add(m_program_version);
                    }

                    version_object.b_program_version_snapshots.add(snap);
                }

            // Definování main Board
            for( Swagger_B_Program_Version_New.Hardware_group group : help.hardware_group) {

                Model_BProgramHwGroup b_program_hw_group = new Model_BProgramHwGroup();

                // Definuji Main Board - Tedy yodu pokud v Json přišel (není podmínkou)
                if(group.main_board_pair != null) {

                    Model_BPair b_pair = new Model_BPair();

                    b_pair.board = Model_Board.get_byId(group.main_board_pair.board_id);
                    if ( b_pair.board == null) return GlobalResult.notFoundObject("Board board_id not found");
                    if (!b_pair.board.type_of_board.connectible_to_internet)  return GlobalResult.result_BadRequest("Main Board must be internet connectible!");
                    if(!b_pair.board.update_permission()) return GlobalResult.forbidden_Permission();

                    b_pair.c_program_version = Model_VersionObject.find.byId(group.main_board_pair.c_program_version_id);
                    if ( b_pair.c_program_version == null) return GlobalResult.notFoundObject("C_Program Version_Object c_program_version_id not found");
                    if ( b_pair.c_program_version.c_program == null)  return GlobalResult.result_BadRequest("Version is not from C_Program");


                    if( Model_TypeOfBoard.find.where().eq("c_programs.id",  b_pair.c_program_version.c_program.id ).where().eq("boards.id",  b_pair.board.id).findRowCount() < 1){
                        return GlobalResult.result_BadRequest("You want upload C++ program version id: " +  b_pair.c_program_version.id + " thats not compatible with hardware " + b_pair.board.id);
                    }

                    b_program_hw_group.main_board_pair = b_pair;

                }else {
                    return GlobalResult.result_BadRequest("Hardware Group hasn't Main Board!");
                }

                // Definuji Devices - Tedy yodu pokud v Json přišly (není podmínkou)

                if(group.device_board_pairs != null && !group.device_board_pairs.isEmpty() ) {

                    for(Swagger_Board_CProgram_Pair connected_board : group.device_board_pairs ){

                        Model_BPair b_pair = new Model_BPair();

                        b_pair.board = Model_Board.find.byId(connected_board.board_id);
                        if ( b_pair.board == null) return GlobalResult.notFoundObject("Board board_id not found");
                        if(!b_pair.board.update_permission()) return GlobalResult.forbidden_Permission();


                        b_pair.c_program_version = Model_VersionObject.find.byId(connected_board.c_program_version_id);
                        if ( b_pair.c_program_version == null) return GlobalResult.notFoundObject("C_Program Version_Object c_program_version_id not found");
                        if ( b_pair.c_program_version.c_program == null)  return GlobalResult.result_BadRequest("Version is not from C_Program");

                        if( Model_TypeOfBoard.find.where().eq("c_programs.id",  b_pair.c_program_version.c_program.id ).where().eq("boards.id",  b_pair.board.id).findRowCount() < 1){
                            return GlobalResult.result_BadRequest("You want upload C++ program version id: " +  b_pair.c_program_version.id + " thats not compatible with hardware " + b_pair.board.id);
                        }

                        b_program_hw_group.device_board_pairs.add(b_pair);
                    }
                }
                version_object.b_program_hw_groups.add(b_program_hw_group);
            }


            /**
             logger.debug("update_b_program_new_version:: Saving Snap");
             for(Model_MProjectProgramSnapShot snap : version_object.b_program_version_snapshots){
             snap.save();
             }

             logger.debug("update_b_program_new_version:: Saving b_program_hw_group");
             for(Model_BProgramHwGroup b_program_hw_group : version_object.b_program_hw_groups){
             b_program_hw_group.save();
             }
             */


            terminal_logger.debug("update_b_program_new_version:: Saving version");
            // Uložení objektu
            version_object.save();


            // Úprava objektu
            b_program.getVersion_objects().add(version_object);

            terminal_logger.debug("update_b_program_new_version:: Updating b_program");
            // Uložení objektu
            b_program.update();

            // Update verze
            version_object.refresh();

            // Nahrání na Azure
            Model_FileRecord.uploadAzure_Version(file_content, "program.js", b_program.get_path() , version_object);

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson( version_object.b_program.program_version(version_object) ));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove B Program",
            tags = {"B_Program"},
            notes = "remove B_Program object",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "B_program.delete_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public  Result remove_b_Program(@ApiParam(value = "b_program_id String path", required = true)  String b_program_id){
        try{

            // Kontrola objektu
            Model_BProgram program = Model_BProgram.find.byId(b_program_id);
            if (program == null) return GlobalResult.notFoundObject("B_Program id not found");

            // Kontrola oprávění
            if (! program.delete_permission() ) return GlobalResult.forbidden_Permission();

            // Smazání objektu
            program.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "remove B Program version",
            tags = {"B_Program"},
            notes = "remove B_Program version object",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "B_program.delete_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public  Result remove_b_Program_version(@ApiParam(value = "version_id String path", required = true) String version_id){
        try{

            // Získání objektu
            Model_VersionObject version_object  = Model_VersionObject.find.byId(version_id);

            // Kontrola objektu
            if (version_object == null) return GlobalResult.notFoundObject("Version_Object id not found");
            if (version_object.b_program == null) return GlobalResult.result_BadRequest("B_Program not found");

            // Kontrola oprávnění
            if (! version_object.b_program.delete_permission() ) return GlobalResult.forbidden_Permission();

            // Smazání objektu
            version_object.removed_by_user = true;
            version_object.update();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get B_Program by Filter",
            tags = {"B_Program"},
            notes = "get B_Program List",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "B_Program_read_permission", value = "No need to check permission, because Tyrion returns only those results which user owns"),
                    }),
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_B_Program_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",             response = Swagger_B_Program_List.class),
            @ApiResponse(code = 400, message = "Invalid body",          response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",  response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",     response = Result_InternalServerError.class)
    })
    public Result get_b_Program_by_Filter(@ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) int page_number){
        try {

            // Získání JSON
            final Form<Swagger_B_Program_Filter> form = Form.form(Swagger_B_Program_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_B_Program_Filter help = form.get();

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_BProgram> query = Ebean.find(Model_BProgram.class);
            query.where().eq("project.participants.person.id", Controller_Security.get_person_id());

            // Pokud JSON obsahuje project_id filtruji podle projektu
            if(help.project_id != null){

                query.where().eq("project.id", help.project_id);
            }

            // Vytvoření odchozího JSON
            Swagger_B_Program_List result = new Swagger_B_Program_List(query, page_number);

            // Vrácení výsledku
            return GlobalResult.result_ok(Json.toJson(result));

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "upload B Program (version) to cloud",
            tags = {"B_Program"},
            notes = "upload version of B Program to cloud. Its possible have only one version from B program in cloud. If you uploud new one - old one will be replaced",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "B_program.update_permission", value = "true"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_B_Program_Upload_Instance",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully uploaded",     response = Result_ok.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_JsonValueMissing.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public  Result upload_b_Program_ToCloud(@ApiParam(value = "version_id String path", required = true) String version_id){
        try {

            // Získání JSON
            final Form<Swagger_B_Program_Upload_Instance> form = Form.form(Swagger_B_Program_Upload_Instance.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_B_Program_Upload_Instance help = form.get();

            // Kontrola objektu: Verze B programu kterou budu nahrávat do cloudu
            Model_VersionObject version_object = Model_VersionObject.find.byId(version_id);
            if (version_object == null) return GlobalResult.notFoundObject("Version_Object version_id not found");

            // Kontrola objektu: B program, který chci nahrát do Cloudu na Blocko cloud_blocko_server
            if (version_object.b_program == null) return GlobalResult.result_BadRequest("Version_Object is not version of B_Program");
            Model_BProgram b_program = version_object.b_program;

            // Kontrola oprávnění
            if (! b_program.update_permission() ) return GlobalResult.forbidden_Permission();

            Model_HomerInstanceRecord record = new Model_HomerInstanceRecord();
            record.main_instance_history = b_program.instance;
            record.version_object = version_object;
            record.date_of_created = new Date();

            if(help.upload_time != null) {

                // Zkontroluji smysluplnost časvé známky
                if (!help.upload_time.after(new Date()))  return GlobalResult.result_BadRequest("time must be set in the future");
                record.planed_when = help.upload_time;

            } else{
                Date date_from = new Date();
                record.running_from = date_from;
                record.planed_when = date_from;
            }
            record.save();

            Long minutes = new Long("60000");
            Long one_month = new Date().getTime() + minutes;
            Date created = new Date(one_month);

            // If immidietly
            if(record.planed_when.getTime() < created.getTime()){

                terminal_logger.debug("upload_b_Program_ToCloud:: Set the instants immediately");
                Model_HomerInstance.upload_Record_immediately(record);

            }else {
                terminal_logger.debug("upload_b_Program_ToCloud:: Set the instants by Time scheduler (not now) ");
            }


            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "shutDown Instance by Instnace Id",
            tags = {"Instance"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully removed",      response = Result_ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result instance_shut_down(String instance_name){
        try{

            // Kontrola objektu
            Model_HomerInstance homer_instance = Model_HomerInstance.find.where().eq("blocko_instance_name", instance_name).findUnique();
            if (homer_instance == null) return GlobalResult.notFoundObject("Homer_Instance id not found");

            if (!homer_instance.getB_program().update_permission() ) return GlobalResult.forbidden_Permission();

            if(homer_instance.actual_instance == null){
                return GlobalResult.result_BadRequest("Instance not running");
            }

            WS_Message_Destroy_instance result = homer_instance.remove_instance_from_server();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Instance by Project ID",
            tags = {"Instance"},
            notes = "get list of instances details under project id",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "B_program.update_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully uploaded",     response = Swagger_Instance_Short_Detail.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result get_b_program_instance_under_project(String project_id){
        try{


            List<Model_HomerInstance> instances = Model_HomerInstance.find.where()
                    .isNotNull("actual_instance")
                    .eq("b_program.project.id", project_id)
                    .findList();

            List<Swagger_Instance_Short_Detail> list = new ArrayList<>();

            for(Model_HomerInstance instance : instances){
                list.add(instance.get_instance_short_detail());
            }

            return GlobalResult.result_ok(Json.toJson(list));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Instance by instance_id",
            tags = {"Instance"},
            notes = "get unique instance under Blocko program (now its 1:1) we are not supporting multi-instance schema yet",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "B_program.update_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully uploaded",     response = Model_HomerInstance.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result get_b_program_instance(String instance_id){
        try{

            Model_HomerInstance instance = Model_HomerInstance.find.byId(instance_id);
            if (instance == null) return GlobalResult.notFoundObject("Homer_Instance instance_id not found");
            if(instance.getB_program() == null ) return GlobalResult.notFoundObject("Homer_Instance is virtual!!");

            if(!instance.getB_program().read_permission()) return GlobalResult.forbidden_Permission();

            return GlobalResult.result_ok(Json.toJson(instance));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Instance with filter parameters",
            tags = { "Instance"},
            notes = "Get List of Instances. Acording by permission - system return only Instance from project, where is user owner or" +
                    " all Instances if user have static Permission key",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Instance_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Instance_List.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result get_b_program_instance_by_filter(){
        try{

            // Zpracování Json
            final Form<Swagger_Instance_Filter> form = Form.form(Swagger_Instance_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Instance_Filter help = form.get();

            // Tvorba parametru dotazu
            Query<Model_HomerInstance> query = Ebean.find(Model_HomerInstance.class);

            // If Json contains TypeOfBoards list of id's
            if(!help.instance_types.isEmpty() ){
                query.where().in("instance_type", help.instance_types);
            }

            if(help.project_id != null ){
                query.where().eq("project.id", help.project_id);
            }


            if(!help.server_unique_names.isEmpty()){
                query.where().in("cloud_homer_server.unique_identificator", help.server_unique_names);
            }

            // Vytvářím seznam podle stránky
            Swagger_Instance_List result = new Swagger_Instance_List(query, help.page_number);

            // Vracím seznam
            return GlobalResult.result_ok(Json.toJson(result));

        } catch (Exception e) {

            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Instance Record by instance_record_id",
            tags = {"Instance"},
            notes = "get unique instance under Blocko program (now its 1:1) we are not supporting multi-instance schema yet",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_HomerInstanceRecord.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result get_b_program_instance_record(String instance_record_id){
        try{


            Model_HomerInstanceRecord instance = Model_HomerInstanceRecord.find.byId(instance_record_id);
            if (instance == null) return GlobalResult.notFoundObject("Homer_Instance instance_id not found");

            if(!instance.main_instance_history.getB_program().read_permission()) return GlobalResult.forbidden_Permission();

            return GlobalResult.result_ok(Json.toJson(instance));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion Front End",  hidden = true)
    public Result ping_instance(String instance_name){
        try{
            // Kontrola objektu
            Model_HomerInstance homer_instance = Model_HomerInstance.find.where().eq("blocko_instance_name",instance_name).findUnique();
            if (homer_instance == null) return GlobalResult.notFoundObject("Homer_Instance id not found");

            if(!homer_instance.instance_online()) return GlobalResult.notFoundObject("Homer_Instance on Tyrion is not online");

            WS_Message_Ping_instance result = homer_instance.ping();

            if(result.status.equals("success")) return GlobalResult.result_ok();
            return GlobalResult.result_BadRequest(Json.toJson(result));
        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion Front End",  hidden = true)
    public Result instance_add_temporary_instance(){
        try{

            // Zpracování Json
            final Form<Swagger_Instance_Temporary> form = Form.form(Swagger_Instance_Temporary.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Instance_Temporary help = form.get();

            Model_HomerServer server = Model_HomerServer.get_model(help.unique_identificator);
            if(server == null) return GlobalResult.notFoundObject("Server not found");

                // JsonNode result = server.add_temporary_instance(help.instance_name);

                // if(result.get("status").asText().equals("success")) return GlobalResult.result_ok(result);
            else return GlobalResult.result_BadRequest("");

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "upload B Program (code) to instnace ",
            hidden = true
    )
    public Result update_blocko_code_in_instance_with_code(String instance_name){
        try{

            // Zpracování Json
            final Form<Swagger_Instance_UpdateCode> form = Form.form(Swagger_Instance_UpdateCode.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Instance_UpdateCode help = form.get();

            JsonNode json = Json.parse(help.code);
            System.out.println(json.toString());

            // Kontrola objektu
            Model_HomerInstance homer_instance = Model_HomerInstance.find.where().eq("blocko_instance_name",instance_name).findUnique();
            if (homer_instance == null) return GlobalResult.notFoundObject("Homer_Instance id not found");

            if(!homer_instance.instance_online()) return GlobalResult.notFoundObject("Homer_Instance is not online");

            // WS_Upload_blocko_program result = homer_instance.upload_blocko_program("fake_program", help.code );

            // if(result.status.equals("success")) return GlobalResult.result_ok();
            return GlobalResult.result_BadRequest();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion Front End",  hidden = true)
    public Result instance_add_yoda(String instance_name, String yoda_id){
        try{

            // Kontrola objektu
            Model_HomerInstance homer_instance = Model_HomerInstance.find.where().eq("blocko_instance_name", instance_name).findUnique();
            if (homer_instance == null) return GlobalResult.notFoundObject("Homer_Instance id not found");

            if (!homer_instance.instance_online()) return GlobalResult.notFoundObject("Homer_Instance on Tyrion is not online");

            WS_Message_Add_yoda_to_instance result = homer_instance.add_Yoda_to_instance( yoda_id);

            if(result.status.equals("success")) return GlobalResult.result_ok();
            return GlobalResult.result_BadRequest();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion Front End",  hidden = true)
    public Result instance_remove_yoda(String instance_name, String yoda_id){
        try{

            Model_HomerInstance homer_instance = Model_HomerInstance.find.where().eq("blocko_instance_name", instance_name).findUnique();
            if (homer_instance == null) return GlobalResult.notFoundObject("Homer_Instance id not found");

            if (!homer_instance.instance_online()) return GlobalResult.notFoundObject("Homer_Instance on Tyrion is not online");

            WS_Message_Remove_yoda_from_instance result = homer_instance.remove_Yoda_from_instance(yoda_id);


            if(result.status.equals("success")) return GlobalResult.result_ok();
            return GlobalResult.result_BadRequest();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion Front End",  hidden = true)
    public Result instance_add_device(String instance_name, String yoda_id, String device_id){
        try{

            // Transformace na seznam
            List<String> list_of_devices = new ArrayList<>();
            list_of_devices.add(device_id);

            // Kontrola objektu
            Model_HomerInstance homer_instance = Model_HomerInstance.find.where().eq("blocko_instance_name", instance_name).findUnique();
            if (homer_instance == null) return GlobalResult.notFoundObject("Homer_Instance id not found");

            if (!homer_instance.instance_online()) return GlobalResult.notFoundObject("Homer_Instance on Tyrion is not online");


            WS_Message_Add_device_to_instance result = homer_instance.add_Device_to_instance(yoda_id, list_of_devices);

            if(result.status.equals("success")) return GlobalResult.result_ok();
            return GlobalResult.result_BadRequest();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "Only for Tyrion Front End",  hidden = true)
    public Result instance_remove_device(String instance_name, String yoda_id, String device_id){
        try{
            // Transformace na seznam
            List<String> list_of_devices = new ArrayList<>();
            list_of_devices.add(device_id);

            // Kontrola objektu
            Model_HomerInstance homer_instance = Model_HomerInstance.find.where().eq("blocko_instance_name", instance_name).findUnique();
            if (homer_instance == null) return GlobalResult.notFoundObject("Homer_Instance id not found");

            if (!homer_instance.instance_online()) return GlobalResult.notFoundObject("Homer_Instance on Tyrion is not online");


            WS_Message_Remove_device_from_instance result = homer_instance.remove_Device_from_instance(yoda_id, list_of_devices);

            if(result.status.equals("success")) return GlobalResult.result_ok();
            return GlobalResult.result_BadRequest();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "send command to instance", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    public Result send_command_to_instance(String instance_name, String target_id, String string_command){
        try{

            // Kontrola oprávnění
            Model_Board board = Model_Board.get_byId(target_id);
            if (board == null) return GlobalResult.notFoundObject("Board targetId not found");

            // Kontrola objektu
            Enum_type_of_command command = Enum_type_of_command.getTypeCommand(string_command);
            if(command == null) return GlobalResult.notFoundObject("Command not found!");

            // Kontrola objektu
            Model_HomerInstance homer_instance = Model_HomerInstance.find.where().eq("blocko_instance_name", instance_name).findUnique();
            if (homer_instance == null) return GlobalResult.notFoundObject("Homer_Instance id not found");

            JsonNode result = homer_instance.devices_commands(target_id, command);

            if(result.has("status") && result.get("status").asText().equals("success")) return GlobalResult.result_ok();
            return GlobalResult.result_BadRequest(result);

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }

    }

// TYPE OF BLOCK #######################################################################################################

    @ApiOperation(value = "create new Type of Block",
            tags = {"Type-of-Block"},
            notes = "creating group for BlockoBlocks -> Type of block",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "TypeOfBlock_create_permission", value = Model_TypeOfBlock.create_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "TypeOfBlock_create_permission" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfBlock_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_TypeOfBlock.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result typeOfBlock_create(){
        try{

            // Zpracování Json
            final Form<Swagger_TypeOfBlock_New> form = Form.form(Swagger_TypeOfBlock_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TypeOfBlock_New help = form.get();

            // Vytvoření objektu
            Model_TypeOfBlock typeOfBlock = new Model_TypeOfBlock();
            typeOfBlock.description = help.description;
            typeOfBlock.name        = help.name;

            // Nejedná se o privátní Typ Bločku
            if(help.project_id != null){

                // Kontrola objektu
                Model_Project project = Model_Project.find.byId(help.project_id);
                if(project == null) return GlobalResult.notFoundObject("Project project_id not found");
                if(! project.update_permission()) return GlobalResult.forbidden_Permission();

                // Úprava objektu
                typeOfBlock.project = project;

            }else {
                if(Model_TypeOfBlock.get_publicByName(help.name) != null)
                    return GlobalResult.result_BadRequest("Type of Block with this name already exists, type a new one.");
            }

            // Kontrola oprávnění těsně před uložením podle standardu
            if (! typeOfBlock.create_permission() ) return GlobalResult.forbidden_Permission();

            // Uložení objektu
            typeOfBlock.save();

            // Vrácení objektu
            return GlobalResult.created( Json.toJson(typeOfBlock));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get TypeOfBlock ",
            tags = {"Type-of-Block"},
            notes = "get BlockoBlock ",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "TypeOfBlock_read_permission", value = Model_TypeOfBlock.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Project == null - Public TypeOfBlock", value = "Permission not Required!"),
                            @ExtensionProperty(name = "Project.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "TypeOfBlock_create_permission" )
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_TypeOfBlock.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result typeOfBlock_get(@ApiParam(value = "type_of_block_id String path",   required = true)  String type_of_block_id){
        try {

            // Kontrola objektu
            Model_TypeOfBlock typeOfBlock = Model_TypeOfBlock.get_byId(type_of_block_id);
            if(typeOfBlock == null) return GlobalResult.notFoundObject("TypeOfBlock not found");

            // Kontrola oprávnění
            if (!typeOfBlock.read_permission()) return GlobalResult.forbidden_Permission();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(typeOfBlock));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }


    }

    @ApiOperation(value = "edit Type of Block",
            tags = {"Type-of-Block"},
            notes = "edit Type of block object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfBlock.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "TypeOfBlock_edit_permission" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfBlock_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_TypeOfBlock.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result typeOfBlock_update(@ApiParam(value = "type_of_block_id String path",   required = true)  String type_of_block_id){
        try{

            // Zpracování Json
            final Form<Swagger_TypeOfBlock_New> form = Form.form(Swagger_TypeOfBlock_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TypeOfBlock_New help = form.get();

            // Kontrola objektu
            Model_TypeOfBlock typeOfBlock = Model_TypeOfBlock.get_byId(type_of_block_id);
            if(typeOfBlock == null) return GlobalResult.notFoundObject("TypeOfBlock not found");

            // Kontrola oprávnění
            if (!typeOfBlock.edit_permission()) return GlobalResult.forbidden_Permission();

            // Úprava objektu
            typeOfBlock.description = help.description;
            typeOfBlock.name        = help.name;

            if(help.project_id != null){

                // Kontrola objektu
                Model_Project project = Model_Project.find.byId(help.project_id);
                if(project == null) return GlobalResult.notFoundObject("Project project_id not found");

                // Úprava objektu
                typeOfBlock.project = project;
            }

            // Uložení objektu
            typeOfBlock.update();

            // Vrácení objektu
            return GlobalResult.result_ok( Json.toJson(typeOfBlock));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Type of Block",
            tags = {"Type-of-Block"},
            notes = "delete group for BlockoBlocks -> Type of block",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfBlock.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "TypeOfBlock_delete_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result typeOfBlock_delete(@ApiParam(value = "type_of_block_id String path",   required = true)  String type_of_block_id){
        try{

            // Kontrola objektu
            Model_TypeOfBlock typeOfBlock = Model_TypeOfBlock.get_byId(type_of_block_id);
            if(typeOfBlock == null) return GlobalResult.notFoundObject("TypeOfBlock type_of_block_id not found");

            // Kontrola oprávnění
            if (! typeOfBlock.delete_permission()) return GlobalResult.forbidden_Permission();

            // Smazání objektu
            typeOfBlock.delete();

            // Vrácení objektu
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all Type of Block list",
            tags = {"Type-of-Block"},
            notes = "get all groups for BlockoBlocks -> Type of block",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_TypeOfBlock.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result typeOfBlock_getAll(){
        try {

            // Získání seznamu
            List<Model_TypeOfBlock> typeOfBlocks = Model_TypeOfBlock.get_all();

            // Vrácení seznamu
            return GlobalResult.result_ok(Json.toJson(typeOfBlocks));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get TypeOfBlock by Filter",
            tags = {"Type-of-Block"},
            notes = "get TypeOfBlock List",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "TypeOfBlock_read_permission", value = "No need to check permission, because Tyrion returns only those results which user owns"),
                    }),
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Type_Of_Block_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",             response = Swagger_Type_Of_Block_List.class),
            @ApiResponse(code = 400, message = "Invalid body",          response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",  response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",     response = Result_InternalServerError.class)
    })
    public Result typeOfBlock_getByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) int page_number){
        try {

            // Získání JSON
            final Form<Swagger_Type_Of_Block_Filter> form = Form.form(Swagger_Type_Of_Block_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Type_Of_Block_Filter help = form.get();

            // Získání všech objektů a následné odfiltrování soukormých TypeOfBlock
            Query<Model_TypeOfBlock> query = Ebean.find(Model_TypeOfBlock.class);

            if(help.private_type){
                query.where().eq("project.participants.person.id", Controller_Security.get_person_id());
            }else{
                query.where().isNull("project");
            }

            // Pokud JSON obsahuje project_id filtruji podle projektu
            if(help.project_id != null){

                query.where().eq("project.id", help.project_id);
            }

            // Vytvoření odchozího JSON
            Swagger_Type_Of_Block_List result = new Swagger_Type_Of_Block_List(query, page_number);

            // Vrácení výsledku
            return GlobalResult.result_ok(Json.toJson(result));

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "manual order UP for Type of Block list",
            tags = {"Type-of-Block"},
            notes = "set up order",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result typeOfBlock_order_up(@ApiParam(value = "blocko_block_id String path",   required = true) String blocko_block_id){
        try{

            Model_TypeOfBlock typeOfBlocks =  Model_TypeOfBlock.find.byId(blocko_block_id);
            if(typeOfBlocks == null) return GlobalResult.notFoundObject("TypeOfBlock not found");

            // Kontrola oprávnění
            if (!typeOfBlocks.edit_permission()) return GlobalResult.forbidden_Permission();

            typeOfBlocks.up();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "manual order Down for Type of Block list",
            tags = {"Type-of-Block"},
            notes = "set up order",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result typeOfBlock_order_down(@ApiParam(value = "type_of_block_id String path",   required = true) String type_of_block_id){
        try{

            Model_TypeOfBlock typeOfBlocks =  Model_TypeOfBlock.find.byId(type_of_block_id);
            if(typeOfBlocks == null) return GlobalResult.notFoundObject("TypeOfBlock not found");

            // Kontrola oprávnění
            if (!typeOfBlocks.edit_permission()) return GlobalResult.forbidden_Permission();

            typeOfBlocks.down();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

// BLOCK ###############################################################################################################

    @ApiOperation(value = "create new Block",
            tags = {"Blocko-Block"},
            notes = "creating new independent Block object for Blocko tools",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "BlockoBlock_create_permission", value = Model_BlockoBlock.create_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfBlocko.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlock_create_permission" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_BlockoBlock_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_BlockoBlock.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_JsonValueMissing.class),
            @ApiResponse(code = 400, message = "Something went wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockoBlock_create(){
        try{

            // Zpracování Json
            final Form<Swagger_BlockoBlock_New> form = Form.form(Swagger_BlockoBlock_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_BlockoBlock_New help = form.get();

            // Kontrola objektu
            Model_TypeOfBlock typeOfBlock = Model_TypeOfBlock.get_byId( help.type_of_block_id);
            if(typeOfBlock == null) return GlobalResult.notFoundObject("TypeOfBlock type_of_block_id not found");

            if (typeOfBlock.project == null && Model_BlockoBlock.get_publicByName(help.name) != null){
                return GlobalResult.result_BadRequest("BlockoBlock with this name already exists, type a new one.");
            }

            // Vytvoření objektu
            Model_BlockoBlock blockoBlock = new Model_BlockoBlock();

            blockoBlock.description = help.general_description;
            blockoBlock.name                = help.name;
            blockoBlock.author              = Controller_Security.get_person();
            blockoBlock.type_of_block       = typeOfBlock;

            // Kontrola oprávnění těsně před uložením
            if (!blockoBlock.create_permission()) return GlobalResult.forbidden_Permission();

            // Uložení objektu
            blockoBlock.save();

            // Získání šablony
            Model_BlockoBlockVersion scheme = Model_BlockoBlockVersion.get_scheme();

            // Kontrola objektu
            if(scheme == null) return GlobalResult.created(Json.toJson(blockoBlock));

            // Vytvoření objektu první verze
            Model_BlockoBlockVersion blockoBlockVersion = new Model_BlockoBlockVersion();
            blockoBlockVersion.version_name = "0.0.0";
            blockoBlockVersion.version_description = "This is a first version of block.";
            blockoBlockVersion.approval_state = Enum_Approval_state.approved;
            blockoBlockVersion.design_json = scheme.design_json;
            blockoBlockVersion.logic_json = scheme.logic_json;
            blockoBlockVersion.date_of_create = new Date();
            blockoBlockVersion.blocko_block = blockoBlock;
            blockoBlockVersion.save();

            // Vrácení objektu
            return GlobalResult.created( Json.toJson(blockoBlock) );

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit basic information of the BlockoBlock",
            tags = {"Blocko-Block"},
            notes = "update basic information (name, and description) of the independent BlockoBlock",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlock.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlock_edit_permission" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_BlockoBlock_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BlockoBlock.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockoBlock_update(@ApiParam(value = "blocko_block_id String path",   required = true)  String blocko_block_id){
        try {

            // Zpracování Json
            final Form<Swagger_BlockoBlock_New> form = Form.form(Swagger_BlockoBlock_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_BlockoBlock_New help = form.get();

            // Kontrola objektu
            Model_BlockoBlock blockoBlock = Model_BlockoBlock.get_byId(blocko_block_id);
            if (blockoBlock == null) return GlobalResult.notFoundObject("BlockoBlock blocko_block_id not found");

            // Kontrola oprávnění
            if (! blockoBlock.edit_permission() ) return GlobalResult.forbidden_Permission("You have no permission to edit");

            // Úprava objektu
            blockoBlock.description = help.general_description;
            blockoBlock.name        = help.name;

            // Kontrola objektu
            Model_TypeOfBlock typeOfBlock = Model_TypeOfBlock.get_byId(  help.type_of_block_id);
            if(typeOfBlock == null) return GlobalResult.notFoundObject("TypeOfBlock type_of_block_id not found");

            // Úprava objektu
            blockoBlock.type_of_block = typeOfBlock;

            // Uložení objektu
            blockoBlock.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(blockoBlock));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }

    }

    @ApiOperation(value = "get version of the BlockoBlock",
            tags = {"Blocko-Block"},
            notes = "get version (content) from independent BlockoBlock",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "BlockoBlockVersion_read_permission", value = Model_BlockoBlockVersion.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlock.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlockVersion_read_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BlockoBlockVersion.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result blockoBlockVersion_get(@ApiParam(value = "blocko_block_version_id String path",   required = true) String blocko_block_version_id){
        try {
            // Kontrola objektu
            Model_BlockoBlockVersion blocko_version = Model_BlockoBlockVersion.get_byId(blocko_block_version_id);
            if(blocko_version == null) return GlobalResult.notFoundObject("BlockoBlock blocko_block_id not found");

            // Kontrola oprávnění
            if (! blocko_version.read_permission() ) return GlobalResult.forbidden_Permission("You have no permission to get that");

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(blocko_version));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }

    }

    @ApiOperation(value = "get BlockoBlock",
            tags = {"Blocko-Block"},
            notes = "get independent BlockoBlock object",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "BlockoBlock_read_permission", value = Model_BlockoBlock.read_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlock.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlock_read_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BlockoBlock.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result blockoBlock_get(@ApiParam(value = "blocko_block_id String path",   required = true) String blocko_block_id){
        try {
            // Kontrola objektu
            Model_BlockoBlock blockoBlock = Model_BlockoBlock.get_byId(blocko_block_id);
            if(blockoBlock == null) return GlobalResult.notFoundObject("BlockoBlock not found");

            // Kontrola oprávnění
            if (! blockoBlock.read_permission() ) return GlobalResult.forbidden_Permission();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(blockoBlock));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }

    }

    @ApiOperation(value = "get BlockoBlock by Filter",
            tags = {"Blocko-Block"},
            notes = "get BlockoBlock List",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "BlockoBlock_read_permission", value = "No need to check permission, because Tyrion returns only those results which user owns"),
                    }),
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Blocko_Block_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",             response = Swagger_Blocko_Block_List.class),
            @ApiResponse(code = 400, message = "Invalid body",          response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",  response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",     response = Result_InternalServerError.class)
    })
    public Result blockoBlock_getByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) int page_number){
        try {

            // Získání JSON
            final Form<Swagger_Blocko_Block_Filter> form = Form.form(Swagger_Blocko_Block_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Blocko_Block_Filter help = form.get();

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_BlockoBlock> query = Ebean.find(Model_BlockoBlock.class);
            query.where().eq("author.id", Controller_Security.get_person_id());

            // Pokud JSON obsahuje project_id filtruji podle projektu
            if(help.project_id != null){

                query.where().eq("type_of_block.project.id", help.project_id);
            }

            // Vytvoření odchozího JSON
            Swagger_Blocko_Block_List result = new Swagger_Blocko_Block_List(query, page_number);

            // Vrácení výsledku
            return GlobalResult.result_ok(Json.toJson(result));

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete BlockoBlock",
            tags = {"Blocko-Block"},
            notes = "delete BlockoBlock",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlock.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlock_delete_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result blockoBlock_delete(@ApiParam(value = "blocko_block_id String path",   required = true)  String blocko_block_id){
        try {

            // Kontrola objektu
            Model_BlockoBlock blockoBlock = Model_BlockoBlock.get_byId(blocko_block_id);
            if(blockoBlock == null) return GlobalResult.notFoundObject("BlockoBlock blocko_block_id not found");

            // Kontrola oprávnění
            if (!blockoBlock.delete_permission()) return GlobalResult.forbidden_Permission();

            // Smazání objektu
            blockoBlock.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete BlockoBlock version",
            tags = {"Blocko-Block"},
            notes = "delete BlockoBlock version",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlockVersion.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlockVersion_delete_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result blockoBlockVersion_delete(@ApiParam(value = "blocko_block_version_id String path",   required = true) String blocko_block_version_id){
        try {

            // Kontrola objektu
            Model_BlockoBlockVersion version = Model_BlockoBlockVersion.get_byId(blocko_block_version_id);
            if(version == null) return GlobalResult.notFoundObject("BlockoBlockVersion blocko_block_version_id not found");

            // Kontrola oprávnění
            if (!version.delete_permission()) return GlobalResult.forbidden_Permission();

            // Smazání objektu
            version.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "create BlockoBlock version",
            tags = {"Blocko-Block"},
            notes = "new BlockoBlock version",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "BlockoBlockVersion_create_permission", value = Model_BlockoBlockVersion.create_permission_docs ),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlock.update_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlockVersion_create_permission" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_BlockoBlock_BlockoVersion_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_BlockoBlockVersion.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_JsonValueMissing.class),
            @ApiResponse(code = 400, message = "Something went wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockoBlockVersion_create(@ApiParam(value = "blocko_block_id String path",   required = true) String blocko_block_id){
        try {

            // Zpracování Json
            final Form<Swagger_BlockoBlock_BlockoVersion_New> form = Form.form(Swagger_BlockoBlock_BlockoVersion_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_BlockoBlock_BlockoVersion_New help = form.get();

            // Kontrola názvu
            if(help.version_name.equals("version_scheme")) return GlobalResult.result_BadRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_BlockoBlock blockoBlock = Model_BlockoBlock.get_byId(blocko_block_id);
            if(blockoBlock == null) return GlobalResult.notFoundObject("blockoBlock not found");

            // Vytvoření objektu
            Model_BlockoBlockVersion version = new Model_BlockoBlockVersion();
            version.date_of_create = new Date();

            version.version_name = help.version_name;
            version.version_description = help.version_description;
            version.design_json = help.design_json;
            version.logic_json = help.logic_json;
            version.blocko_block = blockoBlock;
            version.author = Controller_Security.get_person();

            // Kontrola oprávnění
            if (! version.create_permission()) return GlobalResult.forbidden_Permission();

            // Uložení objektu
            version.save();

            // Vrácení objektu
            return GlobalResult.created(Json.toJson(blockoBlock));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit BlockoBlock version",
            tags = {"Blocko-Block"},
            notes = "You can edit only basic information of the version. If you want to update the code, " +
                    "you have to create a new version!",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlockVersion.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlockVersion_edit_permission" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_BlockoBlock_BlockoVersion_Edit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BlockoBlockVersion.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_JsonValueMissing.class),
            @ApiResponse(code = 400, message = "Something went wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockoBlockVersion_update(@ApiParam(value = "blocko_block_version_id String path",   required = true) String blocko_block_version_id){
        try {

            // Zpracování Json
            final Form<Swagger_BlockoBlock_BlockoVersion_Edit> form = Form.form(Swagger_BlockoBlock_BlockoVersion_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_BlockoBlock_BlockoVersion_Edit help = form.get();

            // Kontrola názvu
            if(help.version_name.equals("version_scheme")) return GlobalResult.result_BadRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_BlockoBlockVersion version = Model_BlockoBlockVersion.get_byId(blocko_block_version_id);
            if(version == null) return GlobalResult.notFoundObject("blocko_block_version_id not found");

            // Úprava objektu
            version.version_name = help.version_name;
            version.version_description = help.version_description;

            // Uložení objektu
            version.update();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(version));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get all BlockoBlock version",
            tags = {"Blocko-Block"},
            notes = "get all versions (content) from independent BlockoBlock",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "BlockoBlockVersion_read_permission", value = Model_BlockoBlockVersion.read_permission_docs),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlock.read_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlockVersion_read_permission")
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_BlockoBlock_BlockoVersion_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BlockoBlockVersion.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result blockoBlockVersion_getAll(@ApiParam(value = "blocko_block_id String path",   required = true) String blocko_block_id){
        try {

            // Kontrola objektu
            Model_BlockoBlock blockoBlock = Model_BlockoBlock.get_byId(blocko_block_id);
            if (blockoBlock == null) return GlobalResult.notFoundObject("BlockoBlock blocko_block_id not found");

            // Kontrola oprávnění
            if (! blockoBlock.read_permission()) return GlobalResult.forbidden_Permission();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(blockoBlock.blocko_versions));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "make BlockoBlock version public",
            tags = {"Blocko-Block"},
            notes = "sets Approval_state to pending",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "BlockoBlockVersion_edit_permission", value = "If user has BlockoBlock.update_permission"),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "BlockoBlockVersion.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "BlockoBlockVersion_edit_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BlockoBlockVersion.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result blockoBlockVersion_makePublic(@ApiParam(value = "blocko_block_version_id String path",   required = true) String blocko_block_version_id){
        try{

            // Kontrola objektu
            Model_BlockoBlockVersion blockoBlockVersion = Model_BlockoBlockVersion.get_byId(blocko_block_version_id);
            if(blockoBlockVersion == null) return GlobalResult.notFoundObject("BlockoBlockVersion blocko_block_version_id not found");

            // Kontrola orávnění
            if(!(blockoBlockVersion.edit_permission())) return GlobalResult.forbidden_Permission();

            // Úprava objektu
            blockoBlockVersion.approval_state = Enum_Approval_state.pending;

            // Uložení změn
            blockoBlockVersion.update();

            // Vrácení výsledku
            return GlobalResult.result_ok(Json.toJson(blockoBlockVersion));

        }catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "manual order UP for Blocko Block list",
            tags = {"Blocko-Block"},
            notes = "set up order",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result blockoBlock_order_up(@ApiParam(value = "blocko_block_version_id String path",   required = true) String blocko_block_id){
        try{

            Model_BlockoBlock blockoBlock =  Model_BlockoBlock.find.byId(blocko_block_id);
            if(blockoBlock == null) return GlobalResult.notFoundObject("BlockoBlock not found");

            // Kontrola oprávnění
            if (! blockoBlock.edit_permission()) return GlobalResult.forbidden_Permission();

            blockoBlock.up();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "manual order Down for Blocko Block list",
            tags = {"Blocko-Block"},
            notes = "set up order",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_PermissionRequired.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result blockoBlock_order_down(@ApiParam(value = "blocko_block_version_id String path",   required = true) String blocko_block_id){
        try{

            Model_BlockoBlock blockoBlock =  Model_BlockoBlock.find.byId(blocko_block_id);
            if(blockoBlock == null) return GlobalResult.notFoundObject("BlockoBlock not found");

            // Kontrola oprávnění
            if (!blockoBlock.edit_permission()) return GlobalResult.forbidden_Permission();

            blockoBlock.down();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

// BLOCKO ADMIN ########################################################################################################*/

    @ApiOperation(value = "", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    public Result blockoDisapprove(){
        try {

            // Získání JSON
            final Form<Swagger_BlockoObject_Approval> form = Form.form(Swagger_BlockoObject_Approval.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_BlockoObject_Approval help = form.get();

            // Kontrola objektu
            Model_BlockoBlockVersion blockoBlockVersion = Model_BlockoBlockVersion.get_byId(help.object_id);
            if (blockoBlockVersion == null) return GlobalResult.notFoundObject("blocko_block_version not found");

            // Změna stavu schválení
            blockoBlockVersion.approval_state = Enum_Approval_state.disapproved;

            // Odeslání emailu s důvodem
            try {
                new Email()
                        .text("Version of Block " + blockoBlockVersion.blocko_block.name + ": " + Email.bold(blockoBlockVersion.version_name) + " was not approved for this reason: ")
                        .text(help.reason)
                        .send(blockoBlockVersion.blocko_block.author.mail, "Version of Block disapproved" );

            } catch (Exception e) {
                terminal_logger.internalServerError(e);
            }

            // Uložení změn
            blockoBlockVersion.update();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());

        }
    }

    @ApiOperation(value = "", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    public Result blockoApproval() {

        try {

            // Získání JSON
            final Form<Swagger_BlockoObject_Approve_withChanges> form = Form.form(Swagger_BlockoObject_Approve_withChanges.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_BlockoObject_Approve_withChanges help = form.get();

            // Kontrola názvu
            if(help.blocko_block_version_name.equals("version_scheme")) return GlobalResult.result_BadRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_BlockoBlockVersion privateBlockoBlockVersion = Model_BlockoBlockVersion.get_byId(help.object_id);
            if (privateBlockoBlockVersion == null) return GlobalResult.notFoundObject("blocko_block_version not found");

            // Kontrola objektu
            Model_TypeOfBlock typeOfBlock = Model_TypeOfBlock.get_byId(help.blocko_block_type_of_block_id);
            if (typeOfBlock == null) return GlobalResult.notFoundObject("type_of_block not found");

            // Vytvoření objektu
            Model_BlockoBlock blockoBlock = new Model_BlockoBlock();
            blockoBlock.name = help.blocko_block_name;
            blockoBlock.description = help.blocko_block_general_description;
            blockoBlock.type_of_block = typeOfBlock;
            blockoBlock.author = privateBlockoBlockVersion.blocko_block.author;
            blockoBlock.save();

            // Vytvoření objektu
            Model_BlockoBlockVersion blockoBlockVersion = new Model_BlockoBlockVersion();
            blockoBlockVersion.version_name = help.blocko_block_version_name;
            blockoBlockVersion.version_description = help.blocko_block_version_description;
            blockoBlockVersion.design_json = help.blocko_block_design_json;
            blockoBlockVersion.logic_json = help.blocko_block_logic_json;
            blockoBlockVersion.approval_state = Enum_Approval_state.approved;
            blockoBlockVersion.blocko_block = blockoBlock;
            blockoBlockVersion.date_of_create = new Date();
            blockoBlockVersion.save();

            // Pokud jde o schválení po ediatci
            if(help.state.equals("edit")) {
                privateBlockoBlockVersion.approval_state = Enum_Approval_state.edited;

                // Odeslání emailu
                try {
                    new Email()
                            .text("Version of Block " + blockoBlockVersion.blocko_block.name + ": " + Email.bold(blockoBlockVersion.version_name) + " was edited before publishing for this reason: ")
                            .text(help.reason)
                            .send(blockoBlockVersion.blocko_block.author.mail, "Version of Block edited" );

                } catch (Exception e) {
                    terminal_logger.internalServerError(e);
                }
            }
            else privateBlockoBlockVersion.approval_state = Enum_Approval_state.approved;

            // Uložení úprav
            privateBlockoBlockVersion.update();

            // Vrácení výsledku
            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    public Result blockoBlockVersion_editScheme(){

        try {

            // Získání JSON
            final Form<Swagger_BlockoBlock_BlockoVersion_Scheme_Edit> form = Form.form(Swagger_BlockoBlock_BlockoVersion_Scheme_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_BlockoBlock_BlockoVersion_Scheme_Edit help = form.get();

            // Kontrola objektu
            Model_BlockoBlockVersion blockoBlockVersion = Model_BlockoBlockVersion.get_scheme();
            if (blockoBlockVersion == null) return GlobalResult.notFoundObject("Scheme not found");

            // Úprava objektu
            blockoBlockVersion.design_json = help.design_json;
            blockoBlockVersion.logic_json = help.logic_json;

            // Uložení změn
            blockoBlockVersion.update();

            // Vrácení výsledku
            return GlobalResult.result_ok();
        }catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    public Result blockoBlockVersion_getScheme(){

        try {

            // Kontrola objektu
            Model_BlockoBlockVersion blockoBlockVersion = Model_BlockoBlockVersion.get_scheme();
            if (blockoBlockVersion == null) return GlobalResult.notFoundObject("Scheme not found");

            // Vytvoření výsledku
            Swagger_BlockoBlock_Version_scheme result = new Swagger_BlockoBlock_Version_scheme();
            result.design_json = blockoBlockVersion.design_json;
            result.logic_json = blockoBlockVersion.logic_json;

            // Vrácení výsledku
            return GlobalResult.result_ok(Json.toJson(result));
        }catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    public Result blockoBlockVersion_createScheme(){

        try {

            Model_BlockoBlockVersion scheme = Model_BlockoBlockVersion.get_scheme();
            if (scheme != null) return GlobalResult.result_BadRequest("Scheme already exists.");

            // Získání JSON
            final Form<Swagger_BlockoBlock_BlockoVersion_Scheme_Edit> form = Form.form(Swagger_BlockoBlock_BlockoVersion_Scheme_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_BlockoBlock_BlockoVersion_Scheme_Edit help = form.get();

            // Úprava objektu
            scheme = new Model_BlockoBlockVersion();
            scheme.version_name = "version_scheme";
            scheme.design_json = help.design_json;
            scheme.logic_json = help.logic_json;

            // Uložení změn
            scheme.save();

            // Vrácení výsledku
            return GlobalResult.result_ok();
        }catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }
}