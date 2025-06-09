package ru.arklual.crm.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TextReplaceFilter {
    private String trigger;
    private String pattern;
    private String replacement;
}
