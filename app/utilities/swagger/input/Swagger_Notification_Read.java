package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for List with ids of read notifications",
             value = "Notification_Read")
public class Swagger_Notification_Read {

    @ApiModelProperty(value = "List of notification.id", required = true)
    public List<UUID> notification_id;


}
