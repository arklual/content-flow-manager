package ru.arklual.crm.converters;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.arklual.crm.dto.*;
import ru.arklual.crm.dto.protobuf.Flow;
import ru.arklual.crm.dto.protobuf.PostProto;
import ru.arklual.crm.dto.requests.FlowRequest;
import ru.arklual.crm.dto.responses.FlowResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class FlowConverterTest {

    @Autowired
    private FlowConverter converter;

    @Test
    void testToGrpcAndFromGrpc() {
        UUID teamId = UUID.randomUUID();
        Instant updatedAt = Instant.now();

        FlowRequest request = FlowRequest.builder()
                .teamId(teamId)
                .source(Source.builder()
                        .telegramSource(TelegramSource.builder()
                                .chatId("12345")
                                .build())
                        .build())
                .sinks(List.of(Sink.builder()
                        .telegramSink(TelegramSink.builder()
                                .chatId("67890")
                                .build())
                        .build()))
                .filters(List.of(Filter.builder()
                        .addPhoto(AddPhotoFilter.builder()
                                .trigger("on_add")
                                .photoUrl("https://example.com/photo.jpg")
                                .build())
                        .build()))
                .build();

        Flow grpcFlow = converter.toGrpc(request);

        assertThat(grpcFlow.getTeamId()).isEqualTo(teamId.toString());
        assertThat(grpcFlow.getSource().getTelegramSource().getChatId()).isEqualTo("12345");
        assertThat(grpcFlow.getSinks(0).getTelegramSink().getChatId()).isEqualTo("67890");
        assertThat(grpcFlow.getFilters(0).hasAddPhoto()).isTrue();
        assertThat(grpcFlow.getFilters(0).getAddPhoto().getPhotoUrl()).isEqualTo("https://example.com/photo.jpg");

        FlowResponse result = converter.fromGrpc(grpcFlow);

        assertThat(result.getTeamId()).isEqualTo(request.getTeamId());
        assertThat(result.getSource().getTelegramSource().getChatId()).isEqualTo("12345");
        assertThat(result.getSinks().getFirst().getTelegramSink().getChatId()).isEqualTo("67890");
        assertThat(result.getFilters().getFirst().getAddPhoto().getPhotoUrl()).isEqualTo("https://example.com/photo.jpg");
    }

    @Test
    void testNullUpdatedAt() {
        FlowRequest request = FlowRequest.builder()
                .teamId(UUID.randomUUID())
                .source(Source.builder().build())
                .sinks(List.of())
                .filters(List.of())
                .build();

        Flow grpcFlow = converter.toGrpc(request);

        assertThat(grpcFlow.hasUpdatedAt()).isFalse();
    }

    @Test
    void testVariousFilters() {
        Filter removePhoto = Filter.builder()
                .removePhoto(RemovePhotoFilter.builder().trigger("on_remove").build())
                .build();

        Filter textReplace = Filter.builder()
                .textReplace(TextReplaceFilter.builder()
                        .trigger("on_text")
                        .pattern("pattern")
                        .replacement("replacement")
                        .build())
                .build();

        Filter aiFilter = Filter.builder()
                .aiFilter(AIFilter.builder()
                        .trigger("on_ai")
                        .prompt("make me an image")
                        .build())
                .build();

        FlowRequest request = FlowRequest.builder()
                .teamId(UUID.randomUUID())
                .source(Source.builder().build())
                .sinks(List.of())
                .filters(List.of(removePhoto, textReplace, aiFilter))
                .build();

        Flow grpcFlow = converter.toGrpc(request);

        assertThat(grpcFlow.getFiltersCount()).isEqualTo(3);

        FlowResponse result = converter.fromGrpc(grpcFlow);

        assertThat(result.getFilters().get(0).getRemovePhoto().getTrigger()).isEqualTo("on_remove");
        assertThat(result.getFilters().get(1).getTextReplace().getPattern()).isEqualTo("pattern");
        assertThat(result.getFilters().get(2).getAiFilter().getPrompt()).isEqualTo("make me an image");
    }

    @Test
    void testSourceWithoutTelegramSource() {
        PostProto.Source grpcSource = PostProto.Source.newBuilder().build();
        Source result = converter.convertSource(grpcSource);
        assertThat(result).isNotNull();
        assertThat(result.getTelegramSource()).isNull();
    }

    @Test
    void testSinkWithoutTelegramSink() {
        PostProto.Sink grpcSink = PostProto.Sink.newBuilder().build();
        Sink result = converter.convertSink(grpcSink);
        assertThat(result).isNotNull();
        assertThat(result.getTelegramSink()).isNull();
    }


    @Test
    void testConvertSinkToGrpc_withoutTelegramSink() {
        Sink sink = Sink.builder().telegramSink(null).build();

        PostProto.Sink grpcSink = converter.convertSinkToGrpc(sink);
        assertThat(grpcSink.hasTelegramSink()).isFalse();
    }



}
