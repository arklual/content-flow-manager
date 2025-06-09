package ru.arklual.approvalservice.services;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.arklual.approvalservice.dto.protobuf.PostProto;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApprovalPostConsumerTest {

    @Mock
    private KafkaProducer kafkaProducer;
    @Mock
    private WaitingPostsRegistry waitingPostsRegistry;
    @InjectMocks
    private ApprovalPostConsumer consumer;

    @Captor
    private ArgumentCaptor<String> topicCaptor;
    @Captor
    private ArgumentCaptor<PostProto.Post> postCaptor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListen_shouldPublishApprovedAndNotAddToRegistry_whenRequiresModerationFalse() throws Exception {
        PostProto.Post incoming = PostProto.Post.newBuilder()
                .setId("id1")
                .setTeamId("team1")
                .setRequiresModeration(false)
                .addSink(PostProto.Sink.newBuilder()
                        .setTelegramSink(PostProto.TelegramSink.newBuilder()
                                .setChatId("chat1")
                                .build())
                        .build())
                .build();
        ConsumerRecord<String, byte[]> record =
                new ConsumerRecord<>("cleaned.content", 0, 0L, null, incoming.toByteArray());

        consumer.listen(record);

        verify(kafkaProducer, times(1))
                .sendMessage(topicCaptor.capture(), postCaptor.capture());
        assertEquals("content.TELEGRAM_SINK.approved", topicCaptor.getValue());
        assertEquals(PostProto.PostStatus.APPROVED, postCaptor.getValue().getStatus());

        verify(waitingPostsRegistry, never()).getPosts(any());
    }

    @Test
    void testListen_shouldAddToRegistryAndNotPublish_whenRequiresModerationTrue() throws Exception {
        PostProto.Post incoming = PostProto.Post.newBuilder()
                .setId("id2")
                .setTeamId("team2")
                .setRequiresModeration(true)
                .build();
        ConsumerRecord<String, byte[]> record =
                new ConsumerRecord<>("cleaned.content", 0, 0L, null, incoming.toByteArray());

        Set<PostProto.Post> posts = new HashSet<>();
        when(waitingPostsRegistry.getPosts("team2")).thenReturn(posts);

        consumer.listen(record);

        verify(kafkaProducer, never()).sendMessage(any(), any());
        assertTrue(posts.contains(incoming));
    }

    @Test
    void testListen_shouldThrowRuntimeException_whenPayloadInvalid() {
        byte[] bad = {0x01, 0x02, 0x03};
        ConsumerRecord<String, byte[]> record =
                new ConsumerRecord<>("cleaned.content", 0, 0L, null, bad);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> consumer.listen(record));
        assertInstanceOf(InvalidProtocolBufferException.class, ex.getCause());
    }
}
