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

    // Sepay Payment Gateway Config
    public static final String SEPAY_BANK = "MSB";
    public static final String SEPAY_ACC_NUM = "80003058262";
    public static final String SEPAY_ACC_NAME = "NGUYEN THAI AN";
    public static final String SEPAY_WEBHOOK_TOKEN = "02122005";
    // Copy this Webhook URL to your Sepay.vn dashboard (replace localhost with your
    // domain when deploying):
    public static final String SEPAY_WEBHOOK_URL = "https://d9b3-14-162-180-201.ngrok-free.app/WebApplication3/api/sepay/webhook";

    public static String resolveJsp(String module, String action) {
        return "/views/" + module + "/" + action + ".jsp";
    }
}
