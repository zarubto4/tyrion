package utilities.swagger.input;

import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Swagger_InstanceSnapShotConfigurationApiKeys {

    public Swagger_InstanceSnapShotConfigurationApiKeys() {}

    public UUID token;

    public String description;

    public Long created;
    public Long blocked;

}
