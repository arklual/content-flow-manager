package ru.arklual.crm.converters;

import org.junit.jupiter.api.Test;
import ru.arklual.crm.dto.*;
import ru.arklual.crm.dto.protobuf.PostProto;
import ru.arklual.crm.dto.responses.PostResponse;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostConverterTest {

    private final FilterConverter filterConverter = mock(FilterConverter.class);
    private final PostConverter postConverter = new PostConverter(filterConverter);

    @Test
    void fromGrpc_shouldConvertTextReplacePost() {
        UUID teamId = UUID.randomUUID();
        String postId = "post123";

        PostProto.TelegramSource telegramSource = PostProto.TelegramSource.newBuilder()
                .setChatId("123456789")
                .build();

        PostProto.Source grpcSource = PostProto.Source.newBuilder()
                .setTelegramSource(telegramSource)
                .build();

        PostProto.Media grpcMedia = PostProto.Media.newBuilder()
                .setUrl("https://media.url")
                .setType(PostProto.MediaType.PHOTO)
                .build();

        PostProto.TelegramSink telegramSink = PostProto.TelegramSink.newBuilder()
                .setChatId("987654321")
                .build();

        PostProto.Sink grpcSink = PostProto.Sink.newBuilder()
                .setTelegramSink(telegramSink)
                .build();

        PostProto.TextReplaceFilter grpcTextReplace = PostProto.TextReplaceFilter.newBuilder()
                .setTrigger("hello")
                .setPattern("foo")
                .setReplacement("bar")
                .build();

        PostProto.Filter grpcFilter = PostProto.Filter.newBuilder()
                .setTextReplace(grpcTextReplace)
                .build();

        Filter mockFilter = Filter.builder()
                .textReplace(TextReplaceFilter.builder()
                        .trigger("hello")
                        .pattern("foo")
                        .replacement("bar")
                        .build())
                .build();

        PostProto.Post grpcPost = PostProto.Post.newBuilder()
                .setId(postId)
                .setTeamId(teamId.toString())
                .setSource(grpcSource)
                .setContent("Test content")
                .addMedia(grpcMedia)
                .setCreatedAt(Instant.now().toEpochMilli())
                .setUpdatedAt(Instant.now().toEpochMilli())
                .setStatus(PostProto.PostStatus.APPROVED)
                .setRequiresModeration(true)
                .setIsCleaned(true)
                .addSink(grpcSink)
                .addFilter(grpcFilter)
                .build();

        when(filterConverter.fromGrpc(grpcFilter)).thenReturn(mockFilter);

        PostResponse response = postConverter.fromGrpc(grpcPost);

        assertEquals(postId, response.getId());
        assertEquals(teamId, response.getTeamId());
        assertEquals("Test content", response.getContent());
        assertEquals(PostProto.PostStatus.APPROVED, response.getStatus());
        assertTrue(response.isRequiresModeration());
        assertTrue(response.isCleaned());

        assertNotNull(response.getSource());
        assertNotNull(response.getSource().getTelegramSource());
        assertEquals("123456789", response.getSource().getTelegramSource().getChatId());

        assertNotNull(response.getMedia());
        assertEquals("https://media.url", response.getMedia().getUrl());
        assertEquals(PostProto.MediaType.PHOTO, response.getMedia().getMediaType());

        assertEquals(1, response.getSinks().size());
        assertEquals("987654321", response.getSinks().getFirst().getTelegramSink().getChatId());

        assertEquals(1, response.getFilters().size());
        assertNotNull(response.getFilters().getFirst().getTextReplace());
        assertEquals("foo", response.getFilters().getFirst().getTextReplace().getPattern());
    }

    @Test
    void fromGrpc_shouldHandleEmptyLists() {
        PostProto.Post grpcPost = PostProto.Post.newBuilder()
                .setId("post123")
                .setTeamId(UUID.randomUUID().toString())
                .setSource(PostProto.Source.newBuilder().build())
                .setContent("Minimal post")
                .setCreatedAt(Instant.now().toEpochMilli())
                .setUpdatedAt(Instant.now().toEpochMilli())
                .setStatus(PostProto.PostStatus.DRAFT)
                .setRequiresModeration(false)
                .setIsCleaned(false)
                .build();

        PostResponse response = postConverter.fromGrpc(grpcPost);

        assertEquals("Minimal post", response.getContent());
        assertNull(response.getMedia());
        assertTrue(response.getSinks().isEmpty());
        assertTrue(response.getFilters().isEmpty());
    }

    @Test
    void supportsGrpc_shouldReturnTrue_whenTextReplaceIsSet() {
        PostProto.TextReplaceFilter grpcTextReplace = PostProto.TextReplaceFilter.newBuilder()
                .setTrigger("hello")
                .setPattern("foo")
                .setReplacement("bar")
                .build();

        PostProto.Filter grpcFilter = PostProto.Filter.newBuilder()
                .setTextReplace(grpcTextReplace)
                .build();

        assertTrue(new TextReplaceFilterProvider().supportsGrpc(grpcFilter));
    }


    @Test
    void supportsDto_shouldReturnTrue_whenDtoHasTextReplace() {
        Filter dto = Filter.builder()
                .textReplace(TextReplaceFilter.builder()
                        .trigger("hi")
                        .pattern("x")
                        .replacement("y")
                        .build())
                .build();

        assertTrue(new TextReplaceFilterProvider().supportsDto(dto));
    }

    @Test
    void getBuilderFromGrpc_shouldFillFilterCorrectly() {
        PostProto.TextReplaceFilter grpcTextReplace = PostProto.TextReplaceFilter.newBuilder()
                .setTrigger("trigger")
                .setPattern("foo")
                .setReplacement("bar")
                .build();

        PostProto.Filter grpc = PostProto.Filter.newBuilder()
                .setTextReplace(grpcTextReplace)
                .build();

        Filter.FilterBuilder builder = Filter.builder();

        new TextReplaceFilterProvider().getBuilderFromGrpc(builder, grpc);
        Filter result = builder.build();

        assertNotNull(result.getTextReplace());
        assertEquals("trigger", result.getTextReplace().getTrigger());
        assertEquals("foo", result.getTextReplace().getPattern());
        assertEquals("bar", result.getTextReplace().getReplacement());
    }

    @Test
    void getBuilderFromDto_shouldSetTextReplace() {
        Filter dto = Filter.builder()
                .textReplace(TextReplaceFilter.builder()
                        .trigger("tr")
                        .pattern("p")
                        .replacement("r")
                        .build())
                .build();

        PostProto.Filter.Builder builder = PostProto.Filter.newBuilder();

        new TextReplaceFilterProvider().getBuilderFromDto(builder, dto);
        PostProto.TextReplaceFilter grpc = builder.getTextReplace();

        assertEquals("tr", grpc.getTrigger());
        assertEquals("p", grpc.getPattern());
        assertEquals("r", grpc.getReplacement());
    }

    @Test
    void fromGrpc_shouldHandleNoMedia() {
        PostProto.Post grpcPost = PostProto.Post.newBuilder()
                .setId("p1")
                .setTeamId(UUID.randomUUID().toString())
                .setSource(PostProto.Source.newBuilder().build())
                .setContent("No media")
                .setCreatedAt(0L)
                .setUpdatedAt(0L)
                .setStatus(PostProto.PostStatus.DRAFT)
                .setRequiresModeration(false)
                .setIsCleaned(false)
                .build();

        PostResponse result = postConverter.fromGrpc(grpcPost);
        assertNull(result.getMedia());
    }

    @Test
    void supportsDto_shouldReturnFalse_whenTextReplaceIsNull() {
        Filter dto = Filter.builder().textReplace(null).build();
        boolean result = new TextReplaceFilterProvider().supportsDto(dto);
        assertFalse(result);
    }

    @Test
    void convertSink_shouldHandleNullTelegramSink() {
        PostProto.Sink grpcSink = PostProto.Sink.newBuilder().build();
        Sink result = new PostConverter(mock(FilterConverter.class)).convertSink(grpcSink);
        assertNotNull(result);
        assertNull(result.getTelegramSink());
    }

}