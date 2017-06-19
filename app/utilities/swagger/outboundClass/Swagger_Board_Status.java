package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Board_status;
import utilities.enums.Enum_Board_type_of_connection;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for Status and all information about embedded Hardware",
        value = "Board_Status")
public class Swagger_Board_Status {


    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = true)
    public String server_name;

    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = true)
    public String homer_server_id;

    @ApiModelProperty(value = "Only if Board is under person Instance (in Blocko)", readOnly = true, required = true)
    public String instance_id;

    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = true)
    public Enum_Board_status status;

    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = true)
    public Enum_Board_type_of_connection where;

    @ApiModelProperty(readOnly = true, required = true)
    public boolean server_online_status;

    @ApiModelProperty(value = "Only if Board is under person Instance (in Blocko)", readOnly = true, required = true)
    public boolean instance_online_status;

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

    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = true)
    public String actual_c_program_version_id;

    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = true)
    public String actual_c_program_version_name;

//*****

    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = false)
    public String actual_backup_c_program_id;

    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = false)
    public String actual_backup_c_program_name;

    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = false)
    public String actual_backup_c_program_version_id;

    @ApiModelProperty(value = "Can be with null value", readOnly = true, required = false)
    public String actual_backup_c_program_version_name;

//*****
@ApiModelProperty(value = "Can be empty", readOnly = true, required = false)
    public List<Swagger_C_Program_Update_plan_Short_Detail> required_c_programs = new ArrayList<>();

//*****

@ApiModelProperty(value = "Can be empty", readOnly = true, required = false)
    public List<Swagger_C_Program_Update_plan_Short_Detail> required_backup_c_programs = new ArrayList<>();

//*****
}
