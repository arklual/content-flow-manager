package ru.arklual.crm.converters;

import org.springframework.stereotype.Component;
import ru.arklual.crm.dto.*;
import ru.arklual.crm.dto.protobuf.PostProto;
import ru.arklual.crm.dto.responses.PostResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PostConverter {

    private final FilterConverter filterConverter;

    public PostConverter(FilterConverter filterConverter) {
        this.filterConverter = filterConverter;
    }

    public PostResponse fromGrpc(PostProto.Post grpc) {
        return PostResponse.builder()
                .id(grpc.getId())
                .teamId(UUID.fromString(grpc.getTeamId()))
                .source(convertSource(grpc.getSource()))
                .content(grpc.getContent())
                .media(convertMedia(grpc.getMediaList()))
                .createdAt(toInstant(grpc.getCreatedAt()))
                .updatedAt(toInstant(grpc.getUpdatedAt()))
                .status(grpc.getStatus())
                .requiresModeration(grpc.getRequiresModeration())
                .isCleaned(grpc.getIsCleaned())
                .sinks(grpc.getSinkList().stream()
                        .map(this::convertSink)
                        .collect(Collectors.toList()))
                .filters(grpc.getFilterList().stream()
                        .map(filterConverter::fromGrpc)
                        .collect(Collectors.toList()))
                .build();
    }

    private Source convertSource(PostProto.Source grpc) {
        return Source.builder()
                .telegramSource(grpc.hasTelegramSource() ? TelegramSource.builder()
                        .chatId(grpc.getTelegramSource().getChatId())
                        .build() : null)
                .build();
    }

    private Media convertMedia(List<PostProto.Media> grpcMediaList) {
        if (grpcMediaList.isEmpty()) return null;
        PostProto.Media grpcMedia = grpcMediaList.getFirst();
        return Media.builder()
                .url(grpcMedia.getUrl())
                .mediaType(grpcMedia.getType())
                .build();
    }

    Sink convertSink(PostProto.Sink grpc) {
        return Sink.builder()
                .telegramSink(grpc.hasTelegramSink() ? TelegramSink.builder()
                        .chatId(grpc.getTelegramSink().getChatId())
                        .build() : null)
                .build();
    }

    private Instant toInstant(long millis) {
        return Instant.ofEpochMilli(millis);
    }
}
