package module.core.auth.response_dto;

import common.type.UserPayload;
import module.core.common.BaseResponse;

public class LoginResponseDto extends BaseResponse {
    private String sessionId;
    private UserPayload user;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public UserPayload getUser() {
        return user;
    }

    public void setUser(UserPayload user) {
        this.user = user;
    }
}
