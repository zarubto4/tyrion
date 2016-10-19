package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.documentationClass.Swagger_M_Program_Version_Interface;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model that contain values with accessible interface of virtual_input_output under M_programs (auto_incrementing == false) or head M_Program.virtual_input_output if auto_incrementing is True",
        value = "M_Program_interface")
public class Swagger_M_Program_Interface {

    public String id;
    public String name;
    public String description;


    @ApiModelProperty(required = true, notes = "If auto_incrementing is true - there is always only one object!")
    public List<Swagger_M_Program_Version_Interface> accessible_versions = new ArrayList<>();
}
