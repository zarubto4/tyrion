package utilities.swagger.outboundClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.notification.Notification;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual Notification List for each person",
          value = "Notifications")
public class Swagger_Notification_List {

    public List<Notification> notifications;
    @ApiModelProperty(required = true, readOnly = true, value = "First value position from all notifications. Minimum is 0!")
    public int from;

    @ApiModelProperty(required = true, readOnly = true, value = "Minimum is \"from!\" Maximum is \"total\"")
    public int to;

    @ApiModelProperty(required = true, readOnly = true, value = "Total notifications. All notification has timestamp - Notification Lifetime (expirate) is one month")
    public int total;

    @ApiModelProperty(required = true, readOnly = true, value = "Numbers of pages, which you can call")
    public List<Integer> pages = new ArrayList<>();
}
