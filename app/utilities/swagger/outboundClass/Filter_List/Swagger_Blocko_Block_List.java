package utilities.swagger.outboundClass.Filter_List;

import com.avaje.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_BProgram;
import models.Model_BlockoBlock;
import utilities.swagger.outboundClass.Swagger_Blocko_Block_Filter_Detail;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual Blocko Block List",
        value = "Blocko_Block_List")
public class Swagger_Blocko_Block_List extends Filter_Common {


/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_Blocko_Block_Filter_Detail> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_Blocko_Block_List(Query<Model_BlockoBlock> query, int page_number){

        if(page_number < 1) page_number = 1;
        List<Model_BlockoBlock> blocko_blocks =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).select("id").findList();

        for(Model_BlockoBlock blockoBlock_not_cached : blocko_blocks){

            Model_BlockoBlock blockoBlock = Model_BlockoBlock.get_byId(blockoBlock_not_cached.id.toString());
            if(blockoBlock == null) continue;

            Swagger_Blocko_Block_Filter_Detail help = new Swagger_Blocko_Block_Filter_Detail();

            help.blocko_block_id = blockoBlock.id.toString();
            help.blocko_block_name = blockoBlock.name;
            help.blocko_block_description = blockoBlock.description;
            help.blocko_block_version_id = blockoBlock.get_blocko_block_versions().size() != 0 ? blockoBlock.get_blocko_block_versions().get(0).id : null;
            help.blocko_block_version_name = blockoBlock.get_blocko_block_versions().size() != 0 ? blockoBlock.get_blocko_block_versions().get(0).version_name : null;
            help.blocko_block_version_description = blockoBlock.get_blocko_block_versions().size() != 0 ? blockoBlock.get_blocko_block_versions().get(0).version_description : null;
            help.blocko_block_type_of_block_id = blockoBlock.type_of_block.id;
            help.blocko_block_type_of_block_name = blockoBlock.type_of_block.name;
            help.blocko_block_type_of_block_description = blockoBlock.type_of_block.description;

            this.content.add(help);
        }

        this.total = query.findRowCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
