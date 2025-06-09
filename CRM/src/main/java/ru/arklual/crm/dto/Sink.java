package ru.arklual.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Sink {
    private TelegramSink telegramSink;
}
