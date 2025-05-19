package ru.arklual.telegramparser.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.arklual.telegramparser.dto.TelegramMessageDTO;

@Service
public class TelegramKafkaProducer {

***REMOVED******REMOVED***private final KafkaTemplate<String, String> kafkaTemplate;
***REMOVED******REMOVED***private final ObjectMapper objectMapper;

***REMOVED******REMOVED***public TelegramKafkaProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
***REMOVED******REMOVED******REMOVED******REMOVED***this.kafkaTemplate = kafkaTemplate;
***REMOVED******REMOVED******REMOVED******REMOVED***this.objectMapper = objectMapper;
***REMOVED******REMOVED***}

***REMOVED******REMOVED***public void sendMessage(String topic, TelegramMessageDTO dto) {
***REMOVED******REMOVED******REMOVED******REMOVED***try {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***String json = objectMapper.writeValueAsString(dto);
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***kafkaTemplate.send(topic, json);
***REMOVED******REMOVED******REMOVED******REMOVED***} catch (Exception e) {
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***throw new RuntimeException("Failed to serialize message", e);
***REMOVED******REMOVED******REMOVED******REMOVED***}
***REMOVED******REMOVED***}
}
