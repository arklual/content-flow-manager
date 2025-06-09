package ru.arklual.telegramparser.converters;

import org.springframework.stereotype.Component;
import ru.arklual.telegramparser.entities.SinkEntity;
import ru.arklual.telegramparser.entities.TelegramSinkEntity;

import static ru.arklual.telegramparser.dto.protobuf.PostProto.Sink;
import static ru.arklual.telegramparser.dto.protobuf.PostProto.TelegramSink;

@Component
public class TelegramSinkConverter
        implements Converter<Sink, SinkEntity> {

    @Override
    public String getKey() {
        return "telegram_sink";
    }

    @Override
    public SinkEntity toEntity(Sink proto) {
        TelegramSinkEntity e = new TelegramSinkEntity();
        TelegramSink ts = proto.getTelegramSink();
        e.setChatId(ts.getChatId());
        return e;
    }

    @Override
    public Sink toProto(SinkEntity entity) {
        TelegramSinkEntity e = (TelegramSinkEntity) entity;
        return Sink.newBuilder()
                .setTelegramSink(
                        TelegramSink.newBuilder()
                                .setChatId(e.getChatId())
                                .build()
                )
                .build();
    }
}
