package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import utilities.Server;
import utilities.swagger.input.Swagger_GitHubReleases;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model with available server updates",
        value = "ServerUpdates")
public class Swagger_ServerUpdates {

    public String current = "v" + Server.version;

    public List<Swagger_GitHubReleases> releases = new ArrayList<>();
}