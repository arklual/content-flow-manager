package ru.arklual.telegramparser.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.arklual.telegramparser.dto.protobuf.PostProto;
import ru.arklual.telegramparser.entities.RemovePhotoFilterEntity;
import ru.arklual.telegramparser.entities.FilterEntity;

import static org.junit.jupiter.api.Assertions.*;

class RemovePhotoFilterConverterTest {

    private RemovePhotoFilterConverter converter;

    @BeforeEach
    void setUp() {
        converter = new RemovePhotoFilterConverter();
    }

    @Test
    void getKey_shouldReturnRemovePhoto() {
        assertEquals("remove_photo", converter.getKey());
    }

    @Test
    void toEntity_shouldExtractTrigger() {
        String trigger = "del-me";
        PostProto.RemovePhotoFilter rpf = PostProto.RemovePhotoFilter.newBuilder()
                .setTrigger(trigger)
                .build();
        PostProto.Filter proto = PostProto.Filter.newBuilder()
                .setRemovePhoto(rpf)
                .build();

        FilterEntity entity = converter.toEntity(proto);

        assertInstanceOf(RemovePhotoFilterEntity.class, entity);
        RemovePhotoFilterEntity e = (RemovePhotoFilterEntity) entity;
        assertEquals(trigger, e.getTrigger());
    }

    @Test
    void toProto_shouldBuildFilterWithTrigger() {
        String trigger = "rm";
        RemovePhotoFilterEntity entity = new RemovePhotoFilterEntity();
        entity.setTrigger(trigger);

        PostProto.Filter proto = converter.toProto(entity);

        assertTrue(proto.hasRemovePhoto());
        PostProto.RemovePhotoFilter rpf = proto.getRemovePhoto();
        assertEquals(trigger, rpf.getTrigger());
    }
}
