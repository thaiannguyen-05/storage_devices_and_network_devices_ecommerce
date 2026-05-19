package module.bussiness.ai;

import module.core.config.ConfigService;

public final class AiChatConfig {
    public static final String API_BASE_URL = "https://api.groq.com/openai/v1";
    public static final String MODEL_NAME = "llama-3.3-70b-versatile";
    public static final String STREAM_ENDPOINT = API_BASE_URL + "/chat/completions";
    public static final String CHAT_ENDPOINT = API_BASE_URL + "/chat/completions";
    public static final String SYSTEM_PROMPT =
            "Ban la tro ly ho tro khach hang cua LinhNamStore, cua hang ban do cong nghe. "
            + "Tra loi ngan gon, than thien, bang tieng Viet va uu tien tu van san pham phu hop.";
    public static final int MAX_TOKENS = 2048;
    public static final double TEMPERATURE = 0.7d;
    public static final int CONNECT_TIMEOUT_MS = 30000;
    public static final int READ_TIMEOUT_MS = 30000;
    public static final boolean ALLOW_INSECURE_SSL = Boolean.parseBoolean(
            ConfigService.getInstance().get("AI_ALLOW_INSECURE_SSL", "true"));

    private AiChatConfig() {
    }

    public static String getApiKey() {
        return ConfigService.getInstance().get("API_AI_TOKEN");
    }
}
