package utilities.homer_auto_update;

import com.google.inject.Inject;
import com.myjeeva.digitalocean.DigitalOcean;
import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;
import com.myjeeva.digitalocean.impl.DigitalOceanClient;
import com.myjeeva.digitalocean.pojo.*;
import controllers._BaseFormFactory;
import models.Model_HomerServer;
import utilities.Server;
import utilities.logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class DigitalOceanTyrionService {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger terminal_logger = new Logger(DigitalOceanTyrionService.class);

    /*  VALUES -------------------------------------------------------------------------------------------------------------*/

    @Inject public static _BaseFormFactory baseFormFactory; // Its Required to set this in Server.class Component
    public static  DigitalOcean apiClient = null;

    public DigitalOceanTyrionService(){

        // Way two, pass on version number & authToken
        this.apiClient = new DigitalOceanClient( "42b67cd7450e5301121fb85f34d6ce39f86f7665c496e9f324c8b0dcb0ff3cfa");

    }

    public static void create_server(Model_HomerServer homer_server) throws RequestUnsuccessfulException, DigitalOceanException {

        // Fetching all the available droplets from control panel
        Images images = apiClient.getAvailableImages(0, 25); // As with any large collection returned by the API, the results will be paginated with only 25 on each page by default.

        Regions regions = apiClient.getAvailableRegions(0);    // As with any large collection returned by the API, the results will be paginated with only 25 on each page by default.
        Sizes sizes = apiClient.getAvailableSizes(0); // As with any large collection returned by the API, the results will be paginated with only 25 on each page by default.

        // Fetching all the available kernels for droplet
        Account account = apiClient.getAccountInfo();

        // Create a new droplet
        Droplet newDroplet = new Droplet();
        newDroplet.setName("homer_server_" + homer_server.id.toString());
        newDroplet.setSize(new Size("512mb").getSlug()); // setting size by slug value
        newDroplet.setRegion(new Region("sgp1")); // setting region by slug value; sgp1 => Singapore 1 Data center
        newDroplet.setImage(new Image(1601)); // setting by Image Id 1601 => centos-5-8-x64 also available in image slug value
        newDroplet.setEnableBackup(Boolean.TRUE);
        newDroplet.setEnableIpv6(Boolean.TRUE);
        newDroplet.setEnablePrivateNetworking(Boolean.TRUE);

        // Add Tags
        List<String> tags = new ArrayList<>();
        tags.add(homer_server.id.toString());
        tags.add("homer_server");
        tags.add(Server.mode.name());

        newDroplet.setTags(tags);

        // Adding Metadata API - User Data
        newDroplet.setUserData(" < YAML Content > "); // Follow DigitalOcean documentation to prepare user_data value
       // Droplet droplet = apiClient.createDroplet(newDroplet);
    }

    public static void shutdownServer(Model_HomerServer homerServer) throws RequestUnsuccessfulException, DigitalOceanException {

        Integer droplet_id = 321415263; // TODO get from Homer_Server Config!
        apiClient.shutdownDroplet(droplet_id);
    }
}
