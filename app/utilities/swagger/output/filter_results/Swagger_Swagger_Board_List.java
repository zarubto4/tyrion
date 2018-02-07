package utilities.swagger.output.filter_results;

import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_TypeOfBoard;

import java.util.List;

@ApiModel(description = "Individual TypeOfBoard List",
          value = "TypeOfBoard_List")
public class Swagger_Swagger_Board_List extends Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_TypeOfBoard> content;

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_Swagger_Board_List(Query<Model_TypeOfBoard> query , int page_number) {

        if (page_number < 1) page_number = 1;

        for (Model_TypeOfBoard type_of_board_not_cached : query.setFirstRow((page_number - 1) * 25).setMaxRows(25).select("id").findList()) {
            Model_TypeOfBoard type_of_board = Model_TypeOfBoard.getById(type_of_board_not_cached.id);
            if (type_of_board == null) continue;
            content.add(type_of_board);
        }

        this.total   = query.findCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
