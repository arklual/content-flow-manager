package ru.arklual.telegramparser.converters;

import org.springframework.stereotype.Component;
import ru.arklual.telegramparser.entities.TextReplaceFilterEntity;
import ru.arklual.telegramparser.entities.FilterEntity;
import static ru.arklual.telegramparser.dto.protobuf.PostProto.Filter;
import static ru.arklual.telegramparser.dto.protobuf.PostProto.TextReplaceFilter;


@Component
public class TextReplaceFilterConverter
        implements Converter<Filter, FilterEntity> {

    @Override
    public String getKey() {
        return "text_replace";
    }

    @Override
    public FilterEntity toEntity(Filter proto) {
        TextReplaceFilterEntity e = new TextReplaceFilterEntity();
        TextReplaceFilter f = proto.getTextReplace();
        e.setTrigger(f.getTrigger());
        e.setPattern(f.getPattern());
        e.setReplacement(f.getReplacement());
        return e;
    }

    @Override
    public Filter toProto(FilterEntity entity) {
        TextReplaceFilterEntity e = (TextReplaceFilterEntity) entity;
        return Filter.newBuilder()
                .setTextReplace(
                    TextReplaceFilter.newBuilder()
                        .setTrigger(e.getTrigger())
                        .setPattern(e.getPattern())
                        .setReplacement(e.getReplacement())
                        .build()
                )
                .build();
    }
}
