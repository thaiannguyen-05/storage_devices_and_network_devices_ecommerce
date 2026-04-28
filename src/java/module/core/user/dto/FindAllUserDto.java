package module.core.user.dto;

public class FindAllUserDto {
    private int page;
    private int limit;

    public FindAllUserDto() {
    }

    public FindAllUserDto(int page, int limit) {
        this.page = page;
        this.limit = limit;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
