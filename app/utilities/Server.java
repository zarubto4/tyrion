package utilities;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.google.inject.Inject;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import models.blocko.Model_BlockoBlock;
import models.blocko.Model_BlockoBlockVersion;
import models.blocko.Model_TypeOfBlock;
import models.compiler.*;
import models.grid.Model_GridWidget;
import models.grid.Model_GridWidgetVersion;
import models.grid.Model_TypeOfWidget;
import models.overflow.*;
import models.person.Model_FloatingPersonToken;
import models.person.Model_Permission;
import models.person.Model_Person;
import models.person.Model_SecurityRole;
import models.project.b_program.Model_BProgram;
import models.project.b_program.servers.Model_HomerServer;
import models.project.c_program.Model_CProgram;
import models.project.global.Model_Product;
import models.project.global.Model_Project;
import models.project.m_program.Model_MProgram;
import models.project.m_program.Model_MProject;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.LoggerFactory;
import play.Configuration;
import play.Play;
import utilities.cache.Server_Cache;
import utilities.hardware_updater.Master_Updater;
import utilities.notifications.Notification_Handler;
import utilities.scheduler.CustomScheduler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Server {



    public static CloudStorageAccount storageAccount;
    public static CloudBlobClient blobClient;
    public static String azureLink;
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

    public static String server_mode;
    public static String server_version;

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


    static play.Logger.ALogger logger = play.Logger.of("Start-Procedures");

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

        server_mode = Configuration.root().getString("Server.mode");
        server_version = Configuration.root().getString("api.version");


        if(server_mode.equals("developer")) {

            // Nastavení pro Tyrion Adresy
            tyrion_serverAddress = "http://" + Configuration.root().getString("Server.localhost");
            tyrion_webSocketAddress ="ws://" + Configuration.root().getString("Server.localhost");

            // Nastavení pro Becki Adresy
            becki_mainUrl                       = "http://" + Configuration.root().getString("Becki.localhost.mainUrl");
            becki_redirectOk                    = Configuration.root().getString("Becki.redirectOk");
            becki_redirectFail                  = Configuration.root().getString("Becki.redirectFail");
            becki_accountAuthorizedSuccessful   = Configuration.root().getString("Becki.accountAuthorizedSuccessful");
            becki_accountAuthorizedFailed       = Configuration.root().getString("Becki.accountAuthorizedFailed");
            becki_passwordReset                 = Configuration.root().getString("Becki.passwordReset");
            becki_invitationToCollaborate       = Configuration.root().getString("Becki.invitationToCollaborate");
            becki_propertyChangeFailed          = Configuration.root().getString("Becki.propertyChangeFailed");

            GitHub_callBack                     = tyrion_serverAddress + Configuration.root().getString("GitHub.localhost.callBack");
            GitHub_clientSecret                 = Configuration.root().getString("GitHub.localhost.clientSecret");
            GitHub_url                          = Configuration.root().getString("GitHub.localhost.url");
            GitHub_apiKey                       = Configuration.root().getString("GitHub.localhost.apiKey  ");

            Facebook_callBack                   = tyrion_serverAddress + Configuration.root().getString("Facebook.localhost.callBack");
            Facebook_clientSecret               = Configuration.root().getString("Facebook.localhost.clientSecret");
            Facebook_url                        = Configuration.root().getString("Facebook.localhost.url");
            Facebook_apiKey                     = Configuration.root().getString("Facebook.localhost.apiKey  ");

            WordPress_callBack                   = tyrion_serverAddress + Configuration.root().getString("WordPress.localhost.callBack");
            WordPress_clientSecret               = Configuration.root().getString("WordPress.localhost.clientSecret");
            WordPress_url                        = Configuration.root().getString("WordPress.localhost.url");
            WordPress_apiKey                     = Configuration.root().getString("WordPress.localhost.apiKey");

            Fakturoid_apiKey                     = Configuration.root().getString("Fakturoid.apiKey");
            Fakturoid_url                        = Configuration.root().getString("Fakturoid.url");
            Fakturoid_user_agent                 = Configuration.root().getString("Fakturoid.userAgent");
            Fakturoid_secret_combo               = Configuration.root().getString("Fakturoid.secret_combo");


            GoPay_api_url                        = Configuration.root().getString("GOPay.localhost.api_url");
            GoPay_client_id                      = Configuration.root().getString("GOPay.localhost.client_id");
            GoPay_client_secret                  = Configuration.root().getString("GOPay.localhost.client_secret");
            GoPay_go_id                          = Configuration.root().getLong("GOPay.localhost.go_id");

            GoPay_return_url                     = Configuration.root().getString("GOPay.localhost.return_url");
            GoPay_notification_url               = Configuration.root().getString("GOPay.localhost.notification_url");

            azureLink                            = Configuration.root().getString("Azure.developer.azureLink");

            link_api_swagger                     = "http://swagger.byzance.cz/?url="+ tyrion_serverAddress +"/api-docs";

    } else if (server_mode.equals("production")) {

            // Nastavení pro Tyrion Adresy
            tyrion_serverAddress = "http://" + Configuration.root().getString("Server.production");
            tyrion_webSocketAddress = "ws://" + Configuration.root().getString("Server.production");

            // Nastavení pro Becki Adresy
            becki_mainUrl                       = "http://" + Configuration.root().getString("Becki.production.mainUrl");
            becki_redirectOk                    = Configuration.root().getString("Becki.redirectOk");
            becki_redirectFail                  = Configuration.root().getString("Becki.redirectFail");
            becki_accountAuthorizedSuccessful   = Configuration.root().getString("Becki.accountAuthorizedSuccessful");
            becki_accountAuthorizedFailed       = Configuration.root().getString("Becki.accountAuthorizedFailed");
            becki_passwordReset                 = Configuration.root().getString("Becki.passwordReset ");
            becki_invitationToCollaborate       = Configuration.root().getString("Becki.invitationToCollaborate");
            becki_propertyChangeFailed          = Configuration.root().getString("Becki.propertyChangeFailed");

            GitHub_callBack                     = tyrion_serverAddress + Configuration.root().getString("GitHub.production.callBack");
            GitHub_clientSecret                 = Configuration.root().getString("GitHub.production.clientSecret");
            GitHub_url                          = Configuration.root().getString("GitHub.production.url");
            GitHub_apiKey                       = Configuration.root().getString("GitHub.production.apiKey  ");

            Facebook_callBack                   = tyrion_serverAddress + Configuration.root().getString("Facebook.production.callBack");
            Facebook_clientSecret               = Configuration.root().getString("Facebook.production.clientSecret");
            Facebook_url                        = Configuration.root().getString("Facebook.production.url");
            Facebook_apiKey                     = Configuration.root().getString("Facebook.production.apiKey  ");

            WordPress_callBack                   = tyrion_serverAddress + Configuration.root().getString("WordPress.production.callBack");
            WordPress_clientSecret               = Configuration.root().getString("WordPress.production.clientSecret");
            WordPress_url                        = Configuration.root().getString("WordPress.production.url");
            WordPress_apiKey                     = Configuration.root().getString("WordPress.production.apiKey");

            Fakturoid_apiKey                     = Configuration.root().getString("Fakturoid.apiKey");
            Fakturoid_url                        = Configuration.root().getString("Fakturoid.url");
            Fakturoid_user_agent                 = Configuration.root().getString("Fakturoid.userAgent");
            Fakturoid_secret_combo               = Configuration.root().getString("Fakturoid.secret_combo");


            GoPay_api_url                        = Configuration.root().getString("GOPay.production.api_url");
            GoPay_client_id                      = Configuration.root().getString("GOPay.production.client_id");
            GoPay_client_secret                  = Configuration.root().getString("GOPay.production.client_secret");
            GoPay_go_id                          = Configuration.root().getLong("GOPay.production.go_id");

            GoPay_return_url                     = Configuration.root().getString("GOPay.production.return_url");
            GoPay_notification_url               = Configuration.root().getString("GOPay.production.notification_url");

            azureLink                            = Configuration.root().getString("Azure.production.azureLink");

            link_api_swagger                     = "http://swagger.byzance.cz/?url=" + tyrion_serverAddress + "/api-docs";

        } else if (server_mode.equals("stage")) {

                // Nastavení pro Tyrion Adresy
                tyrion_serverAddress = "http://" +  Configuration.root().getString("Server.stage");
                tyrion_webSocketAddress = "ws://" + Configuration.root().getString("Server.stage");

                // Nastavení pro Becki Adresy
                becki_mainUrl                       = "http://" + Configuration.root().getString("Becki.stage.mainUrl");
                becki_redirectOk                    = Configuration.root().getString("Becki.redirectOk");
                becki_redirectFail                  = Configuration.root().getString("Becki.redirectFail");
                becki_accountAuthorizedSuccessful   = Configuration.root().getString("Becki.accountAuthorizedSuccessful");
                becki_accountAuthorizedFailed       = Configuration.root().getString("Becki.accountAuthorizedFailed");
                becki_passwordReset                 = Configuration.root().getString("Becki.passwordReset ");
                becki_invitationToCollaborate       = Configuration.root().getString("Becki.invitationToCollaborate");
                becki_propertyChangeFailed          = Configuration.root().getString("Becki.propertyChangeFailed");

                GitHub_callBack                     = tyrion_serverAddress + Configuration.root().getString("GitHub.localhost.callBack");
                GitHub_clientSecret                 = Configuration.root().getString("GitHub.stage.clientSecret");
                GitHub_url                          = Configuration.root().getString("GitHub.stage.url");
                GitHub_apiKey                       = Configuration.root().getString("GitHub.stage.apiKey  ");

                Facebook_callBack                   = tyrion_serverAddress + Configuration.root().getString("Facebook.localhost.callBack");
                Facebook_clientSecret               = Configuration.root().getString("Facebook.stage.clientSecret");
                Facebook_url                        = Configuration.root().getString("Facebook.stage.url");
                Facebook_apiKey                     = Configuration.root().getString("Facebook.stage.apiKey  ");

                WordPress_callBack                   = tyrion_serverAddress + Configuration.root().getString("WordPress.localhost.callBack");
                WordPress_clientSecret               = Configuration.root().getString("WordPress.localhost.clientSecret");
                WordPress_url                        = Configuration.root().getString("WordPress.localhost.url");
                WordPress_apiKey                     = Configuration.root().getString("WordPress.localhost.apiKey");

                Fakturoid_apiKey                     = Configuration.root().getString("Fakturoid.apiKey");
                Fakturoid_url                        = Configuration.root().getString("Fakturoid.url");
                Fakturoid_user_agent                 = Configuration.root().getString("Fakturoid.userAgent");
                Fakturoid_secret_combo               = Configuration.root().getString("Fakturoid.secret_combo");


                GoPay_api_url                        = Configuration.root().getString("GOPay.localhost.api_url");
                GoPay_client_id                      = Configuration.root().getString("GOPay.localhost.client_id");
                GoPay_client_secret                  = Configuration.root().getString("GOPay.localhost.client_secret");
                GoPay_go_id                          = Configuration.root().getLong("GOPay.localhost.go_id");

                GoPay_return_url                     = Configuration.root().getString("GOPay.localhost.return_url");
                GoPay_notification_url               = Configuration.root().getString("GOPay.localhost.notification_url");

                azureLink                            = Configuration.root().getString("Azure.localhost.azureLink");

                link_api_swagger                     = "http://swagger.byzance.cz/?url="+ tyrion_serverAddress +"/api-docs";
            }

        /**
         * 2)
         * Nastavení Azure připojení
         * jelikož v době vývoje nebylo možné realizovat různá připojení, bylo nutné zajistit pouze jedno připojení v počátku
         */
        String azureConnection;
        if(server_mode.equals("developer")||server_mode.equals("stage")) azureConnection = Configuration.root().getString("Azure.developer.azureConnectionSecret");
        else                                                             azureConnection = Configuration.root().getString("Azure.production.azureConnectionSecret");

        storageAccount = CloudStorageAccount.parse(azureConnection);
        blobClient = storageAccount.createCloudBlobClient();

    }

    /**
     * Nastavení Administrátora vždy na startu pokud neexistuje!!!
     */
    public static void set_Developer_objects(){

        // For Developing
        if(Model_SecurityRole.findByName("SuperAdmin") == null){
            Model_SecurityRole role = new Model_SecurityRole();
            role.person_permissions.addAll(Model_Permission.find.all());
            role.name = "SuperAdmin";
            role.save();
        }

        if (Model_Person.find.where().eq("mail", "admin@byzance.cz").findUnique() == null)
        {
            logger.warn("Creating first admin account: admin@byzance.cz, password: 123456789, token: token2");
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
    public static void set_Logback() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            if (!Configuration.root().getString("Server.mode").equals("production")) {
                configurator.doConfigure(Play.application().getFile(Play.application().configuration().getString("Logback.developerSettings")));
            }
            else {
                configurator.doConfigure(Play.application().getFile(Play.application().configuration().getString("Logback.productionSettings")));
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
        logger.info("Setting Permission");

        List<String> permissions = new ArrayList<>();

        // Models
            // Blocko
                for(Enum en : Model_BlockoBlock.permissions.values())             permissions.add(en.name());
                for(Enum en : Model_BlockoBlockVersion.permissions.values())      permissions.add(en.name());
                for(Enum en : Model_HomerServer.permissions.values())             permissions.add(en.name());
                for(Enum en : Model_TypeOfBlock.permissions.values())             permissions.add(en.name());

            // compiler
                for(Enum en : Model_Board.permissions.values())                   permissions.add(en.name());
                for(Enum en : Model_CompilationServer.permissions.values())       permissions.add(en.name());
                for(Enum en : Model_ImportLibrary.permissions.values())           permissions.add(en.name());
                for(Enum en : Model_Processor.permissions.values())               permissions.add(en.name());
                for(Enum en : Model_Producer.permissions.values())                permissions.add(en.name());
                for(Enum en : Model_TypeOfBoard.permissions.values())             permissions.add(en.name());
                for(Enum en : Model_BootLoader.permissions.values())              permissions.add(en.name());

            // overflow
                for(Enum en : Model_FloatingPersonToken.permissions.values())     permissions.add(en.name());
                for(Enum en : LinkedPost.permissions.values())                    permissions.add(en.name());
                for(Enum en : Post.permissions.values())                          permissions.add(en.name());
                for(Enum en : PropertyOfPost.permissions.values())                permissions.add(en.name());
                for(Enum en : TypeOfConfirms.permissions.values())                permissions.add(en.name());
                for(Enum en : TypeOfPost.permissions.values())                    permissions.add(en.name());
            // person
                for(Enum en : Model_FloatingPersonToken.permissions.values())     permissions.add(en.name());
                for(Enum en : Model_Person.permissions.values())                  permissions.add(en.name());
                for(Enum en : Model_SecurityRole.permissions.values())            permissions.add(en.name());
                for(Enum en : Model_Permission.permissions.values())              permissions.add(en.name());
            //grid
                for(Enum en : Model_GridWidget.permissions.values())              permissions.add(en.name());
                for(Enum en : Model_GridWidgetVersion.permissions.values())       permissions.add(en.name());
                for(Enum en : Model_TypeOfWidget.permissions.values())            permissions.add(en.name());

            // project
                // b_program
                    for(Enum en : Model_BProgram.permissions.values())            permissions.add(en.name());
                // c_program
                    for(Enum en : Model_CProgram.permissions.values())            permissions.add(en.name());
                // global
                    for(Enum en : Model_Project.permissions.values())             permissions.add(en.name());
                    for(Enum en : Model_Product.permissions.values())             permissions.add(en.name());
                // m_project
                    for(Enum en : Model_MProject.permissions.values())            permissions.add(en.name());
                    for(Enum en : Model_MProgram.permissions.values())            permissions.add(en.name());


        logger.info("Number of Static Permissions " + permissions.size() );

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
        Master_Updater.start_thread_box();

        //1. Nastartovat notifikační vlákno
        Notification_Handler.start_notification_thread();

    }

    public static void startScheduling_procedures() {
        try {

            CustomScheduler.startScheduler();

        }catch (Exception e){
           logger.error("Scheduler_Exception", e);
        }

    }

    public static void init_cache() {
        try {

            Server_Cache.initCache();

        }catch (Exception e){
            logger.error("Cache_Exception", e);
        }

    }
}
