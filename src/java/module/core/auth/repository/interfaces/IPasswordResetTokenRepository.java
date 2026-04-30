package module.core.auth.repository.interfaces;

import entity.PasswordResetTokenEntity;
public interface IPasswordResetTokenRepository {
    PasswordResetTokenEntity create(String userId, String tokenHash, int expiryMinutes);
    PasswordResetTokenEntity findValidByTokenHash(String tokenHash);
    boolean markUsed(String id);
    int invalidateAllByUserId(String userId);
}
