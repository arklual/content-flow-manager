package ru.arklual.crm.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.arklual.crm.entity.TeamRole;

import java.util.UUID;

@Data
@Builder
public class AddTeamMemberRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private TeamRole role;
}
