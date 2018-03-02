package utilities.swagger.output.filter_results;

import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_CProgram;
import models.Model_CProgramVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for Public Version of C_program List",
        value = "C_Program_Version_Public_List")
public class Swagger_C_Program_Version_Public_List extends _Swagger_Filter_Common {

/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_CProgramVersion> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_C_Program_Version_Public_List(Query<Model_CProgramVersion> query, int page_number) {

        if (page_number < 1) page_number = 1;
        List<UUID> uuids =  query.setFirstRow((page_number - 1) * 25).setMaxRows(25).findIds();

        for (UUID uuid : uuids) {
            this.content.add(Model_CProgramVersion.getById(uuid));
        }

        this.total = query.findCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        this.pages = (total / 25);
    }
}
