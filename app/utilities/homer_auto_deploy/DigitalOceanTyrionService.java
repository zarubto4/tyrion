package utilities.homer_auto_deploy;

import com.google.inject.Inject;
import com.myjeeva.digitalocean.DigitalOcean;
import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;
import com.myjeeva.digitalocean.impl.DigitalOceanClient;
import com.myjeeva.digitalocean.pojo.*;
import com.typesafe.config.Config;
import controllers._BaseFormFactory;
import models.Model_HomerServer;
import models.Model_Product;
import org.ehcache.Cache;
import play.libs.Json;
import play.libs.ws.WSClient;
import utilities.Server;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.homer_auto_deploy.models.common.*;
import utilities.homer_auto_deploy.models.service.Swagger_BlueOcean;
import utilities.logger.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

public class DigitalOceanTyrionService {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(DigitalOceanTyrionService.class);

    /*  VALUES -------------------------------------------------------------------------------------------------------------*/

    public static  DigitalOcean apiClient = new DigitalOceanClient( "2521a027f6120a471fa1187060cc56b58e6a42dbd3e56406606488d9e2d7c07f");

    /**
     * Holds person connection tokens and ids
     */
    public static Cache<String, Swagger_ServerRegistration_FormData> tokenCache;

    private static WSClient ws;
    private static Config configuration;
    private static _BaseFormFactory baseFormFactory;

    @Inject
    public DigitalOceanTyrionService(WSClient ws, Config config, _BaseFormFactory formFactory) {
        DigitalOceanTyrionService.ws = ws;
        DigitalOceanTyrionService.configuration = config;
        DigitalOceanTyrionService.baseFormFactory = formFactory;
    }

    public static void create_server(Model_HomerServer homer_server, String server_size_slug, String region_slug) throws RequestUnsuccessfulException, DigitalOceanException {

        logger.info("create_server:: DigitalOceanTyrionService - New request for Creating Server! Homer Server ID: {}, Name {}, Region Slug: {}, Server Size Slug: {}", homer_server.id, homer_server.name, region_slug, server_size_slug);

        // Find Target Snapshot
        Snapshot target_snapshot = null;

        logger.trace("create_server::First - we have to find Snapshot of Homer Server Image. Requred Snapshot has name: {}", "homer-server-default-defaut-image" );

        for(Snapshot snapshot : apiClient.getAllDropletSnapshots(0, 5).getSnapshots()) {
            logger.trace("create_server::  Image");
            logger.trace("create_server::     Image Name: " + snapshot.getName());
            logger.trace("create_server::     Image Slug: " + snapshot.getSlug());
            logger.trace("create_server::     Image id:   " + snapshot.getId());

            if(snapshot.getName().equals("homer-server-default-image")) {
                logger.trace("create_server::  Done! We found required snapshot!");
                target_snapshot = snapshot;
                break;
            }
        }

        if(target_snapshot == null){
            logger.error("Shit happens. We don't find Homer Server Image {} !!!!", "homer-server-default-defaut-image");
            throw new Result_Error_NotFound(Snapshot.class);
        }


        // Find Target size
        Size target_size = null;
        String target_region = null;
        sizeC: for(Size size : apiClient.getAvailableSizes(0).getSizes()) {
            logger.trace("create_server::  Size");
            logger.trace("create_server::     Slug:          " + size.getSlug());
            logger.trace("create_server::     Price Hourly:  " + size.getPriceHourly());
            logger.trace("create_server::     Price Monthly: " + size.getPriceMonthly());
            logger.trace("create_server::     Memory Size:   " + size.getMemorySizeInMb());
            logger.trace("create_server::     Disk Size:     " + size.getDiskSize());

            if( size.getSlug().equals(server_size_slug)){
                logger.trace("create_server::  Done! We found required target_size!");
                target_size = size;
            }else {
                continue;
            }

            logger.trace("create_server::     Regions  (" + size.getRegions().size() + ")");
            for(String region : size.getRegions()) {
                logger.trace("create_server::         " + region );
                if(region.equals(region_slug)) {
                    logger.trace("create_server::  Done! We found required region!");
                    target_region = region;
                    break sizeC;
                }
            }
        }


        if(target_size == null){
            logger.error("Shit happens. We don't find required server size by slug {}", server_size_slug);
            throw new Result_Error_NotFound(Size.class);
        }

        if(target_region == null){
            logger.error("Shit happens. We don't find required server region by slug {}", region_slug);
            throw new Result_Error_NotFound(Region.class);
        }

        String server_name = "homer-server-" + homer_server.id.toString();
        server_name = server_name.replaceAll("_", "-");

        logger.trace("create_server::Virtual Server set name to:  {}", server_name);

        // Create a new droplet
        Droplet newDroplet = new Droplet();
        newDroplet.setName(server_name);
        newDroplet.setSize(target_size.getSlug()); // setting size by slug value
        newDroplet.setRegion(new Region(target_region)); // setting region by slug value; sgp1 => Singapore 1 Data center
        newDroplet.setImage(target_snapshot); // setting by Image Id 1601 => centos-5-8-x64 also available in image slug value
        newDroplet.setEnableBackup(Boolean.FALSE);
        newDroplet.setEnableIpv6(Boolean.TRUE);
        newDroplet.setEnablePrivateNetworking(Boolean.FALSE);

        logger.trace("create_server::Time to add Tags");

        // Add Tags
        List<String> tags = new ArrayList<>();
        tags.add(homer_server.id.toString());
        tags.add("homer_server");

        if(homer_server.project != null ) {
            Model_Product product = homer_server.project.getProduct();
            tags.add("project_id_" + homer_server.project.id);
            tags.add("product_id_" + product.id);
            tags.add("customer_id_" + product.owner.id);
        }
        tags.add(homer_server.server_type.name());
        tags.add(Server.mode.name());
        logger.trace("create_server::Time to add Tags: {}", tags);

        newDroplet.setTags(tags);

        logger.trace("create_server::Time to Create Dropled");
        Droplet droplet = apiClient.createDroplet(newDroplet);
        logger.trace("create_server::Dropled created");

        logger.trace("create_server::------------------------------");
        logger.trace("create_server:: Server Deploy command Done");
        logger.trace("create_server::    Server Id:      " + droplet.getId());
        logger.trace("create_server::    Server Name:    " + droplet.getName());
        logger.trace("create_server::    Server Status:  " + droplet.getStatus().name());
        logger.trace("create_server::    Server Tags:    " + droplet.getTags());

        Swagger_ExternalService service = new Swagger_ExternalService();
        service.type = Enum_ServiceType.BLUE_OCEAN;

        Swagger_BlueOcean blueOcean = new Swagger_BlueOcean();
        blueOcean.id = droplet.getId();
        blueOcean.server_name = server_name;
        blueOcean.price_monthly = target_size.getPriceMonthly();
        blueOcean.price_hourly = target_size.getPriceHourly();
        blueOcean.price_hourly_set_for_customer = target_size.getPriceHourly().multiply(new BigDecimal(1.5));
        blueOcean.price_monthly_set_for_customer = target_size.getPriceMonthly().multiply(new BigDecimal(1.5));
        blueOcean.region_slug = region_slug;
        blueOcean.size_slug = server_size_slug;

        service.blue_ocean_config = blueOcean;

        homer_server.json_additional_parameter = Json.toJson(service).toString();
        homer_server.deployment_in_progress = true;
        homer_server.update();


        DigitalOceanThreadRegister threadRegister = new DigitalOceanThreadRegister(homer_server, blueOcean);
        threadRegister.start();

    }

    public static void check_status(Model_HomerServer homerServer) throws RequestUnsuccessfulException, DigitalOceanException {

        Swagger_ExternalService help = baseFormFactory.formFromJsonWithValidation(Swagger_ExternalService.class, Json.parse(homerServer.json_additional_parameter));
        if(help.type == Enum_ServiceType.BLUE_OCEAN) {
            Droplet droplet = apiClient.getDropletInfo(help.blue_ocean_config.id);
            logger.trace("check_status::    Server Id:      " + droplet.getId());
            logger.trace("check_status::    Server Name:    " + droplet.getName());
            logger.trace("check_status::    Server Status:  " + droplet.getStatus().name());
            logger.trace("check_status::    Server Tags:    " + droplet.getTags());

            for(Network network : droplet.getNetworks().getVersion6Networks()) {
                logger.trace("check_status::    Server URL:    " + network.getIpAddress());
            }
        }
    }

    public static void powerOff(Model_HomerServer homerServer) throws RequestUnsuccessfulException, DigitalOceanException {
        Swagger_ExternalService service = homerServer.external_settings();
        if(service != null) {
            apiClient.powerOffDroplet(service.blue_ocean_config.id);
        }
    }


    public static void powerOn(Model_HomerServer homerServer) throws RequestUnsuccessfulException, DigitalOceanException {
        Swagger_ExternalService service = homerServer.external_settings();
        if(service != null) {
            apiClient.powerOnDroplet( service.blue_ocean_config.id);
        }
    }

    public static void restartServer(Model_HomerServer homerServer) throws RequestUnsuccessfulException, DigitalOceanException {
        Swagger_ExternalService service = homerServer.external_settings();
        if(service != null) {
            apiClient.rebootDroplet( service.blue_ocean_config.id);
        }
    }

    public static void remove(Model_HomerServer homerServer) throws RequestUnsuccessfulException, DigitalOceanException {
        Swagger_ExternalService service = homerServer.external_settings();
        if(service != null) {
            apiClient.deleteDroplet(service.blue_ocean_config.id);
            apiClient.deleteDomain(homerServer.id + ".do.byzance.cz");
        }
    }



    public static Swagger_ServerRegistration_FormData get_data()  throws RequestUnsuccessfulException, DigitalOceanException {


        if(tokenCache.containsKey("data")) {
            return tokenCache.get("data");
        }

        List<Swagger_ServerRegistration_FormData_ServerSize> server_sizes = new ArrayList<>();

        logger.trace("get_data::Start Wit Requesting");
        List<Size> sizes = apiClient.getAvailableSizes(0).getSizes();
        List<Region> regions =  apiClient.getAvailableRegions(0).getRegions();
        logger.trace("get_data::All data in Cache");

        // Get All Allowed config size
        List<String> groups = configuration.getStringList("digitalOcean.allowed_server_types");

        for(Size size : sizes) {

            // Ignor all not allowed Server Sizes from Config
            if(!groups.contains(size.getSlug())) {
                logger.trace("get_data::  Slug {} is not in allowed list", size.getSlug());
                continue;
            }

            logger.trace("get_data::  Size");
            logger.trace("get_data::     Slug:          " + size.getSlug());
            logger.trace("get_data::     Price Hourly:  " + size.getPriceHourly());
            logger.trace("get_data::     Price Monthly: " + size.getPriceMonthly());
            logger.trace("get_data::     Memory Size:   " + size.getMemorySizeInMb());
            logger.trace("get_data::     Processors:    " + size.getVirutalCpuCount());
            logger.trace("get_data::     Disk Size:     " + size.getDiskSize());

            Swagger_ServerRegistration_FormData_ServerSize server_size = new Swagger_ServerRegistration_FormData_ServerSize();
            server_size.slug = size.getSlug();
            server_size.price_hourly = size.getPriceHourly().multiply(new BigDecimal(1.5));
            server_size.price_monthly = size.getPriceMonthly().multiply(new BigDecimal(1.5)).setScale(3, RoundingMode.CEILING);
            server_size.memory = size.getMemorySizeInMb() / 1000;
            server_size.vcpus = size.getVirutalCpuCount();

           List<String> regions_in_string = size.getRegions();

           logger.trace("get_data::     ");
           logger.trace("get_data::     Regions ----------------------------------------------");
           loop1: for(String region_slug_name: regions_in_string) {

               loop2: for(Region region : regions) {
                   if(region.getSlug().equals(region_slug_name)) {

                       logger.trace("get_data::     Region");
                       logger.trace("get_data::         Slug:          " + region.getSlug());
                       logger.trace("get_data::         Is Available:  " + region.isAvailable());

                       Swagger_ServerRegistration_FormData_ServerRegion server_region = new Swagger_ServerRegistration_FormData_ServerRegion();
                       server_region.slug = region.getSlug();
                       server_region.name = region.getName();
                       server_region.available = region.isAvailable();

                       server_size.regions.add(server_region);
                       continue loop1;
                   }
               }
           }

           server_sizes.add(server_size);

        }

        Swagger_ServerRegistration_FormData data = new Swagger_ServerRegistration_FormData();
        data.server_sizes = server_sizes;

        tokenCache.put("data", data);
        logger.trace("get_data::Complete Done Response is completed ----------------------------------------------");
        return data;
    }
}
