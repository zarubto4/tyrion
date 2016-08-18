package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for new creating new instnace on Homer server",
          value = "Instance_HW_Group")
public class Swagger_Instance_HW_Group {

    public String yodaId;
    public List<String> devicesId = new ArrayList<>();

}
