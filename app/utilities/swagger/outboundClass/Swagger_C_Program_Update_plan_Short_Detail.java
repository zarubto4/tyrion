package utilities.swagger.outboundClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_CProgram_updater_state;
import utilities.enums.Firmware_type;

import java.util.Date;

@ApiModel(description = "Json Model with details of C_program>",
        value = "C_Program_Update_plan_Short_Detail")
public class Swagger_C_Program_Update_plan_Short_Detail {

    @ApiModelProperty(required = true, readOnly = true)
    public String id;

    public Date date_of_create;
    public Date date_of_finish;

    public Firmware_type firmware_type;
    public Enum_CProgram_updater_state state;

    // Pouze Pokud se update týká Firmwaru nebo Backupu
    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = true, value = "only if Firmware_type is firmware or backup", readOnly = true) public String c_program_id;
    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = true, value = "only if Firmware_type is firmware or backup", readOnly = true) public String c_program_version_id;
    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = true, value = "only if Firmware_type is firmware or backup", readOnly = true) public String c_program_program_name;
    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = true, value = "only if Firmware_type is firmware or backup", readOnly = true) public String c_program_version_name;

    // Pouze Pokud se update týká Bootloaderu
    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = true, value = "only if Firmware_type is bootloader", readOnly = true) public String bootloader_id;
    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = true, value = "only if Firmware_type is bootloader", readOnly = true) public String bootloader_name;
    @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty(required = true, value = "only if Firmware_type is bootloader", readOnly = true) public String version_identificator;


}
