package utilities.swagger.output.filter_results;


import controllers._BaseController;
import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Notification;
import utilities.swagger.input._Swagger_filter_parameter;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual Notification List",
          value = "Notification_List")
public class Swagger_Notification_List extends _Swagger_Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_Notification> content;

/* Basic Filter Value --------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true, value = "Total unread subjects")
    public int unread_total = Model_Notification.find.query().where().eq("was_read", false).eq("person.id", _BaseController.personId()).findCount();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_Notification_List(Query<Model_Notification> query , int page_number) {

        if (page_number < 1) page_number = 1;
        this.content =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).findList();
        this.total   = query.findCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();

        this.pages = (int) Math.ceil (total /  25.0);

    }
}
