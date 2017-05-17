package controllers;

import io.swagger.annotations.Api;


import play.mvc.Controller;
import play.mvc.Result;

import utilities.logger.Class_Logger;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Wiky extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Wiky.class);

     public Result test1(){
         try {
             
             return ok();
         }catch (Exception e){
             e.printStackTrace();
             return badRequest();
         }

     }


    public Result test2(){
        try {

            return ok();
        }catch (Exception e){
            e.printStackTrace();
            return badRequest();
        }

    }


    public Result test3(){
        try {

            return ok();
        }catch (Exception e){
            e.printStackTrace();
            return badRequest();
        }

    }

}
