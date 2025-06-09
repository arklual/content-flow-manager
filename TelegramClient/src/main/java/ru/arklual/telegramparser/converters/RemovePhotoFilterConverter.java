package ru.arklual.telegramparser.converters;

import org.springframework.stereotype.Component;
import ru.arklual.telegramparser.entities.RemovePhotoFilterEntity;
import ru.arklual.telegramparser.entities.FilterEntity;
import static ru.arklual.telegramparser.dto.protobuf.PostProto.Filter;
import static ru.arklual.telegramparser.dto.protobuf.PostProto.RemovePhotoFilter;

@Component
public class RemovePhotoFilterConverter
        implements Converter<Filter, FilterEntity> {

    @Override
    public String getKey() {
        return "remove_photo";
    }

    @Override
    public FilterEntity toEntity(Filter proto) {
        RemovePhotoFilterEntity e = new RemovePhotoFilterEntity();
        e.setTrigger(proto.getRemovePhoto().getTrigger());
        return e;
    }

    @Override
    public Filter toProto(FilterEntity entity) {
        RemovePhotoFilterEntity e = (RemovePhotoFilterEntity) entity;
        return Filter.newBuilder()
                .setRemovePhoto(
                    RemovePhotoFilter.newBuilder()
                        .setTrigger(e.getTrigger())
                        .build()
                )
                .build();
    }
}
