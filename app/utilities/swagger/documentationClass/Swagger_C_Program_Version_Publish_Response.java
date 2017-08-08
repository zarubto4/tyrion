package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for admin response for publishing C_Program",
        value = "C_Program_Version_Publish_Response")
public class Swagger_C_Program_Version_Publish_Response {

    @Constraints.Required
    public String version_id;

    @ApiModelProperty(required = true)
    public String version_name;

    @ApiModelProperty(required = true)
    public String version_description;



    public String c_program_name;
    public String c_program_description;


    public String main; // Program


    @Constraints.Required
    public boolean decision;

    public String reason;
}
