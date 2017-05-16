package utilities.financial.goPay.helps_objects;

import java.util.ArrayList;
import java.util.List;

public class GoPay_Payer {

    public PaymentInstrument payment_instrument;

    public List<PaymentInstrument> allowed_payment_instruments = new ArrayList<>();

    public List<String> allowed_swifts = new ArrayList<>();

    public PaymentInstrument default_payment_instrument;

    public String default_swift;

    public GoPay_Contact contact;

    public class Lang {

        public static final String CS = "CS";
        public static final String EN = "EN";
        public static final String SK = "SK";
        public static final String DE = "DE";
        public static final String RU = "RU";
    }

    public enum PaymentInstrument{
        PAYMENT_CARD,
        PAYSAFECARD,
        PAYPAL
    }

}
