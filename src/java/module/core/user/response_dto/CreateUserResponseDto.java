package module.core.user.response_dto;

import module.core.common.BaseResponse;

public class CreateUserResponseDto extends BaseResponse {
    private String userId;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
