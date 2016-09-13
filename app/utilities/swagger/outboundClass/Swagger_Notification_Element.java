package utilities.swagger.outboundClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.notification.Notification.Notification_type;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@ApiModel(description = "Json Model for notification body elements",
        value = "Notification_Element")
public class Swagger_Notification_Element {

    public Swagger_Notification_Element(){}

    @ApiModelProperty(required =  true)    @Enumerated(EnumType.STRING) public Notification_type type = null;
    @ApiModelProperty(required =  true)                                 public boolean required = false;

    @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL)  public String get_url  = null;
    @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL)  public String url      = null;
    @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL)  public String label    = null;

    @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL)  public String value    = null;
    @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL)  public String id       = null;
    @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL)  public String project_id = null;

}
