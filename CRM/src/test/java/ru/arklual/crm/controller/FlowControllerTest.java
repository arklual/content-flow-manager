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
import ru.arklual.crm.converters.FlowConverter;
import ru.arklual.crm.dto.Sink;
import ru.arklual.crm.dto.Source;
import ru.arklual.crm.dto.TelegramSink;
import ru.arklual.crm.dto.TelegramSource;
import ru.arklual.crm.dto.protobuf.*;
import ru.arklual.crm.dto.requests.FlowRequest;
import ru.arklual.crm.dto.responses.FlowResponse;
import ru.arklual.crm.service.UserService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
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
class FlowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ru.arklual.crm.dto.protobuf.FlowServiceGrpc.FlowServiceBlockingStub grpcClient;

    @MockitoBean
    private FlowConverter flowConverter;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(username = "test@crm.ru")
    public void createFlowOk() throws Exception {
        UUID teamId = UUID.randomUUID();

        FlowRequest request = FlowRequest.builder()
                .teamId(teamId)
                .source(new Source(
                        new TelegramSource("987654321")
                ))
                .sinks(List.of(
                        Sink.builder()
                                .telegramSink(
                                        TelegramSink.builder()
                                                .chatId("123456789")
                                                .build()
                                )
                                .build()
                ))
                .filters(List.of())
                .requiresModeration(false)
                .build();

        Flow grpcFlow = Flow.newBuilder()
                .setId("123")
                .setTeamId(teamId.toString())
                .setRequiresModeration(false)
                .build();

        FlowResponse flowResponse = FlowResponse.builder()
                .id("123")
                .teamId(teamId)
                .requiresModeration(false)
                .updatedAt(Instant.now())
                .build();

        Mockito.when(userService.isUserNotInTeam(eq(teamId), anyString())).thenReturn(false);
        Mockito.when(flowConverter.toGrpc(any())).thenReturn(grpcFlow);
        Mockito.when(grpcClient.createFlow(any(CreateFlowRequest.class)))
                .thenReturn(CreateFlowResponse.newBuilder().setFlow(grpcFlow).build());
        Mockito.when(flowConverter.fromGrpc(any())).thenReturn(flowResponse);

        mockMvc.perform(post("/api/flows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.teamId").value(teamId.toString()));
    }

    @Test
    @WithMockUser(username = "test@crm.ru")
    public void createFlowForbiddenifUserNotInTeam() throws Exception {
        UUID teamId = UUID.randomUUID();

        FlowRequest request = FlowRequest.builder()
                .teamId(teamId)
                .source(new Source(
                        new TelegramSource("987654321")
                ))
                .sinks(List.of(
                        Sink.builder()
                                .telegramSink(
                                        TelegramSink.builder()
                                                .chatId("123456789")
                                                .build()
                                )
                                .build()
                ))
                .filters(List.of())
                .requiresModeration(false)
                .build();
        Mockito.when(userService.isUserNotInTeam(eq(teamId), anyString())).thenReturn(true);

        mockMvc.perform(post("/api/flows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @WithMockUser(username = "test@crm.ru")
    void getFlow_shouldReturnFlow() throws Exception {
        String flowId = "flow123";
        UUID teamId = UUID.randomUUID();

        Flow grpcFlow = Flow.newBuilder()
                .setId(flowId)
                .setTeamId(teamId.toString())
                .setRequiresModeration(false)
                .build();

        FlowResponse response = FlowResponse.builder()
                .id(flowId)
                .teamId(teamId)
                .requiresModeration(false)
                .updatedAt(Instant.now())
                .build();

        Mockito.when(grpcClient.getFlow(any())).thenReturn(GetFlowResponse.newBuilder().setFlow(grpcFlow).build());
        Mockito.when(flowConverter.fromGrpc(any())).thenReturn(response);

        mockMvc.perform(get("/api/flows/{id}", flowId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(flowId));
    }

    @Test
    @WithMockUser(username = "test@crm.ru")
    void updateFlow_shouldReturnOk() throws Exception {
        UUID teamId = UUID.randomUUID();
        String flowId = "flow123";

        FlowRequest request = FlowRequest.builder()
                .teamId(teamId)
                .source(new Source(new TelegramSource("987654321")))
                .sinks(List.of(Sink.builder().telegramSink(TelegramSink.builder().chatId("123456789").build()).build()))
                .filters(List.of())
                .requiresModeration(false)
                .build();

        Flow grpcFlow = Flow.newBuilder()
                .setId(flowId)
                .setTeamId(teamId.toString())
                .setRequiresModeration(false)
                .build();

        FlowResponse flowResponse = FlowResponse.builder()
                .id(flowId)
                .teamId(teamId)
                .requiresModeration(false)
                .updatedAt(Instant.now())
                .build();

        Mockito.when(userService.isUserNotInTeam(eq(teamId), anyString())).thenReturn(false);
        Mockito.when(flowConverter.toGrpc(any())).thenReturn(grpcFlow);
        Mockito.when(grpcClient.updateFlow(any())).thenReturn(UpdateFlowResponse.newBuilder().setFlow(grpcFlow).build());
        Mockito.when(flowConverter.fromGrpc(any())).thenReturn(flowResponse);

        mockMvc.perform(put("/api/flows/{id}", flowId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(flowId));
    }

    @Test
    @WithMockUser(username = "test@crm.ru")
    void deleteFlow_shouldReturnSuccess() throws Exception {
        String flowId = "flow123";

        Mockito.when(grpcClient.deleteFlow(any())).thenReturn(DeleteFlowResponse.newBuilder().setSuccess(true).build());

        mockMvc.perform(delete("/api/flows/{id}", flowId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success: true"));
    }

    @Test
    @WithMockUser(username = "test@crm.ru")
    void listFlows_shouldReturnList() throws Exception {
        UUID teamId = UUID.randomUUID();

        Flow grpcFlow = Flow.newBuilder()
                .setId("flow123")
                .setTeamId(teamId.toString())
                .setRequiresModeration(false)
                .build();

        FlowResponse response = FlowResponse.builder()
                .id("flow123")
                .teamId(teamId)
                .requiresModeration(false)
                .updatedAt(Instant.now())
                .build();

        Mockito.when(userService.isUserNotInTeam(eq(teamId), anyString())).thenReturn(false);
        Mockito.when(grpcClient.listFlows(any())).thenReturn(ListFlowsResponse.newBuilder().addFlows(grpcFlow).build());
        Mockito.when(flowConverter.fromGrpc(any())).thenReturn(response);

        mockMvc.perform(get("/api/flows").param("teamId", teamId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("flow123"));
    }

    @Test
    @WithMockUser(username = "test@crm.ru")
    void listFlows_shouldReturnForbidden_ifUserNotInTeam() throws Exception {
        UUID teamId = UUID.randomUUID();

        Mockito.when(userService.isUserNotInTeam(eq(teamId), anyString())).thenReturn(true);

        mockMvc.perform(get("/api/flows")
                        .param("teamId", teamId.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @WithMockUser(username = "test@crm.ru")
    void updateFlow_shouldReturnForbidden_ifUserNotInTeam() throws Exception {
        UUID teamId = UUID.randomUUID();
        String flowId = "flow123";

        FlowRequest request = FlowRequest.builder()
                .teamId(teamId)
                .source(new Source(new TelegramSource("source123")))
                .sinks(List.of(Sink.builder().telegramSink(TelegramSink.builder().chatId("sink123").build()).build()))
                .filters(List.of())
                .requiresModeration(false)
                .build();

        Mockito.when(userService.isUserNotInTeam(eq(teamId), anyString())).thenReturn(true);

        mockMvc.perform(put("/api/flows/{id}", flowId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }


}