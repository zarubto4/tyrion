package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.login.Person;
import models.permission.GroupWithPermissions;
import models.permission.PermissionKey;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.response.GlobalResult;
import utilities.loginEntities.Secured;

import java.util.ArrayList;
import java.util.List;


public class PermissionController extends Controller {

/**
 * Permission controller je
 *
 * Existuje skupina práv, které dávají uživateli možnost upravovat cizí příspěvky nebo je mazat.
 * Jde především o role moderátorů a administrátorů.
 *
 * Skupina Administrator může vždy vše
 * Skupina Moderator může jen
 *
 * 1. Právo vytvářet skupiny
 * 2. Právo mazat skupiny
 * 3. Právo vidět kdo je ve skupinách
 * 4. Právo Zablokovat uživatele
 * 5. Předávat stejná nebo nižší práva
 */

    /**
     * Ihned po startu serveru je tato metoda zavolána z třídy GLOBAL.onStart() aby zajistila, že budou v
     * databázi vhodně zaregistrovány všechny skupiny. Uživatele do skupin to však v žádném případě nikdy nepřidá.
     *
     * Tato metoda slouží jen k počátečnímu nastavení serveru a databáze. Musí ohlídat, že nevytváří duplicity a další.
     *
     */
    public static void onStartPermission(){
        try {

            // Jméno které bude používáno - Lze Refaktoringem v budoucnu měnit název controlleru a ničemu to nebude vadit
            final String controllerName =  PermissionController.class.getSimpleName();

            // ošetříme jednoduchým dotazem zda v databázi už skupina není
            if(PermissionKey.find.byId(controllerName + "_SuperMaster") != null) return;

            new PermissionKey(controllerName + "_SuperMaster",               "Can do everything");
            new PermissionKey(controllerName + "_CreateGroup",               "Can create groups with different permission and set all permission to any group");
            new PermissionKey(controllerName + "_CanAddPersonToGroup",       "Can delete Post in Overflow");
            new PermissionKey(controllerName + "_CanRemovePersonFromGroup",  "Can delete Post in Overflow");

        }catch (Exception e){
            System.out.println("Došlo k chybě v " + PermissionController.class.getCanonicalName() );
            e.printStackTrace();
        }
    }

    public Result getAllPermissions(){
        try{
            List<PermissionKey> keys = PermissionKey.find.all();
            return GlobalResult.okResult(Json.toJson(keys));
        }catch (Exception e){
            return GlobalResult.badRequest(e);
        }
    }

    public Result getAllGroups(){
        try{
            List<GroupWithPermissions> groups = GroupWithPermissions.find.all();
            return GlobalResult.okResult(Json.toJson(groups));
        }catch (Exception e){
            return GlobalResult.badRequest(e);
        }
    }


    // TODO
    @Security.Authenticated(Secured.class)
    public Result createGroup(){
        try {

            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");

            GroupWithPermissions group = new GroupWithPermissions();
            group.groupName = json.get("groupName").asText();
            group.description = json.get("description").asText();

            List<String> exceptionsList = new ArrayList<>();

            for (final JsonNode objNode : json.get("personsID")) {
                Person person = Person.find.byId(objNode.asText());

                if(person == null) {
                    exceptionsList.add( "Person with " + objNode.asText() + "not exist" );
                    continue;
                }
                group.members.add(person);
            }

            for (final JsonNode objNode : json.get("permissions")) {

                PermissionKey permissionKey = PermissionKey.find.byId(objNode.asText());

                if(permissionKey == null) {
                    exceptionsList.add("Permission " + objNode.asText() + " not exist in Tyrion system");
                    continue;
                }
                group.permission.add(permissionKey);
            }

            group.save();


            ObjectNode result = Json.newObject();
            result.put("groupId", group.groupID);
            result.replace("exception", Json.toJson(exceptionsList));

            return GlobalResult.okResult(result);
        }catch (Exception e){
            return GlobalResult.badRequest(e);
        }
    }

    // TODO
    @Security.Authenticated(Secured.class)
    public Result getAllPersonPermission(){
        return null;
    }

    // TODO
    @Security.Authenticated(Secured.class)
    public Result removeAllPersonPermission(){
        return null;
    }

    // TODO
    @Security.Authenticated(Secured.class)
    public Result addAllPersonPermission(){
        return null;
    }






}
