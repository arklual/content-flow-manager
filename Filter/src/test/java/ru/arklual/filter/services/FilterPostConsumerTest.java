package ru.arklual.filter.services;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.arklual.filter.dto.protobuf.PostProto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FilterPostConsumerTest {

    @Mock
    private KafkaProducer kafkaProducer;
    @Mock
    private FilterApplier filterApplier;
    @InjectMocks
    private FilterPostConsumer consumer;

    @Captor
    private ArgumentCaptor<PostProto.Post> postCaptor;
    @Captor
    private ArgumentCaptor<String> topicCaptor;

    private PostProto.Post rawPost;
    private PostProto.Filter filter1;
    private PostProto.Filter filter2;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);

        filter1 = PostProto.Filter.newBuilder()
                .setTextReplace(PostProto.TextReplaceFilter.newBuilder()
                        .setTrigger("t1")
                        .setPattern("foo")
                        .setReplacement("bar"))
                .build();
        filter2 = PostProto.Filter.newBuilder()
                .setAddPhoto(PostProto.AddPhotoFilter.newBuilder()
                        .setTrigger("t2")
                        .setPhotoUrl("url"))
                .build();

        rawPost = PostProto.Post.newBuilder()
                .setId("pX")
                .setTeamId("teamX")
                .setContent("foo baz")
                .addFilter(filter1)
                .addFilter(filter2)
                .setRequiresModeration(false)
                .build();
    }

    @Test
    void testListen_shouldApplyAllFiltersAndSendCleanedMessage_whenPayloadValid() throws Exception {
        byte[] payload = rawPost.toByteArray();
        ConsumerRecord<String, byte[]> record =
            new ConsumerRecord<>("row.content", 0, 0L, null, payload);

        PostProto.Post after1 = rawPost.toBuilder().setContent("bar baz").build();
        PostProto.Post after2 = after1.toBuilder()
                .addMedia(PostProto.Media.newBuilder()
                        .setUrl("url").setType(PostProto.MediaType.PHOTO).build())
                .build();

        when(filterApplier.applyFilter(filter1, rawPost)).thenReturn(after1);
        when(filterApplier.applyFilter(filter2, after1)).thenReturn(after2);

        consumer.listen(record);

        verify(kafkaProducer, times(1))
                .sendMessage(topicCaptor.capture(), postCaptor.capture());

        assertEquals("cleaned.content", topicCaptor.getValue());

        PostProto.Post sent = postCaptor.getValue();
        assertTrue(sent.getIsCleaned());
        assertEquals(PostProto.PostStatus.PENDING_REVIEW, sent.getStatus());
        assertEquals("bar baz", sent.getContent());
        assertEquals(1, sent.getMediaCount());
        assertEquals("url", sent.getMedia(0).getUrl());
    }

    @Test
    void testListen_shouldThrowRuntimeException_whenPayloadInvalid() {
        byte[] bad = {0x0A, 0x0B};
        ConsumerRecord<String, byte[]> record =
            new ConsumerRecord<>("row.content", 0, 0L, null, bad);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> consumer.listen(record));
        assertInstanceOf(InvalidProtocolBufferException.class, ex.getCause());
    }
}
