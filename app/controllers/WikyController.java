package controllers;


import play.mvc.Controller;
import play.mvc.Result;
import utilities.Server;
import utilities.permission.PermissionException;

public class WikyController extends Controller {


    public Result test1(){

        try {
             System.out.println("Check dynamic: project.owner: " +  Server.check_dynamic("project.owner")+ "\n" );
        }catch (PermissionException e){}

        try {
            System.out.println("Check dynamic: project.creator: " +  Server.check_dynamic("project.creator") +"\n" );
        }catch (PermissionException e){}

        try {
            System.out.println("Check dynamic: project.deleter: " +  Server.check_dynamic("project.deleter") + "\n" );
        }catch (PermissionException e){}

        try {
            System.out.println("Check permission: processor.read: "   +  Server.check_permission( "processor.read") + "\n" );

        }catch (PermissionException e){}

        try {
            System.out.println("Check permission: processor.blabla:"  +  Server.check_permission( "processor.blabla") + "\n" );

        }catch (PermissionException e){}

        try {
            System.out.println("Check Permission or Dynamic1: " + Server.check_dynamic_OR_permission("project.owner","processor.read") + "\n");
        }catch (PermissionException e){}

        try {
            System.out.println("Check Permission or Dynamic2: " + Server.check_dynamic_OR_permission("project.owner","processor.blabla") + "\n");
        }catch (PermissionException e){
            System.out.println("Toto se mělo povést");
        }

        try {
            System.out.println("Check Permission or Dynamic2: " + Server.check_dynamic_OR_permission("project.blabla","processor.blabla") + "\n");
        }catch (PermissionException e){
            System.out.println("Toto se nemělo povést");
        }


        return ok("Vše se povedlo");


    }

    // Testovací logger
    public Result test2(){

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
