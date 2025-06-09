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
import ru.arklual.crm.dto.requests.AddTeamMemberRequest;
import ru.arklual.crm.dto.requests.CreateTeamRequest;
import ru.arklual.crm.dto.requests.UpdateTeamRequest;
import ru.arklual.crm.dto.responses.MessageResponse;
import ru.arklual.crm.dto.responses.TeamResponse;
import ru.arklual.crm.entity.TeamStatus;
import ru.arklual.crm.service.TeamService;
import ru.arklual.crm.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
@SecurityRequirement(name = "BearerAuth")
@AllArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final UserService userService;

    @Operation(summary = "Создание команды")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Команда создана"),
            @ApiResponse(responseCode = "409", description = "Команда с таким именем уже существует",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody CreateTeamRequest request,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        TeamResponse createdTeam = teamService.createTeam(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTeam);
    }

    @Operation(summary = "Получить команду по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно"),
            @ApiResponse(responseCode = "404", description = "Команда не найдена",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> getTeamById(@PathVariable UUID id) {
        TeamResponse team = teamService.getTeamById(id);
        return ResponseEntity.ok(team);
    }

    @Operation(summary = "Получить все команды")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAllTeams(@RequestParam(required = false) TeamStatus status) {
        List<TeamResponse> teams = (status != null)
                ? teamService.getTeamsByStatus(status)
                : teamService.getAllTeams();
        return ResponseEntity.ok(teams);
    }

    @Operation(summary = "Обновить команду")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Команда обновлена"),
            @ApiResponse(responseCode = "404", description = "Команда не найдена",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "409", description = "Имя команды занято",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<TeamResponse> updateTeam(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdateTeamRequest request) {
        TeamResponse updatedTeam = teamService.updateTeam(id, request);
        return ResponseEntity.ok(updatedTeam);
    }

    @Operation(summary = "Удалить команду")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Команда удалена"),
            @ApiResponse(responseCode = "404", description = "Команда не найдена",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable UUID id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Добавить участника в команду")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно",
                    content = @Content(schema = @Schema(implementation = TeamResponse.class))),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Команда или пользователь не найдены",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @PostMapping("/{id}/members")
    public ResponseEntity<?> addTeamMember(@PathVariable UUID id,
                                           @Valid @RequestBody AddTeamMemberRequest request,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        if (teamService.isNotUserAdmin(userDetails.getUsername(), id)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Access Denied"));
        }
        TeamResponse updatedTeam = teamService.addTeamMember(id, request);
        return ResponseEntity.ok(updatedTeam);
    }

    @Operation(summary = "Получить участников команды")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TeamResponse.class)))),
            @ApiResponse(responseCode = "403", description = "Нет доступа к команде",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @GetMapping("/{id}/members")
    public ResponseEntity<?> getTeamMembers(@PathVariable UUID id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        if (userService.isUserNotInTeam(id, userDetails.getUsername())) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Access Denied"));
        }
        return ResponseEntity.ok(teamService.getTeamMembersById(id));
    }

    @Operation(summary = "Получить команды текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @GetMapping("/my-teams")
    public ResponseEntity<List<TeamResponse>> getMyTeams(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(teamService.getTeamsByUserEmail(userDetails.getUsername()));
    }

}
