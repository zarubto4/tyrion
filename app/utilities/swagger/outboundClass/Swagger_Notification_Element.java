package utilities.swagger.outboundClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Notification_type;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@ApiModel(description = "Json Model for notification body elements",
        value = "Notification_Element")
public class Swagger_Notification_Element {

    public Swagger_Notification_Element(){}

    @ApiModelProperty(required =  true)    @Enumerated(EnumType.STRING) public Enum_Notification_type type = null;
    @ApiModelProperty(required =  false)@JsonInclude(JsonInclude.Include.NON_NULL) public String text = null;

    @ApiModelProperty(required =  true)                                 public boolean bold = false;
    @ApiModelProperty(required =  true)                                 public boolean italic = false;
    @ApiModelProperty(required =  true)                                 public boolean underline = false;
    @ApiModelProperty(required =  true)                                 public boolean button = false;

    @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL)  public String url      = null;
    @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL)  public String name    = null;
    @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL)  public String color    = null;
    @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL)  public String id       = null;
    @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL)  public String project_id = null;
    @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL)  public String program_id = null;
}
