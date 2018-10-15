package utilities.gsm_services.things_mobile.help_json_class;

import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.ArrayList;
import java.util.List;

public class TM_Sim_Status extends _Swagger_Abstract_Default {

    public TM_Sim_Status() {}

    public String activationDate;
    public Integer balance;
    public Integer blockSimAfterExpirationDate;
    public Integer blockSimDaily;
    public Integer blockSimMonthly;
    public Integer blockSimTotal;
    public Integer dailyTraffic;
    public Integer dailyTrafficThreshold;
    public String expirationDate;
    public String lastConnectionDate;
    public Integer monthlyTraffic;
    public Integer monthlyTrafficThreshold;
    public String msisdn;
    public String name;
    public String plan;
    public String status;
    public String tag;
    public Integer totalTraffic;
    public Integer totalTrafficThreshold;

    public List<TM_Sim_Status_cdr> cdrs = new ArrayList<>();


    public boolean done;
    public Integer errorCode;
    public String errorMessage;

}
