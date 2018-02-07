package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model that contain values with accessible interface of virtual_input_output under M_project (auto_incrementing == false) or head M_project.M_program.virtual_input_output if auto_incrementing is True",
        value = "M_Project_interface")
public class Swagger_M_Project_Interface {

    @ApiModelProperty(required = true, readOnly = true)   public UUID id;

    @ApiModelProperty(required = true, readOnly = true) public String name;

    @ApiModelProperty(required = false, readOnly = true)  public String description;

    @ApiModelProperty(required = false, readOnly = true)  public boolean auto_incrementing;


    @ApiModelProperty(required = true, readOnly = true)  public List<Swagger_M_Program_Interface> accessible_interface = new ArrayList<>();


}
