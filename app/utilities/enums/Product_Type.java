package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Product_Type{

    // TODO změnit název na malé písmeno u type plus opravit git
    @EnumValue("alpha") alpha,
    @EnumValue("free") free,
    @EnumValue("business") business
}