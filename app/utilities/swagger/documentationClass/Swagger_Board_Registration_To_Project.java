package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "",
        value = "Board_Registration_To_Project")
public class Swagger_Board_Registration_To_Project {

    @ApiModelProperty(required = false)
    public List<String> group_ids;

    @Constraints.Required @ApiModelProperty(required = true) public String project_id;
    @Constraints.Required @ApiModelProperty(required = true) public String hash_for_adding;
}
