package utilities.homer_auto_deploy;

import com.google.inject.Inject;
import com.myjeeva.digitalocean.DigitalOcean;
import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;
import com.myjeeva.digitalocean.impl.DigitalOceanClient;
import com.myjeeva.digitalocean.pojo.*;
import controllers._BaseFormFactory;
import models.Model_HomerServer;
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

    private static final Logger terminal_logger = new Logger(DigitalOceanTyrionService.class);

    /*  VALUES -------------------------------------------------------------------------------------------------------------*/

    @Inject public static _BaseFormFactory baseFormFactory; // Its Required to set this in Server.class Component
    public static  DigitalOcean apiClient = new DigitalOceanClient( "42b67cd7450e5301121fb85f34d6ce39f86f7665c496e9f324c8b0dcb0ff3cfa");

    public DigitalOceanTyrionService(){
    }

    public static void create_server(Model_HomerServer homer_server) throws RequestUnsuccessfulException, DigitalOceanException {

        System.out.println("DigitalOceanTyrionService - Create Server!");

        // Find Target Snapshot
        Snapshot target_snapshot = null;

        for(Snapshot snapshot : apiClient.getAllDropletSnapshots(0, 5).getSnapshots()) {
            System.out.println("  Image");
            System.out.println("     Image Name: " + snapshot.getName());
            System.out.println("     Image Slug: " + snapshot.getSlug());
            System.out.println("     Image id:   " + snapshot.getId());

            if(snapshot.getName().equals("homer-server-default-defaut-image")) {
                System.out.println("  Done! We found required snapshot!");
                target_snapshot = snapshot;
                break;
            }
        }

        if(target_snapshot == null){
            throw new Result_Error_NotFound(Snapshot.class);
        }

        for(Region region :  apiClient.getAvailableRegions(0).getRegions()) {
            System.out.println("  Region");
            System.out.println("     Slug:          " + region.getSlug());
            System.out.println("     Is Available:  " + region.isAvailable());
            System.out.println("     Features: "      + region.getFeatures());
            System.out.println("     Sizes:         " + region.getSizes());
        }

        // Find Target size
        Size target_size = null;
        String target_region = null;
        sizeC: for(Size size : apiClient.getAvailableSizes(0).getSizes()) {
            System.out.println("  Size");
            System.out.println("     Slug:          " + size.getSlug());
            System.out.println("     Price Hourly:  " + size.getPriceHourly());
            System.out.println("     Price Monthly: " + size.getPriceMonthly());
            System.out.println("     Memory Size:   " + size.getMemorySizeInMb());
            System.out.println("     Disk Size:     " + size.getDiskSize());

            if( size.getPriceMonthly().compareTo( new BigDecimal(5.0)) == 0 && size.getDiskSize() == 25){
                System.out.println("  Done! We found required target_size!");
                target_size = size;
            }else {
                continue;
            }

            System.out.println("     Regions  (" + size.getRegions().size() + ")");
            for(String region : size.getRegions()) {
                System.out.println("         " + region );
                if(region.equals("ams3")) {
                    System.out.println("  Done! We found required region!");
                    target_region = region;
                    break sizeC;
                }
            }
        }


        if(target_size == null){
            throw new Result_Error_NotFound(Size.class);
        }

        if(target_region == null){
            throw new Result_Error_NotFound(Region.class);
        }

        String server_name = "homer-server-" + homer_server.id.toString();
        server_name = server_name.replaceAll("_", "-");

        // Create a new droplet
        Droplet newDroplet = new Droplet();
        newDroplet.setName(server_name);
        newDroplet.setSize(target_size.getSlug()); // setting size by slug value
        newDroplet.setRegion(new Region(target_region)); // setting region by slug value; sgp1 => Singapore 1 Data center
        newDroplet.setImage(target_snapshot); // setting by Image Id 1601 => centos-5-8-x64 also available in image slug value
        newDroplet.setEnableBackup(Boolean.FALSE);
        newDroplet.setEnableIpv6(Boolean.TRUE);
        newDroplet.setEnablePrivateNetworking(Boolean.FALSE);

        // Add Tags
        List<String> tags = new ArrayList<>();
        tags.add(homer_server.id.toString());
        tags.add("homer_server");
        tags.add(Server.mode.name());

        newDroplet.setTags(tags);

        Droplet droplet = apiClient.createDroplet(newDroplet);

        System.out.println("------------------------------");
        System.out.println("Server Deploy command Done");

        System.out.println("    Server Id:      " + droplet.getId());
        System.out.println("    Server Name:    " + droplet.getName());
        System.out.println("    Server Status:  " + droplet.getStatus().name());
        System.out.println("    Server Tags:    " + droplet.getTags());

        for(Network network : droplet.getNetworks().getVersion6Networks()) {
            System.out.println("    Server URL:    " + network.getIpAddress());
        }

        Swagger_ExternalService service = new Swagger_ExternalService();
        service.type = Enum_ServiceType.BLUE_OCEAN;

        Swagger_BlueOcean blueOcean = new Swagger_BlueOcean();
        blueOcean.id = droplet.getId();

        service.blue_ocean_config = blueOcean;

        homer_server.json_additional_parameter = Json.toJson(service).toString();
        homer_server.update();

        // Start Checking State For First Configuration

    }

    public static void check_status(Model_HomerServer homerServer) throws RequestUnsuccessfulException, DigitalOceanException {

        Swagger_ExternalService help = baseFormFactory.formFromJsonWithValidation(Swagger_ExternalService.class,  Json.parse(homerServer.json_additional_parameter));
        if(help.type == Enum_ServiceType.BLUE_OCEAN) {
            Droplet droplet = apiClient.getDropletInfo(help.blue_ocean_config.id);
            System.out.println("    Server Id:      " + droplet.getId());
            System.out.println("    Server Name:    " + droplet.getName());
            System.out.println("    Server Status:  " + droplet.getStatus().name());
            System.out.println("    Server Tags:    " + droplet.getTags());

            for(Network network : droplet.getNetworks().getVersion6Networks()) {
                System.out.println("    Server URL:    " + network.getIpAddress());
            }
        }

    }

    public static void shutdownServer(Model_HomerServer homerServer) throws RequestUnsuccessfulException, DigitalOceanException {

        Integer droplet_id = 321415263; // TODO get from Homer_Server Config!
        apiClient.shutdownDroplet(droplet_id);
    }



    public static Swagger_ServerRegistration_FormData get_data()  throws RequestUnsuccessfulException, DigitalOceanException {

        List<Swagger_ServerRegistration_FormData_ServerSize> server_sizes = new ArrayList<>();

        System.out.println("Start Wit Requesting");
        List<Size> sizes = apiClient.getAvailableSizes(0).getSizes();
        List<Region> regions =  apiClient.getAvailableRegions(0).getRegions();
        System.out.println("All data in Cache");

        for(Size size : sizes) {

            System.out.println("Size");
            System.out.println("     Slug:          " + size.getSlug());
            System.out.println("     Price Hourly:  " + size.getPriceHourly());
            System.out.println("     Price Monthly: " + size.getPriceMonthly());
            System.out.println("     Memory Size:   " + size.getMemorySizeInMb());
            System.out.println("     Disk Size:     " + size.getVirutalCpuCount());

            Swagger_ServerRegistration_FormData_ServerSize server_size = new Swagger_ServerRegistration_FormData_ServerSize();
            server_size.slug = size.getSlug();
            server_size.price_hourly = size.getPriceHourly().multiply(new BigDecimal(1.5));
            server_size.price_monthly = size.getPriceMonthly().multiply(new BigDecimal(1.5)).setScale(3, RoundingMode.CEILING);
            server_size.memory = size.getMemorySizeInMb() / 1000;
            server_size.vcpus = size.getVirutalCpuCount();

           List<String> regions_in_string = size.getRegions();

           System.out.println("     ");
           System.out.println("     Regions ----------------------------------------------");
           loop1: for(String region_slug_name: regions_in_string) {

               loop2: for(Region region : regions) {
                   if(region.getSlug().equals(region_slug_name)) {

                       System.out.println("     Region");
                       System.out.println("         Slug:          " + region.getSlug());
                       System.out.println("         Is Available:  " + region.isAvailable());

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

        System.out.println("Complete Done Resonse is completed ----------------------------------------------");
        return data;
    }
}
