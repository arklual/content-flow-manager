package ru.arklual.telegramparser.converters;

import org.springframework.stereotype.Component;
import ru.arklual.telegramparser.entities.SourceEntity;
import ru.arklual.telegramparser.entities.TelegramSourceEntity;

import static ru.arklual.telegramparser.dto.protobuf.PostProto.Source;
import static ru.arklual.telegramparser.dto.protobuf.PostProto.TelegramSource;


@Component
public class TelegramSourceConverter
        implements Converter<Source, SourceEntity> {

    @Override
    public String getKey() {
        return "telegram_source";
    }

    @Override
    public SourceEntity toEntity(Source proto) {
        TelegramSourceEntity e = new TelegramSourceEntity();
        TelegramSource ts = proto.getTelegramSource();
        e.setChatId(ts.getChatId());
        return e;
    }

    @Override
    public Source toProto(SourceEntity entity) {
        TelegramSourceEntity e = (TelegramSourceEntity) entity;
        return Source.newBuilder()
                .setTelegramSource(
                        TelegramSource.newBuilder()
                                .setChatId(e.getChatId())
                                .build()
                )
                .build();
    }
}
