package utilities.gsm_services.things_mobile.help_class;

import java.util.ArrayList;
import java.util.List;

public class TM_Sim_List {

    public TM_Sim_List() {}

    public String activationDate;
    public String balance;
    public String blockSimAfterExpirationDate;
    public String blockSimDaily;
    public String blockSimMonthly;
    public String blockSimTotal;
    public String dailyTraffic;
    public String dailyTrafficThreshold;
    public String expirationDate;
    public String lastConnectionDate;
    public String monthlyTraffic;
    public String monthlyTrafficThreshold;
    public String msisdn;
    public String name;
    public String plan;
    public String status;
    public String tag;
    public String totalTraffic;
    public String totalTrafficThreshold;

    public List<TM_Sim_List_cdr> cdrs = new ArrayList<>();


}
