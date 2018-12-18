package utilities.gsm_services.things_mobile.help_json_class;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.gsm_services.things_mobile.Controller_Things_Mobile;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

@ApiModel(description = "Json Model for Blocko in Becki for accessible hardware and firmware versions",
        value = "TMSim_RichStatus")
public class TM_Sim_List extends _Swagger_Abstract_Default {

    public TM_Sim_List() {}

    @JsonProperty(value = "activation_date")  @ApiModelProperty(name = "activation_date") public String activationDate;

    public Integer balance;

    @JsonProperty(value = "block_sim_after_expiration_date") @ApiModelProperty(name = "block_sim_after_expiration_date")  public Integer blockSimAfterExpirationDate;




    @JsonProperty(value = "expiration_date")        @ApiModelProperty(name = "expiration_date")   public String expirationDate;
    @JsonProperty(value = "last_connection_date")   @ApiModelProperty(name = "last_connection_date")   public String lastConnectionDate;


    public Long msisdn;

    // Zatím nepodporováno ze strany Things Mobile
    public String iccid; // Optional Value
    public String type; // OnChip Sim


    public String name;
    public String plan;


    @ApiModelProperty(value = "Sim Status", allowableValues = "{active, not active}" )
    public String status;

    @ApiModelProperty(value = "Not Changeable!", readOnly = true)
    public String tag;


    @JsonProperty(value = "daily_traffic")                   @ApiModelProperty(name = "daily_traffic", value = "in bites")           public Long dailyTraffic;
    @JsonProperty(value = "daily_traffic_threshold")         @ApiModelProperty(name = "daily_traffic_threshold", value = "in MB")    public Long dailyTrafficThreshold;

    @JsonProperty(value = "monthly_traffic")              @ApiModelProperty(name = "monthly_traffic", value = "in bites")            public Long monthlyTraffic;
    @JsonProperty(value = "monthly_traffic_threshold")    @ApiModelProperty(name = "monthly_traffic_threshold", value = "in MB")     public Long monthlyTrafficThreshold;

    @JsonProperty(value = "total_traffic")              @ApiModelProperty(name = "total_traffic", value = "in bites")                public Long totalTraffic;
    @JsonProperty(value = "total_traffic_threshold")    @ApiModelProperty(name = "total_traffic_threshold" , value = "in MB")        public Long totalTrafficThreshold;


    @JsonIgnore public Integer blockSimDaily;
    @JsonIgnore public Integer blockSimMonthly;
    @JsonIgnore public Integer blockSimTotal;

    @JsonIgnore
    public List<TM_Sim_Status_cdr> cdrs = new ArrayList<>();

    @JsonIgnore
    private String country;

    /**
     * Same as in
     * @see TM_Sim_Status_cdr
     * @return
     */
    @JsonProperty
    @ApiModelProperty(value = "Last know Country")
    public String country() {

        if(country == null ) {

            if(cdrs.size() < 5) {
                country = "";
            } else {
                country = cdrs.get(cdrs.size() - 1).cdrCountry;
            }
        }

        return country;
    }

    @JsonProperty
    public Double month_cost() {
        try {
            if(monthlyTraffic == null) return null;
            return monthlyTraffic / 1024 / 1024 * Controller_Things_Mobile.price_per_MB;

        } catch (Exception e) {
            return null;
        }
    }

    @JsonProperty
    public boolean block_sim_daily() {
        return blockSimDaily == 1;
    }

    @JsonProperty
    public boolean block_sim_monthly() {
        return blockSimMonthly == 1;
    }

    @JsonProperty
    public boolean block_sim_total() {
        return blockSimTotal == 1;
    }

    /**
     * Same as in
     * @see TM_Sim_Status_cdr
     * @return
     */
    @JsonProperty
    @ApiModelProperty(name = "days_from_activation")
    public Long days_from_activation() {
        try {

            if(activationDate == null || activationDate.equals("") ) return null;

            return DAYS.between(LocalDate.parse(activationDate, TM_Sim_Status_cdr.formatter_from_tm), LocalDate.now());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Same as in
     * @see TM_Sim_Status_cdr
     * @return
     */
    // Time In Millis
    @JsonProperty
    @ApiModelProperty(name = "activation_date")
    public Long getActivationDate() {
        try {
            if(activationDate == null || activationDate.equals("") ) return null;
            return LocalDate.parse(activationDate, TM_Sim_Status_cdr.formatter_from_tm).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() / 1000;
        } catch (Exception e) {
            System.out.println("Error: getAsLong_ActivationDate:: activationDate is null: " + activationDate);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Same as in
     * @see TM_Sim_Status_cdr
     * @return
     */
    // Time In Millis
    @JsonProperty
    @ApiModelProperty(name = "expiration_date")
    public Long getExpirationDate() {
        try {
            if (expirationDate == null || expirationDate.equals("")) return -1L;
            return LocalDate.parse(expirationDate, TM_Sim_Status_cdr.formatter_from_tm).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() / 1000;
        } catch (Exception e) {
            System.out.println("Error: getAsLong_ExpirationDate is null: " + activationDate);
            return null;
        }
    }


    /**
     * Same as in
     * @see TM_Sim_Status_cdr
     * @return
     */
    // Time In Millis
    @JsonProperty
    @ApiModelProperty(name = "last_connection")
    public Long getLastConnectionDate() {
        try {
            if (lastConnectionDate == null || lastConnectionDate.equals("")) return -1L;
            return LocalDate.parse(lastConnectionDate, TM_Sim_Status_cdr.formatter_from_tm).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() / 1000;
        } catch (Exception e) {
            System.out.println("activationDate: " + activationDate);
            return null;
        }
    }


    /**
     * Same as in
     * @see TM_Sim_Status_cdr
     * @return
     */
    @JsonIgnore
    public Long cdrImsi() {

        if(cdrs.isEmpty()) return null;
        return cdrs.get(0).cdrImsi;
    }
}
