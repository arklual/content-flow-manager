package ru.arklual.telegramparser.converters;

import org.junit.jupiter.api.Test;
import ru.arklual.telegramparser.dto.protobuf.PostProto;
import ru.arklual.telegramparser.entities.AIFilterEntity;

import static org.junit.jupiter.api.Assertions.*;

class AIFilterConverterTest {

    private final AIFilterConverter conv = new AIFilterConverter();

    @Test
    void testGetKey() {
        assertEquals("ai_filter", conv.getKey());
    }

    @Test
    void testToEntity_andBack() {
        PostProto.AIFilter ai = PostProto.AIFilter.newBuilder()
            .setTrigger("trig")
            .setPrompt("pr")
            .build();
        PostProto.Filter proto = PostProto.Filter.newBuilder().setAiFilter(ai).build();

        var entity = conv.toEntity(proto);
        assertInstanceOf(AIFilterEntity.class, entity);
        AIFilterEntity e = (AIFilterEntity) entity;
        assertEquals("trig", e.getTrigger());
        assertEquals("pr", e.getPrompt());

        PostProto.Filter round = conv.toProto(e);
        assertTrue(round.hasAiFilter());
        assertEquals("trig", round.getAiFilter().getTrigger());
        assertEquals("pr", round.getAiFilter().getPrompt());
    }
}
