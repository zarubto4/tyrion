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


    @ApiModelProperty(required = true, value = "Value in MB (Only round)")
    @Constraints.Required public int     daily_traffic_threshold;                     // Přípustná hodnota v b
    @Constraints.Required public boolean block_sim_daily;      // Umožnit překročit limit
    @Constraints.Required public boolean daily_traffic_threshold_notify_type;         // Zákazník bude informován o překročení

    @ApiModelProperty(required = true, value = "Value in MB (Only round)")
    @Constraints.Required public int    monthly_traffic_threshold ;                   // Přípustná hodnota v b
    @Constraints.Required public boolean block_sim_monthly;    // Umožnit překročit limit
    @Constraints.Required public boolean monthly_traffic_threshold_notify_type;       // Zákazník bude informován o překročení

    @ApiModelProperty(required = true, value = "Value in MB (Only round)")
    @Constraints.Required public int     total_traffic_threshold ;                     // Přípustná hodnota v b
    @Constraints.Required public boolean block_sim_total;      // Umožnit překročit limit
    @Constraints.Required public boolean total_traffic_threshold_notify_type;         // Zákazník bude informován o překročení

    @Constraints.Required public boolean daily_statistic;
    @Constraints.Required public boolean weekly_statistic;
    @Constraints.Required public boolean monthly_statistic;
}
