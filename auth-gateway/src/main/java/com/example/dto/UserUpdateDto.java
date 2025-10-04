package com.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserUpdateDto {

    @Pattern(regexp = "^\\S+$", message = "Username cannot contain only whitespace")
    private String username;

    @Email(message = "Invalid email")
    private String email;

    @Pattern(regexp = "^\\S+$", message = "First name cannot contain only whitespace")
    private String firstName;

    @Pattern(regexp = "^\\S+$", message = "Last name cannot contain only whitespace")
    private String lastName;

    @Pattern(regexp = "^$|^\\S+$", message = "Middle name cannot contain only whitespace")
    private String middleName;

    @Pattern(regexp = "^\\S+$", message = "Role cannot contain only whitespace")
    private String role;

    private boolean active;
}
