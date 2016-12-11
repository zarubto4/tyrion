package utilities.swagger.documentationClass;


import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

public class Swagger_C_Program_Version_Approve_WithChanges {

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
