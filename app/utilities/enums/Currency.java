package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Currency {

    @EnumValue("alpha") CZK(203),
    @EnumValue("alpha") EUR(978);

    public static final String CODE_CZK = String.valueOf(CZK);
    public static final String CODE_EUR = String.valueOf(EUR);

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