package ru.arklual.crm.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTeamRequest {

    @NotBlank
    @Size(min = 3, max = 255)
    private String name;

}
