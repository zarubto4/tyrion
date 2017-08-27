package controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import models.*;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.logger.Server_Logger;
import utilities.login_entities.Secured_API;
import utilities.response.GlobalResult;
import utilities.response.response_objects.*;
import utilities.swagger.outboundClass.Swagger_Report_Admin_Dashboard;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured_API.class)
public class Controller_Report extends Controller {


    @ApiOperation(value = "get Report_Admin_Dashboard",
            tags = {"Admin-Report"},
            notes = "",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully removed",      response = Swagger_Report_Admin_Dashboard.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result report_admin_dashboard(){
        try{

            Swagger_Report_Admin_Dashboard report = new Swagger_Report_Admin_Dashboard();

            report.person_registration = Model_Person.find.findRowCount();
            report.project_created     = Model_Project.find.findRowCount();
            report.board_registered = Model_Board.find.findRowCount();

            report.homer_server_public_created     = Model_HomerServer.find.findRowCount();
            report.homer_server_private_created    = 0;

            report.homer_server_public_online      = Controller_WebSocket.homer_servers.size();
            report.homer_server_private_online     = 0;

            report.compilation_server_public_created = Model_CompilationServer.find.where().findRowCount();
            report.compilation_server_public_online  = Controller_WebSocket.compiler_cloud_servers.size();

            report.bugs_reported = Model_LoggyError.find.findRowCount();

            return GlobalResult.result_ok(Json.toJson(report));

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }
}
