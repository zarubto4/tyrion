package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import models.Model_Project;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for specific Project DashBoard ",
        value = "Project_Individual_DashBoard")
public class Swagger_Project_Individual_DashBoard {

    public Model_Project project;
    public List<String> widget = new ArrayList<>();

}
