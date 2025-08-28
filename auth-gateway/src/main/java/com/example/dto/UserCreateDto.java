package com.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserCreateDto {

    @NotBlank(message = "Username required")
    private String username;
    @Email(message = "Invalid email")
    private String email;
    @NotBlank(message = "Password required")
    private String password;
    private String role;
    @NotBlank(message = "First name required")
    private String firstName;
    @NotBlank(message = "Last name required")
    private String lastName;
    private String middleName;
}
