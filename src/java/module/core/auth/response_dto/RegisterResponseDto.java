package module.core.auth.response_dto;

import module.core.common.BaseResponse;

public class RegisterResponseDto extends BaseResponse {
    private String userId;
    private String emailVerificationCode;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmailVerificationCode() {
        return emailVerificationCode;
    }

    public void setEmailVerificationCode(String emailVerificationCode) {
        this.emailVerificationCode = emailVerificationCode;
    }
}
