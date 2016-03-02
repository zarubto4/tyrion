package utilities.a_main_utils;

import controllers.SecurityController;
import models.persons.PersonPermission;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PermissionChecker{


    public static  Map<String, Optional<DynamicResourceHandl> > HANDLERS = new HashMap<>();

    private static final DynamicResourceHandl DENY = new DynamicResourceHandl() {
            @Override
            public boolean check_dynamic(String s) throws PermissionException {
                System.out.println("Mapa neobsahuje dinamický klíč!!!");
                return false;
            }
    };



    public static boolean check_dynamic(String name) throws PermissionException {

        if(HANDLERS.containsKey(name)) return HANDLERS.get(name).get().check_dynamic(name);
        else return DENY.check_dynamic(name);
    }

    public static boolean check_permission(String... args) throws PermissionException {
        try {

            //Zde porovnávám zda uživatel má oprávnění na přímo
            // nebo je ve skupině, která dané oprávnění vlasntí

            if (PersonPermission.find.where().or(
                        com.avaje.ebean.Expr.and(
                                com.avaje.ebean.Expr.in("value", args),
                                com.avaje.ebean.Expr.eq("roles.persons.id", SecurityController.getPerson().id)
                        ),
                            com.avaje.ebean.Expr.and(
                                    com.avaje.ebean.Expr.in("value", args),
                                    com.avaje.ebean.Expr.like("persons.id", SecurityController.getPerson().id)
                        )
            ).findList().size() < 1) throw new PermissionException();


            return true;

        } catch (Exception e) { throw new PermissionException();}
    }



    public static boolean check_dynamic_OR_permission(String value, String... args) throws PermissionException{

       try{
            System.out.println("Kontroluji permission");
           if(check_permission()) return true;

       }catch (PermissionException e){
           System.out.println("permission selhalo - Kontroluji dynamic");
          if (check_dynamic(value)) return true;
       }

       throw new PermissionException();
    }



    static {


        HANDLERS.put("project.owner",Optional.of(new InterfaceDynamic() { public boolean check_dynamic (final String name) {

            System.out.println("Jsem v metodě project.owner");
            return true;
        }}));


        HANDLERS.put("project.creator",Optional.of(new InterfaceDynamic() { public boolean check_dynamic (final String name) {

            System.out.println("Jsem v metodě project.creator");
            return true;

        }}));
    }




}
