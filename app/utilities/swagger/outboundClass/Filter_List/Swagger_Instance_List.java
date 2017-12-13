package utilities.swagger.outboundClass.Filter_List;

import com.avaje.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Board;
import models.Model_HomerInstance;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "List of instancies by Filter Query",
        value = "Instance_List")
public class Swagger_Instance_List extends Filter_Common {

    /* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_HomerInstance> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_Instance_List(Query<Model_HomerInstance> query , int page_number){

        if(page_number < 1) page_number = 1;

        for(Model_HomerInstance instance_not_cached : query.setFirstRow((page_number - 1) * 25).setMaxRows(25).select("id").findList()){
            Model_HomerInstance instance = Model_HomerInstance.get_byId(instance_not_cached.id);
            if(instance == null) continue;
            content.add(instance);
        }

        this.total   = query.findRowCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
