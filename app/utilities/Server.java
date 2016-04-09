package utilities;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import controllers.*;
import models.persons.PersonPermission;
import org.slf4j.LoggerFactory;
import play.Configuration;
import play.Logger;
import play.Play;
import utilities.loggy.Loggy;
import utilities.permission.DynamicResourceHandler;
import utilities.permission.PermissionException;
import utilities.webSocket.ClientThreadChecker;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Server {

    public static CloudStorageAccount storageAccount;
    public static CloudBlobClient blobClient;
    public static String tyrion_serverAddress;
    public static String tyrion_webSocketAddress;

    public static String becki_mainUrl;
    public static String becki_redirectOk;
    public static String becki_redirectFail;
    public static String becki_accountAuthorizedSuccessful;
    public static String becki_accountAuthorizedFailed;
    public static String becki_passwordReset;

    public static String GitHub_callBack;
    public static String GitHub_clientSecret;
    public static String GitHub_url;
    public static String GitHub_apiKey;

    public static String Facebook_callBack;
    public static String Facebook_clientSecret;
    public static String Facebook_url;
    public static String Facebook_apiKey;

    public static Boolean server_mode;
    public static String server_version;

    public static Map<String, Optional<DynamicResourceHandler> > handlers = new HashMap<>();


    public static void set_Server_address() throws Exception{

        /**
         * 1)
         * Nastavení, která globální proměná se bude používat. Vychází z application.conf podle toho zda je Server.production
         * Server Address = například http://localhost:9000/ pokud vývojář vyvíjí systém u sebe na počítači a potřebuje si ho testovat
         * skrze PostMan, nebo http://tyrion.byzance.cz
         *
         * Předpokládá se, že v rámci výpočetních úspor by bylo vhodnější mít pevné řetězce v objektech to jest nahradit
         * --- >@JsonProperty public String  versions()  { return Server.tyrion_serverAddress + "/project/blocko_block/versions/"  + this.id;}
         * --- >@JsonProperty public String  versions()  { return "http//www.byzance.cz/project/blocko_block/versions/"  + this.id;}
         *
         * Zatím se zdá vhodnější varianta přepínání v configuračním souboru. Tomáš Záruba 15.2.16
         */

        server_mode = Configuration.root().getBoolean("Server.developerMode");
        server_version = Configuration.root().getString("api.version");


        if(server_mode) {

            // Nastavení pro Tyrion Adresy
            tyrion_serverAddress = "http://" + Configuration.root().getString("Server.localhost");
            tyrion_webSocketAddress ="ws://" + Configuration.root().getString("Server.localhost");

            // Nastavení pro Becki Adresy
            becki_mainUrl                       = "http://" + Configuration.root().getString("Becki.localhost.mainUrl");
            becki_redirectOk                    = "http://" + Configuration.root().getString("Becki.localhost.redirectOk");
            becki_redirectFail                  = "http://" + Configuration.root().getString("Becki.localhost.redirectFail");
            becki_accountAuthorizedSuccessful   = "http://" + Configuration.root().getString("Becki.localhost.accountAuthorizedSuccessful");
            becki_accountAuthorizedFailed       = "http://" + Configuration.root().getString("Becki.localhost.accountAuthorizedFailed");
            becki_passwordReset                 = "http://" + Configuration.root().getString("Becki.localhost.passwordReset ");

            GitHub_callBack                     = tyrion_serverAddress + Configuration.root().getString("GitHub.localhost.callBack");
            GitHub_clientSecret                 = Configuration.root().getString("GitHub.localhost.clientSecret");
            GitHub_url                          = Configuration.root().getString("GitHub.localhost.url");
            GitHub_apiKey                       = Configuration.root().getString("GitHub.localhost.apiKey  ");

            Facebook_callBack                   = tyrion_serverAddress + Configuration.root().getString("Facebook.localhost.callBack");
            Facebook_clientSecret               = Configuration.root().getString("Facebook.localhost.clientSecret");
            Facebook_url                        = Configuration.root().getString("Facebook.localhost.url");
            Facebook_apiKey                     = Configuration.root().getString("Facebook.localhost.apiKey  ");

        }
        else   {

            // Nastavení pro Tyrion Adresy
            tyrion_serverAddress = "http://" +  Configuration.root().getString("Server.production");
            tyrion_webSocketAddress = "ws://" + Configuration.root().getString("Server.production");

            // Nastavení pro Becki Adresy
            becki_mainUrl                       = "http://" + Configuration.root().getString("Becki.production.mainUrl");
            becki_redirectOk                    = "http://" + Configuration.root().getString("Becki.production.redirectOk");
            becki_redirectFail                  = "http://" + Configuration.root().getString("Becki.production.redirectFail");
            becki_accountAuthorizedSuccessful   = "http://" + Configuration.root().getString("Becki.production.accountAuthorizedSuccessful");
            becki_accountAuthorizedFailed       = "http://" + Configuration.root().getString("Becki.production.accountAuthorizedFailed");
            becki_passwordReset                 = "http://" + Configuration.root().getString("Becki.production.passwordReset ");


            GitHub_callBack                     = tyrion_serverAddress + Configuration.root().getString("GitHub.production.callBack");
            GitHub_clientSecret                 = Configuration.root().getString("GitHub.production.clientSecret");
            GitHub_url                          = Configuration.root().getString("GitHub.production.url");
            GitHub_apiKey                       = Configuration.root().getString("GitHub.production.apiKey  ");

            Facebook_callBack                   = tyrion_serverAddress + Configuration.root().getString("Facebook.production.callBack");
            Facebook_clientSecret               = Configuration.root().getString("Facebook.production.clientSecret");
            Facebook_url                        = Configuration.root().getString("Facebook.production.url");
            Facebook_apiKey                     = Configuration.root().getString("Facebook.production.apiKey  ");


        }

        /**
         * 2)
         * Nastavení Azure připojení
         * jelikož v době vývoje nebylo možné realizovat různá připojení, bylo nutné zajistit pouze jedno připojení v počátku
         */
        String azureConnection;
        if( Configuration.root().getBoolean("Server.developerMode"))   azureConnection = Configuration.root().getString("Azure.developer.azureConnectionSecret");
        else                                                           azureConnection = Configuration.root().getString("Azure.production.azureConnectionSecret");

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
     * Výběr nastavení Logbacku podle Server.developerMode
     */
    public static void set_Logback() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            if (Play.application().configuration().getBoolean("Server.developerMode")) {
                configurator.doConfigure(Play.application().getFile(Play.application().configuration().getString("Logback.developerSettings")));
            }
            else {
                configurator.doConfigure(Play.application().getFile(Play.application().configuration().getString("Logback.productionSettings")));
            }
        } catch (JoranException je) {}
    }

    /**
     * Nastavení Loggyho
     */
    public static void set_Loggy() {
        Loggy.start();
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
        GridController.set_System_Permission();
        OverFlowController.set_System_Permission();
        PersonController.set_System_Permission();
        ProgramingPackageController.set_System_Permission();
        SecurityController.set_System_Permission();

    }


    private static final DynamicResourceHandler denied_permission = s -> {
        System.out.println("Mapa neobsahuje dynamický klíč!!!");
        // TODO zalogování problému
        throw new PermissionException();
    };


    public static boolean check_dynamic(String name) throws PermissionException {
        if(handlers.containsKey(name)) return handlers.get(name).get().check_dynamic(name);
        else return denied_permission.check_dynamic(name);
    }

    public static boolean check_permission(String... args) throws PermissionException {
        try {

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
            ).findList().size() < 1) throw new PermissionException();


            return true;

        } catch (Exception e) { throw new PermissionException();}
    }

    public static boolean check_dynamic_OR_permission(String value, String... args) throws PermissionException{
        try{

            System.out.println("Kontroluji permission");

            if(check_permission(args)) return true;



        }catch (PermissionException e){
            System.out.println("permission selhalo - Kontroluji dynamic");
            if (check_dynamic(value)) return true;
        }

        throw new PermissionException();
    }


    public static void setDirectory() {

        File file = new File("files");
        if (!file.exists()) {
            if (file.mkdir())  Logger.warn("Directory \"file\" is created!");
        }
    }
}
