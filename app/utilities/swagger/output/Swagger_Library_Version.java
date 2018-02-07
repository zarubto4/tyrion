package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_CProgram;
import models.Model_Person;
import utilities.swagger.input.Swagger_Library_Record;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for Library_Version",
        value = "Library_Version")
public class Swagger_Library_Version {

    @ApiModelProperty(required = true, readOnly = true)
    public UUID id;

    @ApiModelProperty(required = true, readOnly = true)
    public String name;

    @ApiModelProperty(required = true, readOnly = true)
    public String description;

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_CProgram> examples = new ArrayList<>();

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_Library_Record> files = new ArrayList<>();

    @ApiModelProperty(required = true, readOnly = true)
    public boolean update_permission;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean delete_permission;

    @ApiModelProperty(required = false, readOnly = true, value = "Optional value")
    public Model_Person author;


}
