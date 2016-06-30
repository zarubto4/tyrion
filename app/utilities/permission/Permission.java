package utilities.permission;

        import controllers.SecurityController;
        import models.person.PersonPermission;

public class Permission {

    public static boolean check_permission(String permission){
        //Zde porovnávám zda uživatel má oprávnění na přímo
        // nebo je ve skupině, která dané oprávnění vlastní

        return   PersonPermission.find.where().eq("value", permission).eq("roles.persons.id", SecurityController.getPerson().id).findRowCount() +
                 PersonPermission.find.where().eq("value", permission).eq("persons.id", SecurityController.getPerson().id).findRowCount() > 0;
    }




}
