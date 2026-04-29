package module.core.sql.interfaces;

import entity.PasswordResetTokenEntity;

public interface IPasswordResetTokenRepository {
    void createToken(String email, String tokenHash, int expiryMinutes);
    PasswordResetTokenEntity findValidByTokenHash(String tokenHash);
    void markUsed(String id);
}
