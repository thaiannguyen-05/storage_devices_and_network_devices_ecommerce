package module.core.user.dto;

public class UpdateUserDto extends CreateUserDto {
    private String id;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}
