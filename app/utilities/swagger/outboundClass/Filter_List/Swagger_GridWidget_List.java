package utilities.swagger.outboundClass.Filter_List;

import com.avaje.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.grid.Model_GridWidget;
import utilities.swagger.outboundClass.Swagger_GridWidget_Short_Detail;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual GridWidget List",
        value = "GridWidget_List")
public class Swagger_GridWidget_List {


/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_GridWidget_Short_Detail> content = new ArrayList<>();

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

    public Swagger_GridWidget_List(Query<Model_GridWidget> query, int page_number){

        if(page_number < 1) page_number = 1;
        List<Model_GridWidget> grid_widgets =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).findList();

        for(Model_GridWidget gridWidget : grid_widgets){

            Swagger_GridWidget_Short_Detail help = new Swagger_GridWidget_Short_Detail();

            help.id                 = gridWidget.id;
            help.name               = gridWidget.name;
            help.description        = gridWidget.description;
            help.delete_permission  = gridWidget.delete_permission();
            help.edit_permission    = gridWidget.edit_permission();
            help.update_permission  = gridWidget.update_permission();

            this.content.add(help);
        }

        this.total = query.findRowCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        for (int i = 1; i < (total / 25) + 2; i++) pages.add(i);
    }
}
