package utilities.swagger.input;

import models.Model_InstanceSnapshot;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class Swagger_InstanceSnapShotConfiguration {

    public Swagger_InstanceSnapShotConfiguration() {}

    @Valid
    public List<Swagger_InstanceSnapShotConfigurationFile> grids_collections = new ArrayList<>();

}
