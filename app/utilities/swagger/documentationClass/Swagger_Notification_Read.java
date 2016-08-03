package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(description = "Json Model for List with ids of read notifications",
             value = "M_Project_New")
public class Swagger_Notification_Read {

    @ApiModelProperty(value = "List of notification.id", required = true)
    public List<String> notification_id;


}
