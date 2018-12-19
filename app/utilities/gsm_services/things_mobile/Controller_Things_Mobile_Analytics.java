package utilities.gsm_services.things_mobile;

import mongo.ModelMongo_ThingsMobile_CRD;
import xyz.morphia.query.Query;
import utilities.Server;
import utilities.enums.TimePeriod;
import utilities.gsm_services.things_mobile.help_json_class.TM_Sim_Status_cdr;
import utilities.gsm_services.things_mobile.statistic_class.DataSim_DataGram;
import utilities.gsm_services.things_mobile.statistic_class.DataSim_overview;
import utilities.logger.Logger;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mongodb.MongoClient;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import utilities.swagger.input.Swagger_DataConsumption_Filter;

public class Controller_Things_Mobile_Analytics {
    
/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    // Logger
    private static final Logger logger = new Logger(Controller_Things_Mobile.class);

/* CONTENT  -------------------------------------------------------------------------------------------------------------*/
    
    public static DataSim_overview group_stats(List<Long> msi_number, Swagger_DataConsumption_Filter filter, LocalDateTime from, LocalDateTime to, TimePeriod time_period) {

        logger.trace(" MSI to find: {} ", msi_number);

        Query<ModelMongo_ThingsMobile_CRD> cdrs_query = ModelMongo_ThingsMobile_CRD.find.query()
                .field("msisdn").in(msi_number)
                .field("cdrDateStart").greaterThanOrEq( from.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .field("cdrDateStop").lessThanOrEq(to.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

        logger.trace("Count CRD: ", cdrs_query.count());


        if(filter.country_code != null && !filter.country_code.isEmpty() && !filter.country_code.contains("ALL")) {

            logger.trace("Country code not null:? {} ", filter.country_code);

            // Objekt do ktereho vsechno ulozim - vsech 12 mesicu
            List<String> country_names = new ArrayList<>();

            for(String code : filter.country_code){
                Locale obj = new Locale("eng", code);
                logger.trace("Country to Check:: getDisplayCountry: {}", obj.getDisplayCountry(Locale.ENGLISH));
                logger.trace("Country to Check:: getDisplayName:    {}", obj.getDisplayName(Locale.ENGLISH));
                logger.trace("Country to Check:: getCountry:        {}", obj.getCountry());
                country_names.add(obj.getDisplayCountry(Locale.ENGLISH));
            }

            filter.country_code = country_names;

            cdrs_query.field("cdrCountry").in(country_names);
        }

        return divide(cdrs_query.asList(), filter);
    }


    private static DataSim_overview divide(List<ModelMongo_ThingsMobile_CRD> crds, Swagger_DataConsumption_Filter filter) {

        DataSim_overview overview = new DataSim_overview();

        // Datagram konkretniho casoveho obdobi
        DataSim_DataGram first_datagram = new DataSim_DataGram();
        first_datagram.date_from = filter.date_from();

        if(filter.time_period == TimePeriod.MONTH) first_datagram.date_to = filter.date_from().plusMonths(1);
        if(filter.time_period == TimePeriod.WEEK) first_datagram.date_to = filter.date_from().plusWeeks(1);
        if(filter.time_period == TimePeriod.DAY) first_datagram.date_to = filter.date_from().plusDays(1);
        if(filter.time_period == TimePeriod.HOUR) first_datagram.date_to = filter.date_from().plusHours(1);

        overview.datagram = new ArrayList<>();
        overview.datagram.add(first_datagram);


        int pointer = 0;

        while (true) {


           // logger.trace("While:: From {} To {}",  overview.datagram.get(overview.datagram.size() - 1).date_from(),  overview.datagram.get(overview.datagram.size() - 1).date_to());

            while (true) {

                // logger.trace("While:: While: Pointer {}, datagram: {}", pointer, overview.datagram.get(overview.datagram.size() - 1));

                if (crds.isEmpty() || crds.size() <= pointer){
                    // logger.trace("While:: While:crds.size() < pointer = break");
                    break;
                }

                logger.trace("While:: While: Pointer {} cdr size {}", pointer, crds.size());
                logger.trace("While:: While: CDR from {} to {}", crds.get(pointer).cdr_date_stop, crds.get(pointer).cdr_date_start);

                // Tady potřebujeme porovnat zda date start je později než date_fist
                if ( crds.get(pointer).cdr_date_start.isBefore( overview.datagram.get(overview.datagram.size() - 1).date_to ) && crds.get(pointer).cdr_date_stop.isAfter(overview.datagram.get(overview.datagram.size() - 1).date_from)) {

                    // logger.trace("While:: While: Condition is ok");

                    // Udělám Záznam do správného dne
                    overview.datagram.get(overview.datagram.size() - 1).data_consumption += crds.get(pointer).cdr_traffic;

                    // Per County
                    if(!filter.country_code.isEmpty() && (filter.country_code.contains("ALL") || filter.country_code.contains(crds.get(pointer).cdr_country))) {

                        if(!overview.datagram.get(overview.datagram.size() - 1).data_traffic_by_country.containsKey(crds.get(pointer).cdr_country)){
                            overview.datagram.get(overview.datagram.size() - 1).data_traffic_by_country.put(crds.get(pointer).cdr_country, 0L);
                        }

                        overview.datagram.get(overview.datagram.size() - 1).data_traffic_by_country.put(crds.get(pointer).cdr_country, overview.datagram.get(overview.datagram.size() - 1).data_traffic_by_country.get(crds.get(pointer).cdr_country) + crds.get(pointer).cdr_traffic);

                    }

                    pointer++;
                } else {
                    logger.trace("While:: While: Nesplnena podminka::  CDR from {} to {}", crds.get(pointer).cdr_date_start , crds.get(pointer).cdr_date_stop);
                    break;
                }

            }

            DataSim_DataGram next_datagram = new DataSim_DataGram();

            if(filter.time_period == TimePeriod.MONTH) next_datagram.date_from = overview.datagram.get(overview.datagram.size() - 1).date_from.plusMonths(1);
            if(filter.time_period == TimePeriod.MONTH) next_datagram.date_to =  overview.datagram.get(overview.datagram.size() - 1).date_to.plusMonths(1);

            if(filter.time_period == TimePeriod.WEEK) next_datagram.date_from = overview.datagram.get(overview.datagram.size() - 1).date_from.plusWeeks(1);
            if(filter.time_period == TimePeriod.WEEK) next_datagram.date_to =  overview.datagram.get(overview.datagram.size() - 1).date_to.plusWeeks(1);

            if(filter.time_period == TimePeriod.DAY) next_datagram.date_from = overview.datagram.get(overview.datagram.size() - 1).date_from.plusDays(1);
            if(filter.time_period == TimePeriod.DAY) next_datagram.date_to =  overview.datagram.get(overview.datagram.size() - 1).date_to.plusDays(1);

            if(filter.time_period == TimePeriod.HOUR) next_datagram.date_from = overview.datagram.get(overview.datagram.size() - 1).date_from.plusHours(1);
            if(filter.time_period == TimePeriod.HOUR) next_datagram.date_to =  overview.datagram.get(overview.datagram.size() - 1).date_to.plusHours(1);

            // And of period
            if(next_datagram.date_from.isAfter(filter.date_to())) {
                break;
            }

            overview.datagram.add(next_datagram);

        }

        return overview;

    }


    /**
     * Není saportování Free Tierem
     * - Odloženo na neurčito
     */
    private static void main() {

        // Connect to mongodb


        String map = "function () {"+
                        "emit('cdrTraffic', {count:1});"+
                    "}";

        String reduce = "function (key, values) { "+
                " total = 0; "+
                " for (var i in values) { "+
                " total += values[i].count; "+
                " } "+
                " return {count:total} }";


        MapReduceCommand cmd = new MapReduceCommand(ModelMongo_ThingsMobile_CRD.find.getCollection(), map, reduce,
                null, MapReduceCommand.OutputType.INLINE, null);

        MapReduceOutput out = ModelMongo_ThingsMobile_CRD.find.getCollection().mapReduce(cmd);

        for (DBObject o : out.results()) {
            System.out.println(o.toString());
        }
        System.out.println("Done");

    }

}
