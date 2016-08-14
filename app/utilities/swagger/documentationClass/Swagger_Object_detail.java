package utilities.swagger.documentationClass;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Transient;

@ApiModel(description = "Json Model for objects details name / id",
          value = "Object_detail")
public class Swagger_Object_detail {

    public Swagger_Object_detail(String name, String id){this.name = name;this.id = id;}
    @JsonProperty @Transient
    @ApiModelProperty(required = true, value = "Name of object for users")         public String name;
    @JsonProperty @Transient @ApiModelProperty(required = true, value = "ID of object for CRUD operations") public String id;
}
