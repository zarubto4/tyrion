package controllers;


import io.swagger.annotations.Api;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.loggy.Loggy;

@Api(value = "Not Documented API - InProgress or Stuck")
public class WikyController extends Controller {


     public Result test1(){
         Loggy.result_internalServerError("testing", request());

         return ok();

     }
}
