package com.airtribe.tasktrackingsystem.dto.response;

import com.airtribe.tasktrackingsystem.model.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String role;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse from (Users users){
        return UserResponse.builder()
                .id(users.getId())
                .username(users.getUsername())
                .email(users.getEmail())
                .firstName(users.getFirstName())
                .lastName(users.getLastName())
                .fullName(users.getFullName())
                .role(users.getRole() != null ? users.getRole().name() : null)
                .isActive(users.getIsActive())
                .createdAt(users.getCreatedAt())
                .updatedAt(users.getUpdatedAt())
                .build();
    }
}















