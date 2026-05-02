package module.core.outbox;

public class TypeEvent {
    public static final String SEND_VERIFY_EMAIL = "SEND_VERIFY_EMAIL";

    public String getSendVerifyEmail() {
        return SEND_VERIFY_EMAIL;
    }
}
