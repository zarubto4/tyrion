package utilities.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

@MappedSuperclass
public abstract class NamedModel extends BaseModel {

    @JsonProperty @ApiModelProperty(required = true, readOnly = true) public String name;
    @JsonProperty @ApiModelProperty(required = true, readOnly = true) @Column(columnDefinition = "TEXT") public String description;

    @JsonIgnore public UUID author_id;

}
