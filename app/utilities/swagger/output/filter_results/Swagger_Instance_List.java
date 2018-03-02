package utilities.swagger.output.filter_results;

import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_HomerServer;
import models.Model_Instance;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "List of instances by Filter Query",
        value = "Instance_List")
public class Swagger_Instance_List extends _Swagger_Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_Instance> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_Instance_List(Query<Model_Instance> query , int page_number) {

        if (page_number < 1) page_number = 1;
        List<UUID> uuids =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).findIds();

        for (UUID uuid : uuids) {
            this.content.add(Model_Instance.getById(uuid));
        }
        this.total   = query.findCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
