package module.core.auth;

import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import module.core.config.AppConfig;

public class PasswordService {
    private final SecureRandom random = new SecureRandom();

    public String hash(String password) {
        try {
            byte[] salt = new byte[AppConfig.SALT_LENGTH];
            random.nextBytes(salt);
            byte[] hash = pbkdf(password, salt);
            return "pbkdf2:" + AppConfig.PBKDF2_ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt)
                    + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public boolean matches(String password, String storedHash) {
        try {
            if (storedHash == null || !storedHash.startsWith("pbkdf2:")) {
                return false;
            }
            String[] parts = storedHash.split(":");
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = pbkdf(password, salt);
            return java.security.MessageDigest.isEqual(expected, actual);
        } catch (Exception ex) {
            return false;
        }
    }

    private byte[] pbkdf(String password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, AppConfig.PBKDF2_ITERATIONS, 256);
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    }
}
