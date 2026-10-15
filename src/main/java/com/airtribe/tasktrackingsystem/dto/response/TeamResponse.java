package com.airtribe.tasktrackingsystem.dto.response;

import com.airtribe.tasktrackingsystem.model.Team;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamResponse {
    private Long id;
    private String name;
    private String description;
    private UserResponse.UserSummary createdBy;
    private List<UserResponse.UserSummary> members;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TeamResponse from(Team team){
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .createdBy(team.getCreatedBy() != null ?
                        new UserResponse.UserSummary(
                                team.getCreatedBy().getId(),
                                team.getCreatedBy().getUsername(),
                                team.getCreatedBy().getEmail()):
                        null)
                .members(team.getMembers() != null ?
                        team.getMembers().stream()
                                .map(user -> new UserResponse.UserSummary(
                                        user.getId(),
                                        user.getUsername(),
                                        user.getEmail()))
                                .collect(Collectors.toList()):
                        Collections.emptyList())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }
}
















