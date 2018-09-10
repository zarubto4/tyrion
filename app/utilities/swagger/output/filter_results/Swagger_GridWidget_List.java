package utilities.swagger.output.filter_results;

import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_CProgram;
import models.Model_Widget;
import utilities.swagger.input._Swagger_filter_parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Individual GridWidget List",
        value = "GridWidget_List")
public class Swagger_GridWidget_List extends _Swagger_Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_Widget> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_GridWidget_List() {

    }

    public Swagger_GridWidget_List(Query<Model_Widget> query, int page_number, _Swagger_filter_parameter filter) {

        if (page_number < 1) page_number = 1;

        List<UUID> ids = query.setFirstRow((page_number - 1) * filter.count_on_page).setMaxRows(filter.count_on_page).select("id").findSingleAttributeList();

        for (UUID id : ids) {
            this.content.add( Model_Widget.find.byId(id));
        }

        this.total  = query.findCount();
        this.from   = (page_number - 1) * filter.count_on_page;
        this.to     = (page_number - 1) * filter.count_on_page + content.size();
        this.pages  = (total / filter.count_on_page);
    }
}
