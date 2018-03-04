package utilities.swagger.output.filter_results;

import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_CProgram;
import models.Model_Library;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Individual C_Program List",
        value = "C_Program_List")
public class Swagger_C_Program_List extends _Swagger_Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_CProgram> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_C_Program_List(Query<Model_CProgram> query , int page_number) {

        if (page_number < 1) page_number = 1;
        List<Model_CProgram> uuids_o =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).select("id").findList();

        List<UUID> uuids = new ArrayList<>();
        for(Model_CProgram l : uuids_o) {
            uuids.add(l.id);
        }

        for (UUID uuid : uuids) {
            this.content.add(Model_CProgram.getById(uuid));
        }

        this.total   = query.findCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}