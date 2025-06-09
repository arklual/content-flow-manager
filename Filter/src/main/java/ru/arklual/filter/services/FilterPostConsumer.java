package ru.arklual.filter.services;


import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.arklual.filter.dto.protobuf.PostProto;

@Slf4j
@Service
public class FilterPostConsumer {

    private final KafkaProducer kafkaProducer;
    private final FilterApplier filterApplier;

    public FilterPostConsumer(KafkaProducer kafkaProducer, FilterApplier filterApplier) {
        this.kafkaProducer = kafkaProducer;
        this.filterApplier = filterApplier;
    }

    @KafkaListener(topics = "row.content", groupId = "data-cleaner-group")
    public void listen(ConsumerRecord<String, byte[]> record) {
        byte[] value = record.value();
        try {
            PostProto.Post raw = PostProto.Post.parseFrom(value);
            PostProto.Post result = raw;
            for (PostProto.Filter f : raw.getFilterList()) {
                result = filterApplier.applyFilter(f, result);
            }
            PostProto.Post.Builder builder = result.toBuilder();
            builder.setIsCleaned(true);
            builder.setStatus(PostProto.PostStatus.PENDING_REVIEW);
            kafkaProducer.sendMessage("cleaned.content", builder.build());
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

}
