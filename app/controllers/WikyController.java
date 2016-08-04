package controllers;


import io.swagger.annotations.Api;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.goPay.GoPay_Controller;

@Api(value = "Not Documented API - InProgress or Stuck")
public class WikyController extends Controller {


     public Result test1(){


         GoPay_Controller.do_on_Demand_payment();

         return ok();

     }





}
