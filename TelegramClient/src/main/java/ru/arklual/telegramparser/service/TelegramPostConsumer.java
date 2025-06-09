package ru.arklual.telegramparser.service;

import com.google.protobuf.InvalidProtocolBufferException;
import it.tdlight.client.SimpleTelegramClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.arklual.telegramparser.utils.TelegramPoster;
import ru.arklual.telegramparser.dto.protobuf.PostProto;

@Service
public class TelegramPostConsumer {

    private final TelegramClientManager telegramClientManager;

    public TelegramPostConsumer(TelegramClientManager telegramClientManager) {
        this.telegramClientManager = telegramClientManager;
    }

    @KafkaListener(topics = "content.TELEGRAM_SINK.approved", groupId = "poster-group")
    public void listen(ConsumerRecord<String, byte[]> record) {
        byte[] value = record.value();
        try {
            PostProto.Post approvedPost = PostProto.Post.parseFrom(value);
            SimpleTelegramClient client = telegramClientManager.getClient(approvedPost.getTeamId());
            new TelegramPoster(client).sendPost(approvedPost);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}
