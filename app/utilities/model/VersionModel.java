package utilities.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import models.*;
import utilities.enums.Approval;
import utilities.enums.ProgramType;
import utilities.logger.Logger;
import utilities.permission.JsonPermission;

import javax.persistence.*;

@MappedSuperclass
public abstract class VersionModel extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(VersionModel.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER) public Model_Blob file; // TODO Cache 

    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = false, value = "Only if user make request for publishing") public Approval approval_state;
    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = false, value = "Only for main / default program - and access only for administrators") public ProgramType publish_type;

    @JsonIgnore public boolean working_copy;


/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/



/* JSON PROPERTY VALUES -------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(value = "Visible only if user has permission to know it", required = false)
    public Model_Person author() {
        try {

            if (author_id != null) {
                return Model_Person.find.byId(author_id);
            }

            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty
    @ApiModelProperty(value = "Visible only for working copy versions", required = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean working_copy(){
        if(working_copy) return true;
        return null;
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonPermission @Transient @ApiModelProperty(required = true, readOnly = true)
    public boolean community_publishing_permission;
}
