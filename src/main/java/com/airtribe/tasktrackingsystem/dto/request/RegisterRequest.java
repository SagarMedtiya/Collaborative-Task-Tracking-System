package com.airtribe.tasktrackingsystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message ="Username is required")
    @Size(min =3, max =50, message ="Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message ="Email is required")
    @Email(message ="Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message="Password must be at least 6 characters")
    private String password;

    @Size(max = 50, message = "First name must be less tthan 50 charcters")
    private String firstName;

    @Size(max = 50, message = "Last name must be les than 50 characters")
    private String lastName;

}
