package utilities.permission;

import controllers.SecurityController;
import models.person.PersonPermission;

public class Permission {

    public static boolean check_permission(String... args){

        System.out.println(" Kontroluji oprávnění: ");
        for(String s : args) System.out.print(s + ", ");

        //Zde porovnávám zda uživatel má oprávnění na přímo
        // nebo je ve skupině, která dané oprávnění vlastní

        if (PersonPermission.find.where().or(
                com.avaje.ebean.Expr.and(
                        com.avaje.ebean.Expr.in("value", args),
                        com.avaje.ebean.Expr.eq("roles.persons.id", SecurityController.getPerson().id)
                ),
                com.avaje.ebean.Expr.and(
                        com.avaje.ebean.Expr.in("value", args),
                        com.avaje.ebean.Expr.like("persons.id", SecurityController.getPerson().id)
                )
        ).findList().size() < 1) return false;


        return true;
    }

    public static boolean check_dynamic(String value){
        return false;
    }

    public static boolean check_dynamic_OR_permission(String value, String... args){

        return  ( check_permission(args) || check_dynamic(value));
    }
}
