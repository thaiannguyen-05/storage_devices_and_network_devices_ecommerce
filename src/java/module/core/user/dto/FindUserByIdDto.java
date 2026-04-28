package module.core.user.dto;

public class FindUserByIdDto {
    private int id;

    public FindUserByIdDto() {
    }

    public FindUserByIdDto(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
