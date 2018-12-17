package utilities.swagger.output.filter_results;

import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_GridProject;
import models.Model_Widget;
import utilities.swagger.input._Swagger_filter_parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@ApiModel(description = "Individual GridProject List",
        value = "GridProject_List")
public class Swagger_GridProjectList extends _Swagger_Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_GridProject> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_GridProjectList(Query<Model_GridProject> query, int page_number, _Swagger_filter_parameter filter) {

        if (page_number < 1) page_number = 1;

        List<UUID> ids = query.setFirstRow((page_number - 1) * filter.count_on_page).setMaxRows(filter.count_on_page).findIds();

        for (UUID id : ids) {
            this.content.add(Model_GridProject.find.byId(id));
        }

        this.total  = query.findCount();
        this.from   = (page_number - 1) * filter.count_on_page;
        this.to     = (page_number - 1) * filter.count_on_page + content.size();
        this.pages = (int) Math.ceil (total / filter.count_on_page.doubleValue());
    }
}
