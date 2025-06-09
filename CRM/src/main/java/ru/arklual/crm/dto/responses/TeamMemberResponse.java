package ru.arklual.crm.dto.responses;

import lombok.Builder;
import lombok.Data;
import ru.arklual.crm.entity.TeamRole;

import java.util.UUID;

@Data
@Builder
public class TeamMemberResponse {

    private UUID teamId;

    private UUID userId;

    private TeamRole role;

}
