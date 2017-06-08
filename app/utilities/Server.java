package utilities;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.microsoft.azure.documentdb.*;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import models.*;
import org.slf4j.LoggerFactory;
import play.Configuration;
import play.Play;
import utilities.cache.Server_Cache;
import utilities.financial.fakturoid.Fakturoid_InvoiceCheck;
import utilities.financial.goPay.GoPay_PaymentCheck;
import utilities.enums.Enum_Tyrion_Server_mode;
import utilities.grid_support.utils.IP_Founder;
import utilities.hardware_updater.Utilities_HW_Updater_Master_thread_updater;
import utilities.logger.Class_Logger;
import utilities.notifications.NotificationHandler;
import utilities.scheduler.CustomScheduler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Server {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Server.class);

/* SERVER COMMON STATIC VALUE  -----------------------------------------------------------------------------------------------------*/

    // Azure - Blob
    public static CloudStorageAccount storageAccount;
    public static CloudBlobClient blobClient;
    public static String azure_blob_Link;

    // Azure - NoSQL Database
    public static DocumentClient documentClient;
    public static Database no_sql_database;
    public static String documentDB_Path;

    public static DocumentCollection online_status_collection = null;

    public static String tyrion_serverAddress;
    public static String tyrion_webSocketAddress;

    public static String becki_mainUrl;
    public static String becki_redirectOk;
    public static String becki_redirectFail;
    public static String becki_accountAuthorizedSuccessful;
    public static String becki_accountAuthorizedFailed;
    public static String becki_passwordReset;
    public static String becki_invitationToCollaborate;
    public static String becki_propertyChangeFailed;

    public static String grid_app_main_url;


    public static String GitHub_callBack;
    public static String GitHub_clientSecret;
    public static String GitHub_url;
    public static String GitHub_apiKey;

    public static String Facebook_callBack;
    public static String Facebook_clientSecret;
    public static String Facebook_url;
    public static String Facebook_apiKey;

    public static String WordPress_callBack;
    public static String WordPress_clientSecret;
    public static String WordPress_url;
    public static String WordPress_apiKey;

    //-------------------------------------------------------------------

    public static Enum_Tyrion_Server_mode server_mode;

    public static String server_version;

    //-------------------------------------------------------------------

    public static Integer financial_spendDailyPeriod;

    //-------------------------------------------------------------------

    public static String Fakturoid_apiKey;
    public static String Fakturoid_url;
    public static String Fakturoid_user_agent;
    public static String Fakturoid_secret_combo;

    //-------------------------------------------------------------------

    public static String GoPay_api_url;
    public static String GoPay_client_id;

    public static String GoPay_client_secret;
    public static Long   GoPay_go_id;
    public static String GoPay_return_url;
    public static String GoPay_notification_url;

    public static String  link_api_swagger;

    public static String slack_webhook_url;

    public static void setServerValues() throws Exception{

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

        String server_mode_string = Configuration.root().getString("Server.mode");
        server_version = Configuration.root().getString("api.version");

        switch (server_mode_string) {
            case "developer" : {

                server_mode = Enum_Tyrion_Server_mode.developer;

                // Nastavení pro Tyrion Adresy
                tyrion_serverAddress = "http://" + Configuration.root().getString("Server." + server_mode);
                tyrion_webSocketAddress = "ws://" + Configuration.root().getString("Server." + server_mode);

                // Nastavení pro Becki Adresy
                becki_mainUrl           = "http://" + Configuration.root().getString("Becki." + server_mode + ".mainUrl");

                // Nastavení adresy, kde běží Grid APP
                grid_app_main_url       = "http://" + IP_Founder.getLocalHostLANAddress().getHostAddress() + ":8888";

                // Swagger URL Redirect - Actual Rest Api docu
                link_api_swagger        = "http://swagger.byzance.cz/?url=" + tyrion_serverAddress + "/api-docs";

                financial_spendDailyPeriod = checkPeriod(Configuration.root().getInt("Financial.developer.spendDailyPeriod"));

                break;
            }
            case "production" : {

                server_mode = Enum_Tyrion_Server_mode.production;
                // Nastavení pro Tyrion Adresy
                tyrion_serverAddress = "https://" + Configuration.root().getString("Server." + server_mode);
                tyrion_webSocketAddress = "wss://" + Configuration.root().getString("Server." + server_mode);

                // Nastavení pro Becki Adresy
                becki_mainUrl                   = "https://" + Configuration.root().getString("Becki." + server_mode + ".mainUrl");

                // Nastavení adresy, kde běží Grid APP
                grid_app_main_url               = "https://" + Configuration.root().getString("Grid_App." + server_mode + ".mainUrl");

                // Swagger URL Redirect - Actual Rest Api docu
                link_api_swagger    = "https://swagger.byzance.cz/?url=" + tyrion_serverAddress + "/api-docs";

                financial_spendDailyPeriod = checkPeriod(Configuration.root().getInt("Financial.production.spendDailyPeriod"));

                break;
            }
            case "stage" : {

                server_mode = Enum_Tyrion_Server_mode.stage;

                // Nastavení pro Tyrion Adresy
                tyrion_serverAddress        = "https://" + Configuration.root().getString("Server." + server_mode);
                tyrion_webSocketAddress     = "wss://" + Configuration.root().getString("Server." + server_mode);

                // Nastavení pro Becki Adresy
                becki_mainUrl               = "https://" + Configuration.root().getString("Becki." + server_mode + ".mainUrl");

                // Nastavení adresy, kde běží Grid APP
                grid_app_main_url           = "https://" + Configuration.root().getString("Grid_App." + server_mode + ".mainUrl");

                // Swagger URL Redirect - Actual Rest Api docu
                link_api_swagger            = "https://swagger.byzance.cz/?url=" + tyrion_serverAddress + "/api-docs";

                financial_spendDailyPeriod = checkPeriod(Configuration.root().getInt("Financial.stage.spendDailyPeriod"));

                break;
            }
            default: {
                System.err.println("Server mode is null or unknown - System will shut down immediately");
                Runtime.getRuntime().exit(10);
            }
        }

        //  Becki Config -------------------------------------------------------------------------------------------------------------

        becki_redirectOk                    = Configuration.root().getString("Becki.redirectOk");
        becki_redirectFail                  = Configuration.root().getString("Becki.redirectFail");
        becki_accountAuthorizedSuccessful   = Configuration.root().getString("Becki.accountAuthorizedSuccessful");
        becki_accountAuthorizedFailed       = Configuration.root().getString("Becki.accountAuthorizedFailed");
        becki_passwordReset                 = Configuration.root().getString("Becki.passwordReset ");
        becki_invitationToCollaborate       = Configuration.root().getString("Becki.invitationToCollaborate");
        becki_propertyChangeFailed          = Configuration.root().getString("Becki.propertyChangeFailed");

        //  Facturoid Config -------------------------------------------------------------------------------------------------------------

        Fakturoid_apiKey        = Configuration.root().getString("Fakturoid." + server_mode.name()  +".apiKey");
        Fakturoid_url           = Configuration.root().getString("Fakturoid." + server_mode.name()  +".url");
        Fakturoid_user_agent    = Configuration.root().getString("Fakturoid." + server_mode.name()  +".userAgent");
        Fakturoid_secret_combo  = Configuration.root().getString("Fakturoid." + server_mode.name()  +".secret_combo");

        //  GitHub Config -------------------------------------------------------------------------------------------------------------

        GitHub_callBack         = tyrion_serverAddress + Configuration.root().getString("GitHub." + server_mode.name() +".callBack");
        GitHub_clientSecret     = Configuration.root().getString("GitHub." + server_mode.name() +".clientSecret");
        GitHub_url              = Configuration.root().getString("GitHub." + server_mode.name() +".url");
        GitHub_apiKey           = Configuration.root().getString("GitHub." + server_mode.name() +".apiKey  ");

        // FaceBook Config -------------------------------------------------------------------------------------------------------------

        Facebook_callBack       = tyrion_serverAddress + Configuration.root().getString("Facebook." + server_mode.name() +".callBack");
        Facebook_clientSecret   = Configuration.root().getString("Facebook." + server_mode.name() +".clientSecret");
        Facebook_url            = Configuration.root().getString("Facebook." + server_mode.name() +".stage.url");
        Facebook_apiKey         = Configuration.root().getString("Facebook." + server_mode.name() +".apiKey  ");

        // Go Pay Config ------------------------------------------------------------------------------------------------------------

        GoPay_api_url           = Configuration.root().getString("GOPay."+ server_mode.name() +".api_url");
        GoPay_client_id         = Configuration.root().getString("GOPay."+ server_mode.name() +".client_id");
        GoPay_client_secret     = Configuration.root().getString("GOPay."+ server_mode.name() +".client_secret");
        GoPay_go_id             = Configuration.root().getLong(  "GOPay."+ server_mode.name() +".go_id");

        GoPay_return_url        = Configuration.root().getString("GOPay."+ server_mode.name() +".return_url");
        GoPay_notification_url  = Configuration.root().getString("GOPay."+ server_mode.name() +".notification_url");

        // Azure Config ------------------------------------------------------------------------------------------------------------

        azure_blob_Link             = Configuration.root().getString("Azure.blob."+ server_mode.name() +".azureLink");
        storageAccount              = CloudStorageAccount.parse( Configuration.root().getString("Azure.blob." + server_mode.name() +".azureConnectionSecret"));
        blobClient                  = storageAccount.createCloudBlobClient();

        documentClient              = new DocumentClient( Configuration.root().getString("Azure.documentDB." + server_mode.name() + ".azureLink"), Configuration.root().getString("Azure.documentDB." + server_mode.name() + ".azureConnectionSecret") , ConnectionPolicy.GetDefault(), ConsistencyLevel.Session);
        no_sql_database             = new Database();
        no_sql_database.setId( Configuration.root().getString("Azure.documentDB." + server_mode.name() + ".databaseName"));
        documentDB_Path = "dbs/" + no_sql_database.getId();

        link_api_swagger    = "http://swagger.byzance.cz/?url=" + tyrion_serverAddress + "/api-docs";

        slack_webhook_url = Configuration.root().getString("Slack.webhook_url");
    }

    /**
     * Nastavení Administrátora vždy na startu pokud neexistuje!!!
     */
    public static void setAdministrator(){

        // For Developing
        if(Model_SecurityRole.findByName("SuperAdmin") == null){
            Model_SecurityRole role = new Model_SecurityRole();
            role.person_permissions.addAll(Model_Permission.find.all());
            role.name = "SuperAdmin";
            role.save();
        }

        if (Model_Person.find.where().eq("mail", "admin@byzance.cz").findUnique() == null) {

            terminal_logger.warn("setAdministrator: Creating first admin account: admin@byzance.cz, password: 123456789, token: token2");

            Model_Person person = new Model_Person();
            person.full_name = "Admin Byzance";
            person.mailValidated = true;
            person.nick_name = "Syndibád";
            person.mail = "admin@byzance.cz";
            person.setSha("123456789");
            person.roles.add(Model_SecurityRole.findByName("SuperAdmin"));

            person.save();

            Model_FloatingPersonToken floatingPersonToken = new Model_FloatingPersonToken();
            floatingPersonToken.authToken = "token2";
            floatingPersonToken.person = person;
            floatingPersonToken.user_agent = "Unknown browser";
            floatingPersonToken.save();

        }else{

            terminal_logger.warn("setAdministrator: admin is already created");

            // updatuji oprávnění
            Model_Person person = Model_Person.find.where().eq("mail", "admin@byzance.cz").findUnique();
            List<Model_Permission> personPermissions = Model_Permission.find.all();

            for(Model_Permission personPermission :  personPermissions) if(!person.person_permissions.contains(personPermission)) person.person_permissions.add(personPermission);
            person.update();
        }

    }

    /**
     * Výběr nastavení Logbacku podle Server.developerMode
     */
    public static void setLogback() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            if (!Configuration.root().getString("Server.mode").equals("production")) {
                configurator.doConfigure(Play.application().getFile(Play.application().configuration().getString("Loggy.developerSettings")));
            }
            else {
                configurator.doConfigure(Play.application().getFile(Play.application().configuration().getString("Loggy.productionSettings")));
            }
        } catch (JoranException je) {}
    }

    /**
     * Metoda slouží k zavolání hlavních neměnných metod v controllerech,
     * kde se evidují přístupové klíče jednotlivých metod controlleru.
     *
     * Každý controller by měl mít svůj seznam oprávnění.
     * @throws Exception
     */
    public static void setPermission() throws Exception{

        terminal_logger.info("setPermission: Setting Permission");

        List<String> permissions = new ArrayList<>();

        // Models
        for(Enum en : Model_TypeOfBlock.permissions.values())             permissions.add(en.name());
        for(Enum en : Model_BlockoBlock.permissions.values())             permissions.add(en.name());
        for(Enum en : Model_BlockoBlockVersion.permissions.values())      permissions.add(en.name());
        for(Enum en : Model_HomerServer.permissions.values())             permissions.add(en.name());
        for(Enum en : Model_Board.permissions.values())                   permissions.add(en.name());
        for(Enum en : Model_CompilationServer.permissions.values())       permissions.add(en.name());
        for(Enum en : Model_Library.permissions.values())           permissions.add(en.name());
        for(Enum en : Model_Processor.permissions.values())               permissions.add(en.name());
        for(Enum en : Model_Producer.permissions.values())                permissions.add(en.name());
        for(Enum en : Model_TypeOfBoard.permissions.values())             permissions.add(en.name());
        for(Enum en : Model_BootLoader.permissions.values())              permissions.add(en.name());
        for(Enum en : Model_FloatingPersonToken.permissions.values())     permissions.add(en.name());
        for(Enum en : Model_Person.permissions.values())                  permissions.add(en.name());
        for(Enum en : Model_SecurityRole.permissions.values())            permissions.add(en.name());
        for(Enum en : Model_Permission.permissions.values())              permissions.add(en.name());
        for(Enum en : Model_GridWidget.permissions.values())              permissions.add(en.name());
        for(Enum en : Model_GridWidgetVersion.permissions.values())       permissions.add(en.name());
        for(Enum en : Model_TypeOfWidget.permissions.values())            permissions.add(en.name());
        for(Enum en : Model_BProgram.permissions.values())                permissions.add(en.name());
        for(Enum en : Model_CProgram.permissions.values())                permissions.add(en.name());
        for(Enum en : Model_Project.permissions.values())                 permissions.add(en.name());
        for(Enum en : Model_Invoice.permissions.values())                 permissions.add(en.name());
        for(Enum en : Model_Product.permissions.values())                 permissions.add(en.name());
        for(Enum en : Model_ProductExtension.permissions.values())        permissions.add(en.name());
        for(Enum en : Model_MProject.permissions.values())                permissions.add(en.name());
        for(Enum en : Model_MProgram.permissions.values())                permissions.add(en.name());


        terminal_logger.info("setPermission: Number of Static Permissions " + permissions.size() );

        for(String permission : permissions) new Model_Permission(permission, "description");

    }

    public static void setDirectory() {

        File file = new File("files");
        if (!file.exists()) {
            if (file.mkdir())  play.Logger.warn("Directory \"file\" is created!");
        }
    }

    public static void startThreads() {

        //1. Nastartovat aktualizační vlákna
        Utilities_HW_Updater_Master_thread_updater.start_thread_box();

        //1. Nastartovat notifikační vlákno
        NotificationHandler.startNotificationThread();

        GoPay_PaymentCheck.startPaymentCheckThread();

        Fakturoid_InvoiceCheck.startInvoiceCheckThread();
    }

    public static void startSchedulingProcedures() {

        CustomScheduler.startScheduler();
    }

    public static void initCache() {
        try {

            Server_Cache.initCache();

        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }

    }

    /**
     * Checks whether the given spending credit period can be used.
     * @param period Given period number.
     * @return Number that can be used.
     * @throws IllegalArgumentException when the number cannot be used.
     */
    private static Integer checkPeriod(Integer period) throws IllegalArgumentException{

        List<Integer> allowable_values = new ArrayList<>(Arrays.asList(1,2,3,4,12,24,48));

        if (allowable_values.contains(period)) return period;

        throw new IllegalArgumentException("Server has wrong configuration. Check the conf/application.conf file. Property Financial.{mode}.spendDailyPeriod should contain only 1-4, 12, 24 or 48.");
    }
}