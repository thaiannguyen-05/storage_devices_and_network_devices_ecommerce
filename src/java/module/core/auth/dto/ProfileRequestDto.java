package module.core.auth.dto;

public class ProfileRequestDto {
    private String authUserEmail;

    public String getAuthUserEmail() {
        return authUserEmail;
    }

    public void setAuthUserEmail(String authUserEmail) {
        this.authUserEmail = authUserEmail;
    }
}
