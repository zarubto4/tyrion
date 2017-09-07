package utilities.swagger.outboundClass.Filter_List;

import com.avaje.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_BProgram;
import utilities.swagger.outboundClass.Swagger_B_Program_Short_Detail;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual B_Program List",
        value = "B_Program_List")
public class Swagger_B_Program_List {


/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_B_Program_Short_Detail> content = new ArrayList<>();


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

    public Swagger_B_Program_List(Query<Model_BProgram> query, int page_number){

        if(page_number < 1) page_number = 1;
        List<Model_BProgram> b_programs =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).findList();

        for(Model_BProgram b_program : b_programs){
            this.content.add(b_program.get_b_program_short_detail());
        }

        this.total = query.findRowCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
