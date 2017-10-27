package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
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
import utilities.enums.Enum_MProgram_SnapShot_settings;
import utilities.enums.Enum_Publishing_type;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.login_entities.Secured_API;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.scheduler.CustomScheduler;
import utilities.swagger.documentationClass.*;
import utilities.swagger.outboundClass.Filter_List.Swagger_B_Program_List;
import utilities.swagger.outboundClass.Filter_List.Swagger_Blocko_Block_List;
import utilities.swagger.outboundClass.Filter_List.Swagger_Instance_List;
import utilities.swagger.outboundClass.Filter_List.Swagger_Type_Of_Block_List;
import utilities.swagger.outboundClass.Swagger_B_Program_Version;
import utilities.swagger.outboundClass.Swagger_Instance_Short_Detail;
import web_socket.message_objects.homer_with_tyrion.WS_Message_Homer_Instance_destroy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Security.Authenticated(Secured_API.class)
@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Blocko extends Controller{

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Blocko.class);
    
// B PROGRAM ###########################################################################################################

    @ApiOperation(value = "create B_Program",
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
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bProgram_create(@ApiParam(value = "project_id String path", required = true) String project_id){
        try{



            // Zpracování Json
            final Form<Swagger_B_Program_New> form = Form.form(Swagger_B_Program_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_B_Program_New help = form.get();


            if(help.name.length() > 2) {
                return GlobalResult.result_badRequest("Tato zpráva by se měla zobrazit jako důvod toho že to nefunguje. ");
            }

            // Kontrola objektu
            Model_Project project = Model_Project.get_byId(project_id);
            if (project == null) return GlobalResult.result_notFound("Project project_id not found");

            // Kontrola oprávnění
            if (!project.update_permission() ) return GlobalResult.result_forbidden();

            // Tvorba programu
            Model_BProgram b_program        = new Model_BProgram();
            b_program.date_of_create        = new Date();
            b_program.description           = help.description;
            b_program.name                  = help.name;
            b_program.project               = project;

            // Kontrola oprávnění těsně před uložením
            if (!b_program.create_permission() ) return GlobalResult.result_forbidden();

            // Uložení objektu
            b_program.save();

            // Vrácení objektu
            return GlobalResult.result_created(Json.toJson(b_program));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get B_Program",
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
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result bProgram_get(@ApiParam(value = "b_program_id String path", required = true) String b_program_id){
        try{

            // Kontrola objektu
            Model_BProgram b_program = Model_BProgram.get_byId(b_program_id);
            if (b_program == null) return GlobalResult.result_notFound("B_Program id not found");

            // Kontrola oprávnění
            if (!b_program.read_permission() ) return GlobalResult.result_forbidden();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(b_program));

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
            @ApiResponse(code = 400, message = "Invalid body",          response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",  response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",     response = Result_InternalServerError.class)
    })
    public Result bProgram_getByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) int page_number){
        try {

            // Získání JSON
            final Form<Swagger_B_Program_Filter> form = Form.form(Swagger_B_Program_Filter.class).bindFromRequest();
            if(form.hasErrors()) return GlobalResult.result_invalidBody(form.errorsAsJson());
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
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bProgram_update(@ApiParam(value = "b_program_id String path", required = true) String b_program_id){
        try{

            // Zpracování Json
            final Form<Swagger_B_Program_New> form = Form.form(Swagger_B_Program_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_B_Program_New help = form.get();

            // Kontrola objektu
            Model_BProgram b_program = Model_BProgram.get_byId(b_program_id);
            if (b_program == null) return GlobalResult.result_notFound("B_Program id not found");

            // Kontrola oprávěnní
            if (!b_program.edit_permission()) return GlobalResult.result_forbidden();

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

    @ApiOperation(value = "delete B_Program",
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result bProgram_delete(@ApiParam(value = "b_program_id String path", required = true) String b_program_id){
        try{

            // Kontrola objektu
            Model_BProgram program = Model_BProgram.get_byId(b_program_id);
            if (program == null) return GlobalResult.result_notFound("B_Program id not found");

            // Kontrola oprávění
            if (! program.delete_permission() ) return GlobalResult.result_forbidden();

            // Smazání objektu
            program.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

// B PROGRAM VERSION ###################################################################################################

    @ApiOperation(value = "create B_Program_Version",
            tags = {"B_Program"},
            notes = "create new vesion in Blocko program",
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
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bProgramVersion_create(@ApiParam(value = "b_program_id String path", required = true) String b_program_id){
        try{

            // Zpracování Json
            final Form<Swagger_B_Program_Version_New> form = Form.form(Swagger_B_Program_Version_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_B_Program_Version_New help = form.get();

            // Program který budu ukládat do data Storage v Azure
            String file_content =  help.program;

            // Ověření programu
            Model_BProgram b_program = Model_BProgram.get_byId(b_program_id);
            if (b_program == null) return GlobalResult.result_notFound("B_Program id not found");

            // Kontrola oprávnění
            if (! b_program.update_permission() ) return GlobalResult.result_forbidden();

            // První nová Verze
            Model_VersionObject version_object     = new Model_VersionObject();
            version_object.version_name            = help.version_name;
            version_object.version_description     = help.version_description;
            version_object.date_of_create          = new Date();
            version_object.b_program               = b_program;
            version_object.author                  = Controller_Security.get_person();

            // Vytvořím Snapshoty Verze M_Programu
            if(help.m_project_snapshots != null) {

                for (Swagger_B_Program_Version_New.M_Project_SnapShot help_m_project_snap : help.m_project_snapshots) {

                    Model_MProject m_project = Model_MProject.get_byId(help_m_project_snap.m_project_id);
                    if (m_project == null) return GlobalResult.result_notFound("M_Project not found");
                    if (!m_project.update_permission()) return GlobalResult.result_forbidden();

                    Model_MProjectProgramSnapShot snap = new Model_MProjectProgramSnapShot();
                    snap.m_project = m_project;

                    for (Swagger_B_Program_Version_New.M_Program_SnapShot help_m_program_snap : help_m_project_snap.m_program_snapshots) {
                        Model_VersionObject m_program_version = Model_VersionObject.find.where().eq("id", help_m_program_snap.version_object_id).eq("m_program.id", help_m_program_snap.m_program_id).eq("m_program.m_project.id", m_project.id).findUnique();

                        if (m_program_version == null) return GlobalResult.result_notFound("M_Program Version id not found");

                        Model_MProgramInstanceParameter snap_shot_parameter = new Model_MProgramInstanceParameter();

                        snap_shot_parameter.m_program_version = m_program_version;
                        snap_shot_parameter.m_project_program_snapshot = snap;

                        snap.m_program_snapshots.add(snap_shot_parameter);
                    }

                    version_object.b_program_version_snapshots.add(snap);
                }
            }
            // Definování main Board
            for( Swagger_B_Program_Version_New.Hardware_group group : help.hardware_group) {

                Model_BProgramHwGroup b_program_hw_group = new Model_BProgramHwGroup();

                // Definuji Main Board - Tedy yodu pokud v Json přišel (není podmínkou)
                if(group.main_board_pair != null) {

                    Model_BPair b_pair = new Model_BPair();

                    b_pair.board = Model_Board.get_byId(group.main_board_pair.board_id);
                    if ( b_pair.board == null) return GlobalResult.result_notFound("Board board_id not found");
                    if (!b_pair.board.get_type_of_board().connectible_to_internet)  return GlobalResult.result_badRequest("Main Board must be internet connectible!");
                    if(!b_pair.board.update_permission()) return GlobalResult.result_forbidden();

                    b_pair.c_program_version = Model_VersionObject.get_byId(group.main_board_pair.c_program_version_id);
                    if ( b_pair.c_program_version == null) return GlobalResult.result_notFound("C_Program Version_Object c_program_version_id not found");
                    if ( b_pair.c_program_version.c_program == null)  return GlobalResult.result_badRequest("Version is not from C_Program");


                    if( Model_TypeOfBoard.find.where().eq("c_programs.id",  b_pair.c_program_version.c_program.id ).where().eq("boards.id",  b_pair.board.id).findRowCount() < 1){
                        return GlobalResult.result_badRequest("You want upload C++ program version id: " +  b_pair.c_program_version.id + " thats not compatible with hardware " + b_pair.board.id);
                    }

                    b_program_hw_group.main_board_pair = b_pair;

                }else {
                    return GlobalResult.result_badRequest("Hardware Group hasn't Main Board!");
                }

                // Definuji Devices - Tedy yodu pokud v Json přišly (není podmínkou)

                if(group.device_board_pairs != null && !group.device_board_pairs.isEmpty() ) {

                    for(Swagger_Board_CProgram_Pair connected_board : group.device_board_pairs ){

                        Model_BPair b_pair = new Model_BPair();

                        b_pair.board = Model_Board.get_byId(connected_board.board_id);
                        if ( b_pair.board == null) return GlobalResult.result_notFound("Board board_id not found");
                        if(!b_pair.board.update_permission()) return GlobalResult.result_forbidden();


                        b_pair.c_program_version = Model_VersionObject.get_byId(connected_board.c_program_version_id);
                        if ( b_pair.c_program_version == null) return GlobalResult.result_notFound("C_Program Version_Object c_program_version_id not found");
                        if ( b_pair.c_program_version.c_program == null)  return GlobalResult.result_badRequest("Version is not from C_Program");

                        if( Model_TypeOfBoard.find.where().eq("c_programs.id",  b_pair.c_program_version.c_program.id ).where().eq("boards.id",  b_pair.board.id).findRowCount() < 1){
                            return GlobalResult.result_badRequest("You want upload C++ program version id: " +  b_pair.c_program_version.id + " thats not compatible with hardware " + b_pair.board.id);
                        }

                        b_program_hw_group.device_board_pairs.add(b_pair);
                    }
                }
                version_object.b_program_hw_groups.add(b_program_hw_group);
            }

            // Uložení objektu
            version_object.save();

            // Nahrání na Azure
            Model_FileRecord.uploadAzure_Version(file_content, "program.js", b_program.get_path() , version_object);

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson( version_object.get_b_program().program_version(version_object) ));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get B_Program_Version",
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
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result bProgramVersion_get(@ApiParam(value = "version_id String path", required = true) String version_id){
        try{

            // Kontrola objektu
            Model_VersionObject version_object = Model_VersionObject.get_byId(version_id);
            if (version_object == null) return GlobalResult.result_notFound("Version_Object version_id not found");

            // Kontrola oprávnění
            if (version_object.get_b_program() == null) return GlobalResult.result_notFound("Version_Object is not version of B_Program");

            // Kontrola oprávnění
            if (! version_object.get_b_program().read_permission() ) return GlobalResult.result_forbidden();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(version_object.get_b_program().program_version(version_object)));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit B_Program_Version",
            tags = {"B_Program"},
            notes = "edit Version object",
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
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_B_Program_Version_Edit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bProgramVersion_update(@ApiParam(value = "version_id String path", required = true) String version_id){
        try{

            // Zpracování Json
            final Form<Swagger_B_Program_Version_Edit> form = Form.form(Swagger_B_Program_Version_Edit.class).bindFromRequest();
            if(form.hasErrors()) return GlobalResult.result_invalidBody(form.errorsAsJson());
            Swagger_B_Program_Version_Edit help = form.get();

            // Získání objektu
            Model_VersionObject version_object = Model_VersionObject.get_byId(version_id);
            if (version_object == null) return GlobalResult.result_notFound("Version not found");

            version_object.version_name = help.version_name;
            version_object.version_description = help.version_description;

            // Kontrola oprávnění
            if (!version_object.get_b_program().edit_permission()) return GlobalResult.result_forbidden();

            // Smazání objektu
            version_object.update();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete B_Program_Version",
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result bProgramVersion_delete(@ApiParam(value = "version_id String path", required = true) String version_id){
        try{

            // Získání objektu
            Model_VersionObject version_object  = Model_VersionObject.get_byId(version_id);

            // Kontrola objektu
            if (version_object == null) return GlobalResult.result_notFound("Version not found");
            if (version_object.get_b_program() == null) return GlobalResult.result_badRequest("BProgram not found");

            // Kontrola oprávnění
            if (!version_object.get_b_program().delete_permission()) return GlobalResult.result_forbidden();

            // Smazání objektu
            version_object.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "upload B_Program_Version to cloud",
            tags = {"B_Program"},
            notes = "upload version of B_Program to cloud. Its possible have only one version from B program in cloud. If you uploud new one - old one will be replaced",
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
            @ApiResponse(code = 200, message = "Successfully uploaded",     response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result bProgramVersion_deploy(@ApiParam(value = "version_id String path", required = true) String version_id){
        try {

            // Získání JSON
            final Form<Swagger_B_Program_Upload_Instance> form = Form.form(Swagger_B_Program_Upload_Instance.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_B_Program_Upload_Instance help = form.get();

            // Kontrola objektu: Verze B programu kterou budu nahrávat do cloudu
            Model_VersionObject version_object = Model_VersionObject.get_byId(version_id);
            if (version_object == null) return GlobalResult.result_notFound("Version not found");

            // Kontrola objektu: B program, který chci nahrát do Cloudu na Blocko cloud_blocko_server
            if (version_object.get_b_program() == null) return GlobalResult.result_badRequest("Version is not version of BProgram");
            Model_BProgram b_program = version_object.get_b_program();

            // Kontrola oprávnění
            if (!b_program.update_permission()) return GlobalResult.result_forbidden();

            Model_HomerInstanceRecord record = new Model_HomerInstanceRecord();
            record.main_instance_history = b_program.instance();
            record.version_object = version_object;
            record.date_of_created = new Date();

            if(help.upload_time != null) {

                Date upload_time = new Date(help.upload_time);

                // Zkontroluji smysluplnost časové známky
                if (!upload_time.after(new Date()))  return GlobalResult.result_badRequest("time must be set in the future");
                record.planed_when = upload_time;

            } else{
                Date date_from = new Date();
                record.running_from = date_from;
                record.planed_when = date_from;
            }
            record.save();

            // If immidietly
            if(help.upload_time == null){

                terminal_logger.debug("bProgramVersion_deploy: Set the instants immediately");
                record.set_record_into_cloud();

            }else {
                terminal_logger.debug("bProgramVersion_deploy: Set the instants by Time scheduler (not now) ");
                CustomScheduler.scheduleBlockoUpload(record);
            }

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

// INSTANCE ############################################################################################################

    @ApiOperation(value = "edit Instance",
            tags = {"Instance"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Instance_Edit",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated",      response = Model_HomerInstance.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result instance_edit(@ApiParam(value = "instance_id String path", required = true) String instance_id){
        try{

            // Zpracování Json
            final Form<Swagger_Instance_Edit> form = Form.form(Swagger_Instance_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Instance_Edit help = form.get();

            // Kontrola objektu
            Model_HomerInstance homer_instance = Model_HomerInstance.find.where().eq("id", instance_id).findUnique();
            if (homer_instance == null) return GlobalResult.result_notFound("Homer_Instance id not found");

            if (!homer_instance.getB_program().update_permission() ) return GlobalResult.result_forbidden();

            if(help.name != null && !help.name.equals("")) homer_instance.name = help.name;
            if(help.description != null && !help.description.equals("")) homer_instance.description = help.description;

            homer_instance.update();

            return GlobalResult.result_ok(Json.toJson(homer_instance));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "set Instance start or shutDown",
            tags = {"Instance"},
            notes = "If instance is not running this Command uploud instance to cloud and starter all procedures. " +
                    "If instance is online, stis Command shutdown instance immidietly with all procedures.",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully removed",      response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result instance_start_or_shut_down(String instance_name){
        try{

            // Kontrola objektu
            Model_HomerInstance homer_instance = Model_HomerInstance.get_byId(instance_name);
            if (homer_instance == null) return GlobalResult.result_notFound("Homer_Instance id not found");

            if (!homer_instance.getB_program().update_permission() ) return GlobalResult.result_forbidden();


            // Pokud má aktuální instance "Actual Instance record - znaemná to, že má běžet v cloudu"
            // Proto tento záznam odstraním
            if(homer_instance.actual_instance != null){

                homer_instance.remove_from_cloud();


                return GlobalResult.result_ok();

            }else{

                if(homer_instance.instance_history.isEmpty()){
                     return GlobalResult.result_badRequest("We did not find any previous version running in the cloud. Please first select version in Blocko editor run.");
                }

                homer_instance.actual_instance = homer_instance.instance_history.get(0);
                homer_instance.update();

                homer_instance.actual_instance.set_record_into_cloud();

                return GlobalResult.result_ok();

            }

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Instances List by Project",
            tags = {"Instance"},
            notes = "get list of instance_ids details under project id",
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

    @ApiOperation(value = "get Instance",
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
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result get_b_program_instance(String instance_id){
        try{

            Model_HomerInstance instance = Model_HomerInstance.get_byId(instance_id);
            if (instance == null) return GlobalResult.result_notFound("Homer_Instance instance_id not found");
            if(instance.getB_program() == null ) return GlobalResult.result_notFound("Homer_Instance is virtual!!");

            if(!instance.getB_program().read_permission()) return GlobalResult.result_forbidden();

            return GlobalResult.result_ok(Json.toJson(instance));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Instance by Filter",
            tags = { "Instance"},
            notes = "Get List of Instances. According to permission - system return only Instance from project, where is user owner or" +
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
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result get_b_program_instance_by_filter(){
        try{

            // Zpracování Json
            final Form<Swagger_Instance_Filter> form = Form.form(Swagger_Instance_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
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


            if(!help.server_unique_ids.isEmpty()){
                query.where().in("cloud_homer_server.id", help.server_unique_ids);
            }

            // Vytvářím seznam podle stránky
            Swagger_Instance_List result = new Swagger_Instance_List(query, help.page_number);

            // Vracím seznam
            return GlobalResult.result_ok(Json.toJson(result));

        } catch (Exception e) {

            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "update Instance Grid Settings",
            tags = { "Instance"},
            notes = "",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Instance_GridApp_Settings",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_MProgramInstanceParameter.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result instance_change_settings_grid_App(){
        try {

            // Zpracování Json
            final Form<Swagger_Instance_GridApp_Settings> form = Form.form(Swagger_Instance_GridApp_Settings.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Instance_GridApp_Settings help = form.get();

            // Hledám objekt
            Model_MProgramInstanceParameter program_parameter = Model_MProgramInstanceParameter.get_byId(help.m_program_parameter_id);
            if (program_parameter == null) return GlobalResult.result_notFound("Object not found");

            //Ohlídám oprávnění
            if (!program_parameter.edit_permission()) return GlobalResult.result_forbidden();

            //PArsuju Enum kdyžtak chyba IllegalArgumentException
            Enum_MProgram_SnapShot_settings settings = Enum_MProgram_SnapShot_settings.valueOf(help.snapshot_settings);

            // Měním parameter
            program_parameter.snapshot_settings = settings;

            // Update
            program_parameter.update();

            // Vracím Objekt
            return GlobalResult.result_ok(Json.toJson(program_parameter));

        } catch (IllegalArgumentException e) {

            terminal_logger.internalServerError(new Exception("Incoming snapshot_settings is invalid."));
            return GlobalResult.result_badRequest("snapshot_settings is not valid");

        } catch (Exception e) {

            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Instance_Record",
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
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result get_b_program_instance_record(String instance_record_id){
        try{


            Model_HomerInstanceRecord instance = Model_HomerInstanceRecord.get_byId(instance_record_id);
            if (instance == null) return GlobalResult.result_notFound("Homer_Instance instance_id not found");

            if(!instance.main_instance_history.getB_program().read_permission()) return GlobalResult.result_forbidden();

            return GlobalResult.result_ok(Json.toJson(instance));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

// TYPE OF BLOCK #######################################################################################################

    @ApiOperation(value = "create TypeOfBlock",
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
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result typeOfBlock_create(){
        try{

            // Zpracování Json
            final Form<Swagger_TypeOfBlock_New> form = Form.form(Swagger_TypeOfBlock_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_TypeOfBlock_New help = form.get();

            // Vytvoření objektu
            Model_TypeOfBlock typeOfBlock = new Model_TypeOfBlock();
            typeOfBlock.description = help.description;
            typeOfBlock.name        = help.name;

            // Nejedná se o privátní Typ Bločku
            if(help.project_id != null){

                // Kontrola objektu
                Model_Project project = Model_Project.get_byId(help.project_id);
                if(project == null) return GlobalResult.result_notFound("Project project_id not found");
                if(! project.update_permission()) return GlobalResult.result_forbidden();

                // Úprava objektu
                typeOfBlock.project = project;
                typeOfBlock.publish_type = Enum_Publishing_type.private_program;

            }else {
                if(Model_TypeOfBlock.get_publicByName(help.name) != null)
                    return GlobalResult.result_badRequest("TypeOfBlock with this name already exists, type a new one.");
            }

            // Kontrola oprávnění těsně před uložením podle standardu
            if (! typeOfBlock.create_permission() ) return GlobalResult.result_forbidden();

            // Uložení objektu
            typeOfBlock.save();

            // Vrácení objektu
            return GlobalResult.result_created( Json.toJson(typeOfBlock));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get TypeOfBlock",
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
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result typeOfBlock_get(@ApiParam(value = "type_of_block_id String path",   required = true)  String type_of_block_id){
        try {

            // Kontrola objektu
            Model_TypeOfBlock typeOfBlock = Model_TypeOfBlock.get_byId(type_of_block_id);
            if(typeOfBlock == null) return GlobalResult.result_notFound("TypeOfBlock not found");

            // Kontrola oprávnění
            if (!typeOfBlock.read_permission()) return GlobalResult.result_forbidden();

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(typeOfBlock));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }


    }

    @ApiOperation(value = "edit TypeOfBlock",
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
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result typeOfBlock_edit(@ApiParam(value = "type_of_block_id String path",   required = true)  String type_of_block_id){
        try{

            // Zpracování Json
            final Form<Swagger_TypeOfBlock_New> form = Form.form(Swagger_TypeOfBlock_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_TypeOfBlock_New help = form.get();

            // Kontrola objektu
            Model_TypeOfBlock typeOfBlock = Model_TypeOfBlock.get_byId(type_of_block_id);
            if(typeOfBlock == null) return GlobalResult.result_notFound("TypeOfBlock not found");

            // Kontrola oprávnění
            if (!typeOfBlock.edit_permission()) return GlobalResult.result_forbidden();

            // Úprava objektu
            typeOfBlock.description = help.description;
            typeOfBlock.name        = help.name;

            if(help.project_id != null){

                // Kontrola objektu
                Model_Project project = Model_Project.get_byId(help.project_id);
                if(project == null) return GlobalResult.result_notFound("Project project_id not found");

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

    @ApiOperation(value = "delete TypeOfBlock",
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result typeOfBlock_delete(@ApiParam(value = "type_of_block_id String path",   required = true)  String type_of_block_id){
        try{

            // Kontrola objektu
            Model_TypeOfBlock typeOfBlock = Model_TypeOfBlock.get_byId(type_of_block_id);
            if(typeOfBlock == null) return GlobalResult.result_notFound("TypeOfBlock type_of_block_id not found");

            // Kontrola oprávnění
            if (! typeOfBlock.delete_permission()) return GlobalResult.result_forbidden();

            // Smazání objektu
            typeOfBlock.delete();

            // Vrácení objektu
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get TypeOfBlocks by Filter",
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
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfBlock_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",             response = Swagger_Type_Of_Block_List.class),
            @ApiResponse(code = 400, message = "Invalid body",          response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",  response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",     response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result typeOfBlock_getByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) int page_number){
        try {

            // Získání JSON
            final Form<Swagger_TypeOfBlock_Filter> form = Form.form(Swagger_TypeOfBlock_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_TypeOfBlock_Filter help = form.get();

            // Získání všech objektů a následné odfiltrování soukormých TypeOfBlock
            Query<Model_TypeOfBlock> query = Ebean.find(Model_TypeOfBlock.class);

            // Order
            query.order().asc("order_position");

            // Pokud JSON obsahuje project_id filtruji podle projektu
            if(help.project_id != null){

                Model_Project project = Model_Project.get_byId(help.project_id);
                if(project == null )return GlobalResult.result_notFound("Project not found");
                if(!project.read_permission())return GlobalResult.result_forbidden();

                query.where().eq("project.id", help.project_id).eq("removed_by_user", false);
            }

            if(help.public_programs){

                if(!Controller_Security.get_person().has_permission(Model_CProgram.permissions.C_Program_community_publishing_permission.name())) {
                    query.where().isNull("project").eq("removed_by_user", false).eq("publish_type", Enum_Publishing_type.public_program.name());
                }else {
                    query.where().isNull("project").eq("removed_by_user", false).eq("active", true).eq("publish_type", Enum_Publishing_type.public_program.name());
                }

            }

            // Vytvoření odchozího JSON
            Swagger_Type_Of_Block_List result = new Swagger_Type_Of_Block_List(query, page_number);

            // Vrácení výsledku
            return GlobalResult.result_ok(Json.toJson(result));

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "deactivate TypeOfBlocks",
            tags = {"Admin-Type-of-Block"},
            notes = "deactivate Type of Widget",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result typeOfBlock_deactivate(String type_of_block_id){
        try {

            Model_TypeOfBlock typeOfBlock = Model_TypeOfBlock.get_byId(type_of_block_id);
            if(typeOfBlock == null) return GlobalResult.result_notFound("TypeOfBlock not found");

            // Kontrola oprávnění
            if (! typeOfBlock.edit_permission() ) return GlobalResult.result_forbidden();

            if(typeOfBlock.project_id() != null ) return GlobalResult.result_forbidden();


            if (!typeOfBlock.active) return GlobalResult.result_badRequest("TypeOfBlock is already deactivated");

            if(!typeOfBlock.update_permission()) return GlobalResult.result_forbidden();

            typeOfBlock.active = false;

            typeOfBlock.update();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "activate TypeOfBlocks",
            tags = {"Admin-Type-of-Block"},
            notes = "activate Type of Widget",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Tariff.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result typeOfBlock_activate(String type_of_block_id){
        try {


            Model_TypeOfBlock typeOfBlock = Model_TypeOfBlock.get_byId(type_of_block_id);
            if(typeOfBlock == null) return GlobalResult.result_notFound("TypeOfBlock not found");

            if(typeOfBlock.project_id() != null ) return GlobalResult.result_forbidden();

            if(!typeOfBlock.update_permission()) return GlobalResult.result_forbidden();

            if (typeOfBlock.active) return GlobalResult.result_badRequest("TypeOfBlock is already activated");

            typeOfBlock.active = true;

            typeOfBlock.update();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "order_Up TypeOfBlock",
            tags = {"Type-of-Block"},
            notes = "Set order in list one position up",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result typeOfBlock_orderUp(@ApiParam(value = "blocko_block_id String path",   required = true) String blocko_block_id){
        try{

            Model_TypeOfBlock typeOfBlocks =  Model_TypeOfBlock.get_byId(blocko_block_id);
            if(typeOfBlocks == null) return GlobalResult.result_notFound("TypeOfBlock not found");

            // Kontrola oprávnění
            if (!typeOfBlocks.edit_permission()) return GlobalResult.result_forbidden();

            typeOfBlocks.up();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "order_Down TypeOfBlock",
            tags = {"Type-of-Block"},
            notes = "Set order in list one position down",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result typeOfBlock_orderDown(@ApiParam(value = "type_of_block_id String path",   required = true) String type_of_block_id){
        try{

            Model_TypeOfBlock typeOfBlocks =  Model_TypeOfBlock.get_byId(type_of_block_id);
            if(typeOfBlocks == null) return GlobalResult.result_notFound("TypeOfBlock not found");

            // Kontrola oprávnění
            if (!typeOfBlocks.edit_permission()) return GlobalResult.result_forbidden();

            typeOfBlocks.down();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

// BLOCK ###############################################################################################################

    @ApiOperation(value = "create BlockoBlock",
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
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something went wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockoBlock_create(){
        try{

            // Zpracování Json
            final Form<Swagger_BlockoBlock_New> form = Form.form(Swagger_BlockoBlock_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_BlockoBlock_New help = form.get();

            // Kontrola objektu
            Model_TypeOfBlock typeOfBlock = Model_TypeOfBlock.get_byId( help.type_of_block_id);
            if(typeOfBlock == null) return GlobalResult.result_notFound("TypeOfBlock type_of_block_id not found");

            if (typeOfBlock.project == null && Model_BlockoBlock.get_publicByName(help.name) != null){
                return GlobalResult.result_badRequest("BlockoBlock with this name already exists, type a new one.");
            }

            // Vytvoření objektu
            Model_BlockoBlock blockoBlock = new Model_BlockoBlock();

            blockoBlock.description = help.general_description;
            blockoBlock.name                = help.name;
            blockoBlock.author              = Controller_Security.get_person();
            blockoBlock.type_of_block       = typeOfBlock;
            blockoBlock.publish_type        = Enum_Publishing_type.private_program;

            // Kontrola oprávnění těsně před uložením
            if (!blockoBlock.create_permission()) return GlobalResult.result_forbidden();

            // Uložení objektu
            blockoBlock.save();

            // Získání šablony
            Model_BlockoBlockVersion scheme = Model_BlockoBlockVersion.get_scheme();

            // Kontrola objektu
            if(scheme == null) return GlobalResult.result_created(Json.toJson(blockoBlock));

            // Vytvoření objektu první verze
            Model_BlockoBlockVersion blockoBlockVersion = new Model_BlockoBlockVersion();
            blockoBlockVersion.version_name = "0.0.0";
            blockoBlockVersion.version_description = "This is a first version of block.";
            blockoBlockVersion.approval_state = Enum_Approval_state.approved;
            blockoBlockVersion.design_json = scheme.design_json;
            blockoBlockVersion.logic_json = scheme.logic_json;
            blockoBlockVersion.date_of_create = new Date();
            blockoBlockVersion.blocko_block = blockoBlock;
            blockoBlockVersion.author = Controller_Security.get_person();
            blockoBlockVersion.save();

            // Vrácení objektu
            return GlobalResult.result_created( Json.toJson(blockoBlock) );

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "make_Clone BlockoBlock",
            tags = {"Blocko-Block"},
            notes = "clone Blocko Block for private",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Blocko_Block_Copy",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BlockoBlock.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Secured_API.class)
    public Result blockoBlock_clone() {
        try {

            // Zpracování Json
            final Form<Swagger_Blocko_Block_Copy> form = Form.form(Swagger_Blocko_Block_Copy.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_Blocko_Block_Copy help = form.get();

            // Vyhledám Objekt
            Model_BlockoBlock blocko_block_old = Model_BlockoBlock.get_byId(help.blocko_block_id);
            if(blocko_block_old == null) return GlobalResult.result_notFound("Model_GridWidget blocko_block_id not found");

            // Zkontroluji oprávnění
            if(!blocko_block_old.read_permission())  return GlobalResult.result_forbidden();

            // Vyhledám Objekt
            Model_Project project = Model_Project.get_byId(help.project_id);
            if (project == null) return GlobalResult.result_notFound("Project project_id not found");

            // Zkontroluji oprávnění
            if(!project.update_permission())  return GlobalResult.result_forbidden();


            // Kontrola objektu
            Model_TypeOfBlock typeOfBlock = Model_TypeOfBlock.get_byId(help.type_of_blocks_id);
            if(typeOfBlock != null) {
                if (!Model_Project.get_byId(typeOfBlock.project_id()).update_permission()) {
                    return GlobalResult.result_forbidden();
                }
            }

            if(typeOfBlock == null) {
                typeOfBlock = new Model_TypeOfBlock();
                typeOfBlock.description = "Yea! My First Blocko Group with Community Widget";
                typeOfBlock.name        = "Private Widget Group";
                typeOfBlock.project     = project;
                typeOfBlock.save();

                typeOfBlock.refresh();
            }


            Model_BlockoBlock blocko_block_new =  new Model_BlockoBlock();
            blocko_block_new.name = help.name;
            blocko_block_new.description = help.description;
            blocko_block_new.type_of_block = typeOfBlock;
            blocko_block_new.save();

            blocko_block_new.refresh();


            for(Model_BlockoBlockVersion version : blocko_block_old.get_blocko_block_versions()){

                Model_BlockoBlockVersion copy_object = new Model_BlockoBlockVersion();
                copy_object.version_name        = version.version_name;
                copy_object.date_of_create      = version.date_of_create;
                copy_object.version_description = version.version_description;
                copy_object.date_of_create      = new Date();
                copy_object.author              = version.author;
                copy_object.design_json         = version.design_json;
                copy_object.logic_json          = version.logic_json;
                copy_object.blocko_block        = blocko_block_new;

                // Zkontroluji oprávnění
                copy_object.save();

            }

            blocko_block_new.refresh();

            // Vracím Objekt
            return GlobalResult.result_ok(Json.toJson(blocko_block_new));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }
    @ApiOperation(value = "edit BlockoBlock",
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
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockoBlock_update(@ApiParam(value = "blocko_block_id String path",   required = true)  String blocko_block_id){
        try {

            // Zpracování Json
            final Form<Swagger_BlockoBlock_New> form = Form.form(Swagger_BlockoBlock_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_BlockoBlock_New help = form.get();

            // Kontrola objektu
            Model_BlockoBlock blockoBlock = Model_BlockoBlock.get_byId(blocko_block_id);
            if (blockoBlock == null) return GlobalResult.result_notFound("BlockoBlock blocko_block_id not found");

            // Kontrola oprávnění
            if (! blockoBlock.edit_permission() ) return GlobalResult.result_forbidden("You have no permission to edit");

            // Úprava objektu
            blockoBlock.description = help.general_description;
            blockoBlock.name        = help.name;

            // Kontrola objektu
            Model_TypeOfBlock typeOfBlock = Model_TypeOfBlock.get_byId(  help.type_of_block_id);
            if(typeOfBlock == null) return GlobalResult.result_notFound("TypeOfBlock type_of_block_id not found");

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
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result blockoBlock_get(@ApiParam(value = "blocko_block_id String path",   required = true) String blocko_block_id){
        try {
            // Kontrola objektu
            Model_BlockoBlock blockoBlock = Model_BlockoBlock.get_byId(blocko_block_id);
            if(blockoBlock == null) return GlobalResult.result_notFound("BlockoBlock not found");

            // Kontrola oprávnění
            if (! blockoBlock.read_permission() ) return GlobalResult.result_forbidden();

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
            @ApiResponse(code = 400, message = "Invalid body",          response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",  response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",     response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockoBlock_getByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) int page_number){
        try {

            // Získání JSON
            final Form<Swagger_Blocko_Block_Filter> form = Form.form(Swagger_Blocko_Block_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result blockoBlock_delete(@ApiParam(value = "blocko_block_id String path",   required = true)  String blocko_block_id){
        try {

            // Kontrola objektu
            Model_BlockoBlock blockoBlock = Model_BlockoBlock.get_byId(blocko_block_id);
            if(blockoBlock == null) return GlobalResult.result_notFound("BlockoBlock blocko_block_id not found");

            // Kontrola oprávnění
            if (!blockoBlock.delete_permission()) return GlobalResult.result_forbidden();

            // Smazání objektu
            blockoBlock.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "order BlockoBlock Up",
            tags = {"Blocko-Block"},
            notes = "set up order",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result blockoBlock_orderUp(@ApiParam(value = "blocko_block_version_id String path",   required = true) String blocko_block_id){
        try{

            Model_BlockoBlock blockoBlock =  Model_BlockoBlock.get_byId(blocko_block_id);
            if(blockoBlock == null) return GlobalResult.result_notFound("BlockoBlock not found");

            // Kontrola oprávnění
            if (! blockoBlock.edit_permission()) return GlobalResult.result_forbidden();

            blockoBlock.up();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "order BlockoBlock Down",
            tags = {"Blocko-Block"},
            notes = "set up order",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result blockoBlock_orderDown(@ApiParam(value = "blocko_block_version_id String path",   required = true) String blocko_block_id){
        try{

            Model_BlockoBlock blockoBlock =  Model_BlockoBlock.get_byId(blocko_block_id);
            if(blockoBlock == null) return GlobalResult.result_notFound("BlockoBlock not found");

            // Kontrola oprávnění
            if (!blockoBlock.edit_permission()) return GlobalResult.result_forbidden();

            blockoBlock.down();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "deactivate BlockoBlock",
            tags = {"Admin-Blocko-Block"},
            notes = "deactivate BlockoBlock",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result blockoBlock_deactivate(String blocko_block_id){
        try {

            Model_BlockoBlock blockoBlock = Model_BlockoBlock.get_byId(blocko_block_id);
            if(blockoBlock == null) return GlobalResult.result_notFound("BlockoBlock blocko_block_id not found");

            // Kontrola oprávnění
            if (! blockoBlock.edit_permission() ) return GlobalResult.result_forbidden();


            if (!blockoBlock.active) return GlobalResult.result_badRequest("BlockoBlock is already deactivated");

            if(!blockoBlock.update_permission()) return GlobalResult.result_forbidden();

            blockoBlock.active = false;

            blockoBlock.update();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "activate BlockoBlock",
            tags = {"Admin-Blocko-Block"},
            notes = "activate Blocko Block",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_Tariff.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result blockoBlock_activate(String blocko_block_id){
        try {

            Model_BlockoBlock blockoBlock = Model_BlockoBlock.get_byId(blocko_block_id);
            if(blockoBlock == null) return GlobalResult.result_notFound("BlockoBlock blocko_block_id not found");

            if (blockoBlock.active) return GlobalResult.result_badRequest("BlockoBlock is already activated");

            if(!blockoBlock.update_permission()) return GlobalResult.result_forbidden();

            blockoBlock.active = true;

            blockoBlock.update();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit BlockoBlock_Version Response publication",
            tags = {"Admin-Blocko-Block"},
            notes = "sets Approval_state to pending",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_BlockoBlock_Publish_Response",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockoBlock_public_response() {

        try {

            // Získání JSON
            final Form<Swagger_BlockoBlock_Publish_Response> form = Form.form(Swagger_BlockoBlock_Publish_Response.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_BlockoBlock_Publish_Response help = form.get();

            // Kontrola názvu
            if(help.version_name.equals("version_scheme")) return GlobalResult.result_badRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_BlockoBlockVersion private_blocko_block_version = Model_BlockoBlockVersion.get_byId(help.version_id);
            if (private_blocko_block_version == null) return GlobalResult.result_notFound("grid_widget_version not found");

            // Kontrola nadřazeného objektu
            Model_BlockoBlock block_old = private_blocko_block_version.get_blocko_block();

            // Zkontroluji oprávnění
            if(!block_old.community_publishing_permission()){
                return GlobalResult.result_forbidden();
            }


            if(help.decision) {

                // Kontrola skupiny kam se widget zařadí
                Model_TypeOfBlock type_of_block_public = Model_TypeOfBlock.find.byId(help.blocko_block_type_of_block_id);
                if (type_of_block_public == null) {
                    return GlobalResult.result_notFound("Model_TypeOfBlock not found");
                }

                if (type_of_block_public.publish_type != Enum_Publishing_type.public_program) {
                    return GlobalResult.result_badRequest("You cannot register Widget to non-public group");
                }

                System.out.println("help.decision je true!!!");

                private_blocko_block_version.approval_state = Enum_Approval_state.approved;
                private_blocko_block_version.update();

                Model_BlockoBlock blocko_block = Model_BlockoBlock.find.where().eq("id",block_old.id.toString() + "_public_copy").findUnique();

                if(blocko_block == null) {
                    // Vytvoření objektu
                    blocko_block = new Model_BlockoBlock();
                    blocko_block.name = help.program_name;
                    blocko_block.description = help.program_description;
                    blocko_block.type_of_block = type_of_block_public;
                    blocko_block.author = private_blocko_block_version.get_blocko_block().get_author();
                    blocko_block.publish_type = Enum_Publishing_type.public_program;
                    blocko_block.save();
                }

                // Vytvoření objektu
                Model_BlockoBlockVersion blocko_blockVersion = new Model_BlockoBlockVersion();
                blocko_blockVersion.version_name = help.version_name;
                blocko_blockVersion.version_description = help.version_description;
                blocko_blockVersion.design_json = private_blocko_block_version.design_json;
                blocko_blockVersion.logic_json = private_blocko_block_version.logic_json;
                blocko_blockVersion.approval_state = Enum_Approval_state.approved;
                blocko_blockVersion.blocko_block = blocko_block;
                blocko_blockVersion.date_of_create = new Date();
                blocko_blockVersion.save();

                blocko_block.refresh();

                // TODO notifikace a emaily

                return GlobalResult.result_ok();

            }else {
                // Změna stavu schválení
                private_blocko_block_version.approval_state = Enum_Approval_state.disapproved;

                // Odeslání emailu s důvodem
                try {

                    new Email()
                            .text("Version of Widget " + private_blocko_block_version.get_blocko_block().name + ": " + Email.bold(private_blocko_block_version.version_name) + " was not approved for this reason: ")
                            .text(help.reason)
                            .send(private_blocko_block_version.get_blocko_block().get_author().mail, "Version of Widget disapproved" );

                } catch (Exception e) {
                    terminal_logger.internalServerError (e);
                }

                // Uložení změn
                private_blocko_block_version.update();

                // Vrácení výsledku
                return GlobalResult.result_ok();
            }

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

// BLOCK VERSION #######################################################################################################

    @ApiOperation(value = "create BlockoBlock_Version",
            tags = {"Blocko-Block"},
            notes = "new BlockoBlock version",
            produces = "application/json",
            protocols = "https",
            code = 201
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
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something went wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockoBlockVersion_create(@ApiParam(value = "blocko_block_id String path",   required = true) String blocko_block_id){
        try {

            // Zpracování Json
            final Form<Swagger_BlockoBlock_BlockoVersion_New> form = Form.form(Swagger_BlockoBlock_BlockoVersion_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_BlockoBlock_BlockoVersion_New help = form.get();

            // Kontrola názvu
            if(help.version_name.equals("version_scheme")) return GlobalResult.result_badRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_BlockoBlock blockoBlock = Model_BlockoBlock.get_byId(blocko_block_id);
            if(blockoBlock == null) return GlobalResult.result_notFound("blockoBlock not found");

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
            if (!version.create_permission()) return GlobalResult.result_forbidden();

            // Uložení objektu
            version.save();

            // Vrácení objektu
            return GlobalResult.result_created(Json.toJson(blockoBlock));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get BlockoBlock_Version",
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
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result blockoBlockVersion_get(@ApiParam(value = "blocko_block_version_id String path",   required = true) String blocko_block_version_id){
        try {
            // Kontrola objektu
            Model_BlockoBlockVersion blocko_version = Model_BlockoBlockVersion.get_byId(blocko_block_version_id);
            if(blocko_version == null) return GlobalResult.result_notFound("BlockoBlock blocko_block_id not found");

            // Kontrola oprávnění
            if (! blocko_version.read_permission() ) return GlobalResult.result_forbidden("You have no permission to get that");

            // Vrácení objektu
            return GlobalResult.result_ok(Json.toJson(blocko_version));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }

    }

    @ApiOperation(value = "edit BlockoBlock_Version",
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
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 400, message = "Something went wrong",      response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockoBlockVersion_update(@ApiParam(value = "blocko_block_version_id String path",   required = true) String blocko_block_version_id){
        try {

            // Zpracování Json
            final Form<Swagger_BlockoBlock_BlockoVersion_Edit> form = Form.form(Swagger_BlockoBlock_BlockoVersion_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_BlockoBlock_BlockoVersion_Edit help = form.get();

            // Kontrola názvu
            if(help.version_name.equals("version_scheme")) return GlobalResult.result_badRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_BlockoBlockVersion version = Model_BlockoBlockVersion.get_byId(blocko_block_version_id);
            if(version == null) return GlobalResult.result_notFound("blocko_block_version_id not found");

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

    @ApiOperation(value = "delete BlockoBlock_Version",
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
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result blockoBlockVersion_delete(@ApiParam(value = "blocko_block_version_id String path",   required = true) String blocko_block_version_id){
        try {

            // Kontrola objektu
            Model_BlockoBlockVersion version = Model_BlockoBlockVersion.get_byId(blocko_block_version_id);
            if(version == null) return GlobalResult.result_notFound("BlockoBlockVersion blocko_block_version_id not found");

            // Kontrola oprávnění
            if (!version.delete_permission()) return GlobalResult.result_forbidden();

            // Smazání objektu
            version.delete();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "make BlockoBlock_Version public",
            tags = {"Blocko-Block"},
            notes = "sets Approval_state to pending",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BlockoBlockVersion.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result blockoBlockVersion_makePublic(@ApiParam(value = "blocko_block_version_id String path",   required = true) String blocko_block_version_id){
        try{

            // Kontrola objektu
            Model_BlockoBlockVersion blockoBlockVersion = Model_BlockoBlockVersion.get_byId(blocko_block_version_id);
            if(blockoBlockVersion == null) return GlobalResult.result_notFound("BlockoBlockVersion blocko_block_version_id not found");

            // Kontrola orávnění
            if(!(blockoBlockVersion.edit_permission())) return GlobalResult.result_forbidden();

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

    @ApiOperation(value = "set_As_Main BlockoBlock_Version",
            tags = {"Admin-Blocko-Block"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "GridWidgetVersion.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "GridWidgetVersion_delete_permission")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",               response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Object not found",        response = Result_NotFound.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_Forbidden.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result blockoBlockVersion_set_main(String blocko_block_version_id){
        try {

            // Kontrola objektu
            Model_BlockoBlockVersion version = Model_BlockoBlockVersion.get_byId(blocko_block_version_id);
            if(version == null) return GlobalResult.result_notFound("BlockoBlockVersion blocko_block_version_id not found");

            // Kontrola oprávnění
            if (!version.edit_permission()) return GlobalResult.result_forbidden();

            if(!version.get_blocko_block_id().equals("00000000-0000-0000-0000-000000000001")){
                return GlobalResult.result_notFound("BlockoBlockVersion blocko_block_version_id not from default program");
            }

            Model_BlockoBlockVersion old_version = Model_BlockoBlockVersion.find.where().eq("publish_type", Enum_Publishing_type.default_version.name()).select("id").findUnique();
            if(old_version != null) {
                old_version = Model_BlockoBlockVersion.get_byId(old_version.id);
                old_version.publish_type = null;
                old_version.update();
            }

            version.publish_type = Enum_Publishing_type.default_version;
            version.update();

            // Vrácení potvrzení
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

// BLOCKO ADMIN ########################################################################################################*/

    @ApiOperation(value = "edit BlockoBlock_Version refuse publication",
            tags = {"Admin-Blocko-Block"},
            notes = "sets disapproved from pending",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockoDisapprove(){
        try {

            // Získání JSON
            final Form<Swagger_BlockoObject_Approval> form = Form.form(Swagger_BlockoObject_Approval.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_BlockoObject_Approval help = form.get();

            // Kontrola objektu
            Model_BlockoBlockVersion blockoBlockVersion = Model_BlockoBlockVersion.get_byId(help.object_id);
            if (blockoBlockVersion == null) return GlobalResult.result_notFound("blocko_block_version not found");

            // Změna stavu schválení
            blockoBlockVersion.approval_state = Enum_Approval_state.disapproved;

            // Odeslání emailu s důvodem
            try {
                new Email()
                        .text("Version of Block " + blockoBlockVersion.get_blocko_block().name + ": " + Email.bold(blockoBlockVersion.version_name) + " was not approved for this reason: ")
                        .text(help.reason)
                        .send(blockoBlockVersion.get_blocko_block().get_author().mail, "Version of Block disapproved" );

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

    @ApiOperation(value = "edit BlockoBlock_Version accept publication",
            tags = {"Admin-Blocko-Block"},
            notes = "sets Approval_state to pending",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BlockoBlockVersion.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result blockoApproval() {

        try {

            // Získání JSON
            final Form<Swagger_BlockoObject_Approve_withChanges> form = Form.form(Swagger_BlockoObject_Approve_withChanges.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_BlockoObject_Approve_withChanges help = form.get();

            // Kontrola názvu
            if(help.blocko_block_version_name.equals("version_scheme")) return GlobalResult.result_badRequest("This name is reserved for the system");

            // Kontrola objektu
            Model_BlockoBlockVersion privateBlockoBlockVersion = Model_BlockoBlockVersion.get_byId(help.object_id);
            if (privateBlockoBlockVersion == null) return GlobalResult.result_notFound("blocko_block_version not found");

            // Kontrola objektu
            Model_TypeOfBlock typeOfBlock = Model_TypeOfBlock.get_byId(help.blocko_block_type_of_block_id);
            if (typeOfBlock == null) return GlobalResult.result_notFound("type_of_block not found");

            // Vytvoření objektu
            Model_BlockoBlock blockoBlock = new Model_BlockoBlock();
            blockoBlock.name = help.blocko_block_name;
            blockoBlock.description = help.blocko_block_general_description;
            blockoBlock.type_of_block = typeOfBlock;
            blockoBlock.author = privateBlockoBlockVersion.get_blocko_block().get_author();
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
                            .text("Version of Block " + blockoBlockVersion.get_blocko_block().name + ": " + Email.bold(blockoBlockVersion.version_name) + " was edited before publishing for this reason: ")
                            .text(help.reason)
                            .send(blockoBlockVersion.get_blocko_block().get_author().mail, "Version of Block edited" );

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

    /**
    @ApiOperation(value = "", hidden = true)
    @Security.Authenticated(Secured_Admin.class)
    public Result blockoBlockVersion_editScheme(){

        try {

            // Získání JSON
            final Form<Swagger_BlockoBlock_BlockoVersion_Scheme_Edit> form = Form.form(Swagger_BlockoBlock_BlockoVersion_Scheme_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
            Swagger_BlockoBlock_BlockoVersion_Scheme_Edit help = form.get();

            // Kontrola objektu
            Model_BlockoBlockVersion blockoBlockVersion = Model_BlockoBlockVersion.get_scheme();
            if (blockoBlockVersion == null) return GlobalResult.result_notFound("Scheme not found");

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
    public Result blockoBlockVersion_createScheme(){

        try {

            Model_BlockoBlockVersion scheme = Model_BlockoBlockVersion.get_scheme();
            if (scheme != null) return GlobalResult.result_badRequest("Scheme already exists.");

            // Získání JSON
            final Form<Swagger_BlockoBlock_BlockoVersion_Scheme_Edit> form = Form.form(Swagger_BlockoBlock_BlockoVersion_Scheme_Edit.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.result_invalidBody(form.errorsAsJson());}
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
    */
}