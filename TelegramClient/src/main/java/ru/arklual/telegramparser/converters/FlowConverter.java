package ru.arklual.telegramparser.converters;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.arklual.telegramparser.dto.protobuf.Flow;
import ru.arklual.telegramparser.entities.FlowEntity;

import java.time.Instant;
import java.util.stream.Collectors;

@Component
public class FlowConverter {

    private final SourceConverterProvider sourceProvider;
    private final SinkConverterProvider sinkProvider;
    private final FilterConverterProvider filterProvider;

    @Autowired
    public FlowConverter(
            SourceConverterProvider sourceProvider,
            SinkConverterProvider sinkProvider,
            FilterConverterProvider filterProvider
    ) {
        this.sourceProvider = sourceProvider;
        this.sinkProvider = sinkProvider;
        this.filterProvider = filterProvider;
    }

    public FlowEntity toEntity(Flow proto) {
        FlowEntity e = new FlowEntity();

        if (!proto.getId().isEmpty()) {
            e.setId(new ObjectId(proto.getId()));
        } else {
            e.setId(new ObjectId());
        }

        e.setTeamId(proto.getTeamId());

        if (proto.hasSource()) {
            e.setSource(sourceProvider.toEntity(proto.getSource()));
        }

        e.setSinks(
                proto.getSinksList().stream()
                        .map(sinkProvider::toEntity)
                        .collect(Collectors.toList())
        );

        e.setFilters(
                proto.getFiltersList().stream()
                        .map(filterProvider::toEntity)
                        .collect(Collectors.toList())
        );

        e.setRequiresModeration(proto.getRequiresModeration());

        if (proto.hasUpdatedAt())
            e.setUpdatedAt(Instant.ofEpochSecond(proto.getUpdatedAt().getSeconds(), proto.getUpdatedAt().getNanos()));

        return e;
    }

    public Flow toProto(FlowEntity e) {
        Flow.Builder b = Flow.newBuilder()
                .setId(e.getId().toHexString())
                .setTeamId(e.getTeamId())
                .setRequiresModeration(e.isRequiresModeration());

        if (e.getSource() != null) {
            b.setSource(sourceProvider.toProto(e.getSource()));
        }

        b.addAllSinks(
                e.getSinks().stream()
                        .map(sinkProvider::toProto)
                        .collect(Collectors.toList())
        );

        b.addAllFilters(
                e.getFilters().stream()
                        .map(filterProvider::toProto)
                        .collect(Collectors.toList())
        );

        if (e.getUpdatedAt() != null) {
            b.setUpdatedAt(com.google.protobuf.Timestamp.newBuilder()
                    .setSeconds(e.getUpdatedAt().getEpochSecond())
                    .setNanos(e.getUpdatedAt().getNano())
                    .build());
        }

        return b.build();
    }
}
