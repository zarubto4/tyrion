package utilities.swagger.output.filter_results;


import controllers.BaseController;
import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Notification;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual Notification List",
          value = "Notification_List")
public class Swagger_Notification_List extends Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_Notification> content;

/* Basic Filter Value --------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true, value = "Numbers of pages, which you can call")
    public List<Integer> pages = new ArrayList<>();

    @ApiModelProperty(required = true, readOnly = true, value = "Total unread subjects")
    public int unread_total = Model_Notification.find.query().where().eq("was_read", false).eq("person.id", BaseController.personId()).findCount();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_Notification_List(Query<Model_Notification> query , int page_number) {

        if (page_number < 1) page_number = 1;
        this.content =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).findList();
        this.total   = query.findCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        for (int i = 1; i < (total / 25) + 2; i++) pages.add(i);

    }
}
