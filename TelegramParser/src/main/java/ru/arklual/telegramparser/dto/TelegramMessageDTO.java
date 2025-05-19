package ru.arklual.telegramparser.dto;

public record TelegramMessageDTO(String teamId, long chatId, int date, String text) {}