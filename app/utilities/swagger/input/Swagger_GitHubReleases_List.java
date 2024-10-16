package utilities.swagger.input;

import io.swagger.annotations.ApiModel;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model - Private for List of GitHub Releases",
          value = "GitHubReleases_List")
public class Swagger_GitHubReleases_List {

    @Valid
    public List<Swagger_GitHubReleases> list = new ArrayList<>();
}
