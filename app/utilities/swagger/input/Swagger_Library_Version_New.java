package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for new Version of Library",
        value = "Library_Version_New")
public class Swagger_Library_Version_New extends Swagger_NameAndDescription {

    @ApiModelProperty(required = false)
    @Valid public List<Swagger_Library_Record> files = new ArrayList<>();
}
