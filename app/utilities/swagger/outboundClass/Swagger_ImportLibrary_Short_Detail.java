package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Model_TypeOfBoard;
import utilities.enums.Library_tag;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for ImportLibrary Short Detail",
        value = "ImportLibrary_Short_Detail")
public class Swagger_ImportLibrary_Short_Detail {


    @ApiModelProperty(required = true, readOnly = true)
    public String id;

    @ApiModelProperty(required = true, readOnly = true)
    public String name;

    @ApiModelProperty(required = true, readOnly = true)
    public String description;

    @ApiModelProperty(required = true, readOnly = true)
    public Swagger_ImportLibrary_Version_Short_Detail last_version;

    @ApiModelProperty(required = true, readOnly = true)
    public Library_tag tag;
}
