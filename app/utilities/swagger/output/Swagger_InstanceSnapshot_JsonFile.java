package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;
import utilities.swagger.input.Swagger_InstanceSnapshot_New;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "InstanceSnapshot_JsonFile")
public class Swagger_InstanceSnapshot_JsonFile extends _Swagger_Abstract_Default {

    @Constraints.Required
    public String snapshot;

    @Valid
    public List<Swagger_InstanceSnapshot_JsonFile_Interface> interfaces = new ArrayList<>();
}
