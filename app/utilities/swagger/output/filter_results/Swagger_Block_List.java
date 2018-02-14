package utilities.swagger.output.filter_results;

import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Block;
import utilities.swagger.output.Swagger_Blocko_Block_Filter_Detail;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual Blocko Block List",
        value = "Blocko_Block_List")
public class Swagger_Block_List extends _Swagger_Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_Blocko_Block_Filter_Detail> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_Block_List(Query<Model_Block> query, int page_number) {

        if (page_number < 1) page_number = 1;
        List<Model_Block> blocko_blocks =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).select("id").findList();

        for (Model_Block blockoBlock_not_cached : blocko_blocks) {

            Model_Block blockoBlock = Model_Block.getById(blockoBlock_not_cached.id);
            if (blockoBlock == null) continue;

            Swagger_Blocko_Block_Filter_Detail help = new Swagger_Blocko_Block_Filter_Detail();

            help.blocko_block_id = blockoBlock.id;
            help.blocko_block_name = blockoBlock.name;
            help.blocko_block_description = blockoBlock.description;
            help.blocko_block_version_id = blockoBlock.getVersions().size() != 0 ? blockoBlock.getVersions().get(0).id : null;
            help.blocko_block_version_name = blockoBlock.getVersions().size() != 0 ? blockoBlock.getVersions().get(0).name : null;
            help.blocko_block_version_description = blockoBlock.getVersions().size() != 0 ? blockoBlock.getVersions().get(0).description : null;
            //help.blocko_block_type_of_block_id = blockoBlock.type_of_block.id; TODO
            //help.blocko_block_type_of_block_name = blockoBlock.type_of_block.name;
            //help.blocko_block_type_of_block_description = blockoBlock.type_of_block.description;

            this.content.add(help);
        }

        this.total = query.findCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
