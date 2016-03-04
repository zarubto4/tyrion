package utilities;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import controllers.CompilationLibrariesController;
import controllers.PermissionController;
import controllers.SecurityController;
import models.persons.PersonPermission;
import play.Configuration;
import play.Logger;
import utilities.permission.DynamicResourceHandler;
import utilities.permission.PermissionException;
import utilities.webSocket.ClientThreadChecker;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Server {

    public static CloudStorageAccount storageAccount;
    public static CloudBlobClient blobClient;
    public static String serverAddress;
    public static Map<String, Optional<DynamicResourceHandler> > handlers = new HashMap<>();


    public static void set_Server() throws Exception{

        /**
         * 1)
         * Nastavení, která globální proměná se bude používat. Vychází z application.conf podle toho zda je Server.production
         * Server Address = například http://localhost:9000/ pokud vývojář vyvíjí systém u sebe na počítači a potřebuje si ho testovat
         * skrze PostMan, nebo http://tyrion.byzance.cz
         *
         * Předpokládá se, že v rámci výpočetních úspor by bylo vhodnější mít pevné řetězce v objektech to jest nahradit
         * --- >@JsonProperty public String  versions()  { return Server.serverAddress + "/project/blocko_block/versions/"  + this.id;}
         * --- >@JsonProperty public String  versions()  { return "http//www.byzance.cz/project/blocko_block/versions/"  + this.id;}
         *
         * Zatím, se zdá vhodnější varianta přepínání v configuračním souboru. Tomáš Záruba 15.2.16
         */
        if( Configuration.root().getBoolean("Server.developerMode"))   serverAddress = Configuration.root().getString("Server.localhost");
        else                                                           serverAddress = Configuration.root().getString("Server.production");

        /**
         * 2)
         * Nastavení Azure připojení
         * jelikož v době vývoje nebylo možné realizovat různá připojení, bylo nutné zajistit pouze jedno připojení v počátku
         */
        String azureConnection = Configuration.root().getString("Azure.azureConnectionSecret");
        storageAccount = CloudStorageAccount.parse(azureConnection);
        blobClient = storageAccount.createCloudBlobClient();

    }

    public static void set_Blocko_Server_Connection(){

        if (Configuration.root().getBoolean("Servers.blocko.server1.run")) {

            Logger.warn("Starting Main Thread for Blocko Server1 ");

            ClientThreadChecker clientThreadChecker = new ClientThreadChecker()
                    .setIDentificator(Configuration.root().getString("Servers.blocko.server1.name"))
                    .setPeriodReconnectionTime(Configuration.root().getInt("Servers.blocko.server1.periodicTime"))
                    .setReconnection(true)
                    .setServerAddress(Configuration.root().getString("Servers.blocko.server1.url"))
                    .connectToServer();
        }

    }

    /**
     * Metoda slouží k zavolání hlavníchm neměnných metod v controllerech,
     * kde se evidují přístupové klíče jednotlivých metod controlleru.
     *
     * Každý controller by měl mít svůj seznam oprávnění.
     * @throws Exception
     */
    public static void setPermission() throws Exception{

        CompilationLibrariesController.set_System_Permission();
        PermissionController.set_System_Permission();

    }


    private static final DynamicResourceHandler denied_permission = s -> {
        System.out.println("Mapa neobsahuje dinamický klíč!!!");
        // TODO zalogování problému
        return false;
    };


    public static boolean check_dynamic(String name) throws PermissionException {
        if(handlers.containsKey(name)) return handlers.get(name).get().check_dynamic(name);
        else return denied_permission.check_dynamic(name);
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





}
