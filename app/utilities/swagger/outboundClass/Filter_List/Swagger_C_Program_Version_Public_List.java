package utilities.swagger.outboundClass.Filter_List;


import com.avaje.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_CProgram;
import models.Model_VersionObject;
import utilities.swagger.outboundClass.Swagger_C_Program_Version_Short_Detail;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for Public Version of C_program List",
        value = "C_Program_Version_Public_List")
public class Swagger_C_Program_Version_Public_List extends Filter_Common{

    /* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_C_Program_Version_Short_Detail> content = new ArrayList<>();


/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_C_Program_Version_Public_List(Query<Model_VersionObject> query, int page_number){

        if(page_number < 1) page_number = 1;

        for(Model_VersionObject version_not_cached : query.setFirstRow((page_number - 1) * 25).setMaxRows(25).select("id").findList()){
            Model_VersionObject version = Model_VersionObject.get_byId(version_not_cached.id);
            if(version == null) continue;
            this.content.add(version.get_short_c_program_version());
        }

        this.total = query.findRowCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
