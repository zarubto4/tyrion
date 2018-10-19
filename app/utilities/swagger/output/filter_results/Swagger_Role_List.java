package utilities.swagger.output.filter_results;

import exceptions.NotFoundException;
import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Role;
import utilities.swagger.input._Swagger_filter_parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Individual Role List",
        value = "Role_List")
public class Swagger_Role_List extends _Swagger_Filter_Common {


/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_Role> content = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_Role_List() {
        // If return empty list!
    }

    public Swagger_Role_List(Query<Model_Role> query, int page_number, _Swagger_filter_parameter filter) {

        if (page_number < 1) page_number = 1;
        List<UUID> ids =  query.setFirstRow((page_number - 1) * filter.count_on_page).setMaxRows(filter.count_on_page).findIds();

        for (UUID id : ids) {
            try {
                Model_Role role = Model_Role.find.byId(id);
                this.content.add(role);
            } catch (NotFoundException e) {
                // nothing
            }
        }

        this.total = query.findCount();
        this.from   = (page_number - 1) * filter.count_on_page;
        this.to     = (page_number - 1) * filter.count_on_page + content.size();
        this.pages = (total / filter.count_on_page);
    }
}
