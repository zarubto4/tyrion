package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.List;

@ApiModel(description = "Json Model with Library Compilation",
          value = "CompilationLibrary")
public class Swagger_CompilationLibrary {

    @ApiModelProperty(required = true, example = "v1.0.1") public String tag_name;
    @ApiModelProperty(required = false)  public String name;
    @ApiModelProperty(required = false)  public String body;
    @ApiModelProperty(required = true)  public boolean draft;
    @ApiModelProperty(required = true)  public boolean prerelease;
    @ApiModelProperty(required = true) public String created_at;
    @ApiModelProperty(required = true)  public String published_at;

}
