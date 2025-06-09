package ru.arklual.crm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.arklual.crm.converters.FlowConverter;
import ru.arklual.crm.dto.requests.FlowRequest;
import ru.arklual.crm.dto.protobuf.*;
import ru.arklual.crm.dto.responses.FlowResponse;
import ru.arklual.crm.dto.responses.MessageResponse;
import ru.arklual.crm.dto.responses.StatusResponse;
import ru.arklual.crm.service.UserService;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/flows")
@SecurityRequirement(name = "BearerAuth")
@AllArgsConstructor
public class FlowController {

    private final FlowServiceGrpc.FlowServiceBlockingStub grpcClient;
    private final FlowConverter flowConverter;
    private final UserService userService;

    @Operation(summary = "Создание потока")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Поток создан",
                    content = @Content(schema = @Schema(implementation = FlowResponse.class))),
            @ApiResponse(responseCode = "403", description = "Пользователь не состоит в команде",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или аргумента",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })

    @PostMapping
    public ResponseEntity<?> createFlow(@RequestBody FlowRequest flowRequest,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        if (isUserNotInTeam(userDetails, flowRequest.getTeamId())) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Access Denied"));
        }

        var grpcRequest = CreateFlowRequest.newBuilder()
                .setFlow(flowConverter.toGrpc(flowRequest))
                .build();

        var grpcResponse = grpcClient.createFlow(grpcRequest).getFlow();
        return ResponseEntity.ok(flowConverter.fromGrpc(grpcResponse));
    }

    @Operation(summary = "Получить поток по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно",
                    content = @Content(schema = @Schema(implementation = FlowResponse.class))),
            @ApiResponse(responseCode = "404", description = "Поток не найден",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<FlowResponse> getFlow(@PathVariable String id) {
        var grpcResponse = grpcClient.getFlow(GetFlowRequest.newBuilder().setId(id).build()).getFlow();
        return ResponseEntity.ok(flowConverter.fromGrpc(grpcResponse));
    }

    @Operation(summary = "Обновить поток")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Поток обновлён",
                    content = @Content(schema = @Schema(implementation = FlowResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа к команде",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Поток не найден",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFlow(@PathVariable String id,
                                        @RequestBody @Valid FlowRequest flowRequest,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        if (isUserNotInTeam(userDetails, flowRequest.getTeamId())) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Access Denied"));
        }

        var grpcRequest = UpdateFlowRequest.newBuilder()
                .setFlow(flowConverter.toGrpc(flowRequest).toBuilder().setId(id).build())
                .build();

        var grpcResponse = grpcClient.updateFlow(grpcRequest).getFlow();
        return ResponseEntity.ok(flowConverter.fromGrpc(grpcResponse));
    }

    @Operation(summary = "Удалить поток")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Поток удалён",
                    content = @Content(schema = @Schema(implementation = StatusResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Поток не найден",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })

    @DeleteMapping("/{id}")
    public ResponseEntity<StatusResponse> deleteFlow(@PathVariable String id) {
        var grpcResponse = grpcClient.deleteFlow(DeleteFlowRequest.newBuilder().setId(id).build());
        return ResponseEntity.ok(new StatusResponse("success: %s".formatted(grpcResponse.getSuccess())));
    }

    @Operation(summary = "Получить список потоков по команде")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список потоков",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = FlowResponse.class)))),
            @ApiResponse(responseCode = "403", description = "Нет доступа к команде",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })

    @GetMapping
    public ResponseEntity<?> listFlows(@RequestParam UUID teamId,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        if (isUserNotInTeam(userDetails, teamId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Access Denied"));
        }

        var grpcResponse = grpcClient.listFlows(ListFlowsRequest.newBuilder()
                .setTeamId(String.valueOf(teamId)).build());

        var flows = grpcResponse.getFlowsList().stream()
                .map(flowConverter::fromGrpc)
                .collect(Collectors.toList());

        return ResponseEntity.ok(flows);
    }


    private boolean isUserNotInTeam(UserDetails userDetails, UUID teamId) {
        String email = userDetails.getUsername();
        return userService.isUserNotInTeam(teamId, email);
    }
}
