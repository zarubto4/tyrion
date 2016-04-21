package utilities.permission;

import controllers.SecurityController;
import models.person.PersonPermission;

public class Permission {

    public static boolean check_permission(String... args){
        //Zde porovnávám zda uživatel má oprávnění na přímo
        // nebo je ve skupině, která dané oprávnění vlastní

        if (
           (PersonPermission.find.where().in("value", args).eq("roles.persons.id", SecurityController.getPerson().id).findRowCount() < 1) &&
           (PersonPermission.find.where().in("value", args).eq("persons.id", SecurityController.getPerson().id).findRowCount() < 1) ){
           return false;
        }

        return true;
    }

    public static boolean check_dynamic(String value){
        return false;
    }

    public static boolean check_dynamic_OR_permission(String value, String... args){

        return  ( check_permission(args) || check_dynamic(value));
    }
}
