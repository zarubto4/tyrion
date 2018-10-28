package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model - Private for GitHub Release",
          value = "GitHubReleases")
public class Swagger_GitHubReleases extends _Swagger_Abstract_Default {

    public String url;
    public String html_url;
    public String assets_url;
    public String upload_url;
    public String tarball_url;
    public String zipball_url;
    public Integer id;
    public String tag_name;
    public String target_commitish;
    public String name;
    public String body;
    public boolean draft;
    public boolean prerelease;
    public String created_at;
    public String published_at;

    public Swagger_GitHub_author author;

    @Valid
    public List<Swagger_GitHubReleases_Asset> assets = new ArrayList<>();

}
