package utilities.swagger.input;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.enums.TimePeriod;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for getting Data From Filter List",
        value = "DataConsumption_Filter")
public class Swagger_DataConsumption_Filter {

    @ApiModelProperty(required = true, value = "Required if you have not Admin permission")
    public UUID project_id;

    @ApiModelProperty(required = false, value = "Include only if you want to get Roles of given project")
    public List<UUID> sim_id_list;

    @ApiModelProperty(required = false, value = "Include only if you want to get Roles of given project")
    public List<Long> sim_msi_list;

    public boolean blocked = false;

    @ApiModelProperty(required = false, value = "Default Value Year 2018.01.01")
    public Long from = 1514761200000L;

    @ApiModelProperty(required = false, value = "Default Value Year 2018.30.12")
    public Long to = 1594591199000L;

    @ApiModelProperty(required = false, value = "Default value MONTH")
    public TimePeriod time_period = TimePeriod.MONTH;

    @ApiModelProperty(required = false, value = "Default value null, Use ISO 3166-1 alpha-2/alpha-3/numeric Country code. For all set one value \"ALL\"  for get all countries")
    public List<String> country_code = new ArrayList<>();


    @JsonIgnore()
    public LocalDateTime date_from(){
        return  Instant.ofEpochMilli(from).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @JsonIgnore()
    public LocalDateTime date_to(){
        return  Instant.ofEpochMilli(to).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
