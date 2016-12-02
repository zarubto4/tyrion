package utilities.swagger.outboundClass.Filter_List;

import com.avaje.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.grid.TypeOfWidget;
import utilities.swagger.outboundClass.Swagger_Type_Of_Widget_Light;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual Type_Of_Widget List",
        value = "Type_Of_Widget_List")
public class Swagger_Type_Of_Widget_List {


/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_Type_Of_Widget_Light> content = new ArrayList<>();


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

    public Swagger_Type_Of_Widget_List(Query<TypeOfWidget> query , int page_number){

        if(page_number < 1) page_number = 1;
        List<TypeOfWidget> typeOfWidgets =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).findList();

        for(TypeOfWidget typeOfWidget : typeOfWidgets){

            Swagger_Type_Of_Widget_Light help = new Swagger_Type_Of_Widget_Light();

            help.type_of_widget_id = typeOfWidget.id;
            help.type_of_widget_name = typeOfWidget.name;
            help.type_of_widget_description = typeOfWidget.general_description;

            this.content.add(help);
        }

        this.total = query.findRowCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        for (int i = 1; i < (total / 25) + 2; i++) pages.add(i);
    }
}
