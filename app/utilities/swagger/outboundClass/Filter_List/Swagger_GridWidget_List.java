package utilities.swagger.outboundClass.Filter_List;

import com.avaje.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_GridWidget;
import models.Model_VersionObject;
import utilities.swagger.outboundClass.Swagger_GridWidget_Short_Detail;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual GridWidget List",
        value = "GridWidget_List")
public class Swagger_GridWidget_List extends Filter_Common{


/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_GridWidget_Short_Detail> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_GridWidget_List(Query<Model_GridWidget> query, int page_number){

        if(page_number < 1) page_number = 1;
        List<Model_GridWidget> grid_widgets =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).select("id").findList();

        for(Model_GridWidget gridWidget_not_cached : grid_widgets){
            Model_GridWidget gridWidget = Model_GridWidget.get_byId(gridWidget_not_cached.id.toString());
            if(gridWidget == null) continue;

            Swagger_GridWidget_Short_Detail help = new Swagger_GridWidget_Short_Detail();

            help.id                 = gridWidget.id.toString();
            help.name               = gridWidget.name;
            help.description        = gridWidget.description;
            help.delete_permission  = gridWidget.delete_permission();
            help.edit_permission    = gridWidget.edit_permission();
            help.update_permission  = gridWidget.update_permission();


            this.content.add(help);
        }

        this.total  = query.findRowCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages  = (total / 25);
    }
}
