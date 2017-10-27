package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_BProgramHwGroup;
import models.Model_CProgramUpdatePlan;
import models.Model_MProjectProgramSnapShot;
import utilities.enums.Enum_Firmware_type;
import utilities.enums.Enum_Update_group_procedure_state;
import utilities.enums.Enum_Update_type_of_update;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApiModel(description = "Json Model for ActualizationProcedure procedure",
        value = "ActualizationProcedure_short_detail")
public class Swagger_ActualizationProcedure_Short_Detail {


    @ApiModelProperty(required = true,  readOnly = true)     public String id;

    @ApiModelProperty(required = true,  readOnly = true, value = "can be null")     public Date date_of_create;
    @ApiModelProperty(required = true,  readOnly = true, value = "can be null")     public Date date_of_planing;
    @ApiModelProperty(required = false, readOnly = true, value = "can be null")     public Date date_of_finish;

    @ApiModelProperty(required = false, readOnly = true, value = "Visible only if type_of_update === MANUALLY_RELEASE_MANAGER and firmware_type is BOOTLOADER") public Swagger_C_Program_Update_program program;
    @ApiModelProperty(required = false, readOnly = true, value = "Visible only if type_of_update === MANUALLY_RELEASE_MANAGER and firmware_type is FIRMWARE")  public  Swagger_Bootloader_Update_program bootloader;

    @ApiModelProperty(required = true, readOnly = true)  public Enum_Update_group_procedure_state state;
    @ApiModelProperty(required = true, readOnly = true)  public Enum_Update_type_of_update type_of_update;

    @ApiModelProperty(required = false, readOnly = true)  public Integer procedure_size_all;
    @ApiModelProperty(required = false, readOnly = true)  public Integer procedure_size_complete;

    @ApiModelProperty(required = true, readOnly = true)  public boolean edit_permission;
    @ApiModelProperty(required = true, readOnly = true)  public Enum_Firmware_type firmware_type;

}
