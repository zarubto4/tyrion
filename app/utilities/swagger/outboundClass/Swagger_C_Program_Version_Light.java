package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Version_Object;
import models.project.c_program.C_Compilation;
import utilities.swagger.documentationClass.Swagger_C_Program_Version_New;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for Public Version of C_program",
        value = "C_Program_Version_Light")
public class Swagger_C_Program_Version_Light {


    @ApiModelProperty(required = true, readOnly = true)
    public String version_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String version_name;

    @ApiModelProperty(required = true, readOnly = true)
    public String version_description;

    //TODO Lexa - vlastnosti odlehčené verze

}
