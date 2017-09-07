package utilities.swagger.outboundClass.Filter_List;


import com.avaje.ebean.Model;
import com.avaje.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Board;
import utilities.swagger.outboundClass.Swagger_Board_Short_Detail;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual Board List",
          value = "Board_List")
public class Swagger_Board_List {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_Board_Short_Detail> content = new ArrayList<>();

/* Basic Filter Value --------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true, value = "First value position from all subjects. Minimum is 0.")
    public int from;

    @ApiModelProperty(required = true, readOnly = true, value = "Minimum is \"from\" Maximum is \"total\"")
    public int to;

    @ApiModelProperty(required = true, readOnly = true, value = "Total subjects")
    public int total;

    @ApiModelProperty(required = true, readOnly = true, value = "Numbers of pages, which you can call")
    public int pages;


/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_Board_List(Query<Model_Board> query , int page_number){

        if(page_number < 1) page_number = 1;

        for(Model_Board board : query.setFirstRow((page_number - 1) * 25).setMaxRows(25).findList()){
            content.add(board.get_short_board());
        }

        this.total   = query.findRowCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
