package ru.arklual.crm.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AIFilter {
    private String trigger;
    private String prompt;
}
