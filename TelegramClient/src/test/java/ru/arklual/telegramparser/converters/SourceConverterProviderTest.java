package ru.arklual.telegramparser.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.arklual.telegramparser.dto.protobuf.PostProto.Source;
import ru.arklual.telegramparser.dto.protobuf.PostProto.TelegramSource;
import ru.arklual.telegramparser.entities.SourceEntity;
import ru.arklual.telegramparser.entities.TelegramSourceEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SourceConverterProviderTest {

    private SourceConverterProvider provider;

    @BeforeEach
    void setUp() {
        provider = new SourceConverterProvider(List.of(
                new TelegramSourceConverter()
        ));
    }

    @Test
    void toEntity_shouldConvertTelegramSource() {
        String chatId = "chatABC";
        Source proto = Source.newBuilder()
                .setTelegramSource(TelegramSource.newBuilder().setChatId(chatId).build())
                .build();

        SourceEntity entity = provider.toEntity(proto);

        assertInstanceOf(TelegramSourceEntity.class, entity);
        TelegramSourceEntity tse = (TelegramSourceEntity) entity;
        assertEquals(chatId, tse.getChatId());
        assertEquals("telegram_source", tse.getType());
    }

    @Test
    void toProto_shouldConvertTelegramSourceEntity() {
        String chatId = "chatDEF";
        TelegramSourceEntity tse = new TelegramSourceEntity();
        tse.setChatId(chatId);

        Source proto = provider.toProto(tse);

        assertTrue(proto.hasTelegramSource());
        assertEquals(chatId, proto.getTelegramSource().getChatId());
    }

    @Test
    void toEntity_shouldThrow_whenUnknownType() {
        Source proto = Source.newBuilder().build();
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> provider.toEntity(proto)
        );
        assertTrue(ex.getMessage().contains("No converter for source type"));
    }

    @Test
    void toProto_shouldThrow_whenUnknownEntity() {
        SourceEntity fake = () -> "unknown";
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> provider.toProto(fake)
        );
        assertTrue(ex.getMessage().contains("No converter for source entity"));
    }
}
