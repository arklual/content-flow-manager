package ru.arklual.crm.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.arklual.crm.entity.UserStatus;

import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
public class UserResponse {

    private UUID uuid;
    private String email;
    private String name;
    private UserStatus status;

}
