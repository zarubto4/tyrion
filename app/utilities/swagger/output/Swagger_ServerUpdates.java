package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.Server;
import utilities.swagger.input.Swagger_GitHubReleases;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApiModel(description = "Json Model with available server updates",
        value = "ServerUpdates")
public class Swagger_ServerUpdates extends _Swagger_Abstract_Default {

    public String current = "v" + Server.version;

    public List<Swagger_GitHubReleases> releases = new ArrayList<>();

    public String schedule_release;

    @ApiModelProperty(required = true, readOnly = true, dataType = "integer", example = "1536424319")
    public Date schedule_release_time;

}