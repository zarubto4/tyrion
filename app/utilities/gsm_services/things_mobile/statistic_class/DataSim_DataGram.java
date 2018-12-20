package utilities.gsm_services.things_mobile.statistic_class;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.gsm_services.things_mobile.Controller_Things_Mobile;
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
    @ApiModelProperty(required = true, value = "unixTime", readOnly = true, dataType = "integer", example = "1536424319")
    public LocalDateTime date_from;

    @ApiModelProperty(required = true, value = "unixTime", readOnly = true, dataType = "integer", example = "1536424319")
    public LocalDateTime date_to;


    @JsonIgnore
    public HashMap<String, Long> data_traffic_by_country = new HashMap<>();

    
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

    @JsonProperty
    public Double month_cost() {
        return  data_consumption / 1024 / 1024 * Controller_Things_Mobile.price_per_MB;
    }

    @ApiModel(description = "Value for parsing per country, only if its required by Filter",
            value = "DataSim_CountryDataGram")
    public class DataSim_CountryDataGram {

        @ApiModelProperty(required = true, value = "Country name")
        public String country;

        public Long data_consumption;
    }
}