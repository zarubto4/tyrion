package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.outboundClass.Swagger_Example_Short_Detail;
import utilities.swagger.outboundClass.Swagger_Person_Short_Detail;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for Library_Version",
        value = "Library_Version")
public class Swagger_Library_Version {

    @ApiModelProperty(required = true, readOnly = true)
    public String version_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String version_name;

    @ApiModelProperty(required = true, readOnly = true)
    public String version_description;

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_Example_Short_Detail> examples = new ArrayList<>();

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_Library_Record> files = new ArrayList<>();

    @ApiModelProperty(required = true, readOnly = true)
    public boolean update_permission;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean delete_permission;

    @ApiModelProperty(required = false, readOnly = true, value = "Optional value")
    public Swagger_Person_Short_Detail author;


}
