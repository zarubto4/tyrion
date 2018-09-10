package utilities.enums;

import io.ebean.annotation.EnumValue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum PaymentMethod {
    @EnumValue("INVOICE_BASED") INVOICE_BASED, // user can choose how to pay the invoice after it is issued
    @EnumValue("CREDIT_CARD")   CREDIT_CARD; // after invoice is issued, it will be payed automatically

    public static List<PaymentMethod> fromString(String listString) {
        return Arrays.stream(listString.split("\\|")).map(PaymentMethod::valueOf).collect(Collectors.toList());
    }
}
