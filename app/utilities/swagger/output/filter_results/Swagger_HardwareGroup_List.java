package utilities.swagger.output.filter_results;

import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_HardwareGroup;
import utilities.swagger.input._Swagger_filter_parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Individual Hardware Group List",
        value = "HardwareGroup_List")
public class Swagger_HardwareGroup_List extends _Swagger_Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_HardwareGroup> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_HardwareGroup_List() {

    }

    public Swagger_HardwareGroup_List(Query<Model_HardwareGroup> query , int page_number, _Swagger_filter_parameter filter) {

        if (page_number < 1) page_number = 1;
        List<UUID> uuids =  query.setFirstRow((page_number - 1) * filter.count_on_page).setMaxRows(filter.count_on_page).findIds();

        for (UUID uuid : uuids) {
            this.content.add(Model_HardwareGroup.find.byId(uuid));
        }

        this.total   = query.findCount();
        this.from   = (page_number - 1) * filter.count_on_page;
        this.to     = (page_number - 1) * filter.count_on_page + content.size();
        this.pages = (int) Math.ceil (total / filter.count_on_page.doubleValue());
    }
}
