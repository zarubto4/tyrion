package utilities.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Blob;
import models.Model_CProgram;
import models.Model_Person;
import models.Model_WidgetVersion;
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


/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/



/* JSON PROPERTY VALUES -------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_Person author() throws _Base_Result_Exception {
        try {

            if (author_id != null) {
                return Model_Person.getById(author_id);
            }

            return null;
        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


    @JsonIgnore @Override
    public void save() {
        System.out.println("Probublalo to přes VersionModel.save(), zde teoreticky mužu automaticky uložit autora");
        super.save();
    }

/* BLOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore public String blob_version_link;
    @JsonIgnore @Transient public String get_path() {
        return  blob_version_link;
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty("Visible only for Administrator with permission") @JsonInclude(JsonInclude.Include.NON_NULL) public Boolean community_publishing_permission()  {
        try {
            if(_BaseController.person().has_permission(Model_CProgram.Permission.C_Program_community_publishing_permission.name())) return true;
            return null;
        }catch (Exception e){
            return null;
        }
    }


}