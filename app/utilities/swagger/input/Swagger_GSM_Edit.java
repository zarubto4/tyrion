package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApiModel(description = "",
        value = "GSM_Update")
public class Swagger_GSM_Edit {

    @ApiModelProperty(required = false, value = "name or alias can be null or maximum length of 255 characters.")
    public String name;

    @ApiModelProperty(required = false, value = "description can be null or maximum length of 255 characters.")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    public String description;

    @ApiModelProperty(value = "Tags - Optional", required = false)
    public List<String> tags = new ArrayList<>();


    @Constraints.Required public Long    daily_traffic_threshold = 0L;                     // Přípustná hodnota v KB
    @Constraints.Required public boolean daily_traffic_threshold_exceeded_limit;      // Umožnit překročit limit
    @Constraints.Required public boolean daily_traffic_threshold_notify_type;         // Zákazník bude informován o překročení

    @Constraints.Required public Long    monthly_traffic_threshold = 0L;                   // Přípustná hodnota v KB
    @Constraints.Required public boolean monthly_traffic_threshold_exceeded_limit;    // Umožnit překročit limit
    @Constraints.Required public boolean monthly_traffic_threshold_notify_type;       // Zákazník bude informován o překročení

    @Constraints.Required public Long    total_traffic_threshold = 0L;                     // Přípustná hodnota v KB
    @Constraints.Required public boolean total_traffic_threshold_exceeded_limit;      // Umožnit překročit limit
    @Constraints.Required public boolean total_traffic_threshold_notify_type;         // Zákazník bude informován o překročení
}
