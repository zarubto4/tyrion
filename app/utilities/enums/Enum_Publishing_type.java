package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "States of update plan for each board are: ")
public enum Enum_Publishing_type {

    @EnumValue("private_program")       private_program,
    @EnumValue("public_program")        public_program,
    @EnumValue("default_main_program")  default_main_program,
    @EnumValue("default_test_program")  default_test_program,
    @EnumValue("default_version")       default_version,
}
