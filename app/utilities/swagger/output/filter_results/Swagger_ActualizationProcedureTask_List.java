package utilities.swagger.output.filter_results;

import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_HardwareUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "", value = "ActualizationProcedureTask_List")
public class Swagger_ActualizationProcedureTask_List extends _Swagger_Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_HardwareUpdate> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_ActualizationProcedureTask_List(Query<Model_HardwareUpdate> query , int page_number) {

        if (page_number < 1) page_number = 1;
        List<UUID> ids =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).findIds();

        for (UUID id : ids) {
            Model_HardwareUpdate task = Model_HardwareUpdate.getById(id);
            if (task == null) continue;
            this.content.add(task);
        }

        this.total   = query.findCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
