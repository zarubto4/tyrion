package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.UUID;

@ApiModel(value = "InstanceSnapshot_JsonFile_Interface")
public class Swagger_InstanceSnapshot_JsonFile_Interface extends _Swagger_Abstract_Default {

    public Swagger_InstanceSnapshot_JsonFile_Interface() {}

    @Constraints.Required
    public UUID target_id; // Device ID or HW group ID

    @Constraints.Required
    public UUID interface_id; // Id of the C program version!

    @Constraints.Required
    @ApiModelProperty(required = true, allowableValues = "group, hardware")
    public String type; // group or hardware

}
