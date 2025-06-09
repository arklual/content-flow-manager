package ru.arklual.filter.services;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.arklual.filter.dto.protobuf.PostProto;

@Service
public class KafkaProducer {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, PostProto.Post message) {
        try {
            byte[] data = message.toByteArray();
            kafkaTemplate.send(topic, data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize protobuf message", e);
        }
    }
}
