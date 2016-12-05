package utilities.swagger.outboundClass.Filter_List;

import com.avaje.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.grid.GridWidget;
import utilities.swagger.outboundClass.Swagger_GridWidget_Light;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual GridWidget List",
        value = "GridWidget_List")
public class Swagger_GridWidget_List {


/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_GridWidget_Light> content = new ArrayList<>();

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

    public Swagger_GridWidget_List(Query<GridWidget> query, int page_number){

        if(page_number < 1) page_number = 1;
        List<GridWidget> grid_widgets =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).findList();

        for(GridWidget gridWidget : grid_widgets){

            Swagger_GridWidget_Light help = new Swagger_GridWidget_Light();

            help.grid_widget_id = gridWidget.id;
            help.grid_widget_name = gridWidget.name;
            help.grid_widget_description = gridWidget.general_description;
            help.grid_widget_version_id = gridWidget.grid_widget_versions.get(0).id;
            help.grid_widget_version_name = gridWidget.grid_widget_versions.get(0).version_name;
            help.grid_widget_version_description = gridWidget.grid_widget_versions.get(0).version_description;
            help.grid_widget_type_of_widget_id = gridWidget.type_of_widget.id;
            help.grid_widget_type_of_widget_name = gridWidget.type_of_widget.name;
            help.grid_widget_type_of_widget_description = gridWidget.type_of_widget.general_description;

            this.content.add(help);
        }

        this.total = query.findRowCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        for (int i = 1; i < (total / 25) + 2; i++) pages.add(i);
    }
}
