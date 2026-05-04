package module.bussiness.payment;

public class Constant {

    private Constant() {
    }

    public static final String PAYMENT_METHOD_COD = "cod";
    public static final String PAYMENT_METHOD_SEPAY = "sepay";

    public static final String PAYMENT_STATUS_PENDING = "PENDING";
    public static final String PAYMENT_STATUS_SUCCESS = "SUCCESS";
    public static final String PAYMENT_STATUS_FAILED = "FAILED";
    public static final String PAYMENT_STATUS_CANCELLED = "CANCELLED";

    public static final String ORDER_STATUS_PENDING = "PENDING";
    public static final String ORDER_STATUS_CONFIRMED = "CONFIRMED";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";
}
