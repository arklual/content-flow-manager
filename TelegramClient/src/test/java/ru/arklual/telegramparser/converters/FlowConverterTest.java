package ru.arklual.telegramparser.converters;

import com.google.protobuf.Timestamp;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.arklual.telegramparser.dto.protobuf.Flow;
import ru.arklual.telegramparser.dto.protobuf.PostProto;
import ru.arklual.telegramparser.entities.FlowEntity;
import ru.arklual.telegramparser.entities.TelegramSinkEntity;
import ru.arklual.telegramparser.entities.TelegramSourceEntity;
import ru.arklual.telegramparser.entities.TextReplaceFilterEntity;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlowConverterTest {

    private SourceConverterProvider srcProv;
    private SinkConverterProvider sinkProv;
    private FilterConverterProvider filterProv;
    private FlowConverter converter;

    @BeforeEach
    void setUp() {
        srcProv = mock(SourceConverterProvider.class);
        sinkProv = mock(SinkConverterProvider.class);
        filterProv = mock(FilterConverterProvider.class);
        converter = new FlowConverter(srcProv, sinkProv, filterProv);
    }

    @Test
    void testToEntity_minimal_shouldPopulateDefaults() {
        Flow proto = Flow.newBuilder()
                .setTeamId("teamA")
                .setRequiresModeration(true)
                .build();

        FlowEntity entity = converter.toEntity(proto);

        assertNotNull(entity.getId());
        assertEquals("teamA", entity.getTeamId());
        assertTrue(entity.isRequiresModeration());
        assertNull(entity.getSource());
        assertTrue(entity.getSinks().isEmpty());
        assertTrue(entity.getFilters().isEmpty());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    void testToEntity_full_shouldMapAllFields() {
        ObjectId oid = new ObjectId();
        PostProto.Source protoSource = PostProto.Source.newBuilder().build();
        PostProto.Sink protoSink = PostProto.Sink.newBuilder().build();
        PostProto.Filter protoFilter = PostProto.Filter.newBuilder().build();
        Timestamp ts = Timestamp.newBuilder()
                .setSeconds(1234)
                .setNanos(567)
                .build();

        Flow proto = Flow.newBuilder()
                .setId(oid.toHexString())
                .setTeamId("T1")
                .setSource(protoSource)
                .addSinks(protoSink)
                .addFilters(protoFilter)
                .setRequiresModeration(false)
                .setUpdatedAt(ts)
                .build();

        when(srcProv.toEntity(protoSource)).thenReturn(new TelegramSourceEntity());
        when(sinkProv.toEntity(protoSink)).thenReturn(new TelegramSinkEntity());
        when(filterProv.toEntity(protoFilter)).thenReturn(new TextReplaceFilterEntity());

        FlowEntity entity = converter.toEntity(proto);

        assertEquals(oid, entity.getId());
        assertEquals("T1", entity.getTeamId());
        assertFalse(entity.isRequiresModeration());
        assertInstanceOf(TelegramSourceEntity.class, entity.getSource());
        assertEquals(1, entity.getSinks().size());
        assertInstanceOf(TelegramSinkEntity.class, entity.getSinks().getFirst());
        assertEquals(1, entity.getFilters().size());
        assertInstanceOf(TextReplaceFilterEntity.class, entity.getFilters().getFirst());
        assertEquals(Instant.ofEpochSecond(1234, 567), entity.getUpdatedAt());
    }

    @Test
    void testToProto_minimal_shouldPopulateDefaults() {
        FlowEntity entity = new FlowEntity();
        entity.setId(new ObjectId());
        entity.setTeamId("teamB");
        entity.setRequiresModeration(false);
        entity.setSinks(List.of());
        entity.setFilters(List.of());

        Flow proto = converter.toProto(entity);

        assertEquals(entity.getId().toHexString(), proto.getId());
        assertEquals("teamB", proto.getTeamId());
        assertFalse(proto.getRequiresModeration());
        assertFalse(proto.hasSource());
        assertTrue(proto.getSinksList().isEmpty());
        assertTrue(proto.getFiltersList().isEmpty());
        assertFalse(proto.hasUpdatedAt());
    }


    @Test
    void testToProto_full_shouldMapAllFields() {
        ObjectId oid = new ObjectId();
        Instant instant = Instant.ofEpochSecond(2222, 333);
        PostProto.Source protoSource = PostProto.Source.newBuilder().build();
        PostProto.Sink protoSink = PostProto.Sink.newBuilder().build();
        PostProto.Filter protoFilter = PostProto.Filter.newBuilder().build();

        FlowEntity entity = new FlowEntity();
        entity.setId(oid);
        entity.setTeamId("T2");
        entity.setRequiresModeration(true);
        entity.setSource(new TelegramSourceEntity());
        entity.setSinks(List.of(new TelegramSinkEntity()));
        entity.setFilters(List.of(new TextReplaceFilterEntity()));
        entity.setUpdatedAt(instant);

        when(srcProv.toProto(entity.getSource())).thenReturn(protoSource);
        when(sinkProv.toProto(entity.getSinks().getFirst())).thenReturn(protoSink);
        when(filterProv.toProto(entity.getFilters().getFirst())).thenReturn(protoFilter);

        Flow proto = converter.toProto(entity);

        assertEquals(oid.toHexString(), proto.getId());
        assertEquals("T2", proto.getTeamId());
        assertTrue(proto.getRequiresModeration());
        assertTrue(proto.hasSource());
        assertEquals(1, proto.getSinksCount());
        assertEquals(1, proto.getFiltersCount());
        assertTrue(proto.hasUpdatedAt());
        Timestamp outTs = proto.getUpdatedAt();
        assertEquals(2222, outTs.getSeconds());
        assertEquals(333, outTs.getNanos());
    }
}
