package utilities.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Blob;
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

    @JsonIgnore @OneToMany(mappedBy = "version", cascade = CascadeType.ALL, fetch = FetchType.EAGER) public List<Model_Blob> files = new ArrayList<>();
    @ManyToOne(fetch = FetchType.LAZY) public Model_Person author;

    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = false, value = "Only if user make request for publishing") @Enumerated(EnumType.STRING) public Approval approval_state;
    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = false, value = "Only for main / default program - and access only for administrators") @Enumerated(EnumType.STRING) public ProgramType publish_type;


/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private UUID cache_author_id;

/* JSON PROPERTY VALUES -------------------------------------------------------------------------------------------------*/

    @JsonProperty
    public Model_Person author() {
        try {
            return get_author();
        } catch (_Base_Result_Exception e){
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_Person get_author() throws _Base_Result_Exception {

        if (cache_author_id == null) {
            Model_Person person = Model_Person.find.query().where().eq("widgetVersionsAuthor.id", id).select("id").findOne();

            if(person == null) throw new Result_Error_NotFound(Model_Person.class);
            cache_author_id = person.id;
        }

        return Model_Person.getById(cache_author_id);
    }

/* BLOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore public String blob_version_link;
    @JsonIgnore @Transient public String get_path() {
        return  blob_version_link;
    }


}