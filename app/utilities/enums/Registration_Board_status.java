package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Registration_Board_status {

    @EnumValue("CAN_REGISTER")                  CAN_REGISTER,
    @EnumValue("NOT_EXIST")                     NOT_EXIST,
    @EnumValue("ALREADY_REGISTERED_YOUR_ACC")   ALREADY_REGISTERED_IN_YOUR_ACCOUNT,
    @EnumValue("ALREADY_REGISTERED")            ALREADY_REGISTERED,
    @EnumValue("PERMANENTLY_DISABLED")          PERMANENTLY_DISABLED,
    @EnumValue("BROKEN_DEVICE")                 BROKEN_DEVICE;
}
