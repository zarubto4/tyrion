package utilities.swagger.output.filter_results;

import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Hardware;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual Hardware List",
          value = "Hardware_List")
public class Swagger_Hardware_List extends _Swagger_Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_Hardware> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_Hardware_List(Query<Model_Hardware> query , int page_number) {

        if (page_number < 1) page_number = 1;

        for (Model_Hardware board_not_cached : query.setFirstRow((page_number - 1) * 25).setMaxRows(25).select("id").findList()) {
            Model_Hardware board = Model_Hardware.getById(board_not_cached.id);
            if (board == null) continue;
            content.add(board);
        }

        this.total   = query.findCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
