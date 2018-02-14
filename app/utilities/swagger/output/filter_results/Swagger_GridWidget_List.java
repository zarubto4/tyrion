package utilities.swagger.output.filter_results;

import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Widget;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual GridWidget List",
        value = "GridWidget_List")
public class Swagger_GridWidget_List extends _Swagger_Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_Widget> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_GridWidget_List(Query<Model_Widget> query, int page_number) {

        if (page_number < 1) page_number = 1;
        List<Model_Widget> grid_widgets =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).select("id").findList();

        for (Model_Widget gridWidget_not_cached : grid_widgets) {
            Model_Widget gridWidget = Model_Widget.getById(gridWidget_not_cached.id);
            if (gridWidget == null) continue;

            this.content.add(gridWidget);
        }

        this.total  = query.findCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages  = (total / 25);
    }
}
