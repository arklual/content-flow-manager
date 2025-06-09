package ru.arklual.telegramparser.service;

import com.google.protobuf.InvalidProtocolBufferException;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.jni.TdApi;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.arklual.telegramparser.dto.protobuf.PostProto;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TelegramPostConsumerTest {

    @Mock
    private TelegramClientManager telegramClientManager;

    @Mock
    private SimpleTelegramClient client;

    @InjectMocks
    private TelegramPostConsumer consumer;

    @Captor
    private ArgumentCaptor<TdApi.Function<?>> captor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(client.send(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(telegramClientManager.getClient(anyString())).thenReturn(client);
    }

    @Test
    void listen_shouldSendViaTelegramPoster_whenRecordIsValid() throws Exception {
        String teamId = "team42";
        String chatId = "123456";
        String content = "Hello, Telegram!";
        PostProto.Post proto = PostProto.Post.newBuilder()
                .setTeamId(teamId)
                .setContent(content)
                .addSink(PostProto.Sink.newBuilder()
                        .setTelegramSink(PostProto.TelegramSink.newBuilder()
                                .setChatId(chatId)
                                .build()))
                .build();
        byte[] payload = proto.toByteArray();
        ConsumerRecord<String, byte[]> record =
                new ConsumerRecord<>("content.TELEGRAM_SINK.approved", 0, 0L, null, payload);

        consumer.listen(record);

        verify(telegramClientManager).getClient(teamId);
        verify(client).send(captor.capture());

        TdApi.Function<?> fn = captor.getValue();
        assertInstanceOf(TdApi.SendMessage.class, fn, "Expected a SendMessage request");
        TdApi.SendMessage req = (TdApi.SendMessage) fn;
        assertEquals(Long.parseLong(chatId), req.chatId);
    }

    @Test
    void listen_shouldThrowRuntimeException_whenPayloadInvalid() {
        byte[] bad = new byte[]{0x01, 0x02, 0x03};
        ConsumerRecord<String, byte[]> record =
                new ConsumerRecord<>("content.TELEGRAM_SINK.approved", 0, 0L, null, bad);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> consumer.listen(record));
        assertInstanceOf(InvalidProtocolBufferException.class, ex.getCause());
    }
}
