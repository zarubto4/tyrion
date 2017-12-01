package utilities.swagger.outboundClass.Filter_List;


import com.avaje.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_HomerInstance;
import models.Model_TypeOfBoard;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual TypeOfBoard List",
          value = "TypeOfBoard_List")
public class Swagger_Swagger_Board_List extends Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_TypeOfBoard> content;

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_Swagger_Board_List(Query<Model_TypeOfBoard> query , int page_number){

        if(page_number < 1) page_number = 1;

        for(Model_TypeOfBoard type_of_board_not_cached : query.setFirstRow((page_number - 1) * 25).setMaxRows(25).select("id").findList()){
            Model_TypeOfBoard type_of_board = Model_TypeOfBoard.get_byId(type_of_board_not_cached.id);
            if(type_of_board == null) continue;
            content.add(type_of_board);
        }

        this.total   = query.findRowCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
