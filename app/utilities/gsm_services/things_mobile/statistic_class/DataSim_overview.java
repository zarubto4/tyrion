package utilities.gsm_services.things_mobile.statistic_class;

import models.Model_GSM;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.ArrayList;
import java.util.List;

public class DataSim_overview extends _Swagger_Abstract_Default {
    public void DataSim_overview(){}
    public List<DataSim_DataGram> datagram = new ArrayList<>();
}

// TODO MARTIN - Takto by měl vypadat na konci zpracovan datagram

    /*
       {
        "datagram" : [
            {
                "period_name" : "leden",
                "from" : 131231231231,
                "to" : 123141231312331,
                "data_consumption" : 31412213,
                "detailed_datagram" : [
                    {
                         "period_name" : "week-1",
                         "from" : 131231231231,
                         "to" : 123141231312331,
                         "data_consumption" : 31311,
                         "detailed_datagram" : []
                    },
                    {
                         "period_name" : "week-2",
                         "from" : 131231231231,
                         "to" : 123141231312331,
                         "data_consumption" : 31311.
                         "detailed_datagram" : []
                    },
                    {
                         "period_name" : "week-3",
                         "from" : 131231231231,
                         "to" : 123141231312331,
                         "data_consumption" : 1231231.
                         "detailed_datagram" : []
                    }
                ]
            },
            {
                "period_name" : "unor",
                "from" : 131231231231,
                "to" : 123141231312331,
                "data_consumption" : 32123,
                "detailed_datagram" : [
                    {
                         "period_name" : "day-1",
                         "from" : 131231231231,
                         "to" : 123141231312331,
                         "data_consumption" : 31311,
                         "detailed_datagram" : []
                    },
                    {
                         "period_name" : "week-6",
                         "from" : 131231231231,
                         "to" : 123141231312331,
                         "data_consumption" : 31311.
                         "detailed_datagram" : []
                    },
                    {
                         "period_name" : "week-7",
                         "from" : 131231231231,
                         "to" : 123141231312331,
                         "data_consumption" : 1231231.
                         "detailed_datagram" : []
                    }
                ]
            }
       ]
       }
     */