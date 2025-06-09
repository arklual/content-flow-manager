package ru.arklual.crm.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.arklual.crm.dto.requests.AddTeamMemberRequest;
import ru.arklual.crm.dto.requests.CreateTeamRequest;
import ru.arklual.crm.dto.requests.UpdateTeamRequest;
import ru.arklual.crm.dto.responses.TeamMemberResponse;
import ru.arklual.crm.dto.responses.TeamResponse;
import ru.arklual.crm.entity.TeamRole;
import ru.arklual.crm.entity.TeamStatus;
import ru.arklual.crm.service.TeamService;
import ru.arklual.crm.service.UserService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class TeamControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    TeamService teamService;

    @MockitoBean
    UserService userService;

    @Test
    @WithMockUser(username = "user@example.com")
    void createTeam_shouldReturnCreated() throws Exception {
        CreateTeamRequest request = new CreateTeamRequest();
        request.setName("Team Rocket");

        TeamResponse response = new TeamResponse();
        response.setId(UUID.randomUUID());
        response.setName("Team Rocket");

        Mockito.when(teamService.createTeam(eq(request), eq("user@example.com")))
                .thenReturn(response);

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Team Rocket"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void addTeamMember_shouldReturnForbidden_ifNotAdmin() throws Exception {
        UUID teamId = UUID.randomUUID();
        AddTeamMemberRequest request = AddTeamMemberRequest.builder()
                .userId(UUID.randomUUID())
                .role(TeamRole.VIEWER)
                .build();

        Mockito.when(teamService.isNotUserAdmin(eq("user@example.com"), eq(teamId)))
                .thenReturn(true);

        mockMvc.perform(post("/api/teams/{id}/members", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getTeamMembers_shouldReturnForbidden_ifUserNotInTeam() throws Exception {
        UUID teamId = UUID.randomUUID();

        Mockito.when(userService.isUserNotInTeam(eq(teamId), eq("user@example.com")))
                .thenReturn(true);

        mockMvc.perform(get("/api/teams/{id}/members", teamId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @WithMockUser
    void getAllTeams_shouldReturnAllTeams() throws Exception {
        TeamResponse team = new TeamResponse();
        team.setId(UUID.randomUUID());
        team.setName("All Teams");

        Mockito.when(teamService.getAllTeams()).thenReturn(List.of(team));

        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("All Teams"));
    }

    @Test
    @WithMockUser
    void getAllTeams_withStatus_shouldReturnFiltered() throws Exception {
        TeamResponse team = new TeamResponse();
        team.setId(UUID.randomUUID());
        team.setName("Active Team");

        Mockito.when(teamService.getTeamsByStatus(eq(TeamStatus.ACTIVE)))
                .thenReturn(List.of(team));

        mockMvc.perform(get("/api/teams?status=ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Active Team"));
    }

    @Test
    @WithMockUser
    void getTeamById_shouldReturnTeam() throws Exception {
        UUID id = UUID.randomUUID();
        TeamResponse team = new TeamResponse();
        team.setId(id);
        team.setName("Team X");

        Mockito.when(teamService.getTeamById(eq(id))).thenReturn(team);

        mockMvc.perform(get("/api/teams/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Team X"));
    }

    @Test
    @WithMockUser
    void deleteTeam_shouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/teams/" + id))
                .andExpect(status().isNoContent());

        Mockito.verify(teamService).deleteTeam(eq(id));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getMyTeams_shouldReturnList() throws Exception {
        TeamResponse team = new TeamResponse();
        team.setId(UUID.randomUUID());
        team.setName("My Team");

        Mockito.when(teamService.getTeamsByUserEmail(eq("user@example.com")))
                .thenReturn(List.of(team));

        mockMvc.perform(get("/api/teams/my-teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("My Team"));
    }

    @Test
    @WithMockUser(username = "admin@crm.ru")
    void addTeamMember_shouldSucceed_ifAdmin() throws Exception {
        UUID teamId = UUID.randomUUID();
        AddTeamMemberRequest request = AddTeamMemberRequest.builder()
                .userId(UUID.randomUUID())
                .role(TeamRole.VIEWER)
                .build();

        TeamResponse response = new TeamResponse();
        response.setId(teamId);
        response.setName("Team With New Member");

        Mockito.when(teamService.isNotUserAdmin(eq("admin@crm.ru"), eq(teamId))).thenReturn(false);
        Mockito.when(teamService.addTeamMember(eq(teamId), eq(request))).thenReturn(response);

        mockMvc.perform(post("/api/teams/{id}/members", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Team With New Member"));
    }

    @Test
    @WithMockUser
    void updateTeam_shouldReturnUpdated() throws Exception {
        UUID teamId = UUID.randomUUID();
        UpdateTeamRequest request = new UpdateTeamRequest();
        request.setName("Updated Team");
        request.setStatus(TeamStatus.ACTIVE);

        TeamResponse response = new TeamResponse();
        response.setId(teamId);
        response.setName("Updated Team");

        Mockito.when(teamService.updateTeam(eq(teamId), eq(request))).thenReturn(response);

        mockMvc.perform(put("/api/teams/{id}", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Team"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getTeamMembers_shouldReturnList_ifUserInTeam() throws Exception {
        UUID teamId = UUID.randomUUID();

        TeamMemberResponse member = TeamMemberResponse.builder()
                .teamId(teamId)
                .userId(UUID.randomUUID())
                .role(TeamRole.VIEWER)
                .build();

        Mockito.when(userService.isUserNotInTeam(eq(teamId), eq("user@example.com"))).thenReturn(false);
        Mockito.when(teamService.getTeamMembersById(eq(teamId))).thenReturn(List.of(member));

        mockMvc.perform(get("/api/teams/{id}/members", teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(member.getUserId().toString()));
    }

}
