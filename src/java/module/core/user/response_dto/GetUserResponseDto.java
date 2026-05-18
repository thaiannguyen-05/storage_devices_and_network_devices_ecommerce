package module.core.user.response_dto;

import entity.UserEntity;
import module.core.common.BaseResponse;

public class GetUserResponseDto extends BaseResponse {
    private UserEntity user;

    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
}
