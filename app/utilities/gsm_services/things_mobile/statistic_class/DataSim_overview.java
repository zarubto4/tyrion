package utilities.gsm_services.things_mobile.statistic_class;

import io.swagger.annotations.ApiModel;
import models.Model_GSM;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.ArrayList;
import java.util.List;


@ApiModel(description = "Json Model with list of filtered values for GSM",
        value = "DataSim_Overview")
public class DataSim_overview extends _Swagger_Abstract_Default {

    public void DataSim_overview(){}
    public List<DataSim_DataGram> datagram = new ArrayList<>();

}
