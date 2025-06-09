package ru.arklual.crm.controller;

import com.google.protobuf.Empty;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.arklual.crm.converters.TelegramClientConverter;
import ru.arklual.crm.dto.responses.LinkResponse;
import ru.arklual.crm.dto.responses.MessageResponse;
import ru.arklual.crm.dto.responses.TelegramClientStatusResponse;
import ru.arklual.crm.dto.protobuf.TelegramClientProto;
import ru.arklual.crm.dto.protobuf.TelegramClientServiceGrpc;
import ru.arklual.crm.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@SecurityRequirement(name = "BearerAuth")
@RequestMapping("/api/telegram")
@AllArgsConstructor
public class TelegramClientController {

    private final TelegramClientServiceGrpc.TelegramClientServiceBlockingStub grpcClient;
    private final TelegramClientConverter telegramClientConverter;
    private final UserService userService;

    @Operation(summary = "Запустить Telegram-клиент для команды")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Клиент запущен",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа к команде",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @PostMapping("/start")
    public ResponseEntity<?> startClient(@RequestParam UUID teamId,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        if (isUserNotInTeam(userDetails, teamId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Access Denied"));
        }

        var response = grpcClient.startClient(
                TelegramClientProto.TeamId.newBuilder().setTeamId(String.valueOf(teamId)).build());

        return ResponseEntity.ok(new MessageResponse(response.getMessage()));
    }

    @Operation(summary = "Остановить Telegram-клиент для команды")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Клиент остановлен",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа к команде",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @PostMapping("/stop")
    public ResponseEntity<?> stopClient(@RequestParam UUID teamId,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        if (isUserNotInTeam(userDetails, teamId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Access Denied"));
        }

        var response = grpcClient.stopClient(
                TelegramClientProto.TeamId.newBuilder().setTeamId(String.valueOf(teamId)).build());

        return ResponseEntity.ok(new MessageResponse(response.getMessage()));
    }

    @Operation(summary = "Получить статус Telegram-клиента команды")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус получен",
                    content = @Content(schema = @Schema(implementation = TelegramClientStatusResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа к команде",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @GetMapping("/status")
    public ResponseEntity<?> getClientStatus(@RequestParam UUID teamId,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        if (isUserNotInTeam(userDetails, teamId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Access Denied"));
        }

        var grpcResponse = grpcClient.getClientStatus(
                TelegramClientProto.TeamId.newBuilder().setTeamId(String.valueOf(teamId)).build());

        return ResponseEntity.ok(telegramClientConverter.fromGrpc(grpcResponse));
    }

    @Operation(summary = "Получить QR-ссылку авторизации Telegram-клиента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ссылка получена",
                    content = @Content(schema = @Schema(implementation = LinkResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа к команде",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @GetMapping("/qr")
    public ResponseEntity<?> getQrLink(@RequestParam UUID teamId,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        if (isUserNotInTeam(userDetails, teamId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Access Denied"));
        }

        var response = grpcClient.getQrLink(
                TelegramClientProto.TeamId.newBuilder().setTeamId(String.valueOf(teamId)).build());

        return ResponseEntity.ok(new LinkResponse(response.getLink()));
    }

    @Operation(summary = "Список всех команд, где активен Telegram-клиент")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список ID команд",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @GetMapping("/clients")
    public ResponseEntity<List<String>> listClients() {
        var response = grpcClient.listClients(Empty.newBuilder().build());
        return ResponseEntity.ok(response.getTeamIdsList());
    }

    private boolean isUserNotInTeam(UserDetails userDetails, UUID teamId) {
        String email = userDetails.getUsername();
        return userService.isUserNotInTeam(teamId, email);
    }
}
