package utilities.swagger.outboundClass.Filter_List;

import com.avaje.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.project.b_program.B_Program;
import utilities.swagger.outboundClass.Swagger_B_Program_Light;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual B_Program List",
        value = "B_Program_List")
public class Swagger_B_Program_List {


/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_B_Program_Light> content = new ArrayList<>();


/* Basic Filter Value --------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true, value = "First value position from all subjects. Minimum is 0.")
    public int from;

    @ApiModelProperty(required = true, readOnly = true, value = "Minimum is \"from\" Maximum is \"total\"")
    public int to;

    @ApiModelProperty(required = true, readOnly = true, value = "Total subjects")
    public int total;


    @ApiModelProperty(required = true, readOnly = true, value = "Numbers of pages, which you can call")
    public List<Integer> pages = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_B_Program_List(Query<B_Program> query, int page_number){

        if(page_number < 1) page_number = 1;
        List<B_Program> b_programs =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).findList();

        for(B_Program b_program : b_programs){

            Swagger_B_Program_Light help = new Swagger_B_Program_Light();

            help.b_program_id = b_program.id;
            help.b_program_name = b_program.name;
            help.b_program_description = b_program.description;
            help.b_program_version_id = b_program.version_objects.get(0).id;
            help.b_program_version_name = b_program.version_objects.get(0).version_name;
            help.b_program_version_description = b_program.version_objects.get(0).version_description;

            this.content.add(help);
        }

        this.total = query.findRowCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        for (int i = 1; i < (total / 25) + 2; i++) pages.add(i);
    }
}
