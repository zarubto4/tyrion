package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model that contain values with accessible interface of virtual_input_output under M_project (auto_incrementing == false) or head M_project.M_program.virtual_input_output if auto_incrementing is True",
        value = "M_Project_interface")
public class Swagger_M_Project_Interface {

    public String id;

    public String name;

    public String description;

    public boolean auto_incrementing;


    public List<Swagger_M_Program_Interface> accessible_interface = new ArrayList<>();


}
