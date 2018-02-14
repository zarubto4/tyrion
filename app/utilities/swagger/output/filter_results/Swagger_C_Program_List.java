package utilities.swagger.output.filter_results;

import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_CProgram;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual C_Program List",
        value = "C_Program_List")
public class Swagger_C_Program_List extends _Swagger_Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_CProgram> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_C_Program_List(Query<Model_CProgram> query , int page_number) {

        if (page_number < 1) page_number = 1;

        for (Model_CProgram c_program_not_cached : query.setFirstRow((page_number - 1) * 25).setMaxRows(25).select("id").findList()) {
            Model_CProgram c_program = Model_CProgram.getById(c_program_not_cached.id);
            if (c_program == null) continue;
            this.content.add(c_program);
        }

        this.total   = query.findCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}