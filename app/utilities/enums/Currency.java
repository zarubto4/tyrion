package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum Currency {

    @EnumValue("CZK") CZK(203),
    @EnumValue("EUR") EUR(978),
    @EnumValue("USD") USD(911);

    public static final String CODE_CZK = String.valueOf(CZK);
    public static final String CODE_EUR = String.valueOf(EUR);
    public static final String CODE_USD = String.valueOf(USD);

    private Integer numericalCode;

    private Currency(Integer numericalCode) {
        this.numericalCode = numericalCode;
    }

    public Integer getNumericalCode() {
        return numericalCode;
    }

    public String getCode() {
        return String.valueOf(this);
    }

}