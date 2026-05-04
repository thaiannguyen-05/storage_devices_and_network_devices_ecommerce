package module.core.outbox;

public class TypeEvent {
    public static final String SEND_VERIFY_EMAIL = "SEND_VERIFY_EMAIL";
    public static final String SEND_FORGOT_PASSWORD_CODE = "SEND_FORGOT_PASSWORD_CODE";

    public String getSendVerifyEmail() {
        return SEND_VERIFY_EMAIL;
    }

    public String getSendForgotPasswordCode() {
        return SEND_FORGOT_PASSWORD_CODE;
    }
}
