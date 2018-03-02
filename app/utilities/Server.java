package utilities;

import com.google.inject.Injector;
import com.microsoft.azure.documentdb.*;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.typesafe.config.Config;
import controllers.Controller_WebSocket;
import controllers._BaseFormFactory;
import models.*;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import utilities.cache.ServerCache;
import utilities.document_db.DocumentDB;
import utilities.enums.ProgramType;
import utilities.enums.ServerMode;
import utilities.grid_support.utils.IP_Founder;
import utilities.hardware_registration_auhtority.Hardware_Registration_Authority;
import utilities.homer_auto_deploy.DigitalOceanTyrionService;
import utilities.logger.Logger;
import utilities.logger.ServerLogger;
import utilities.model.BaseModel;
import utilities.threads.homer_server.Synchronize_Homer_Synchronize_Settings;
import websocket.interfaces.WS_Homer;
import websocket.interfaces.WS_Portal;

import javax.persistence.PersistenceException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Server {

    private static final Logger logger = new Logger(Server.class);

    public static Config configuration;
    public static Injector injector;

    public static ServerMode mode;
    public static String version;
    public static String httpAddress;
    public static String wsAddress;

    // Azure - Blob
    public static CloudStorageAccount storageAccount;
    public static CloudBlobClient blobClient;
    public static String azure_blob_Link;

    // Azure - NoSQL Database
    public static DocumentClient documentClient;
    public static Database no_sql_database;
    public static String documentDB_Path;

    public static DocumentCollection online_status_collection = null;

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

    public static Integer financial_spendDailyPeriod = 1;

    public static String Fakturoid_apiKey;
    public static String Fakturoid_url;
    public static String Fakturoid_user_agent;
    public static String Fakturoid_secret_combo;

    public static String PrintNode_url;
    public static String PrintNode_apiKey;

    public static String GoPay_api_url;
    public static String GoPay_client_id;

    public static String GoPay_client_secret;
    public static Long   GoPay_go_id;
    public static String GoPay_return_url;
    public static String GoPay_notification_url;

    public static String link_api_swagger;

    public static String slack_webhook_url_channel_servers;
    public static String slack_webhook_url_channel_hardware;

    /**
     * Loads all configurations and start all server components.
     * @param config file
     * @param injector default application injector
     * @throws Exception if error occurs when starting the server
     */
    public static void start(Config config, Injector injector) throws Exception {
        configuration = config;
        Server.injector = injector;

        Server.mode = configuration.getEnum(ServerMode.class,"server.mode");

        ServerLogger.init(config);

        setConstants();

        ServerCache.init();

        try {
            setPermission();
            setAdministrator();
            setWidgetAndBlock();
        } catch (PersistenceException e) {
            logger.error("start - DB is inconsistent, probably evolution will occur", e);
        }

        DocumentDB.init();


        setBaseForm();

        // TODO Batch_Registration_Authority.synchronize();

        // TODO Hardware_Registration_Authority.synchronize_hardware();
    }

    /**
     * Stops server components, that need to be properly shut down
     */
    public static void stop() {
        ServerCache.close();

        Controller_WebSocket.close();
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
            String mac_address = getMacAddress().toLowerCase();
            logger.warn("setServerValues - local macAddress: {}", mac_address);

            // Speciální podmínka, která nastaví podklady sice v Developerském modu - ale s URL adresami tak, aby byly v síti přístupné
            if (mac_address.equals("60:f8:1d:bc:71:42")|| // Mac Mini Server Wifi
                    mac_address.equals("ac:87:a3:18:a1:1c")|| // Mac Mini Server Ethernet
                    mac_address.equals("2c:4d:54:4f:68:6e")) { // Linux Lexa

                logger.warn("setConstants - special settings for DEV office servers.");
                logger.warn("setConstants - local URL: {}", IP_Founder.getLocalHostLANAddress().getHostAddress());

                httpAddress = "http://" + IP_Founder.getLocalHostLANAddress().getHostAddress() + ":9000";
                wsAddress   = "ws://"   + IP_Founder.getLocalHostLANAddress().getHostAddress() + ":9000";
                becki_mainUrl = "http://" + IP_Founder.getLocalHostLANAddress().getHostAddress();
            }

            // Nastavení adresy, kde běží Grid APP
            grid_app_main_url       = "http://" + IP_Founder.getLocalHostLANAddress().getHostAddress() + ":8888";
        }

        // Nastavení pro Tyrion Adresy
        if(httpAddress == null) httpAddress = "http://" + configuration.getString("server." + mode);
        if(wsAddress == null)   wsAddress   = "ws://" + configuration.getString("server." + mode);

        // Nastavení adresy, kde běží Grid APP
        if(grid_app_main_url == null) grid_app_main_url = "https://" + configuration.getString("Grid_App." + mode + ".mainUrl");

        // Nastavení pro Becki Adresy
        if(becki_mainUrl == null) becki_mainUrl           = "http://" + configuration.getString("Becki." + mode + ".mainUrl");

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

        // Go Pay Config ------------------------------------------------------------------------------------------------------------

        GoPay_api_url           = configuration.getString("GOPay."+ mode +".api_url");
        GoPay_client_id         = configuration.getString("GOPay."+ mode +".client_id");
        GoPay_client_secret     = configuration.getString("GOPay."+ mode +".client_secret");
        GoPay_go_id             = configuration.getLong(  "GOPay."+ mode +".go_id");

        GoPay_return_url        = configuration.getString("GOPay."+ mode +".return_url");
        GoPay_notification_url  = configuration.getString("GOPay."+ mode +".notification_url");

        // Azure Config ------------------------------------------------------------------------------------------------------------

        azure_blob_Link = configuration.getString("blob." + mode + ".url");
        storageAccount  = CloudStorageAccount.parse(configuration.getString("blob." + mode + ".secret"));
        blobClient      = storageAccount.createCloudBlobClient();

        documentClient  = new DocumentClient(configuration.getString("documentDB." + mode + ".url"), configuration.getString("documentDB." + mode + ".secret") , ConnectionPolicy.GetDefault(), ConsistencyLevel.Session);
        no_sql_database = new Database();
        no_sql_database.setId(configuration.getString("documentDB." + mode + ".databaseName"));
        documentDB_Path = "dbs/" + no_sql_database.getId();

        slack_webhook_url_channel_servers = configuration.getString("Slack.servers");
        slack_webhook_url_channel_hardware = configuration.getString("Slack.hardware");
    }

    /**
     * Creates first admin account, if it does not exists
     * and update permissions.
     */
    private static void setAdministrator() {

        // For Developing
        Model_Role role = Model_Role.getByName("SuperAdmin");
        List<Model_Permission> permissions = Model_Permission.find.all();

        if(role == null) {
            logger.warn("setAdministrator - RoleGroup SuperAdmin is missing - its required create it now. Total Permissions for registrations: {}", permissions.size());

            role = new Model_Role();

            if(role.permissions == null) role.permissions = new ArrayList<>();
            role.permissions.addAll(permissions);
            role.name = "SuperAdmin";

            logger.warn("setAdministrator - Save Role SuperAdmin now");
            role.save();

        } else {
            logger.warn("setAdministrator - RoleGroup SuperAdmin- already exist - Check all permissions");
            for (Model_Permission permission : permissions) {
                if (!role.permissions.contains(permission)) {
                    role.permissions.add(permission);
                }
            }
            role.update();
        }

        Model_Person person = Model_Person.getByEmail("admin@byzance.cz");

        if (person == null) {

            logger.warn("setAdministrator - Creating first admin account: admin@byzance.cz, password: 123456789");

            Model_Role role_super_admin = Model_Role.getByName("SuperAdmin");

            person = new Model_Person();
            person.first_name = "Admin";
            person.last_name = "Byzance";
            person.validated = true;
            person.nick_name = "Syndibád";
            person.email = "admin@byzance.cz";
            person.setPassword("123456789");

            if(person.roles == null){
                logger.warn("setAdministrator - person.roles is null");
                person.roles = new ArrayList<>();
            }

            person.roles.add(role_super_admin);

            person.save();

        } else {

            logger.warn("setAdministrator - admin is already created");

            // updatuji oprávnění
            List<Model_Permission> personPermissions = Model_Permission.find.all();

            for (Model_Permission personPermission :  personPermissions) {
                if(!person.permissions.contains(personPermission)) {
                    person.permissions.add(personPermission);
                }
            }
            person.update();
        }
    }

    /**
     * Creates default block and widget if it does not exists.
     */
    private static void setWidgetAndBlock() {

        if(new Model_Widget().check_if_exist("0000000-0000-0000-0000-000000000001")) {
            Model_Widget gridWidget = new Model_Widget();
            gridWidget.id = UUID.fromString("00000000-0000-0000-0000-000000000001");
            gridWidget.description = "Default Widget";
            gridWidget.name = "Default Widget";
            gridWidget.project = null;
            gridWidget.author_id = null;
            gridWidget.publish_type = ProgramType.DEFAULT_MAIN;
            gridWidget.save();
        }

        if(new Model_Widget().check_if_exist("0000000-0000-0000-0000-000000000001")) {
            Model_Block block = new Model_Block();
            block.id = UUID.fromString("00000000-0000-0000-0000-000000000001");
            block.description = "Default Block";
            block.name = "Default Block";
            block.author_id = null;
            block.project = null;
            block.publish_type = ProgramType.DEFAULT_MAIN;
            block.save();
        }
    }

    /**
     * Method will look up all enums called 'Permission'
     * and updates the database values of permissions.
     */
    private static void setPermission() {

        List<String> permissions = new ArrayList<>();

        long start = System.currentTimeMillis();

        // Get classes in 'models' package
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("models"))
                .setScanners(new SubTypesScanner()));

        // Get classes that extends _BaseModel.class
        Set<Class<? extends BaseModel>> classes = reflections.getSubTypesOf(BaseModel.class);

        logger.trace("setPermission - found {} classes", classes.size());

        // Find inner enum called 'Permission'
        classes.forEach(cls -> {
            logger.trace("setPermission - scanning class: {}", cls.getSimpleName());
            Class<?>[] innerClasses = cls.getDeclaredClasses();
            for (Class<?> inner : innerClasses) {
                try {
                    if (inner.isEnum() && inner.getSimpleName().equals("Permission")) {
                        // logger.trace("setPermission - found enum Permission in class: {}", cls.getSimpleName());
                        Enum[] enums = (Enum[]) inner.getEnumConstants();
                        for (Enum e : enums) {
                            // logger.trace("setPermission - found permission: {}", e.name());
                            permissions.add(e.name());
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        logger.trace("setPermission - scanning for permissions took: {} ms", System.currentTimeMillis() - start);

        List<Model_Permission> perms = Model_Permission.find.all();

        logger.trace("setPermission - get all permissions from database. Count: {}", perms.size());

        for (Model_Permission permission : perms) {
            if (!permissions.contains(permission.name)) {
                logger.info("setPermission - removing permission: {} from DB", permission.name);
                permission.delete();
            }
        }
        logger.trace("setPermission - time to save all, witch are not in database. Count of read by system {}", permissions.size());
        for (String permission_name : permissions) {
            // logger.trace("setPermission - Permission {} try to get from database", permission_name);
            if (Model_Permission.find.query().where().eq("name", permission_name).findOne() == null) {
                // logger.trace("setPermission - saving permission: {} to DB", permission_name);
                Model_Permission permission = new Model_Permission();
                permission.name = permission_name;
                permission.description = "description";
                permission.save();
            }else {
                // logger.trace("setPermission - Permission {} is already in database", permission_name);
            }
        }
    }

    /**
     * Set BaseForm for Json Control
     */
    private static void setBaseForm() {
        Hardware_Registration_Authority.baseFormFactory = Server.injector.getInstance(_BaseFormFactory.class);
        WS_Homer.baseFormFactory                        = Server.injector.getInstance(_BaseFormFactory.class);
        Synchronize_Homer_Synchronize_Settings.baseFormFactory = Server.injector.getInstance(_BaseFormFactory.class);
        Model_HardwareBatch.baseFormFactory             = Server.injector.getInstance(_BaseFormFactory.class);
        BaseModel.baseFormFactory                       = Server.injector.getInstance(_BaseFormFactory.class);
        DigitalOceanTyrionService.baseFormFactory       = Server.injector.getInstance(_BaseFormFactory.class);
        WS_Portal.baseFormFactory                       = Server.injector.getInstance(_BaseFormFactory.class);
    }

    /**
     * Finds the MAC address of the current host.
     * @return String mac address
     */
    private static String getMacAddress(){
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
