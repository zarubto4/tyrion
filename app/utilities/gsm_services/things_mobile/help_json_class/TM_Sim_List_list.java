package utilities.gsm_services.things_mobile.help_json_class;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.ArrayList;
import java.util.List;

public class TM_Sim_List_list extends _Swagger_Abstract_Default {

    public List<TM_Sim_List> sims = new ArrayList<>();

    public boolean done;
    public Integer errorCode;
    public String errorMessage;

}
