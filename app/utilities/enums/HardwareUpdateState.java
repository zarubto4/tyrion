package utilities.enums;

import io.ebean.annotation.EnumValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "HardwareUpdateState")
public enum HardwareUpdateState {

    @ApiModelProperty(value = "Update cannot be executed for some reason. (hardware is offline, server is offline, etc.)")
    @EnumValue("PENDING") PENDING,

    @ApiModelProperty(value = "Update is executing. This state indicates, that the update is being executed on the physical hardware.")
    @EnumValue("RUNNING") RUNNING,

    @ApiModelProperty(value = "Update was executed successfully.")
    @EnumValue("COMPLETE") COMPLETE,

    @ApiModelProperty(value = "Update was canceled.")
    @EnumValue("CANCELED") CANCELED,

    @ApiModelProperty(value = "Update was overwritten by some newer update.")
    @EnumValue("OBSOLETE") OBSOLETE,

    @ApiModelProperty(value = "Update failed and will not be repeated.")
    @EnumValue("FAILED") FAILED
}
