package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model - Private for GitHub Release",
          value = "GitHubReleases")
public class Swagger_GitHubReleases {

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

    @Valid
    public List<Swagger_GitHubReleases_Asset> assets = new ArrayList<>();

}
