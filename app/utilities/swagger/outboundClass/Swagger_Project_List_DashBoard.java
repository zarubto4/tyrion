package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import models.project.global.Project;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for get all Projects DashBoard ",
        value = "Project_List_DashBoard")
public class Swagger_Project_List_DashBoard {

    public List<Project> projects = new ArrayList<>();
    public List<String> widget = new ArrayList<>();

}
