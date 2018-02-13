package utilities.swagger.output.filter_results;

import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_HardwareGroup;
import models.Model_HomerServer;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual Hardware Group List",
        value = "HardwareGroup_List")
public class Swagger_HardwareGroup_List extends Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_HardwareGroup> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_HardwareGroup_List(Query<Model_HardwareGroup> query , int page_number) {

        if (page_number < 1) page_number = 1;
        List<Model_HardwareGroup> list =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).findList();

        for (Model_HardwareGroup group : list) {
            this.content.add(group);
        }

        this.total   = query.findCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
