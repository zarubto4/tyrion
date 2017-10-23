package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;

import java.util.List;

@ApiModel(description = "Json Model - Private for GitHub Release Assets",
        value = "GitHubReleases_Asset")
public class Swagger_GitHubReleases_Asset {

    public Swagger_GitHubReleases_Asset(){}

    public String  url;
    public String  browser_download_url;
    public Integer id;
    public String  name;                    // File Name in Case of Library its lib.zip
    public String  label;
    public String  state;
    public String  content_type;
    public Integer size;
    public Integer download_count;
    public String  created_at;
    public String  updated_at;
}
