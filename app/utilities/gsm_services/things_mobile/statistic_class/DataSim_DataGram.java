package utilities.gsm_services.things_mobile.statistic_class;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.gsm_services.things_mobile.help_json_class.TM_Sim_Status_cdr;

import javax.persistence.CollectionTable;
import javax.persistence.MapKeyColumn;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@ApiModel(description = "Json Model value of filtered values for GSM. Its represent DAY, HOUR, MONTH etc.",
        value = "DataSim_DataGram")
public class DataSim_DataGram {

    public void DataSim_DataGram(){}

    @Constraints.Required
    @ApiModelProperty(required = true)
    public Long data_consumption = 0L; // v KB // Inicializace je nutná

    @Constraints.Required
    @ApiModelProperty(required = true)
    public LocalDateTime date_from;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public LocalDateTime date_to;


    @JsonIgnore
    public HashMap<String, Long> data_traffic_by_country = new HashMap<>();

    @JsonProperty
    @ApiModelProperty(required = true, value = "Represent time in millis")
    public Long long_from() {
        return date_from.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    @JsonProperty
    @ApiModelProperty(required = true, value = "Represent time in millis")
    public Long long_to() {
        return date_from.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    @JsonProperty
    @ApiModelProperty(required = true, value = "Readable value for users")
    public String date_from() {
        return  date_from.format(TM_Sim_Status_cdr.formatter_from_tm);
    }

    @JsonProperty
    @ApiModelProperty(required = true, value = "Readable value for users")
    public String date_to() {
        return  date_to.format(TM_Sim_Status_cdr.formatter_from_tm);
    }

    
    @JsonProperty
    @ApiModelProperty(required = true, value = "List of Pairs with country name and data_consumption", name = "data_traffic_by_country")
    public List<DataSim_CountryDataGram> getdata_traffic_by_country() {
        List<DataSim_CountryDataGram> list = new ArrayList<>();
        
        for (Map.Entry<String, Long> entry : data_traffic_by_country.entrySet())
        {
            DataSim_CountryDataGram dataGram = new DataSim_CountryDataGram();
            dataGram.country = entry.getKey();
            dataGram.data_consumption = entry.getValue();
            list.add(dataGram);
        }
        
        return list;
        
    }

    @ApiModel(description = "Value for parsing per country, only if its required by Filter",
            value = "DataSim_CountryDataGram")
    public class DataSim_CountryDataGram {

        @ApiModelProperty(required = true, value = "Country name")
        public String country;

        public Long data_consumption;
    }
}