package utilities.gsm_services.things_mobile.help_json_class;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class TM_Sim_Status_cdr extends _Swagger_Abstract_Default {

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TM_Sim_Status_cdr() {}
    public Long cdrImsi;

    // DATE
    public String cdrDateStart;
    public String cdrDateStop;

    // String
    public String cdrNetwork;

    // String
    public String cdrCountry;

    // Trafic
    public Long cdrTraffic; // In Bitees

    /**
     * Same as in
     * @see TM_Sim_List
     * @return
     */
    // Time In Millis
    @JsonProperty
    @ApiModelProperty(name = "cdr_date_stop", dataType = "integer", example = "1536424319")
    public LocalDateTime getAsLong_CdrDateStop() {
        try {

            return LocalDateTime.parse(cdrDateStop, formatter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Same as inLocalDate.parse
     * @see TM_Sim_List
     * @retur
     */
    // Time In Millis
    @JsonProperty()
    @ApiModelProperty(name = "cdr_date_start")
    public LocalDateTime getAsLong_CdrDateStart() {
        try {
            return LocalDateTime.parse(cdrDateStart, formatter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @JsonProperty()
    @ApiModelProperty(name = "data_in_bites")
    public Long data_in_bites(){
        return cdrTraffic;
    }

    @JsonProperty()
    @ApiModelProperty(name = "data_in_kilo_bites")
    public Long data_in_kb(){
        return cdrTraffic / 1024;
    }

    @JsonProperty()
    @ApiModelProperty(name = "data_in_mega_bites")
    public Long data_in_mb(){
        return cdrTraffic / 1024 / 1024;
    }



}
