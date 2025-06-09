package ru.arklual.crm.dto.responses;

import lombok.Data;
import ru.arklual.crm.entity.TeamStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TeamResponse {
    private UUID id;
    private String name;
    private TeamStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
