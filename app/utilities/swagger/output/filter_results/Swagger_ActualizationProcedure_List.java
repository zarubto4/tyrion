package utilities.swagger.output.filter_results;

import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_ActualizationProcedure;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "",
    value = "ActualizationProcedure_List")
public class Swagger_ActualizationProcedure_List extends Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_ActualizationProcedure> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_ActualizationProcedure_List(Query<Model_ActualizationProcedure> query , int page_number) {

        if (page_number < 1) page_number = 1;

        for (Model_ActualizationProcedure procedure_not_cached : query.setFirstRow((page_number - 1) * 25).setMaxRows(25).select("id").findList()) {
            Model_ActualizationProcedure procedure = Model_ActualizationProcedure.getById(procedure_not_cached.id);
            if (procedure == null) continue;
            this.content.add(procedure);
        }

        this.total   = query.findCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
