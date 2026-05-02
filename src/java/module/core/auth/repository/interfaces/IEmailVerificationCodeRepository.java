package module.core.auth.repository.interfaces;

import entity.EmailVerificationCodeEntity;

public interface IEmailVerificationCodeRepository {
    EmailVerificationCodeEntity create(String userId, String codeHash, int expiryMinutes);
    EmailVerificationCodeEntity findValidByUserId(String userId);
    boolean markUsed(String id);
    int invalidateAllByUserId(String userId);
}
