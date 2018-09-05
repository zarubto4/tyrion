package utilities.swagger.output.filter_results;

import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_UpdateProcedure;
import utilities.swagger.input._Swagger_filter_parameter;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "",
        value = "ActualizationProcedure_List")
public class Swagger_ActualizationProcedure_List extends _Swagger_Filter_Common {

    /* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_UpdateProcedure> content = new ArrayList<>();

    /* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_ActualizationProcedure_List(Query<Model_UpdateProcedure> query , int page_number, _Swagger_filter_parameter filter) {

        if (page_number < 1) page_number = 1;

        for (Model_UpdateProcedure procedure_not_cached : query.setFirstRow((page_number - 1) * filter.count_on_page).setMaxRows(filter.count_on_page).select("id").findList()) {
            Model_UpdateProcedure procedure = Model_UpdateProcedure.find.byId(procedure_not_cached.id);
            if (procedure == null) continue;
            this.content.add(procedure);
        }

        this.total   = query.findCount();
        this.from   = (page_number - 1) * filter.count_on_page;
        this.to     = (page_number - 1) * filter.count_on_page + content.size();
        this.pages = (total / filter.count_on_page);
    }
}
