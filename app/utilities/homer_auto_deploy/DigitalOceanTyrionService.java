package utilities.homer_auto_deploy;

import com.google.inject.Inject;
import com.myjeeva.digitalocean.DigitalOcean;
import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;
import com.myjeeva.digitalocean.impl.DigitalOceanClient;
import com.myjeeva.digitalocean.pojo.*;
import controllers._BaseFormFactory;
import models.Model_HomerServer;
import models.Model_Product;
import play.libs.Json;
import utilities.Server;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.homer_auto_deploy.models.common.*;
import utilities.homer_auto_deploy.models.service.Swagger_BlueOcean;
import utilities.logger.Logger;
import utilities.swagger.input.Swagger_B_Program_Filter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class DigitalOceanTyrionService {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(DigitalOceanTyrionService.class);

    /*  VALUES -------------------------------------------------------------------------------------------------------------*/

    @Inject public static _BaseFormFactory baseFormFactory; // Its Required to set this in Server.class Component
    public static  DigitalOcean apiClient = new DigitalOceanClient( "42b67cd7450e5301121fb85f34d6ce39f86f7665c496e9f324c8b0dcb0ff3cfa");

    public DigitalOceanTyrionService(){
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

            if(snapshot.getName().equals("homer-server-default-defaut-image")) {
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
            tags.add("customer_id_" + product.customer.id);
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
        homer_server.update();

        DigitalOceanThreadRegister threadRegister = new DigitalOceanThreadRegister(homer_server, blueOcean);
        threadRegister.run();

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

    public static void shutdownServer(Model_HomerServer homerServer) throws RequestUnsuccessfulException, DigitalOceanException {

        Integer droplet_id = 321415263; // TODO get from Homer_Server Config!
        apiClient.shutdownDroplet(droplet_id);
    }



    public static Swagger_ServerRegistration_FormData get_data()  throws RequestUnsuccessfulException, DigitalOceanException {

        List<Swagger_ServerRegistration_FormData_ServerSize> server_sizes = new ArrayList<>();

        logger.trace("get_data::Start Wit Requesting");
        List<Size> sizes = apiClient.getAvailableSizes(0).getSizes();
        List<Region> regions =  apiClient.getAvailableRegions(0).getRegions();
        logger.trace("get_data::All data in Cache");

        for(Size size : sizes) {

            logger.trace("get_data::  Size");
            logger.trace("get_data::     Slug:          " + size.getSlug());
            logger.trace("get_data::     Price Hourly:  " + size.getPriceHourly());
            logger.trace("get_data::     Price Monthly: " + size.getPriceMonthly());
            logger.trace("get_data::     Memory Size:   " + size.getMemorySizeInMb());
            logger.trace("get_data::     Disk Size:     " + size.getVirutalCpuCount());

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

        logger.trace("get_data::Complete Done Response is completed ----------------------------------------------");
        return data;
    }
}
