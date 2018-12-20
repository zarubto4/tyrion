package utilities;

import com.google.inject.Injector;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.typesafe.config.Config;
import controllers._BaseFormFactory;
import io.intercom.api.Intercom;
import models.*;
import utilities.enums.ProgramType;
import utilities.enums.ServerMode;
import xyz.morphia.Datastore;
import xyz.morphia.Morphia;
import xyz.morphia.annotations.Entity;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import utilities.grid_support.utils.IP_Founder;
import utilities.homer_auto_deploy.DigitalOceanThreadRegister;
import utilities.logger.Logger;
import utilities.scheduler.jobs.Job_GetCompilationLibraries;

import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.UUID;
import javax.inject.*;

@Singleton
public class Server {

    private static final Logger logger = new Logger(Server.class);

    public static _BaseFormFactory formFactory;
    public static Config configuration;
    public static Injector injector;

    public static ServerMode mode;
    public static String version;
    public static String ip;
    public static String httpAddress;
    public static String clearAddress;
    public static String wsAddress;

    // Azure - Blob
    public static CloudStorageAccount storageAccount;
    public static CloudBlobClient blobClient;
    public static String azure_blob_Link;

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

    public static String Fakturoid_apiKey;
    public static String Fakturoid_url;
    public static String Fakturoid_user_agent;
    public static String Fakturoid_secret_combo;

    public static String PrintNode_url;
    public static String PrintNode_apiKey;

    public static String link_api_swagger;

    // Slack

    public static String slack_webhook_url_channel_servers;
    public static String slack_webhook_url_channel_homer;
    public static String slack_webhook_url_channel_hardware;

    // Financial

    public static int financial_quantity_scale = 2;
    public static RoundingMode financial_quantity_rounding = RoundingMode.HALF_UP;

    public static int financial_price_scale = 2;
    public static RoundingMode financial_price_rounding = RoundingMode.HALF_UP;

    public static int financial_tax_scale = 2;
    public static RoundingMode financial_tax_rounding = RoundingMode.UP;


    /**
     * Loads all configurations and start all server components.
     * @param injector default application injector
     * @throws Exception if error occurs when starting the server
     */
    public static void start(Injector injector) throws Exception {
        Server.injector = injector;
        Server.formFactory = injector.getInstance(_BaseFormFactory.class);

        setConstants();

        try {
            setWidgetAndBlock();
        } catch (Exception e) {
            logger.error("start - DB is inconsistent, probably evolution will occur", e);
        }

        setBaseForm();

        // Set Libraries
        injector.getInstance(Job_GetCompilationLibraries.class).execute(null);
    }

    /**
     * Finds various configurations from the application.conf file
     * and stores it in the static fields of this class.
     * @throws Exception when config was not found
     */
    private static void setConstants() throws Exception {

        String mode = Server.mode.name().toLowerCase();

        version = configuration.getString("api.version");

        if (Server.mode == ServerMode.DEVELOPER) {
            // String mac_address = getMacAddress().toLowerCase();
            // logger.warn("setServerValues - local macAddress: {}", mac_address);

            // Speciální podmínka, která nastaví podklady sice v Developerském modu - ale s URL adresami tak, aby byly v síti přístupné

            logger.info("setConstants - special settings for DEV office servers.");


            ip = IP_Founder.getLocalHostLANAddress().getHostAddress();
            httpAddress = "http://" + IP_Founder.getLocalHostLANAddress().getHostAddress() + ":9000";
            wsAddress   = "ws://"   + IP_Founder.getLocalHostLANAddress().getHostAddress() + ":9000";
            becki_mainUrl = "http://" + IP_Founder.getLocalHostLANAddress().getHostAddress() + ":8080";

            logger.info("setConstants - local URL: {}", ip);

            // Nastavení adresy, kde běží Grid APP
            grid_app_main_url       = "http://" + IP_Founder.getLocalHostLANAddress().getHostAddress() + ":8888";
        }

        // Nastavení pro Tyrion Adresy - pokud není developer je automaticky wss a httms
        if(httpAddress == null) httpAddress = "https://" + configuration.getString("server." + mode);
        if(wsAddress == null)   wsAddress   = "wss://" + configuration.getString("server." + mode);
        if(clearAddress == null)   clearAddress  = configuration.getString("server." + mode);

        // Nastavení adresy, kde běží Grid APP
        if(grid_app_main_url == null) grid_app_main_url = "https://" + configuration.getString("Grid_App." + mode + ".mainUrl");

        // Nastavení pro Becki Adresy
        if(becki_mainUrl == null) becki_mainUrl           = "https://" + configuration.getString("Becki." + mode + ".mainUrl");

        // Swagger URL Redirect - Actual Rest Api docu
        link_api_swagger = "http://swagger.byzance.cz/?url=" + httpAddress + "/swagger.json";

        //financial_spendDailyPeriod = checkPeriod(configuration.getInt("Financial." + mode + ".spendDailyPeriod"));

        //  Becki Config -------------------------------------------------------------------------------------------------------------

        becki_redirectOk                    = configuration.getString("Becki.redirectOk");
        becki_redirectFail                  = configuration.getString("Becki.redirectFail");
        becki_accountAuthorizedSuccessful   = configuration.getString("Becki.accountAuthorizedSuccessful");
        becki_accountAuthorizedFailed       = configuration.getString("Becki.accountAuthorizedFailed");
        becki_passwordReset                 = configuration.getString("Becki.passwordReset ");
        becki_invitationToCollaborate       = configuration.getString("Becki.invitationToCollaborate");
        becki_propertyChangeFailed          = configuration.getString("Becki.redirectFail");

        //  Facturoid Config -------------------------------------------------------------------------------------------------------------

        Fakturoid_apiKey        = configuration.getString("Fakturoid." + mode  +".apiKey");
        Fakturoid_url           = configuration.getString("Fakturoid." + mode  +".url");
        Fakturoid_user_agent    = configuration.getString("Fakturoid." + mode  +".userAgent");
        Fakturoid_secret_combo  = configuration.getString("Fakturoid." + mode  +".secret_combo");

        //  PrintNode Config ------------------------------------------------------------------------------------------------------------

        PrintNode_apiKey        = configuration.getString("PrintNode." + mode  +".apiKey");
        PrintNode_url           = configuration.getString("PrintNode." + mode  +".url");

        //  GitHub Config -------------------------------------------------------------------------------------------------------------

        GitHub_callBack         = httpAddress + configuration.getString("GitHub." + mode +".callBack");
        GitHub_clientSecret     = configuration.getString("GitHub." + mode +".clientSecret");
        GitHub_url              = configuration.getString("GitHub." + mode +".url");
        GitHub_apiKey           = configuration.getString("GitHub." + mode +".apiKey  ");

        // FaceBook Config -------------------------------------------------------------------------------------------------------------

        Facebook_callBack       = httpAddress + configuration.getString("Facebook." + mode +".callBack");
        Facebook_clientSecret   = configuration.getString("Facebook." + mode +".clientSecret");
        Facebook_url            = configuration.getString("Facebook." + mode +".url");
        Facebook_apiKey         = configuration.getString("Facebook." + mode +".apiKey  ");

        // Azure Config ------------------------------------------------------------------------------------------------------------

        azure_blob_Link = configuration.getString("blob." + mode + ".url");
        storageAccount  = CloudStorageAccount.parse(configuration.getString("blob." + mode + ".secret"));
        blobClient      = storageAccount.createCloudBlobClient();

        slack_webhook_url_channel_servers = configuration.getString("Slack.servers");
        slack_webhook_url_channel_hardware = configuration.getString("Slack.hardware");
        slack_webhook_url_channel_homer = configuration.getString("Slack.homer");

        // Set token to InterCom
        Intercom.setToken(configuration.getString("Intercom.token"));
    }

    /**
     * Creates default block and widget if it does not exists.
     */
    private static void setWidgetAndBlock() {
        try {

            if (Model_Widget.find.query().where().eq("id", UUID.fromString("00000000-0000-0000-0000-000000000001")).findCount() == 0) {

                logger.warn("setWidgetAndBlock - Creating Widget with " + "0000000-0000-0000-0000-000000000001");

                Model_Widget widget = new Model_Widget();
                widget.id = UUID.fromString("00000000-0000-0000-0000-000000000001");
                widget.description = "Default Widget";
                widget.name = "Default Widget";
                widget.project = null;
                widget.author_id = null;
                widget.publish_type = ProgramType.DEFAULT_MAIN;
                widget.save();
            } else {
                logger.trace("setWidgetAndBlock - Model_Widget already exist");
            }

            if (Model_Block.find.query().where().eq("id", UUID.fromString("00000000-0000-0000-0000-000000000001")).findCount() == 0) {

                logger.warn("setWidgetAndBlock - Creating Block with " + "0000000-0000-0000-0000-000000000001");

                Model_Block block = new Model_Block();
                block.id = UUID.fromString("00000000-0000-0000-0000-000000000001");
                block.description = "Default Block";
                block.name = "Default Block";
                block.author_id = null;
                block.project = null;
                block.publish_type = ProgramType.DEFAULT_MAIN;
                block.save();
            } else {
                logger.trace("setWidgetAndBlock - Model_Block already exist");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set BaseForm for Json Control
     */
    private static void setBaseForm() {
        DigitalOceanThreadRegister.formFactory              = Server.injector.getInstance(_BaseFormFactory.class);
        Model_InstanceSnapshot.formFactory                  = Server.injector.getInstance(_BaseFormFactory.class);
    }

    /**
     * Finds the MAC address of the current host.
     * @return String mac address
     */
    private static String getMacAddress() {
        try {

            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
            }

            return sb.toString();

        }catch (Exception e){
            logger.internalServerError(e);
            return "Not-know";
        }
    }
}
