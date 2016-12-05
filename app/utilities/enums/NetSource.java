package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum NetSource {

    @EnumValue("ethernet")   ethernet,
    @EnumValue("wifi")       wifi,
    @EnumValue("nothing")    nothing
}
