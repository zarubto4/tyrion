package utilities.swagger.outboundClass.Filter_List;


import com.avaje.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Version_Object;
import utilities.swagger.outboundClass.Swagger_C_Program_Version_Light;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for Public Version of C_program List",
        value = "C_Program_Version_Public_List")
public class Swagger_C_Program_Version_Public_List {

    /* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_C_Program_Version_Light> content = new ArrayList<>();


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

    public Swagger_C_Program_Version_Public_List(Query<Version_Object> query, int page_number){

        if(page_number < 1) page_number = 1;
        List<Version_Object> versions =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).findList();

        for(Version_Object version : versions){

            Swagger_C_Program_Version_Light help = new Swagger_C_Program_Version_Light();

            help.version_id = version.id;
            help.version_name = version.version_name;
            help.version_description = version.version_description;

            //TODO Lexa - doplnit

            this.content.add(help);
        }

        this.total = query.findRowCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        for (int i = 1; i < (total / 25) + 2; i++) pages.add(i);
    }
}
