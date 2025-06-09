package ru.arklual.telegramparser.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.arklual.telegramparser.dto.protobuf.PostProto.Filter;
import ru.arklual.telegramparser.dto.protobuf.PostProto.TextReplaceFilter;
import ru.arklual.telegramparser.entities.FilterEntity;
import ru.arklual.telegramparser.entities.TextReplaceFilterEntity;

import static org.junit.jupiter.api.Assertions.*;

class TextReplaceFilterConverterTest {

    private TextReplaceFilterConverter converter;

    @BeforeEach
    void setUp() {
        converter = new TextReplaceFilterConverter();
    }

    @Test
    void getKey_shouldReturnTextReplace() {
        assertEquals("text_replace", converter.getKey());
    }

    @Test
    void toEntity_shouldExtractTriggerPatternAndReplacement() {
        String trigger = "foo";
        String pattern = "old";
        String replacement = "new";
        TextReplaceFilter tr = TextReplaceFilter.newBuilder()
                .setTrigger(trigger)
                .setPattern(pattern)
                .setReplacement(replacement)
                .build();
        Filter proto = Filter.newBuilder().setTextReplace(tr).build();

        FilterEntity entity = converter.toEntity(proto);

        assertInstanceOf(TextReplaceFilterEntity.class, entity);
        TextReplaceFilterEntity e = (TextReplaceFilterEntity) entity;
        assertEquals(trigger, e.getTrigger());
        assertEquals(pattern, e.getPattern());
        assertEquals(replacement, e.getReplacement());
    }

    @Test
    void toProto_shouldBuildFilterWithTriggerPatternAndReplacement() {
        String trigger = "bar";
        String pattern = "x";
        String replacement = "y";
        TextReplaceFilterEntity entity = new TextReplaceFilterEntity();
        entity.setTrigger(trigger);
        entity.setPattern(pattern);
        entity.setReplacement(replacement);

        Filter proto = converter.toProto(entity);

        assertTrue(proto.hasTextReplace());
        TextReplaceFilter tr = proto.getTextReplace();
        assertEquals(trigger, tr.getTrigger());
        assertEquals(pattern, tr.getPattern());
        assertEquals(replacement, tr.getReplacement());
    }
}
