package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.documentationClass.Swagger_ImportLibrary_Version_New;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for ImportLibrary_Version_Short_Detail",
        value = "ImportLibrary_Version_Short_Detail")
public class Swagger_ImportLibrary_Version_Short_Detail {


    @ApiModelProperty(required = true, readOnly = true)
    public String version_id;

    @ApiModelProperty(required = true, readOnly = true)
    public String version_name;

    @ApiModelProperty(required = true, readOnly = true)
    public String version_description;

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_Example_Short_Detail> examples = new ArrayList<>();

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_ImportLibrary_Version_New.Library_File> library_files = new ArrayList<>();

    @ApiModelProperty(required = true, readOnly = true)
    public String library_id;
}
