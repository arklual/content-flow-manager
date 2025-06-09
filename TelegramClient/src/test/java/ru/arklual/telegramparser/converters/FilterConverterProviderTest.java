package ru.arklual.telegramparser.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.arklual.telegramparser.dto.protobuf.PostProto;
import ru.arklual.telegramparser.entities.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilterConverterProviderTest {

    private FilterConverterProvider provider;

    @BeforeEach
    void init() {
        provider = new FilterConverterProvider(List.of(
            new AddPhotoFilterConverter(),
            new RemovePhotoFilterConverter(),
            new TextReplaceFilterConverter(),
            new AIFilterConverter()
        ));
    }

    @Test
    void testToEntity_addPhoto() {
        var proto = PostProto.Filter.newBuilder()
            .setAddPhoto(PostProto.AddPhotoFilter.newBuilder()
                .setTrigger("t").setPhotoUrl("u").build())
            .build();
        var ent = provider.toEntity(proto);
        assertInstanceOf(AddPhotoFilterEntity.class, ent);
        assertEquals("t", ((AddPhotoFilterEntity) ent).getTrigger());
        assertEquals("u", ((AddPhotoFilterEntity) ent).getPhotoUrl());
    }

    @Test
    void testToEntity_removePhoto() {
        var proto = PostProto.Filter.newBuilder()
            .setRemovePhoto(PostProto.RemovePhotoFilter.newBuilder().setTrigger("x").build())
            .build();
        var ent = provider.toEntity(proto);
        assertInstanceOf(RemovePhotoFilterEntity.class, ent);
        assertEquals("x", ((RemovePhotoFilterEntity) ent).getTrigger());
    }

    @Test
    void testToEntity_textReplace() {
        var tf = PostProto.TextReplaceFilter.newBuilder()
            .setTrigger("a").setPattern("p").setReplacement("r").build();
        var proto = PostProto.Filter.newBuilder().setTextReplace(tf).build();
        var ent = provider.toEntity(proto);
        assertInstanceOf(TextReplaceFilterEntity.class, ent);
        assertEquals("a", ((TextReplaceFilterEntity) ent).getTrigger());
        assertEquals("p", ((TextReplaceFilterEntity) ent).getPattern());
        assertEquals("r", ((TextReplaceFilterEntity) ent).getReplacement());
    }

    @Test
    void testToEntity_aiFilter() {
        var ai = PostProto.AIFilter.newBuilder().setTrigger("z").setPrompt("q").build();
        var proto = PostProto.Filter.newBuilder().setAiFilter(ai).build();
        var ent = provider.toEntity(proto);
        assertInstanceOf(AIFilterEntity.class, ent);
        assertEquals("z", ((AIFilterEntity) ent).getTrigger());
        assertEquals("q", ((AIFilterEntity) ent).getPrompt());
    }

    @Test
    void testToEntity_unknown_throws() {
        var proto = PostProto.Filter.newBuilder().build();
        var ex = assertThrows(IllegalArgumentException.class, () -> provider.toEntity(proto));
        assertTrue(ex.getMessage().contains("No converter"));
    }

    @Test
    void testToProto_roundtrip() {
        AIFilterEntity aiE = new AIFilterEntity();
        aiE.setTrigger("t"); aiE.setPrompt("p");
        var proto = provider.toProto(aiE);
        assertTrue(proto.hasAiFilter());
        assertEquals("t", proto.getAiFilter().getTrigger());
    }

    @Test
    void testToProto_unknownEntity_throws() {
        FilterEntity fake = new FilterEntity() {
            public String getType() { return "nope"; }

            @Override
            public String getTrigger() {
                return "";
            }
        };
        var ex = assertThrows(IllegalArgumentException.class, () -> provider.toProto(fake));
        assertTrue(ex.getMessage().contains("No converter"));
    }
}
