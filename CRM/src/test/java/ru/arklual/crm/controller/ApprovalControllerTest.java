package ru.arklual.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.arklual.crm.converters.PostConverter;
import ru.arklual.crm.dto.protobuf.ApprovalServiceGrpc;
import ru.arklual.crm.dto.protobuf.ApprovalServiceProto;
import ru.arklual.crm.dto.protobuf.PostProto;
import ru.arklual.crm.dto.responses.PostResponse;
import ru.arklual.crm.service.TeamService;
import ru.arklual.crm.service.UserService;

import java.util.UUID;

import static org.mockito.Mockito.*;
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
public class ApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TeamService teamService;

    @MockitoBean
    private ApprovalServiceGrpc.ApprovalServiceBlockingStub grpcClient;

    @MockitoBean
    private PostConverter postConverter;

    @Test
    @WithMockUser(username = "user@example.com")
    void getPendingPosts_shouldReturnList_ifUserInTeam() throws Exception {
        UUID teamId = UUID.randomUUID();
        PostProto.Post grpcPost = PostProto.Post.newBuilder().setId("post123").build();
        ApprovalServiceProto.PostList grpcResponse = ApprovalServiceProto.PostList.newBuilder()
                .addPosts(grpcPost).build();
        PostResponse response = PostResponse.builder().id("post123").build();

        when(userService.isUserNotInTeam(eq(teamId), eq("user@example.com"))).thenReturn(false);
        when(grpcClient.getPendingPosts(any())).thenReturn(grpcResponse);
        when(postConverter.fromGrpc(any())).thenReturn(response);

        mockMvc.perform(get("/api/approval/pending").param("teamId", teamId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("post123"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getPendingPosts_shouldReturnForbidden_ifUserNotInTeam() throws Exception {
        UUID teamId = UUID.randomUUID();

        when(userService.isUserNotInTeam(eq(teamId), eq("user@example.com"))).thenReturn(true);

        mockMvc.perform(get("/api/approval/pending").param("teamId", teamId.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @WithMockUser(username = "editor@example.com")
    void approvePost_shouldReturnOk_ifUserHasEditSupportRole() throws Exception {
        UUID teamId = UUID.randomUUID();
        String postId = "post123";

        when(teamService.isNotEditSupportRole(eq("editor@example.com"), eq(teamId))).thenReturn(false);
        when(grpcClient.approvePost(any())).thenReturn(
                ApprovalServiceProto.MessageResponse.newBuilder().setMessage("Approved").build());

        mockMvc.perform(post("/api/approval/approve")
                        .param("teamId", teamId.toString())
                        .param("postId", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Approved"));
    }

    @Test
    @WithMockUser(username = "viewer@example.com")
    void approvePost_shouldReturnForbidden_ifNoAccess() throws Exception {
        UUID teamId = UUID.randomUUID();

        when(teamService.isNotEditSupportRole(eq("viewer@example.com"), eq(teamId))).thenReturn(true);

        mockMvc.perform(post("/api/approval/approve")
                        .param("teamId", teamId.toString())
                        .param("postId", "post123"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @WithMockUser(username = "editor@example.com")
    void rejectPost_shouldReturnOk_ifUserHasEditSupportRole() throws Exception {
        UUID teamId = UUID.randomUUID();
        String postId = "post123";

        when(teamService.isNotEditSupportRole(eq("editor@example.com"), eq(teamId))).thenReturn(false);
        when(grpcClient.rejectPost(any())).thenReturn(
                ApprovalServiceProto.MessageResponse.newBuilder().setMessage("Rejected").build());

        mockMvc.perform(post("/api/approval/reject")
                        .param("teamId", teamId.toString())
                        .param("postId", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Rejected"));
    }

    @Test
    @WithMockUser(username = "viewer@example.com")
    void rejectPost_shouldReturnForbidden_ifNoAccess() throws Exception {
        UUID teamId = UUID.randomUUID();

        when(teamService.isNotEditSupportRole(eq("viewer@example.com"), eq(teamId))).thenReturn(true);

        mockMvc.perform(post("/api/approval/reject")
                        .param("teamId", teamId.toString())
                        .param("postId", "post123"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }
}