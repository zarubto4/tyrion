package utilities.swagger.input;

import io.swagger.annotations.ApiModelProperty;
import models.Model_InstanceSnapshot;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Swagger_InstanceSnapShotConfigurationFile {

    public Swagger_InstanceSnapShotConfigurationFile() {}

    @Constraints.Required @ApiModelProperty(required = true)
    public UUID grid_project_id;

    @Valid
    public List<Swagger_InstanceSnapShotConfigurationProgram> grid_programs = new ArrayList<>();

}
