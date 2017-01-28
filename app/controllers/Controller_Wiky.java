package controllers;


import io.swagger.annotations.Api;
import models.project.b_program.instnace.Model_HomerInstance;
import models.project.b_program.servers.Model_HomerServer;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.enums.CLoud_Homer_Server_Type;

import java.util.List;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Wiky extends Controller {


     public Result test1(){

         try {



             return ok();
         }catch (Exception e){
             e.printStackTrace();
             return badRequest();
         }

     }





}
