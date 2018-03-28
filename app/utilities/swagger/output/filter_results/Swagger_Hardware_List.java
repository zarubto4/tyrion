package utilities.swagger.output.filter_results;

import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Hardware;
import models.Model_Widget;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Individual Hardware List",
          value = "Hardware_List")
public class Swagger_Hardware_List extends _Swagger_Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_Hardware> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_Hardware_List(Query<Model_Hardware> query , int page_number) {

        if (page_number < 1) page_number = 1;

        List<UUID> ids = query.setFirstRow((page_number - 1) * 25).setMaxRows(25).findIds();

        for (UUID id :ids) {
            System.out.println("Swagger_Hardware_List: ADD HArdware ID" + id);
            this.content.add(Model_Hardware.getById(id));
        }

        this.total   = query.findCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
