package ru.arklual.telegramparser.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.arklual.telegramparser.dto.protobuf.PostProto.Source;
import ru.arklual.telegramparser.dto.protobuf.PostProto.TelegramSource;
import ru.arklual.telegramparser.entities.SourceEntity;
import ru.arklual.telegramparser.entities.TelegramSourceEntity;

import static org.junit.jupiter.api.Assertions.*;

class TelegramSourceConverterTest {

    private TelegramSourceConverter converter;

    @BeforeEach
    void setUp() {
        converter = new TelegramSourceConverter();
    }

    @Test
    void getKey_shouldReturnTelegramSource() {
        assertEquals("telegram_source", converter.getKey());
    }

    @Test
    void toEntity_shouldExtractChatId() {
        String chatId = "chat-321";
        Source proto = Source.newBuilder()
                .setTelegramSource(TelegramSource.newBuilder().setChatId(chatId).build())
                .build();

        SourceEntity entity = converter.toEntity(proto);

        assertInstanceOf(TelegramSourceEntity.class, entity);
        TelegramSourceEntity tse = (TelegramSourceEntity) entity;
        assertEquals(chatId, tse.getChatId());
    }

    @Test
    void toProto_shouldBuildSourceWithChatId() {
        String chatId = "chat-123";
        TelegramSourceEntity entity = new TelegramSourceEntity();
        entity.setChatId(chatId);

        Source proto = converter.toProto(entity);

        assertTrue(proto.hasTelegramSource());
        assertEquals(chatId, proto.getTelegramSource().getChatId());
    }
}
