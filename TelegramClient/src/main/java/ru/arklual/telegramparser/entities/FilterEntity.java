package ru.arklual.telegramparser.entities;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextReplaceFilterEntity.class, name = "text_replace"),
        @JsonSubTypes.Type(value = AIFilterEntity.class, name = "ai_filter")
})
public interface FilterEntity {
    String getType();
    String getTrigger();
}

