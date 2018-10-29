package utilities;

import com.google.inject.Injector;
import com.microsoft.azure.documentdb.*;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import controllers.Controller_WebSocket;
import controllers._BaseFormFactory;
import io.intercom.api.Intercom;
import models.*;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.Entity;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import utilities.enums.EntityType;
import utilities.enums.ProgramType;
import utilities.enums.ServerMode;
import exceptions.NotFoundException;
import utilities.grid_support.utils.IP_Founder;
import utilities.homer_auto_deploy.DigitalOceanThreadRegister;
import utilities.logger.Logger;
import utilities.model._Abstract_MongoModel;
import utilities.models_update_echo.EchoHandler;
import utilities.models_update_echo.RefreshTouch_echo_handler;
import utilities.notifications.NotificationHandler;
import utilities.permission.Action;
import utilities.permission.Permissible;
import utilities.threads.homer_server.Synchronize_Homer_Synchronize_Settings;
import websocket.interfaces.WS_Homer;

import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Server {

    private static final Logger logger = new Logger(Server.class);

    public static _BaseFormFactory baseFormFactory;
    public static Config configuration;
    public static Injector injector;

    public static ServerMode mode;
    public static String version;
    public static String httpAddress;
    public static String clearAddress;
    public static String wsAddress;

    // Azure - Blob
    public static CloudStorageAccount storageAccount;
    public static CloudBlobClient blobClient;
    public static String azure_blob_Link;

    // Azure - NoSQL Database
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


    // Mongo Databases
    private static final Morphia morphia = new Morphia();
    public static MongoClient mongoClient = null;

    public static MongoDatabase main_database = null;
    public static Datastore main_data_store = null;


    /**
     * Loads all configurations and start all server components.
     * @param injector default application injector
     * @throws Exception if error occurs when starting the server
     */
    public static void start(Injector injector) throws Exception {
        Server.injector = injector;
        Server.baseFormFactory = Server.injector.getInstance(_BaseFormFactory.class);

        setConstants();

        cleanUpdateMess();

        // Init DocumentDB
        init_mongo_database();

        try {
            setPermission();
            setAdministrator();
            setWidgetAndBlock();
        } catch (Exception e) {
            logger.error("start - DB is inconsistent, probably evolution will occur", e);
        }

        // TODO po prvním spuštění je možné odstranit
        mongoTransferScript();

        setBaseForm();
        startThreads();
    }

    /**
     * Stops server components, that need to be properly shut down
     */
    public static void stop() {
        Controller_WebSocket.close();
    }


    /**
     * Some Threads are required to start on beginin. Thay have implemented sleep and wake up mode
     */
    public static void startThreads() {
        EchoHandler.startThread();
        NotificationHandler.startThread();
        RefreshTouch_echo_handler.startThread();
       // GoPay_PaymentCheck.startThread();
       // Fakturoid_InvoiceCheck.startThread();
    }

    /**
     * Some time, after restart of Tyrion, we have more In Progress Updates on Hardware,
     * So we Clean this mess
     */
    public static void cleanUpdateMess() {

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
            logger.info("setConstants - local URL: {}", IP_Founder.getLocalHostLANAddress().getHostAddress());

            httpAddress = "http://" + IP_Founder.getLocalHostLANAddress().getHostAddress() + ":9000";
            wsAddress   = "ws://"   + IP_Founder.getLocalHostLANAddress().getHostAddress() + ":9000";
            becki_mainUrl = "http://" + IP_Founder.getLocalHostLANAddress().getHostAddress() + ":8080";

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

        slack_webhook_url_channel_servers = configuration.getString("Slack.servers");
        slack_webhook_url_channel_hardware = configuration.getString("Slack.hardware");
        slack_webhook_url_channel_homer = configuration.getString("Slack.homer");

        // Set token to InterCom
        Intercom.setToken(configuration.getString("Intercom.token"));
    }

    /**
     * Creates first admin account, if it does not exists
     * and update permissions.
     */
    private static void setAdministrator() {

        // For Developing
        Model_Role role;

        try {
            role = Model_Role.getByName("SuperAdmin");

            logger.trace("setAdministrator - role SuperAdmin exists");

        } catch (NotFoundException e) {

            logger.warn("setAdministrator - SuperAdmin role was not found, creating it");

            role = new Model_Role();
            role.name = "SuperAdmin";
            role.save();
        }

        logger.info("setAdministrator - updating permissions in the role");

        List<UUID> permissionIds = role.permissions.stream().map(permission -> permission.id).collect(Collectors.toList());

        logger.trace("setAdministrator - role contains {} permission(s)", permissionIds.size());

        List<Model_Permission> permissions = Model_Permission.find.query().where().notIn("id", permissionIds).findList();

        logger.trace("setAdministrator - role is missing {} permission(s)", permissions.size());

        if (!permissions.isEmpty()) {
            logger.debug("setAdministrator - adding {} permission(s)", permissions.size());
            role.permissions.addAll(permissions);
            role.update();
        }

        Model_Person person;

        try {
            person = Model_Person.getByEmail("admin@byzance.cz");

            logger.trace("setAdministrator - admin is already created");

        } catch (NotFoundException e) {

            logger.warn("setAdministrator - creating first admin account: admin@byzance.cz, password: 123456789");

            person = new Model_Person();
            person.first_name = "Admin";
            person.last_name = "Byzance";
            person.validated = true;
            person.nick_name = "Syndibád";
            person.email = "admin@byzance.cz";
            person.setPassword("123456789");
            person.save();
        }

        if (!role.persons.contains(person)) {
            logger.info("setAdministrator - adding admin account to role");
            role.persons.add(person);
            role.update();
        }
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
     * Method will look up all enums called 'Permission'
     * and updates the database values of permissions.
     */
    private static void setPermission() {

        long start = System.currentTimeMillis();

        // Get classes in 'models' package
        Reflections reflections_postgress = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("models"))
                .setScanners(new SubTypesScanner()));


        Reflections reflections_mongo = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("mongo"))
                .setScanners(new SubTypesScanner()));

        // Get classes that implements Permittable
        Set<Class<? extends Permissible>> classes_post = reflections_postgress.getSubTypesOf(Permissible.class);
        Set<Class<? extends Permissible>> classes_mongo = reflections_mongo.getSubTypesOf(Permissible.class);


        classes_post.addAll(classes_mongo);


        logger.trace("setPermission - found {} classes", classes_post.size());

        List<Model_Permission> permissions = Model_Permission.find.all();

        classes_post.forEach(cls -> {
            try {
                Permissible permissible = cls.newInstance();
                EntityType entityType = permissible.getEntityType();
                List<Action> actions = permissible.getSupportedActions();

                actions.forEach(action -> {
                    if (permissions.stream().noneMatch(p -> p.action == action && p.entity_type == entityType)) {
                        Model_Permission permission = new Model_Permission();
                        permission.entity_type = entityType;
                        permission.action = action;
                        permission.save();
                    }
                });

            } catch (Exception e) {
                logger.internalServerError(e);
            }
        });

        logger.trace("setPermission - scanning for permissions took: {} ms", System.currentTimeMillis() - start);

        // Set default project roles (temporary)
        List<Model_Project> projects = Model_Project.find.query().where().isEmpty("roles").findList();
        projects.forEach(project -> {

            List<Model_Person> persons = Model_Person.find.query().where().eq("projects.id", project.id).findList();
            Model_Role adminRole = Model_Role.createProjectAdminRole();
            adminRole.project = project;
            if(adminRole.persons == null) adminRole.persons = new ArrayList<>();
            adminRole.persons.addAll(persons);
            adminRole.save();

            Model_Role memberRole = Model_Role.createProjectMemberRole();
            memberRole.project = project;
            memberRole.save();
        });
    }

    /**
     * Set BaseForm for Json Control
     */
    private static void setBaseForm() {

        WS_Homer.baseFormFactory                                = Server.injector.getInstance(_BaseFormFactory.class);
        Synchronize_Homer_Synchronize_Settings.baseFormFactory  = Server.injector.getInstance(_BaseFormFactory.class);
        DigitalOceanThreadRegister.baseFormFactory              = Server.injector.getInstance(_BaseFormFactory.class);
        Model_InstanceSnapshot.baseFormFactory                  = Server.injector.getInstance(_BaseFormFactory.class);
    }

    /*
        Dočasný script, který přemigruje staré mongo na nové mongo.
        Migrační script je nutný zejména kvuli jinému typu ukládání a frameworku
     */
    private static void mongoTransferScript() {

        List<Model_Hardware> hardware_1 = Model_Hardware.find.query().where().eq("batch_id", "cc6b3643-652a-40c5-88ee-04cff043afa5").findList();
        List<Model_Hardware> hardware_2 = Model_Hardware.find.query().where().eq("batch_id", "26d189c5-b61f-4565-a8f7-5a043a73963e").findList();
        List<Model_Hardware> hardware_3 = Model_Hardware.find.query().where().eq("batch_id", "abd218dc-14ca-4d2e-a731-66f71ed41245").findList();

        if(hardware_1.isEmpty() && hardware_2.isEmpty() && hardware_3.isEmpty()) return;

        List<Model_Hardware> collection = new ArrayList<>();
        collection.addAll(hardware_1);
        collection.addAll(hardware_2);
        collection.addAll(hardware_3);

        for(Model_Hardware hardware : collection) {


            if(hardware.batch_id.equals("cc6b3643-652a-40c5-88ee-04cff043afa5")  ) {
                hardware.batch_id = new ObjectId("5bd5dd5423548a6f3082b428").toString();
            }
            else if(hardware.batch_id.equals("26d189c5-b61f-4565-a8f7-5a043a73963e")  ) {
                hardware.batch_id = new ObjectId("5bd5dd5423548a6f3082b427").toString();
            }
            else if(hardware.batch_id.equals("abd218dc-14ca-4d2e-a731-66f71ed41245")  ) {
                hardware.batch_id =  new ObjectId("5bd5dd5423548a6f3082b426").toString();
            }


            hardware.update();
        }

    }

    /**
     * Initialization Mongo Databases from config file, all collection are checked, if some missing, this method will
     * create it.
     */
    @SuppressWarnings("unchecked")
    public static void init_mongo_database() {

        String mode = Server.mode.name().toLowerCase();

        // Připojení na MongoClient v Azure
        logger.info("init_mongo_database:: URL {}", configuration.getString("MongoDB." + mode + ".url"));


        MongoClientOptions.Builder options_builder = new MongoClientOptions.Builder();
        options_builder.maxConnectionIdleTime(1000 * 60 * 60 *24);
        options_builder.retryWrites(true);

        MongoClientURI uri = new MongoClientURI(configuration.getString("MongoDB." + mode + ".url"), options_builder);
        Server.mongoClient = new MongoClient(uri);

        try {

            mongoClient.getAddress();

        } catch (Exception e) {
            logger.error("init_mongo_database:: Mongo is down");
            mongoClient.close();
            return;
        }

        // Mongo ORM zástupný onbjekt pro lepší práci s databází
        main_data_store = morphia.createDatastore(mongoClient, configuration.getString("MongoDB." + mode + ".main_database_name"));

        // Připojení na konkrétní Databázi clienta
        main_database = mongoClient.getDatabase(configuration.getString("MongoDB." + mode + ".main_database_name"));

        if(main_data_store == null) {
            logger.error("init_mongo_database:: Required Main Database not Exist!");
        }

        // Kontrola databáze
        if(! mongoClient.getDatabaseNames().contains(configuration.getString("MongoDB." + mode + ".main_database_name"))){
            logger.error("init_mongo_database:: Required Main Database not Exist!");
        }


        // Kontrola kolekcí nad Mongo Databází
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("models"))
                .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner()));

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Entity.class);

        classes.forEach(cls -> {
            try {

                Class<? extends _Abstract_MongoModel> model = (Class<? extends _Abstract_MongoModel>) cls; // Cast to model
                Entity annotation = model.getAnnotation(Entity.class);

                String value = annotation.value();

                if (annotation == null) {
                    logger.error("init_mongo_database:: In Class {} is not set anotation  @Entity! FIX THAT!", cls.getSimpleName());
                    return;
                }

                if (!main_database.listCollectionNames().into(new ArrayList<String>()).contains(annotation.value())) {
                    logger.warn("init_mongo_database:: {} Collection:: {}  - not exist. System will create that! ", model.getSimpleName(), annotation.value());
                    main_database.createCollection(annotation.value());

                }

            } catch (Exception e) {
                logger.error("init_mongo_database:: {} Collection Class:: {}  - not exist. Its Required create that! ", main_database.getName(), cls.getSimpleName());
                logger.internalServerError(e);
            }
        });

    }

    public static Datastore getMainMongoDatabase() {
        if(Server.main_data_store == null) {
            init_mongo_database();
        }

        return main_data_store;
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
