package ru.arklual.telegramparser.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.arklual.telegramparser.dto.protobuf.PostProto.Sink;
import ru.arklual.telegramparser.dto.protobuf.PostProto.TelegramSink;
import ru.arklual.telegramparser.entities.SinkEntity;
import ru.arklual.telegramparser.entities.TelegramSinkEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SinkConverterProviderTest {

    private SinkConverterProvider provider;

    @BeforeEach
    void setUp() {
        provider = new SinkConverterProvider(List.of(
                new TelegramSinkConverter()
        ));
    }

    @Test
    void toEntity_shouldConvertTelegramSink() {
        String chatId = "chat123";
        Sink proto = Sink.newBuilder()
                .setTelegramSink(TelegramSink.newBuilder().setChatId(chatId).build())
                .build();

        SinkEntity entity = provider.toEntity(proto);

        assertInstanceOf(TelegramSinkEntity.class, entity);
        TelegramSinkEntity tse = (TelegramSinkEntity) entity;
        assertEquals(chatId, tse.getChatId());
        assertEquals("telegram_sink", tse.getType());
    }

    @Test
    void toProto_shouldConvertTelegramSinkEntity() {
        String chatId = "chatXYZ";
        TelegramSinkEntity tse = new TelegramSinkEntity();
        tse.setChatId(chatId);

        Sink proto = provider.toProto(tse);

        assertTrue(proto.hasTelegramSink());
        assertEquals(chatId, proto.getTelegramSink().getChatId());
    }

    @Test
    void toEntity_shouldThrow_whenUnknownType() {
        Sink proto = Sink.newBuilder().build();
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> provider.toEntity(proto)
        );
        assertTrue(ex.getMessage().contains("No converter for sink type"));
    }

    @Test
    void toProto_shouldThrow_whenUnknownEntity() {
        SinkEntity fake = () -> "nope";
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> provider.toProto(fake)
        );
        assertTrue(ex.getMessage().contains("No converter for sink entity"));
    }
}
