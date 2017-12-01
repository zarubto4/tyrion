package utilities.swagger.outboundClass.Filter_List;

import com.avaje.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_BProgram;
import models.Model_CProgramUpdatePlan;
import utilities.swagger.outboundClass.Swagger_B_Program_Short_Detail;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual B_Program List",
        value = "B_Program_List")
public class Swagger_B_Program_List extends Filter_Common {


/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_B_Program_Short_Detail> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_B_Program_List(Query<Model_BProgram> query, int page_number){

        if(page_number < 1) page_number = 1;
        List<Model_BProgram> b_programs =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).select("id").findList();

        for(Model_BProgram b_program_not_cached : b_programs){
            Model_BProgram b_program = Model_BProgram.get_byId(b_program_not_cached.id);
            if(b_program == null) continue;
            this.content.add(b_program.get_b_program_short_detail());
        }

        this.total = query.findRowCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
