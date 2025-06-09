package ru.arklual.telegramparser.converters;

import org.springframework.stereotype.Component;
import ru.arklual.telegramparser.entities.AIFilterEntity;
import ru.arklual.telegramparser.entities.FilterEntity;

import static ru.arklual.telegramparser.dto.protobuf.PostProto.Filter;
import static ru.arklual.telegramparser.dto.protobuf.PostProto.AIFilter;

@Component
public class AIFilterConverter
        implements Converter<Filter, FilterEntity> {

    @Override
    public String getKey() {
        return "ai_filter";
    }

    @Override
    public FilterEntity toEntity(Filter proto) {
        AIFilterEntity e = new AIFilterEntity();
        AIFilter f = proto.getAiFilter();
        e.setTrigger(f.getTrigger());
        e.setPrompt(f.getPrompt());
        return e;
    }

    @Override
    public Filter toProto(FilterEntity entity) {
        AIFilterEntity e = (AIFilterEntity) entity;
        return Filter.newBuilder()
                .setAiFilter(
                        AIFilter.newBuilder()
                                .setTrigger(e.getTrigger())
                                .setPrompt(e.getPrompt())
                                .build()
                )
                .build();
    }
}
