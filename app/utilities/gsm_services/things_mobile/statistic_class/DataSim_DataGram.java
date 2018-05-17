package utilities.gsm_services.things_mobile.statistic_class;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataSim_DataGram {

    public void DataSim_DataGram(){}
    public String period_name;
    public Long from;
    public Long to;
    public Long data_consumption = 0L; // v KB

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<DataSim_DataGram> detailed_datagram;

}