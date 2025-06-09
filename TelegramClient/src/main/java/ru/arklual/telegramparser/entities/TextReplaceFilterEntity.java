package ru.arklual.telegramparser.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TextReplaceFilterEntity implements FilterEntity {
    private final String type = "text_replace";
    private String trigger;
    private String pattern;
    private String replacement;
}
