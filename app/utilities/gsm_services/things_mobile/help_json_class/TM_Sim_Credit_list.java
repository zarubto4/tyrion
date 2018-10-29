package utilities.gsm_services.things_mobile.help_json_class;

import com.fasterxml.jackson.annotation.JsonInclude;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class TM_Sim_Credit_list extends _Swagger_Abstract_Default {

    public double amount;
    public String currency;
    public boolean done;

    public List<TM_Sim_Credit> historyRow = new ArrayList<>();

    @JsonInclude(value = NON_NULL)
    public Integer errorCode;

    @JsonInclude(value = NON_NULL)
    public String errorMessage;
}
