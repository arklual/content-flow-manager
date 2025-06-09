package ru.arklual.crm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.arklual.crm.converters.PostConverter;
import ru.arklual.crm.dto.protobuf.ApprovalServiceGrpc;
import ru.arklual.crm.dto.protobuf.ApprovalServiceProto;
import ru.arklual.crm.dto.responses.MessageResponse;
import ru.arklual.crm.dto.responses.PostResponse;
import ru.arklual.crm.service.TeamService;
import ru.arklual.crm.service.UserService;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/approval")
@SecurityRequirement(name = "BearerAuth")
@AllArgsConstructor
public class ApprovalController {

    private final ApprovalServiceGrpc.ApprovalServiceBlockingStub grpcClient;
    private final PostConverter postConverter;
    private final TeamService teamService;
    private final UserService userService;

    @Operation(summary = "Одобрить пост")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пост одобрен",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @PostMapping("/approve")
    public ResponseEntity<?> approvePost(
            @RequestParam UUID teamId,
            @RequestParam String postId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (teamService.isNotEditSupportRole(userDetails.getUsername(), teamId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Access Denied"));
        }

        var request = ApprovalServiceProto.TeamPostRequest.newBuilder()
                .setTeamId(String.valueOf(teamId))
                .setPostId(postId)
                .build();

        var response = grpcClient.approvePost(request);
        return ResponseEntity.ok(new MessageResponse(response.getMessage()));
    }

    @Operation(summary = "Отклонить пост")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пост отклонён",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @PostMapping("/reject")
    public ResponseEntity<?> rejectPost(
            @RequestParam UUID teamId,
            @RequestParam String postId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (teamService.isNotEditSupportRole(userDetails.getUsername(), teamId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Access Denied"));
        }

        var request = ApprovalServiceProto.TeamPostRequest.newBuilder()
                .setTeamId(String.valueOf(teamId))
                .setPostId(postId)
                .build();

        var response = grpcClient.rejectPost(request);
        return ResponseEntity.ok(new MessageResponse(response.getMessage()));
    }

    @Operation(summary = "Получить посты на модерации")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Посты",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PostResponse.class)))),
            @ApiResponse(responseCode = "403", description = "Нет доступа к команде",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingPosts(@RequestParam UUID teamId,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        if (userService.isUserNotInTeam(teamId, userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Access Denied"));
        }

        var request = ApprovalServiceProto.TeamIdRequest.newBuilder().setTeamId(String.valueOf(teamId)).build();
        var response = grpcClient.getPendingPosts(request);
        var posts = Set.copyOf(response.getPostsList());

        List<PostResponse> result = posts.stream()
                .map(postConverter::fromGrpc)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
