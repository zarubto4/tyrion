package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Enum_Currency {

    @EnumValue("czk") CZK(203),
    @EnumValue("eur") EUR(978),
    @EnumValue("price_in_usd") USD(911);

    public static final String CODE_CZK = String.valueOf(CZK);
    public static final String CODE_EUR = String.valueOf(EUR);
    public static final String CODE_USD = String.valueOf(USD);

    private Integer numericalCode;

    private Enum_Currency(Integer numericalCode) {
        this.numericalCode = numericalCode;
    }

    public Integer getNumericalCode() {
        return numericalCode;
    }

    public String getCode() {
        return String.valueOf(this);
    }

}