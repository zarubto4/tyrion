package utilities.gsm_services.things_mobile.statistic_class;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataSim_DataGram {

    public void DataSim_DataGram(){}
    public String period_name;
    public Long from;
    public Long to;
    public Long data_consumption; // v KB
    public List<DataSim_DataGram> detailed_datagram = new ArrayList<>();

}