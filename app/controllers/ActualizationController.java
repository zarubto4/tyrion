package controllers;

import io.swagger.annotations.ApiParam;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.loggy.Loggy;
import utilities.response.GlobalResult;

import javax.websocket.server.PathParam;

public class ActualizationController extends Controller {


    public Result get_Actualization_progress(@ApiParam(required = true) @PathParam("board_id")  String project_id) {
        try {

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


    public Result set_actualization_sheduling(@ApiParam(required = true) @PathParam("board_id")  String project_id) {
        try {

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }



    public Result interupt_sheduling(@ApiParam(required = true) @PathParam("board_id")  String project_id) {
        try {

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }




}
