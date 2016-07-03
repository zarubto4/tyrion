package controllers;


import models.compiler.FileRecord;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.UtilTools;
import utilities.loggy.Loggy;


public class WikyController extends Controller {


     public Result test1(){
         Loggy.result_internalServerError("testing", request());

         return ok();


     }

    // Testovací logger
    public Result test2(){

        try {
            FileRecord fileRecord = FileRecord.find.byId("21");

           String string = UtilTools.get_encoded_binary_file_from_azure(fileRecord.file_path);


            return ok( new String(string));

        } catch (Exception e) {
            e.printStackTrace();
            return badRequest();
        }
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
