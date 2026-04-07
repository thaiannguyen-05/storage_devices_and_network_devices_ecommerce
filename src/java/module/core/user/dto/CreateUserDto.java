package module.core.user.dto;

import java.time.LocalDate;

public class CreateUserDto {
    private String name;
    private LocalDate dateOfBirth;
    private String hashPassword;
    private String email;

    public CreateUserDto() {
    }

    public CreateUserDto(String name, LocalDate dateOfBirth, String hashPassword, String email) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.hashPassword = hashPassword;
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

    public String getHashPassword() {
        return hashPassword;
    }

    public void setHashPassword(String hashPassword) {
        this.hashPassword = hashPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
