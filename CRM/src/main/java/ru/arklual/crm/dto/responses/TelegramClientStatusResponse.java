package ru.arklual.crm.dto.responses;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TelegramClientStatusResponse {
    private UUID teamId;
    private String state;
    private boolean isRunning;
}
