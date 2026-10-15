package com.airtribe.tasktrackingsystem.controller;

import com.airtribe.tasktrackingsystem.dto.request.TeamRequest;
import com.airtribe.tasktrackingsystem.dto.response.TeamResponse;
import com.airtribe.tasktrackingsystem.dto.response.UserResponse;
import com.airtribe.tasktrackingsystem.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(
            @Valid @RequestBody TeamRequest request,
            Authentication authentication
    ){
        return ResponseEntity.ok(teamService.createTeam(request, authentication.getName()));
    }
    @GetMapping
    public ResponseEntity<Page<TeamResponse>> getMyTeams(
            Pageable pageable,
            Authentication authentication
    ){
        String username = authentication.getName();
        //need to get user Id from username - simplified for example
        return ResponseEntity.ok(teamService.getTeamsByUser(1L, pageable));
    }
    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> getTeamById(@PathVariable Long id){
        return ResponseEntity.ok(teamService.getTeamById(id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<TeamResponse> updateTeam(
            @PathVariable Long id,
            @Valid @RequestBody TeamRequest request
    ){
        return ResponseEntity.ok(teamService.updateTeam(id, request));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id){
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{teamId}/members/{userId}")
    public ResponseEntity<TeamResponse> addMember(
            @PathVariable Long teamId,
            @PathVariable Long userId
    ){
        return ResponseEntity.ok(teamService.addMemberToTeam(teamId, userId));
    }
    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<TeamResponse> removeMember(
            @PathVariable Long teamId,
            @PathVariable Long userId
    ){
        return ResponseEntity.ok(teamService.removeMemberFromTeam(teamId,userId));
    }

    @GetMapping("/{teamId}/members")
    public ResponseEntity<List<UserResponse>> getTeamMembers(@PathVariable Long teamId){
        List<UserResponse> members = teamService.getTeamMembers(teamId).stream()
                .map(UserResponse::from)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(members);
    }
}



















