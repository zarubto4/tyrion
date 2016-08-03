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




    public Result test_notifications(String mail){
        try {
            Person person = Person.find.where().eq("mail", mail).findUnique();
            NotificationController.test_notification(person);
            return redirect("/public/websocket");
        }catch (Exception e){
            return GlobalResult.internalServerError();
        }
    }
}
