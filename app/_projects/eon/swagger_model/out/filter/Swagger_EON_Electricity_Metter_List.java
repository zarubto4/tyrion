package _projects.eon.swagger_model.out.filter;

import _projects.eon.mongo_model.ModelMongo_Electricity_meter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import mongo.ModelMongo_ThingsMobile_CRD;
import utilities.swagger.input._Swagger_filter_parameter;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;
import utilities.swagger.output.filter_results._Swagger_Filter_Common;
import xyz.morphia.query.FindOptions;
import xyz.morphia.query.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApiModel(description = "Json Model for EON",
        value = "EON_Electricity_Metter_List")
public class Swagger_EON_Electricity_Metter_List  extends _Swagger_Filter_Common {
    /* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<ModelMongo_Electricity_meter> content = new ArrayList<>();


    /* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_EON_Electricity_Metter_List(Query<ModelMongo_Electricity_meter> query , int page_number, _Swagger_filter_parameter filter) {

        if (page_number < 1) page_number = 1;

        content = query.asList(
                new FindOptions()
                        .skip((page_number - 1) * filter.count_on_page)
                        .batchSize(filter.count_on_page)
        );

        this.total   = (int) query.count();
        this.from   = (page_number - 1) * filter.count_on_page;
        this.to     = (page_number - 1) * filter.count_on_page + content.size();
        this.pages = (int) Math.ceil (total / filter.count_on_page.doubleValue());
    }
}

