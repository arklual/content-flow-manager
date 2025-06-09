package ru.arklual.telegramparser.converters;

import org.springframework.stereotype.Component;
import ru.arklual.telegramparser.dto.protobuf.PostProto;
import ru.arklual.telegramparser.entities.AddPhotoFilterEntity;
import ru.arklual.telegramparser.entities.FilterEntity;

@Component
public class AddPhotoFilterConverter
        implements Converter<PostProto.Filter, FilterEntity> {

    @Override
    public String getKey() {
        return "add_photo";
    }

    @Override
    public FilterEntity toEntity(PostProto.Filter proto) {
        AddPhotoFilterEntity e = new AddPhotoFilterEntity();
        e.setTrigger(proto.getAddPhoto().getTrigger());
        e.setPhotoUrl(proto.getAddPhoto().getPhotoUrl());
        return e;
    }

    @Override
    public PostProto.Filter toProto(FilterEntity entity) {
        AddPhotoFilterEntity e = (AddPhotoFilterEntity) entity;
        return PostProto.Filter.newBuilder()
                .setAddPhoto(
                        PostProto.AddPhotoFilter.newBuilder()
                        .setTrigger(e.getTrigger())
                        .setPhotoUrl(e.getPhotoUrl())
                        .build()
                ).build();
    }
}
