package ru.arklual.telegramparser.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.arklual.telegramparser.dto.protobuf.PostProto;
import ru.arklual.telegramparser.entities.FilterEntity;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class FilterConverterProvider {

    private final Map<String, Converter<PostProto.Filter, FilterEntity>> converters;

    @Autowired
    public FilterConverterProvider(List<Converter<PostProto.Filter, FilterEntity>> list) {
        this.converters = list.stream()
            .collect(Collectors.toMap(Converter::getKey, Function.identity()));
    }

    public FilterEntity toEntity(PostProto.Filter proto) {
        String key = proto.getFilterTypeCase().name().toLowerCase(); 
        Converter<PostProto.Filter, FilterEntity> conv = converters.get(key);
        if (conv == null) {
            throw new IllegalArgumentException("No converter for filter type: " + key);
        }
        return conv.toEntity(proto);
    }

    public PostProto.Filter toProto(FilterEntity entity) {
        Converter<PostProto.Filter, FilterEntity> conv = converters.get(entity.getType());
        if (conv == null) {
            throw new IllegalArgumentException("No converter for filter entity: " + entity.getType());
        }
        return conv.toProto(entity);
    }
}
