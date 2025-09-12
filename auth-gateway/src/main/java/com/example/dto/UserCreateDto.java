package com.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserCreateDto {

    @NotNull(message = "Username cannot be null")
    @NotBlank(message = "Username required")
    private String username;
    @NotNull(message = "Email cannot be null")
    @NotBlank(message = "Email required")
    @Email(message = "Invalid email")
    private String email;
    @NotNull(message = "Password cannot be null")
    @NotBlank(message = "Password required")
    private String password;
    @NotNull(message = "Role cannot be null")
    @NotBlank(message = "Role required")
    private String role;
    @NotNull(message = "First name cannot be null")
    @NotBlank(message = "First name required")
    private String firstName;
    @NotNull(message = "Last name cannot be null")
    @NotBlank(message = "Last name required")
    private String lastName;
    private String middleName;
}
