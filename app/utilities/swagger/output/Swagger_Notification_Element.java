package utilities.swagger.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.NotificationElement;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;
import java.util.UUID;

@ApiModel(description = "Json Model for notification body elements",
        value = "Notification_Element")
public class Swagger_Notification_Element extends _Swagger_Abstract_Default {

    public Swagger_Notification_Element() {}

    @ApiModelProperty(required =  true)                                 public NotificationElement type = null;


    @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL) public String text = null;

    @ApiModelProperty(required =  true)                                 public boolean bold = false;
    @ApiModelProperty(required =  true)                                 public boolean italic = false;
    @ApiModelProperty(required =  true)                                 public boolean underline = false;
    @ApiModelProperty(required =  true)                                 public boolean button = false;


    @ApiModelProperty(required =  false, dataType = "number", example = "1536424319") @JsonInclude(JsonInclude.Include.NON_NULL)  public Date date  = null;
    @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL)  public String url          = null;
    @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL)  public String name         = null;
    @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL)  public String color        = null;
    @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL)  public UUID id             = null;
    @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL)  public UUID project_id   = null;
    @ApiModelProperty(required =  false) @JsonInclude(JsonInclude.Include.NON_NULL)  public UUID program_id   = null;
}
