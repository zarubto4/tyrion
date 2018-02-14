package utilities.swagger.output.filter_results;

import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Library;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual Library List",
        value = "Library_List")
public class Swagger_Library_List extends _Swagger_Filter_Common{

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_Library> content = new ArrayList<>();


/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_Library_List(Query<Model_Library> query , int page_number) {

        if (page_number < 1) page_number = 1;
        List<Model_Library> list =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).findList();

        for (Model_Library library : list) {
            this.content.add(library);
        }

        this.total   = query.findCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
