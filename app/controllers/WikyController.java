package controllers;


import play.mvc.Controller;
import play.mvc.Result;
import utilities.loggy.Loggy;

public class WikyController extends Controller {


     public Result test1(){
       return TODO;
    }

    // Testovací logger
    public Result test2(){
        Loggy.result_internalServerError("testing", request());
        return ok();
    }

    public Result test3(){
        return ok("Vše se povedlo");
    }

    public Result test4(String projectId){
        return ok("Vše se povedlo");
    }

    //Předpoklad
    // DefaultPermission- tímto anotuji nějakou vlasntost (třeba že je vlastník)
    public Result test5(String projectId){
        return ok("Vše se povedlo");
    }



    public Result test6()
    {
        return ok("Vše se povedlo");
    }


    public Result test57()
    {
        return ok("Vše se povedlo");
    }

    public Result test8(final String userName)
    {
        return ok("Vše se povedlo");
    }



    public Result test9(){return ok();}


}
