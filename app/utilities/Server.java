package utilities;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import models.blocko.BlockoBlock;
import models.blocko.BlockoBlockVersion;
import models.blocko.TypeOfBlock;
import models.compiler.*;
import models.grid.Screen_Size_Type;
import models.overflow.*;
import models.person.FloatingPersonToken;
import models.person.Person;
import models.person.PersonPermission;
import models.person.SecurityRole;
import models.project.b_program.B_Program;
import models.project.b_program.servers.Cloud_Homer_Server;
import models.project.b_program.servers.Private_Homer_Server;
import models.project.c_program.C_Program;
import models.project.global.Project;
import models.project.m_program.M_Program;
import models.project.m_program.M_Project;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.LoggerFactory;
import play.Configuration;
import play.Play;
import utilities.hardware_updater.Master_Updater;
import utilities.schedules_activities.Removing_Unused_Tokens;
import utilities.schedules_activities.Sending_Invoices;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.quartz.CronScheduleBuilder.dailyAtHourAndMinute;
import static org.quartz.DateBuilder.tomorrowAt;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

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

    public static Boolean server_mode;
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

    public static Scheduler scheduler;

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
            becki_passwordReset                 = "http://" + Configuration.root().getString("Becki.localhost.passwordReset");
            becki_invitationToCollaborate       = "http://" + Configuration.root().getString("Becki.localhost.invitationToCollaborate");
            becki_propertyChangeFailed          = "http://" + Configuration.root().getString("Becki.localhost.propertyChangeFailed");

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
            becki_invitationToCollaborate       = "http://" + Configuration.root().getString("Becki.production.invitationToCollaborate");
            becki_propertyChangeFailed          = "http://" + Configuration.root().getString("Becki.production.propertyChangeFailed");

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
                for(Enum en : BlockoBlock.permissions.values())             permissions.add(en.name());
                for(Enum en : BlockoBlockVersion.permissions.values())      permissions.add(en.name());
                for(Enum en : Cloud_Homer_Server.permissions.values())     permissions.add(en.name());
                for(Enum en : TypeOfBlock.permissions.values())             permissions.add(en.name());

            // compiler
                for(Enum en : Board.permissions.values())                   permissions.add(en.name());
                for(Enum en : Cloud_Compilation_Server.permissions.values())permissions.add(en.name());
                for(Enum en : LibraryGroup.permissions.values())            permissions.add(en.name());
                for(Enum en : Processor.permissions.values())               permissions.add(en.name());
                for(Enum en : Producer.permissions.values())                permissions.add(en.name());
                for(Enum en : SingleLibrary.permissions.values())           permissions.add(en.name());
                for(Enum en : TypeOfBoard.permissions.values())             permissions.add(en.name());

            // grid
                for(Enum en : Screen_Size_Type.permissions.values())           permissions.add(en.name());

            // overflow
                for(Enum en : FloatingPersonToken.permissions.values())     permissions.add(en.name());
                for(Enum en : LinkedPost.permissions.values())              permissions.add(en.name());
                for(Enum en : Post.permissions.values())                    permissions.add(en.name());
                for(Enum en : PropertyOfPost.permissions.values())          permissions.add(en.name());
                for(Enum en : TypeOfConfirms.permissions.values())          permissions.add(en.name());
                for(Enum en : TypeOfPost.permissions.values())              permissions.add(en.name());
            // person
                for(Enum en : FloatingPersonToken.permissions.values())     permissions.add(en.name());
                for(Enum en : Person.permissions.values())                  permissions.add(en.name());
                for(Enum en : SecurityRole.permissions.values())            permissions.add(en.name());
                for(Enum en : PersonPermission.permissions.values())        permissions.add(en.name());

            // project
                // b_program
                    for(Enum en : B_Program.permissions.values())           permissions.add(en.name());
                // c_program
                    for(Enum en : C_Program.permissions.values())           permissions.add(en.name());
                // global
                    for(Enum en : Private_Homer_Server.permissions.values())               permissions.add(en.name());
                    for(Enum en : Project.permissions.values())             permissions.add(en.name());
                // m_project
                    for(Enum en : M_Project.permissions.values())           permissions.add(en.name());
                    for(Enum en : M_Program.permissions.values())           permissions.add(en.name());


        logger.info("Number of Static Permissions " + permissions.size() );

        for(String permission : permissions) new PersonPermission(permission, "description");

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

    }

    public static void startScheduling_procedures() {
        try {

            // Nastavení schedulleru (Aktivity, která se pravidelně v časových úsecích vykonává)
            scheduler = StdSchedulerFactory.getDefaultScheduler();



            // Klíč / identifikátor Trrigru definující, kdy se konkrétní job zapne.
            TriggerKey every_day_key = TriggerKey.triggerKey("every_day_02:10");
            TriggerKey every_second_day_key = TriggerKey.triggerKey("every_second_day_2:30");

            //-------------------------

            // Mažu scheduler v operační paměti po předchozí instanci - není doporučeno mít aktivní
            // slr pomáhá v případě problémů s operační pamětí - v režimu developer  je v metodě která ukončuje server třeba při buildu procedura, která vyčistí RAM
            // scheduler.clear();


            // Definované Trigry
            if(!scheduler.checkExists(every_day_key)){

                Trigger every_day_1 = newTrigger().withIdentity(every_day_key).startNow()
                        .withSchedule(dailyAtHourAndMinute(2,10))// Spuštění každý den v 02:10 AM
                        .build();

                Trigger every_day_2 = newTrigger().withIdentity(every_day_key).startNow()
                        .withSchedule(dailyAtHourAndMinute(2,10))// Spuštění každý den v 02:10 AM
                        .build();

                // Přidání úkolů do scheduleru

                // 1) Odstraňování nepřihlášených tokenů ze sociálních sítí, kteér mají živostnost jen 24h
                scheduler.scheduleJob(  newJob(     Removing_Unused_Tokens.class    ).withIdentity( JobKey.jobKey("removing_unused_tokens") ).build(), every_day_1);

                // 2) Kontrola a fakturace klientů na měsíční bázi
                scheduler.scheduleJob(  newJob(     Sending_Invoices.class          ).withIdentity( JobKey.jobKey("sending_invoices")       ).build(), every_day_2);

                // 3) Přesouvání logů v DB do Blob Serveru a uvolňování místa v DB a na serveru



            }else {
                logger.warn("CRON (Every-Day) is in RAM yet. Be careful with that!");
            }


            if(!scheduler.checkExists(every_second_day_key)) {

                Trigger every_second_day = newTrigger()
                        .withIdentity(every_second_day_key)
                        .startAt(tomorrowAt(15, 0, 0)) // První spuštění další den v 03:30
                        .withSchedule(simpleSchedule().withIntervalInHours(2 * 24).repeatForever()) // A opakovávání každé 2 dny || .withIntervalInDays(2)
                        .build();


                // Přidávání úkolů do Schedulleru

            }else {
                logger.warn("CRON (Every-SecondDay) is in RAM yet. Be careful with that!");
            }




            // Nastartování scheduleru
            scheduler.start();

        }catch (Exception e){
           // logger.error("Scheduller_Exception", e);
        }

    }
}
