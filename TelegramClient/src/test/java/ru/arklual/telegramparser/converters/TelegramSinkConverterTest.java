package ru.arklual.telegramparser.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.arklual.telegramparser.dto.protobuf.PostProto.Sink;
import ru.arklual.telegramparser.dto.protobuf.PostProto.TelegramSink;
import ru.arklual.telegramparser.entities.SinkEntity;
import ru.arklual.telegramparser.entities.TelegramSinkEntity;

import static org.junit.jupiter.api.Assertions.*;

class TelegramSinkConverterTest {

    private TelegramSinkConverter converter;

    @BeforeEach
    void setUp() {
        converter = new TelegramSinkConverter();
    }

    @Test
    void getKey_shouldReturnTelegramSink() {
        assertEquals("telegram_sink", converter.getKey());
    }

    @Test
    void toEntity_shouldExtractChatId() {
        String chatId = "chat-789";
        Sink proto = Sink.newBuilder()
                .setTelegramSink(TelegramSink.newBuilder().setChatId(chatId).build())
                .build();

        SinkEntity entity = converter.toEntity(proto);

        assertInstanceOf(TelegramSinkEntity.class, entity);
        TelegramSinkEntity tse = (TelegramSinkEntity) entity;
        assertEquals(chatId, tse.getChatId());
    }

    @Test
    void toProto_shouldBuildSinkWithChatId() {
        String chatId = "chat-987";
        TelegramSinkEntity entity = new TelegramSinkEntity();
        entity.setChatId(chatId);

        Sink proto = converter.toProto(entity);

        assertTrue(proto.hasTelegramSink());
        assertEquals(chatId, proto.getTelegramSink().getChatId());
    }
}
