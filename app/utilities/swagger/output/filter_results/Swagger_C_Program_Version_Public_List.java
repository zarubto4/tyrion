package utilities.swagger.output.filter_results;

import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Version;
import utilities.swagger.output.Swagger_C_Program_Version;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for Public Version of C_program List",
        value = "C_Program_Version_Public_List")
public class Swagger_C_Program_Version_Public_List extends Filter_Common{

    /* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_C_Program_Version> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_C_Program_Version_Public_List(Query<Model_Version> query, int page_number) {

        if (page_number < 1) page_number = 1;

        for (Model_Version version_not_cached : query.setFirstRow((page_number - 1) * 25).setMaxRows(25).select("id").findList()) {
            Model_Version version = Model_Version.getById(version_not_cached.id);
            if (version == null) continue;
            this.content.add(version.get_c_program().program_version(version));
        }

        this.total = query.findCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
