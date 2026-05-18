package module.core.config;

public class AppConfig {
    public static final String JWT_SECRET = "change-this-secret-for-production";
    public static final long JWT_EXPIRY_MS = 24L * 60 * 60 * 1000;
    public static final int PBKDF2_ITERATIONS = 120000;
    public static final int SALT_LENGTH = 32;
    public static final int PAGE_SIZE = 12;
    public static final int MAX_CART_ITEMS = 50;
    public static final String CURRENCY = "VND";

    private AppConfig() {
    }

    public static String resolveJsp(String module, String action) {
        return "/views/" + module + "/" + action + ".jsp";
    }
}
