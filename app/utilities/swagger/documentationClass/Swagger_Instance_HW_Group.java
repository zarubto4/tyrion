package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for new creating new instnace on Homer server",
          value = "Instance_HW_Group ")
public class Swagger_Instance_HW_Group {

    public String yoda_id;
    public List<String> devices_id = new ArrayList<>();

}
