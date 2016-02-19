package controllers;


import be.objectify.deadbolt.core.PatternType;
import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Pattern;
import be.objectify.deadbolt.java.actions.Restrict;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.deadbolt.actions.CustomRestrict;
import utilities.deadbolt.actions.RoleGroup;
import utilities.deadbolt.understand.Roles;
import utilities.loginEntities.Secured;

@Security.Authenticated(Secured.class)
public class WikyController extends Controller {


    // Autorizuji Role admin a superadmin, ale vyloučím všechny, kdo jsou operátoři, dál netuším...
    @CustomRestrict(value = { @RoleGroup({Roles.admin, Roles.superAdmin}), @RoleGroup(Roles.operator), @RoleGroup(Roles.user) }, config = @Restrict(value = {}, handlerKey = "defaultHandler", content = "project.owner"))
    public Result test1(){
        return ok("Vše se povedlo");
    }

    //Předpoklad
    // MyDiynamicResourceHandlerer - tímto anotuji nějakou vlasntost (třeba že je vlastník)
    @Dynamic("pureLuck")
    public Result test2(){
        return ok("Vše se povedlo");
    }

    @Dynamic(value = "pureLuck", handlerKey = "alternativeHandler")
    public Result test3(){
        return ok("Vše se povedlo");
    }

    //@CustomRestrict(value = { @RoleGroup({Roles.admin}), @RoleGroup(Roles.user) }, config = @Restrict(value = {}, handlerKey = "defaultHandler", content = "project.owner"))
    @CustomRestrict(value = {}, config = @Restrict(value = { @Group(value = {"admin"}) }, handlerKey = "defaultHandler", content = "project.owner"))
    public Result test4(String projectId){
        return ok("Vše se povedlo");
    }


    //Předpoklad
    // DefaultPermission- tímto anotuji nějakou vlasntost (třeba že je vlastník)
    @Dynamic(value = "project.b_program_owner")
    public Result test5(String projectId){
        return ok("Vše se povedlo");
    }


    @Pattern("printers.edit")
    public Result test6()
    {
        return ok("Vše se povedlo");
    }

    @Pattern(value = "(.)*\\.edit", patternType = PatternType.CUSTOM)
    public Result test57()
    {

        return ok("Vše se povedlo");
    }

    @CustomRestrict(value = {@RoleGroup({Roles.admin, Roles.operator})}, config = @Restrict({ }))

    public Result test8(final String userName)
    {
        return ok("Vše se povedlo");
    }

    public Result test9(){ return ok();}


}
