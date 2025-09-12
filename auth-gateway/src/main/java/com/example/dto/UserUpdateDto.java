package com.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateDto {

    @NotBlank(message = "Username cannot be empty")
    private String username;
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email")
    private String email;
    @NotBlank(message = "First name cannot be empty")
    private String firstName;
    @NotBlank(message = "Last name cannot be empty")
    private String lastName;
    @NotBlank(message = "Middle name cannot be empty")
    private String middleName;
    @NotBlank(message = "Role cannot be empty")
    private String role;
    @NotBlank(message = "Active cannot be empty")
    private boolean active;
}
