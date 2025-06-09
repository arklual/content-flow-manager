package ru.arklual.crm.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.arklual.crm.dto.responses.TeamMemberResponse;
import ru.arklual.crm.dto.responses.TeamResponse;
import ru.arklual.crm.dto.requests.AddTeamMemberRequest;
import ru.arklual.crm.dto.requests.CreateTeamRequest;
import ru.arklual.crm.dto.requests.UpdateTeamRequest;
import ru.arklual.crm.entity.*;
import ru.arklual.crm.exception.ResourceAlreadyExistsException;
import ru.arklual.crm.exception.ResourceNotFoundException;
import ru.arklual.crm.repository.TeamMemberRepository;
import ru.arklual.crm.repository.TeamRepository;
import ru.arklual.crm.repository.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public TeamResponse createTeam(CreateTeamRequest request, String email) {
        if (teamRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Team with name " + request.getName() + " already exists");
        }

        Team team = new Team();
        team.setName(request.getName());
        team.setStatus(TeamStatus.ACTIVE);

        Team savedTeam = teamRepository.save(team);
        User author = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found"));
        addTeamMember(savedTeam.getId(), AddTeamMemberRequest.builder().role(TeamRole.ADMIN).userId(author.getId()).build());
        return mapToDto(savedTeam);
    }

    @Transactional(readOnly = true)
    public TeamResponse getTeamById(UUID id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
        return mapToDto(team);
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsByStatus(TeamStatus status) {
        return teamRepository.findByStatus(status).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TeamResponse updateTeam(UUID id, UpdateTeamRequest request) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));

        if (!team.getName().equals(request.getName()) && teamRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Team with name " + request.getName() + " already exists");
        }

        team.setName(request.getName());
        if (request.getStatus() != null) {
            team.setStatus(request.getStatus());
        }

        Team updatedTeam = teamRepository.save(team);
        return mapToDto(updatedTeam);
    }

    @Transactional
    public TeamResponse addTeamMember(UUID teamId, AddTeamMemberRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        TeamMember member = new TeamMember();
        member.setTeam(team);
        member.setUser(user);
        member.setRole(request.getRole());
        teamMemberRepository.save(member);
        team.getMembers().add(member);
        return mapToDto(team);
    }

    @Transactional
    public void deleteTeam(UUID id) {
        if (!teamRepository.existsById(id)) {
            throw new ResourceNotFoundException("Team not found with id: " + id);
        }
        teamRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean isNotUserAdmin(String email, UUID teamId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return !teamMemberRepository.existsByTeamIdAndUserIdAndRole(teamId, user.getId(), TeamRole.ADMIN);
    }

    @Transactional(readOnly = true)
    public boolean isNotEditSupportRole(String email, UUID teamId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return teamMemberRepository.existsByTeamIdAndUserIdAndRole(teamId, user.getId(), TeamRole.VIEWER);
    }

    public List<TeamMemberResponse> getTeamMembersById(UUID id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
        Set<TeamMember> teamMembers = team.getMembers();
        return teamMembers.stream()
                .map(member -> TeamMemberResponse.builder()
                        .teamId(id)
                        .role(member.getRole())
                        .userId(member.getUser().getId())
                        .build()
                ).toList();
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        List<TeamMember> teamMembers = teamMemberRepository.findByUserId(user.getId());
        return teamMembers.stream()
                .map(TeamMember::getTeam)
                .distinct()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private TeamResponse mapToDto(Team team) {
        TeamResponse dto = new TeamResponse();
        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setStatus(team.getStatus());
        dto.setCreatedAt(team.getCreatedAt());
        dto.setUpdatedAt(team.getUpdatedAt());
        return dto;
    }


}
