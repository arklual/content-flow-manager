package ru.arklual.crm.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.arklual.crm.converters.TelegramClientConverter;
import ru.arklual.crm.dto.protobuf.TelegramClientProto;
import ru.arklual.crm.dto.protobuf.TelegramClientServiceGrpc;
import ru.arklual.crm.dto.responses.TelegramClientStatusResponse;
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
class TelegramClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TelegramClientServiceGrpc.TelegramClientServiceBlockingStub grpcClient;

    @MockitoBean
    private TelegramClientConverter telegramClientConverter;

    @Test
    @WithMockUser(username = "user@example.com")
    void startClient_shouldReturnOk_ifUserInTeam() throws Exception {
        UUID teamId = UUID.randomUUID();

        when(userService.isUserNotInTeam(eq(teamId), eq("user@example.com"))).thenReturn(false);
        when(grpcClient.startClient(any())).thenReturn(
                TelegramClientProto.MessageResponse.newBuilder().setMessage("Started").build());

        mockMvc.perform(post("/api/telegram/start")
                        .param("teamId", teamId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Started"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void stopClient_shouldReturnOk_ifUserInTeam() throws Exception {
        UUID teamId = UUID.randomUUID();

        when(userService.isUserNotInTeam(eq(teamId), eq("user@example.com"))).thenReturn(false);
        when(grpcClient.stopClient(any())).thenReturn(
                TelegramClientProto.MessageResponse.newBuilder().setMessage("Stopped").build());

        mockMvc.perform(post("/api/telegram/stop")
                        .param("teamId", teamId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Stopped"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getClientStatus_shouldReturnStatus_ifUserInTeam() throws Exception {
        UUID teamId = UUID.randomUUID();
        TelegramClientStatusResponse statusResponse = TelegramClientStatusResponse.builder()
                .isRunning(true)
                .state("READY")
                .build();

        when(userService.isUserNotInTeam(eq(teamId), eq("user@example.com"))).thenReturn(false);
        when(grpcClient.getClientStatus(any())).thenReturn(
                TelegramClientProto.ClientStatusResponse.newBuilder()
                        .setTeamId(teamId.toString())
                        .setState("READY")
                        .setIsRunning(true)
                        .build());
        when(telegramClientConverter.fromGrpc(any())).thenReturn(statusResponse);

        mockMvc.perform(get("/api/telegram/status")
                        .param("teamId", teamId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("READY"))
                .andExpect(jsonPath("$.running").value(true));
    }


    @Test
    @WithMockUser(username = "user@example.com")
    void getQrLink_shouldReturnLink_ifUserInTeam() throws Exception {
        UUID teamId = UUID.randomUUID();

        when(userService.isUserNotInTeam(eq(teamId), eq("user@example.com"))).thenReturn(false);
        when(grpcClient.getQrLink(any())).thenReturn(
                TelegramClientProto.LinkResponse.newBuilder().setLink("https://t.me/qr").build());

        mockMvc.perform(get("/api/telegram/qr")
                        .param("teamId", teamId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.link").value("https://t.me/qr"));
    }

    @Test
    @WithMockUser
    void listClients_shouldReturnTeamIds() throws Exception {
        when(grpcClient.listClients(any())).thenReturn(
                TelegramClientProto.ListClientsResponse.newBuilder()
                        .addTeamIds("team-1")
                        .addTeamIds("team-2")
                        .build());

        mockMvc.perform(get("/api/telegram/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("team-1"))
                .andExpect(jsonPath("$[1]").value("team-2"));
    }

    @Test
    @WithMockUser(username = "stranger@example.com")
    void startClient_shouldReturnForbidden_ifUserNotInTeam() throws Exception {
        UUID teamId = UUID.randomUUID();
        when(userService.isUserNotInTeam(eq(teamId), eq("stranger@example.com"))).thenReturn(true);

        mockMvc.perform(post("/api/telegram/start")
                        .param("teamId", teamId.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @WithMockUser(username = "stranger@example.com")
    void getClientStatus_shouldReturnForbidden_ifUserNotInTeam() throws Exception {
        UUID teamId = UUID.randomUUID();
        when(userService.isUserNotInTeam(eq(teamId), eq("stranger@example.com"))).thenReturn(true);

        mockMvc.perform(get("/api/telegram/status")
                        .param("teamId", teamId.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @WithMockUser(username = "stranger@example.com")
    void stopClient_shouldReturnForbidden_ifUserNotInTeam() throws Exception {
        UUID teamId = UUID.randomUUID();

        when(userService.isUserNotInTeam(eq(teamId), eq("stranger@example.com"))).thenReturn(true);

        mockMvc.perform(post("/api/telegram/stop")
                        .param("teamId", teamId.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @WithMockUser(username = "stranger@example.com")
    void getQrLink_shouldReturnForbidden_ifUserNotInTeam() throws Exception {
        UUID teamId = UUID.randomUUID();

        when(userService.isUserNotInTeam(eq(teamId), eq("stranger@example.com"))).thenReturn(true);

        mockMvc.perform(get("/api/telegram/qr")
                        .param("teamId", teamId.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

}