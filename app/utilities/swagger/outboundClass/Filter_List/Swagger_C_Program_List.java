package utilities.swagger.outboundClass.Filter_List;

import com.avaje.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.project.c_program.C_Program;
import utilities.swagger.outboundClass.Swagger_C_Program_Light;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual C_Program List",
        value = "C_Program_List")
public class Swagger_C_Program_List {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_C_Program_Light> content = new ArrayList<>();

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

    public Swagger_C_Program_List(Query<C_Program> query , int page_number){

        if(page_number < 1) page_number = 1;
        List<C_Program> list =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).findList();

        for(C_Program c_program : list){

            Swagger_C_Program_Light help = new Swagger_C_Program_Light();

            help.c_program_id = c_program.id;
            help.c_program_name = c_program.name;
            help.c_program_version_id = c_program.version_objects.get(0).id;
            help.c_program_version_name = c_program.version_objects.get(0).version_name;
            help.type_of_board_id = c_program.type_of_board.id;
            help.type_of_board_name = c_program.type_of_board.name;

            this.content.add(help);
        }

        this.total   = query.findRowCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        for (int i = 1; i < (total / 25) + 2; i++) pages.add(i);
    }
}
