package utilities.swagger.outboundClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Notification_Type;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@ApiModel(description = "Json Model for Notification body=[Array of Notification_Element]",
          value = "Notification_Element")
public class Swagger_Notification_Element {

        public Swagger_Notification_Element(){}

        @ApiModelProperty(required =  true)    @Enumerated(EnumType.STRING) public Notification_Type type = null;
        @ApiModelProperty(required =  true)                                 public boolean required = false;

        @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL)  public String get_url  = null;
        @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL)  public String url      = null;
        @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL)  public String label    = null;

        @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL)  public String value    = null;
        @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL)  public String id       = null;

}
