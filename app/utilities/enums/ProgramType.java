package utilities.enums;

import io.ebean.annotation.EnumValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "States of update plan for each hardware are: ")
public enum ProgramType {

    @EnumValue("PRIVATE")           PRIVATE,
    @EnumValue("PUBLIC")            PUBLIC,
    @EnumValue("DEFAULT_MAIN")      DEFAULT_MAIN,
    @EnumValue("DEFAULT_TEST")      DEFAULT_TEST,
    @EnumValue("DEFAULT_VERSION")   DEFAULT_VERSION,
}
