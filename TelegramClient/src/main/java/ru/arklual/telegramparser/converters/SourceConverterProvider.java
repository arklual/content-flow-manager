package ru.arklual.telegramparser.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.arklual.telegramparser.entities.SourceEntity;
import ru.arklual.telegramparser.dto.protobuf.PostProto.Source;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SourceConverterProvider {
    private final Map<String, Converter<Source, SourceEntity>> converters;

    @Autowired
    public SourceConverterProvider(List<Converter<Source, SourceEntity>> list) {
        this.converters = list.stream()
                .collect(Collectors.toMap(Converter::getKey, Function.identity()));
    }

    public SourceEntity toEntity(Source proto) {
        String key = proto.getSourceTypeCase().name().toLowerCase();
        Converter<Source, SourceEntity> conv = converters.get(key);
        if (conv == null) {
            throw new IllegalArgumentException("No converter for source type: " + key);
        }
        return conv.toEntity(proto);
    }

    public Source toProto(SourceEntity entity) {
        Converter<Source, SourceEntity> conv = converters.get(entity.getType());
        if (conv == null) {
            throw new IllegalArgumentException("No converter for source entity: " + entity.getType());
        }
        return conv.toProto(entity);
    }
}
