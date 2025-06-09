package ru.arklual.telegramparser.entities;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TelegramSinkEntity.class, name = "telegram_sink")
})
public interface SinkEntity {
    String getType();
}

