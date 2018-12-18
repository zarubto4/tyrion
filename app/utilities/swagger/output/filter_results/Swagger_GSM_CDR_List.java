package utilities.swagger.output.filter_results;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_GSM;
import mongo.ModelMongo_ThingsMobile_CRD;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;
import utilities.swagger.input._Swagger_filter_parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "",
        value = "GSM_CDR_List")
public class Swagger_GSM_CDR_List extends _Swagger_Filter_Common {
    /* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<ModelMongo_ThingsMobile_CRD> content = new ArrayList<>();


    /* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_GSM_CDR_List(Query<ModelMongo_ThingsMobile_CRD> query , int page_number, _Swagger_filter_parameter filter) {

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

