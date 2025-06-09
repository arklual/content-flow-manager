package ru.arklual.telegramparser.entities;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TelegramSourceEntity.class, name = "telegram_source")
})
public interface SourceEntity {
    String getType();
}

