package utilities.swagger.output.filter_results;

import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_CProgramUpdatePlan;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "", value = "ActualizationProcedureTask_List")
public class Swagger_ActualizationProcedureTask_List extends Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_CProgramUpdatePlan> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_ActualizationProcedureTask_List(Query<Model_CProgramUpdatePlan> query , int page_number) {

        if (page_number < 1) page_number = 1;
        List<Model_CProgramUpdatePlan> list =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).select("id").findList();

        for (Model_CProgramUpdatePlan task_not_cached : list) {
            Model_CProgramUpdatePlan task = Model_CProgramUpdatePlan.getById(task_not_cached.id.toString());
            if (task == null) continue;
            this.content.add(task);
        }

        this.total   = query.findCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
