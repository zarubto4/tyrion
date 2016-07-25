package controllers;


import io.swagger.annotations.Api;
import models.person.Person;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.loggy.Loggy;
import utilities.response.GlobalResult;

@Api(value = "Not Documented API - InProgress or Stuck")
public class WikyController extends Controller {


     public Result test1(){
         Loggy.result_internalServerError("testing", request());

         return ok();

     }

    public Result test_notifications(){
        try {
            Person person = Person.find.byId("1");
            NotificationController.test_notification(person);
            return GlobalResult.result_ok();
        }catch (Exception e){
            return GlobalResult.internalServerError();
        }
    }

}
