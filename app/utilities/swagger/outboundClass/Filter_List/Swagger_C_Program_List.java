package utilities.swagger.outboundClass.Filter_List;

import com.avaje.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Board;
import models.Model_CProgram;
import utilities.swagger.outboundClass.Swagger_C_program_Short_Detail;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual C_Program List",
        value = "C_Program_List")
public class Swagger_C_Program_List extends Filter_Common{

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_C_program_Short_Detail> content = new ArrayList<>();


/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_C_Program_List(Query<Model_CProgram> query , int page_number){

        if(page_number < 1) page_number = 1;

        for(Model_CProgram c_program_not_cached : query.setFirstRow((page_number - 1) * 25).setMaxRows(25).select("id").findList()){
            Model_CProgram c_program = Model_CProgram.get_byId(c_program_not_cached.id);
            if(c_program == null) continue;
            this.content.add(c_program.get_c_program_short_detail());
        }

        this.total   = query.findRowCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}