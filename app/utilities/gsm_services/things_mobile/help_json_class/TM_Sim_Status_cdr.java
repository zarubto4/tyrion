package utilities.gsm_services.things_mobile.help_json_class;

import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

public class TM_Sim_Status_cdr extends _Swagger_Abstract_Default {
    public TM_Sim_Status_cdr() {}
    public Long cdrImsi;
    public String cdrDateStart;
    public String cdrDateStop;
    public String cdrNetwork;
    public String cdrCountry;
    public Float cdrTraffic;
}
