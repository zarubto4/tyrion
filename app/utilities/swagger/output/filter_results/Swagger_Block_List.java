package utilities.swagger.output.filter_results;

import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Block;
import utilities.swagger.input._Swagger_filter_parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Individual Block List",
        value = "Block_List")
public class Swagger_Block_List extends _Swagger_Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_Block> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_Block_List(){
        // If return empty list!
    }

    public Swagger_Block_List(Query<Model_Block> query, int page_number, _Swagger_filter_parameter filter) {

        if (page_number < 1) page_number = 1;
        List<Model_Block> uuids_o =  query.setFirstRow((page_number - 1) * filter.count_on_page).setMaxRows(filter.count_on_page).select("id").findList();

        List<UUID> uuids = new ArrayList<>();
        for(Model_Block l : uuids_o) {
            uuids.add(l.id);
        }

        for (UUID id : uuids) {
            Model_Block block = Model_Block.find.byId(id);
            this.content.add(block);
        }

        this.total = query.findCount();
        this.from   = (page_number - 1) * filter.count_on_page;
        this.to     = (page_number - 1) * filter.count_on_page + content.size();
        this.pages = (int) Math.ceil (total / filter.count_on_page.doubleValue());
    }
}
