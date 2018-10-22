package utilities.gsm_services.things_mobile.help_json_class;

import com.fasterxml.jackson.annotation.JsonInclude;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class TM_Update_Sim_Name extends _Swagger_Abstract_Default {

    public boolean done;

    @JsonInclude(value = NON_NULL)
    public Integer errorCode;

    @JsonInclude(value = NON_NULL)
    public String errorMessage;

}
