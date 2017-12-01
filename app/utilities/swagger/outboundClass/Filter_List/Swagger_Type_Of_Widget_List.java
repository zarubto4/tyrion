package utilities.swagger.outboundClass.Filter_List;

import com.avaje.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_TypeOfBlock;
import models.Model_TypeOfWidget;
import utilities.swagger.outboundClass.Swagger_TypeOfWidget_Short_Detail;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual Type_Of_Widget List",
        value = "Type_Of_Widget_List")
public class Swagger_Type_Of_Widget_List extends Filter_Common {


/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_TypeOfWidget_Short_Detail> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_Type_Of_Widget_List(Query<Model_TypeOfWidget> query , int page_number){

        if(page_number < 1) page_number = 1;
        List<Model_TypeOfWidget> typeOfWidgets =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).select("id").findList();

        for(Model_TypeOfWidget typeOfWidget_not_cached : typeOfWidgets){

            Model_TypeOfWidget typeOfWidget = Model_TypeOfWidget.get_byId(typeOfWidget_not_cached.id);
            if(typeOfWidget == null) continue;

            this.content.add(typeOfWidget.get_typeOfWidget_short_detail());
        }

        this.total = query.findRowCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
