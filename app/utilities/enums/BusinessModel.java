package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum BusinessModel {

    @EnumValue("ALPHA")         ALPHA,
    @EnumValue("SAAS")          SAAS,
    @EnumValue("FEE")           FEE,
    @EnumValue("CAL")           CAL,
    @EnumValue("INTEGRATOR")    INTEGRATOR,
    @EnumValue("INTEGRATION")   INTEGRATION
}
