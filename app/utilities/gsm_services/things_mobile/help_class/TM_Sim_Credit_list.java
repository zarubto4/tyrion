package utilities.gsm_services.things_mobile.help_class;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class TM_Sim_Credit_list {

    public double amount;
    public String currency;
    public boolean done;
    public Integer errorCode;
    public String errorMessage;

    public List<TM_Sim_Credit> historyRow = new ArrayList<>();
}
