package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Board_Status;
import utilities.enums.Board_Type_of_connection;

@ApiModel(description = "Json Model for Status and all information about embedded Hardware",
        value = "Board_Status")
public class Swagger_Board_Status {


    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = true)
    public String server_name;

    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = true)
    public String homer_server_id;

    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = true)
    public String instance_id;

    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = true)
    public Board_Status status;

    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = true)
    public Board_Type_of_connection where;

    @ApiModelProperty(readOnly = true, required = true)
    public boolean server_online_status;

//*****
    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = false)
    public String b_program_id;

    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = false)
    public String b_program_name;

//*****
    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = false)
    public String b_program_version_id;

    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = false)
    public String b_program_version_name;

//*****

    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = true)
    public String actual_c_program_id;

    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = true)
    public String actual_c_program_name;

//*****
    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = true)
    public String actual_c_program_version_id;

    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = true)
    public String actual_c_program_version_name;

//*****

    public String required_c_program_id;
    public String required_c_program_version_id;
    public String required_c_program_name;
    public String required_c_program_version_name;
//*****
}
