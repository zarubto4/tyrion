package utilities.swagger.output.filter_results;

import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Block;
import models.Model_CProgram;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Individual Blocko Block List",
        value = "Block_List")
public class Swagger_Block_List extends _Swagger_Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_Block> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_Block_List(Query<Model_Block> query, int page_number) {

        if (page_number < 1) page_number = 1;
        List<Model_Block> uuids_o =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).select("id").findList();

        List<UUID> uuids = new ArrayList<>();
        for(Model_Block l : uuids_o) {
            uuids.add(l.id);
        }

        for (UUID id : uuids) {
            Model_Block blockoBlock = Model_Block.getById(id);
            this.content.add(blockoBlock);
        }

        this.total = query.findCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
