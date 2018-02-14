package utilities.swagger.output.filter_results;

import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_CProgramVersion;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for Public Version of C_program List",
        value = "C_Program_Version_Public_List")
public class Swagger_C_Program_Version_Public_List extends _Swagger_Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_CProgramVersion> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_C_Program_Version_Public_List(Query<Model_CProgramVersion> query, int page_number) {

        if (page_number < 1) page_number = 1;

        for (Model_CProgramVersion version_not_cached : query.setFirstRow((page_number - 1) * 25).setMaxRows(25).select("id").findList()) {
            Model_CProgramVersion version = Model_CProgramVersion.getById(version_not_cached.id);
            if (version == null) continue;
            this.content.add(version);
        }

        this.total = query.findCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
