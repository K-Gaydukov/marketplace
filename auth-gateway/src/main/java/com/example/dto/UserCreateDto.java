package com.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserCreateDto {

    @NotNull(message = "Username cannot be null")
    @NotBlank(message = "Username required")
    @Pattern(regexp = "^\\S+$", message = "Username cannot contain only whitespace")
    private String username;

    @NotNull(message = "Email cannot be null")
    @NotBlank(message = "Email required")
    @Email(message = "Invalid email")
    private String email;

    @NotNull(message = "Password cannot be null")
    @NotBlank(message = "Password required")
    @Pattern(regexp = "^\\S+$", message = "Password cannot contain only whitespace")
    private String password;

    private String role;

    @NotNull(message = "First name cannot be null")
    @NotBlank(message = "First name required")
    @Pattern(regexp = "^\\S+$", message = "First name cannot contain only whitespace")
    private String firstName;

    @NotNull(message = "Last name cannot be null")
    @NotBlank(message = "Last name required")
    @Pattern(regexp = "^\\S+$", message = "Last name cannot contain only whitespace")
    private String lastName;

    @Pattern(regexp = "^$|^\\S+$", message = "Middle name cannot contain only whitespace")
    private String middleName;
}
