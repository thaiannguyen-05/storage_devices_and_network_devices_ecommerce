package module.core.user.response_dto;

import entity.UserEntity;
import java.util.List;
import module.core.common.BaseResponse;

public class ListUserResponseDto extends BaseResponse {
    private List<UserEntity> users;
    private int total;

    public List<UserEntity> getUsers() { return users; }
    public void setUsers(List<UserEntity> users) { this.users = users; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
}
