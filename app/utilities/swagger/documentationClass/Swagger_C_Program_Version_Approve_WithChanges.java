package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

public class Swagger_C_Program_Version_Approve_WithChanges {

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String id;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String name;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String description;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String main;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String user_files;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String external_libraries;

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String reason;
}
