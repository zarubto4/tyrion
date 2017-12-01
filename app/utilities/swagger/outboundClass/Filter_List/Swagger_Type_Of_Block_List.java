package utilities.swagger.outboundClass.Filter_List;

import com.avaje.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_BlockoBlock;
import models.Model_TypeOfBlock;
import utilities.swagger.outboundClass.Swagger_TypeOfBlock_Short_Detail;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual Type_Of_Block List",
        value = "Type_Of_Block_List")
public class Swagger_Type_Of_Block_List extends Filter_Common {


/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_TypeOfBlock_Short_Detail> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_Type_Of_Block_List(Query<Model_TypeOfBlock> query , int page_number){

        if(page_number < 1) page_number = 1;

        for(Model_TypeOfBlock typeOfBlock_not_cached : query.setFirstRow((page_number - 1) * 25).setMaxRows(25).select("id").findList()){

            Model_TypeOfBlock typeOfBlock = Model_TypeOfBlock.get_byId(typeOfBlock_not_cached.id);
            if(typeOfBlock == null) continue;

            this.content.add(typeOfBlock.get_type_of_block_short_detail());
        }

        this.total = query.findRowCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
