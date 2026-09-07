package com.airtribe.tasktrackingsystem.service;

import com.airtribe.tasktrackingsystem.dto.request.TeamRequest;
import com.airtribe.tasktrackingsystem.model.Team;
import com.airtribe.tasktrackingsystem.model.Users;
import com.airtribe.tasktrackingsystem.repository.TeamRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

private final TeamRepository teamRepository;
private final UserService userService;

@Transactional
    public TeamResponse createTeam(TeamRequest request, String username){
    Users createdBy = userService.getEntityByUsername(username);

    if(teamRepository.existsByName(request.getName())){
        throw new RuntimeException("Team with name '" + request.getName() + "' already exists");
    }
    Team team = Team.builder()
            .name(request.getName())
            .description(request.getDescription())
            .createdBy(createdBy)
            .build();

    //add creator as member
    team.getMembers().add(createdBy);

    //Add other members
    if(request.getMemberIds() != null){
        for(Long memberId : request.getMemberIds()){
            if(!memberId.equals(createdBy.getId())){
                Users member = userService.getEntityById(memberId);
                team.getMembers().add(member);
            }
        }
    }
    team = teamRepository.save(team);
    return TeamResponse.from(team);
    }
    public Page<TeamResponse> getTeamsById(Long userId, Pageable pageable){
        return teamRespository.findByMembersId(userId, pageable)
                .map(TeamResponse::from)
    }
    public TeamResponse getTeamById(Long id){
        Team team = teamRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Team not found with id:" + id));
        return TeamResponse.from(team);
    }
    @Transactional
    public TeamResponse updateTeam(Long id, TeamRequest request){
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: "+ id));
        if(request.getName() != null && !request.getName().equals(team.getName())){
            if(teamRepository.existsByName(request.getName())){
                throw new RuntimeException("Team with name '"+ request.getName() + "' already exists");
            }
            team.setName(request.getName());
        }
        if(request.getDescription() != null){
            team.setDescription(request.getDescription());
        }
        team = teamRepository.save(team);
        return TeamResponse.from(team);
    }
    @Transactional
    public void deleteTeam(Long id){
        if(!teamRepository.existsById(id)){
            throw new ResourceNotFoundException("Team not found with id: "+ id);
        }
        teamRepository.deleteById(id);
    }
    @Transactional
    public TeamResponse addMemberToTeam(Long teamId, Long userId){
        Team team = teamRepository.findById(teamId)
                .orElseThrow(()-> new ResourceNotFoundException("Team not found with id: "+ teamId));
        Users user = userService.getEntityById(userId);
        if(team.getMembers().contains(user)){
            throw new RuntimeException("User is already a member of this team");
        }
        team.getMembers().add(user);
        team = teamRepository.save(team);
        return TeamResponse.from(team);
    }
    @Transactional
    public TeamResponse removeMemberfromTeam(Long teamId, Long userId){
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id:" + teamId));
        Users user = userService.getEntityById(userId);
        if(!team.getMembers().contains(user)){
            throw new RuntimeException("User is not a member of this team");

        }
        //check if user is the creator
        if (team.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("Cannot remove the team creator from the team");
        }
        team.getMembers().remove(user);
        team = teamRepository.save(team);
        return TeamResponse.from(team);
    }
    public List<Users> getTeamMembers(Long teamId){
        return teamRepository.findTeamMembers(teamId);
    }
    public Team getEntityById(Long id){
        return teamRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Team not found with id: "+ id));
    }

}




















