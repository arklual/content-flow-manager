package ru.arklual.telegramparser.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TelegramSinkEntity implements SinkEntity {
    private final String type = "telegram_sink";
    private String chatId;
}
