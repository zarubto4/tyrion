package utilities.swagger.outboundClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_CProgram_updater_state;
import utilities.enums.Enum_Firmware_type;
import utilities.enums.Enum_Update_type_of_update;

import java.util.Date;

@ApiModel(description = "Json Model with details of C_program>",
        value = "C_Program_Update_plan_Short_Detail")
public class Swagger_C_Program_Update_plan_Short_Detail {

    @ApiModelProperty(required = true, readOnly = true) public String id;
    @ApiModelProperty(required = true, readOnly = true) public String actualization_procedure_id;
    @ApiModelProperty(required = true, readOnly = true) public Enum_Update_type_of_update type_of_update;

    @ApiModelProperty(required = true, readOnly = false) public Date date_of_planed;
    @ApiModelProperty(required = true, readOnly = true) public Date date_of_create;
    @ApiModelProperty(required = true, readOnly = false, value = "can be null") public Date date_of_finish;

    public Enum_Firmware_type firmware_type;
    public Enum_CProgram_updater_state state;

    @ApiModelProperty(required = false, readOnly = true, value = "Visible only if type_of_update === MANUALLY_RELEASE_MANAGER and firmware_type is BOOTLOADER") public Swagger_C_Program_Update_program program;
    @ApiModelProperty(required = false, readOnly = true, value = "Visible only if type_of_update === MANUALLY_RELEASE_MANAGER and firmware_type is FIRMWARE")  public  Swagger_Bootloader_Update_program bootloader;


}
