package ru.arklual.telegramparser.entities;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TelegramSourceEntity implements SourceEntity {
    private final String type = "telegram_source";
    private String chatId;
}
