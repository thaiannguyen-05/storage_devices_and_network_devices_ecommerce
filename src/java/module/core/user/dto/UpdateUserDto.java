package module.core.user.dto;

import java.time.LocalDate;

public class UpdateUserDto {
    private String name;
    private LocalDate dateOfBirth;
    private String status;
    private String role;
    private String email;

    public UpdateUserDto() {
    }

    public UpdateUserDto(String name, LocalDate dateOfBirth, String status, String role, String email) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.status = status;
        this.role = role;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
