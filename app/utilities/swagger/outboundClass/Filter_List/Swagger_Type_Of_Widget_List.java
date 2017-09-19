package utilities.swagger.outboundClass.Filter_List;

import com.avaje.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_TypeOfWidget;
import utilities.swagger.outboundClass.Swagger_TypeOfWidget_Short_Detail;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual Type_Of_Widget List",
        value = "Type_Of_Widget_List")
public class Swagger_Type_Of_Widget_List {


/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_TypeOfWidget_Short_Detail> content = new ArrayList<>();


/* Basic Filter Value --------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true, value = "First value position from all subjects. Minimum is 0.")
    public int from;

    @ApiModelProperty(required = true, readOnly = true, value = "Minimum is \"from\" Maximum is \"total\"")
    public int to;

    @ApiModelProperty(required = true, readOnly = true, value = "Total subjects")
    public int total;

    @ApiModelProperty(required = true, readOnly = true, value = "Numbers of pages, which you can call")
    public int pages;

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_Type_Of_Widget_List(Query<Model_TypeOfWidget> query , int page_number){

        if(page_number < 1) page_number = 1;
        List<Model_TypeOfWidget> typeOfWidgets =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).findList();

        for(Model_TypeOfWidget typeOfWidget : typeOfWidgets){
            this.content.add(typeOfWidget.get_typeOfWidget_short_detail());
        }

        this.total = query.findRowCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
