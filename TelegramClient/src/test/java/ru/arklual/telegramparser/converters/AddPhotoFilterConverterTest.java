package ru.arklual.telegramparser.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.arklual.telegramparser.dto.protobuf.PostProto;
import ru.arklual.telegramparser.entities.AddPhotoFilterEntity;
import ru.arklual.telegramparser.entities.FilterEntity;

import static org.junit.jupiter.api.Assertions.*;

class AddPhotoFilterConverterTest {

    private AddPhotoFilterConverter converter;

    @BeforeEach
    void setUp() {
        converter = new AddPhotoFilterConverter();
    }

    @Test
    void getKey_shouldReturnAddPhoto() {
        assertEquals("add_photo", converter.getKey());
    }

    @Test
    void toEntity_shouldExtractTriggerAndUrl() {
        String trigger = "foo";
        String url = "http://example.com/img.jpg";
        PostProto.AddPhotoFilter af = PostProto.AddPhotoFilter.newBuilder()
                .setTrigger(trigger)
                .setPhotoUrl(url)
                .build();
        PostProto.Filter proto = PostProto.Filter.newBuilder()
                .setAddPhoto(af)
                .build();

        FilterEntity entity = converter.toEntity(proto);

        assertInstanceOf(AddPhotoFilterEntity.class, entity);
        AddPhotoFilterEntity e = (AddPhotoFilterEntity) entity;
        assertEquals(trigger, e.getTrigger());
        assertEquals(url, e.getPhotoUrl());
    }

    @Test
    void toProto_shouldBuildFilterWithTriggerAndUrl() {
        String trigger = "bar";
        String url = "https://img.test/pic.png";
        AddPhotoFilterEntity entity = new AddPhotoFilterEntity();
        entity.setTrigger(trigger);
        entity.setPhotoUrl(url);

        PostProto.Filter proto = converter.toProto(entity);

        assertTrue(proto.hasAddPhoto());
        PostProto.AddPhotoFilter af = proto.getAddPhoto();
        assertEquals(trigger, af.getTrigger());
        assertEquals(url, af.getPhotoUrl());
    }
}
