package ru.arklual.telegramparser.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.arklual.telegramparser.entities.SinkEntity;
import ru.arklual.telegramparser.dto.protobuf.PostProto.Sink;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SinkConverterProvider {
    private final Map<String, Converter<Sink, SinkEntity>> converters;

    @Autowired
    public SinkConverterProvider(List<Converter<Sink, SinkEntity>> list) {
        this.converters = list.stream()
                .collect(Collectors.toMap(Converter::getKey, Function.identity()));
    }

    public SinkEntity toEntity(Sink proto) {
        String key = proto.getSinkTypeCase().name().toLowerCase();
        Converter<Sink, SinkEntity> conv = converters.get(key);
        if (conv == null) {
            throw new IllegalArgumentException("No converter for sink type: " + key);
        }
        return conv.toEntity(proto);
    }

    public Sink toProto(SinkEntity entity) {
        Converter<Sink, SinkEntity> conv = converters.get(entity.getType());
        if (conv == null) {
            throw new IllegalArgumentException("No converter for sink entity: " + entity.getType());
        }
        return conv.toProto(entity);
    }
}
