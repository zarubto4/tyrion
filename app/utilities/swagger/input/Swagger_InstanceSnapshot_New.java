package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(value = "InstanceSnapshot_New")
public class Swagger_InstanceSnapshot_New extends Swagger_NameAndDescription{

    @Constraints.Required
    public UUID instance_id;

    @Constraints.Required
    public UUID version_id;

    @Constraints.Required
    public String snapshot;


    @Valid
    public List<Interface> interfaces = new ArrayList<>();

    public static class Interface {

        public Interface() {}

        @Constraints.Required
        public UUID target_id; // Device ID or HW group ID

        @Constraints.Required
        public UUID interface_id; // Id of the C program version!

        @Constraints.Required
        @ApiModelProperty(required = true, allowableValues = "group, hardware")
        public String type; // group or hardware
    }
}
