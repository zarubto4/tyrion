package utilities.gsm_services.things_mobile.help_class;

import java.util.ArrayList;
import java.util.List;

public class TM_Sim_List {

    public TM_Sim_List() {}

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
    public Long msisdn;
    public String name;
    public String plan;
    public String status;
    public String tag;
    public Integer totalTraffic;
    public Integer totalTrafficThreshold;

    public List<TM_Sim_List_cdr> cdrs = new ArrayList<>();


}
