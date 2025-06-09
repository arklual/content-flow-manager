package ru.arklual.approvalservice.services;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.arklual.approvalservice.dto.protobuf.PostProto;

@Service
public class ApprovalPostConsumer {

    private final KafkaProducer kafkaProducer;
    private final WaitingPostsRegistry waitingPostsRegistry;

    public ApprovalPostConsumer(KafkaProducer kafkaProducer, WaitingPostsRegistry waitingPostsRegistry) {
        this.kafkaProducer = kafkaProducer;
        this.waitingPostsRegistry = waitingPostsRegistry;
    }

    @KafkaListener(topics = "cleaned.content", groupId = "approval-group")
    public void listen(ConsumerRecord<String, byte[]> record) {
        byte[] value = record.value();
        try {
            PostProto.Post cleaned = PostProto.Post.parseFrom(value);
            if (!cleaned.getRequiresModeration()) {
                PostProto.Post.Builder builder = cleaned.toBuilder();
                builder.setStatus(PostProto.PostStatus.APPROVED);
                for (PostProto.Sink s: cleaned.getSinkList()) {
                    builder.clearSink();
                    builder.addSink(s);
                    kafkaProducer.sendMessage(String.format("content.%s.approved", s.getSinkTypeCase()), builder.build());
                }
                return;
            }
            waitingPostsRegistry.getPosts(cleaned.getTeamId()).add(cleaned);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

}