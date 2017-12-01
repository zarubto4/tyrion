package utilities.swagger.outboundClass.Filter_List;

import com.avaje.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_ActualizationProcedure;
import models.Model_CProgramUpdatePlan;
import models.Model_Library;
import utilities.swagger.outboundClass.Swagger_ActualizationProcedure_Short_Detail;
import utilities.swagger.outboundClass.Swagger_Library_Short_Detail;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "",
    value = "ActualizationProcedure_List")
public class Swagger_ActualizationProcedure_List extends Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_ActualizationProcedure_Short_Detail> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_ActualizationProcedure_List(Query<Model_ActualizationProcedure> query , int page_number){

        if(page_number < 1) page_number = 1;

        for(Model_ActualizationProcedure procedure_not_cached : query.setFirstRow((page_number - 1) * 25).setMaxRows(25).select("id").findList()){
            Model_ActualizationProcedure procedure = Model_ActualizationProcedure.get_byId(procedure_not_cached.id.toString());
            if(procedure == null) continue;
            this.content.add(procedure.short_detail());
        }

        this.total   = query.findRowCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
