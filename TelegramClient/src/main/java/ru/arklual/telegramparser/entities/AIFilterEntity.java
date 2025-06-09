package ru.arklual.telegramparser.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AIFilterEntity implements FilterEntity {
    private final String type = "ai_filter";
    private String trigger;
    private String prompt;
}
