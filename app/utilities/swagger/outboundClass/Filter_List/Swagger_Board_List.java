package utilities.swagger.outboundClass.Filter_List;


import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_ActualizationProcedure;
import models.Model_Board;
import utilities.swagger.outboundClass.Swagger_Board_Short_Detail;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual Board List",
          value = "Board_List")
public class Swagger_Board_List extends Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_Board_Short_Detail> content = new ArrayList<>();


/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_Board_List(Query<Model_Board> query , int page_number){

        if(page_number < 1) page_number = 1;

        for(Model_Board board_not_cached : query.setFirstRow((page_number - 1) * 25).setMaxRows(25).select("id").findList()){
            Model_Board board = Model_Board.get_byId(board_not_cached.id);
            if(board == null) continue;
            content.add(board.get_short_board());
        }

        this.total   = query.findRowCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
