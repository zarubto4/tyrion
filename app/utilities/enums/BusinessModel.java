package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum BusinessModel {

    @EnumValue("SAAS")          SAAS,
    @EnumValue("FEE")           FEE,
    @EnumValue("INTEGRATOR")    INTEGRATOR,
    @EnumValue("INTEGRATION")   INTEGRATION
}
