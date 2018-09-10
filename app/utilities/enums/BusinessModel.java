package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum BusinessModel {
    @EnumValue("ALPHA")         ALPHA, // deprecated, only for testing purposes
    @EnumValue("SAAS")          SAAS,
    @EnumValue("FEE")           FEE,
    @EnumValue("INTEGRATOR")    INTEGRATOR,
    @EnumValue("INTEGRATION")   INTEGRATION
}
