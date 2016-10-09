package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Currency {

    @EnumValue("czk") CZK(203),
    @EnumValue("eur") EUR(978),
    @EnumValue("usd") USD(911);

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