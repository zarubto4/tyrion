package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.swagger.output.Swagger_InstanceSnapshot_JsonFile_Interface;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(value = "InstanceSnapshot_New")
public class Swagger_InstanceSnapshot_New extends Swagger_NameAndDescription{

    @Constraints.Required
    public UUID version_id;

    @Constraints.Required
    public String snapshot;

    @Valid
    public List<Swagger_InstanceSnapshot_JsonFile_Interface> interfaces = new ArrayList<>();

}
