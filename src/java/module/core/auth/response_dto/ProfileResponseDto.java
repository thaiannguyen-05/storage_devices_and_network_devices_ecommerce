package module.core.auth.response_dto;

import entity.UserEntity;

public class ProfileResponseDto {
    private boolean success;
    private String errorMessage;
    private UserEntity profileUser;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public UserEntity getProfileUser() {
        return profileUser;
    }

    public void setProfileUser(UserEntity profileUser) {
        this.profileUser = profileUser;
    }
}
