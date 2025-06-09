package ru.arklual.crm.converters;

import org.springframework.stereotype.Component;
import ru.arklual.crm.dto.*;
import ru.arklual.crm.dto.protobuf.Flow;
import ru.arklual.crm.dto.protobuf.PostProto;
import com.google.protobuf.Timestamp;
import ru.arklual.crm.dto.requests.FlowRequest;
import ru.arklual.crm.dto.responses.FlowResponse;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class FlowConverter {

    private final FilterConverter filterConverter;

    public FlowConverter(FilterConverter filterConverter) {
        this.filterConverter = filterConverter;
    }

    public FlowResponse fromGrpc(Flow grpc) {
        return FlowResponse.builder()
                .id(grpc.getId())
                .teamId(UUID.fromString(grpc.getTeamId()))
                .source(convertSource(grpc.getSource()))
                .sinks(grpc.getSinksList().stream()
                        .map(this::convertSink)
                        .collect(Collectors.toList()))
                .filters(grpc.getFiltersList().stream()
                        .map(filterConverter::fromGrpc)
                        .collect(Collectors.toList()))
                .requiresModeration(grpc.getRequiresModeration())
                .updatedAt(toInstant(grpc.getUpdatedAt()))
                .build();
    }

    Source convertSource(PostProto.Source grpc) {
        return Source.builder()
                .telegramSource(grpc.hasTelegramSource() ? TelegramSource.builder()
                        .chatId(grpc.getTelegramSource().getChatId())
                        .build() : null)
                .build();
    }

    Sink convertSink(PostProto.Sink grpc) {
        return Sink.builder()
                .telegramSink(grpc.hasTelegramSink() ? TelegramSink.builder()
                        .chatId(grpc.getTelegramSink().getChatId())
                        .build() : null)
                .build();
    }

    private PostProto.Source convertSourceToGrpc(Source dto) {
        PostProto.Source.Builder builder = PostProto.Source.newBuilder();

        if (dto.getTelegramSource() != null) {
            builder.setTelegramSource(
                    PostProto.TelegramSource.newBuilder()
                            .setChatId(dto.getTelegramSource().getChatId())
                            .build()
            );
        }

        return builder.build();
    }

    PostProto.Sink convertSinkToGrpc(Sink dto) {
        PostProto.Sink.Builder builder = PostProto.Sink.newBuilder();

        if (dto.getTelegramSink() != null) {
            builder.setTelegramSink(
                    PostProto.TelegramSink.newBuilder()
                            .setChatId(dto.getTelegramSink().getChatId())
                            .build()
            );
        }

        return builder.build();
    }

    public Flow toGrpc(FlowRequest dto) {
        Flow.Builder builder = Flow.newBuilder()
                .setTeamId(String.valueOf(dto.getTeamId()))
                .setSource(convertSourceToGrpc(dto.getSource()))
                .setRequiresModeration(dto.isRequiresModeration());

        dto.getSinks().forEach(s -> builder.addSinks(convertSinkToGrpc(s)));
        dto.getFilters().forEach(f -> builder.addFilters(filterConverter.toGrpc(f)));
        return builder.build();
    }

    private Instant toInstant(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}
