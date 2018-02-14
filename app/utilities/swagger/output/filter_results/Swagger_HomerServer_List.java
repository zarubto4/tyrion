package utilities.swagger.output.filter_results;

import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_HomerServer;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual Homer Server List",
        value = "HomerServer_List")
public class Swagger_HomerServer_List extends _Swagger_Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_HomerServer> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_HomerServer_List(Query<Model_HomerServer> query , int page_number) {

        if (page_number < 1) page_number = 1;
        List<Model_HomerServer> list =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).findList();

        for (Model_HomerServer server : list) {
            this.content.add(server);
        }

        this.total   = query.findCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
