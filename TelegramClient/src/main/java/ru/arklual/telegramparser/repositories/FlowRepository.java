package ru.arklual.telegramparser.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import ru.arklual.telegramparser.entities.FlowEntity;

import java.util.List;

@Repository
public interface FlowRepository extends MongoRepository<FlowEntity, ObjectId> {
    List<FlowEntity> findAllByTeamId(String teamId);
    @Query("{ 'teamId': ?0, 'source.type': 'telegram_source', 'source.chatId': ?1 }")
    List<FlowEntity> findAllByTeamIdAndSourceChatId(String teamId, String chatId);

}
