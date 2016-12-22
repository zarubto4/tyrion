package utilities.permission;

        import controllers.Controller_Security;
        import models.person.Model_Permission;

public class Permission {

    public static boolean check_permission(String permission){
        //Zde porovnávám zda uživatel má oprávnění na přímo
        // nebo je ve skupině, která dané oprávnění vlastní

        return   Model_Permission.find.where().eq("value", permission).eq("roles.persons.id", Controller_Security.getPerson().id).findRowCount() +
                 Model_Permission.find.where().eq("value", permission).eq("persons.id", Controller_Security.getPerson().id).findRowCount() > 0;
    }




}
