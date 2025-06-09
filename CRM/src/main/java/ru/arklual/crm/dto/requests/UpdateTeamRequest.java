package ru.arklual.crm.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.arklual.crm.entity.TeamStatus;

@Data
public class UpdateTeamRequest {

    @NotBlank
    @Size(min = 3, max = 255)
    private String name;
    
    private TeamStatus status;

}
