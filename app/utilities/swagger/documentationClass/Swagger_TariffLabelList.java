package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "",
          value = "TariffLabelList")
public class Swagger_TariffLabelList {

    @Valid
    public List<Swagger_TariffLabel> labels;

}


