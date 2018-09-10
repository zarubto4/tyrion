package utilities.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.swagger.annotations.ApiModelProperty;
import models.*;
import utilities.cache.Cached;
import utilities.enums.Approval;
import utilities.enums.ProgramType;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@MappedSuperclass
public abstract class VersionModel extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(VersionModel.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER) public Model_Blob file; // TODO Cache 

    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = false, value = "Only if user make request for publishing") @Enumerated(EnumType.STRING) public Approval approval_state;
    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = false, value = "Only for main / default program - and access only for administrators") @Enumerated(EnumType.STRING) public ProgramType publish_type;

    @JsonIgnore public boolean working_copy;


/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/



/* JSON PROPERTY VALUES -------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(value = "Visible only if user has permission to know it", required = false)
    public Model_Person author() throws _Base_Result_Exception {
        try {

            if (author_id != null) {
                return Model_Person.find.byId(author_id);
            }

            return null;
        }catch (Exception e){
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

    @JsonIgnore @Override
    public void save() {
        super.save();
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty("Visible only for Administrator with permission") @JsonInclude(JsonInclude.Include.NON_NULL) public Boolean community_publishing_permission()  {
        try {

            if(_BaseController.person().has_permission(Model_CProgram.Permission.C_Program_community_publishing_permission.name())) {
                return true;
            }

            return null;
        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }


}