package utilities.swagger.outboundClass.Filter_List;


import com.avaje.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Board;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual Notification List",
          value = "Board_List")
public class Swagger_Board_List {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Board> content;

/* Basic Filter Value --------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true, value = "First value position from all subjects. Minimum is 0.")
    public int from;

    @ApiModelProperty(required = true, readOnly = true, value = "Minimum is \"from\" Maximum is \"total\"")
    public int to;

    @ApiModelProperty(required = true, readOnly = true, value = "Total subjects")
    public int total;

    @ApiModelProperty(required = true, readOnly = true, value = "Numbers of pages, which you can call")
    public List<Integer> pages = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_Board_List(Query<Board> query , int page_number){

        if(page_number < 1) page_number = 1;
        this.content =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).findList();
        this.total   = query.findRowCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        for (int i = 1; i < (total / 25) + 2; i++) pages.add(i);
    }
}
