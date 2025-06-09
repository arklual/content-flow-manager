package ru.arklual.telegramparser.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.arklual.telegramparser.converters.FlowConverter;
import ru.arklual.telegramparser.dto.protobuf.*;
import ru.arklual.telegramparser.entities.FlowEntity;
import ru.arklual.telegramparser.repositories.FlowRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlowGrpcServiceTest {

    @Mock
    private FlowRepository repository;
    @Mock
    private FlowConverter converter;
    @Mock
    private TelegramClientManager manager;

    @InjectMocks
    private FlowGrpcService service;

    @Captor
    private ArgumentCaptor<CreateFlowResponse> createCaptor;
    @Captor
    private ArgumentCaptor<GetFlowResponse> getCaptor;
    @Captor
    private ArgumentCaptor<UpdateFlowResponse> updateCaptor;
    @Captor
    private ArgumentCaptor<DeleteFlowResponse> deleteCaptor;
    @Captor
    private ArgumentCaptor<ListFlowsResponse> listCaptor;
    @Mock
    private StreamObserver<CreateFlowResponse> createObs;
    @Mock
    private StreamObserver<GetFlowResponse> getObs;
    @Mock
    private StreamObserver<UpdateFlowResponse> updateObs;
    @Mock
    private StreamObserver<DeleteFlowResponse> deleteObs;
    @Mock
    private StreamObserver<ListFlowsResponse> listObs;

    private FlowEntity entity;
    private Flow proto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        entity = new FlowEntity();
        entity.setId(new ObjectId());
        entity.setTeamId("team");
        proto = Flow.newBuilder().setId(entity.getId().toHexString()).setTeamId("team").build();
    }

    @Test
    void createFlow_shouldSaveAndReturnProto_andAddListener() {
        CreateFlowRequest req = CreateFlowRequest.newBuilder().setFlow(proto).build();
        when(converter.toEntity(proto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(converter.toProto(entity)).thenReturn(proto);

        service.createFlow(req, createObs);

        verify(repository).save(entity);
        verify(converter).toProto(entity);
        verify(manager).addListener(anyString(), eq("team"));
        verify(createObs).onNext(createCaptor.capture());
        assertEquals(proto, createCaptor.getValue().getFlow());
        verify(createObs).onCompleted();
    }

    @Test
    void getFlow_shouldReturnProto_whenFound() {
        GetFlowRequest req = GetFlowRequest.newBuilder().setId(entity.getId().toHexString()).build();
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(converter.toProto(entity)).thenReturn(proto);

        service.getFlow(req, getObs);

        verify(getObs).onNext(getCaptor.capture());
        assertEquals(proto, getCaptor.getValue().getFlow());
        verify(getObs).onCompleted();
    }

    @Test
    void getFlow_shouldError_whenNotFound() {
        String id = entity.getId().toHexString();
        GetFlowRequest req = GetFlowRequest.newBuilder().setId(id).build();
        when(repository.findById(entity.getId())).thenReturn(Optional.empty());

        service.getFlow(req, getObs);

        verify(getObs).onError(argThat(err ->
                err instanceof StatusRuntimeException &&
                        ((StatusRuntimeException) err).getStatus().getCode() == Status.NOT_FOUND.getCode()
        ));
        verify(getObs, never()).onNext(any());
    }

    @Test
    void updateFlow_shouldError_whenInvalidId() {
        Flow bad = Flow.newBuilder().setId("bad-id").build();
        UpdateFlowRequest req = UpdateFlowRequest.newBuilder().setFlow(bad).build();

        service.updateFlow(req, updateObs);

        verify(updateObs).onError(argThat(err ->
                err instanceof StatusRuntimeException &&
                        ((StatusRuntimeException) err).getStatus().getCode() == Status.INVALID_ARGUMENT.getCode()
        ));
        verify(updateObs, never()).onNext(any());
    }

    @Test
    void updateFlow_shouldError_whenNotFound() {
        Flow f = Flow.newBuilder().setId(entity.getId().toHexString()).build();
        UpdateFlowRequest req = UpdateFlowRequest.newBuilder().setFlow(f).build();
        when(repository.findById(entity.getId())).thenReturn(Optional.empty());

        service.updateFlow(req, updateObs);

        verify(updateObs).onError(argThat(err ->
                err instanceof StatusRuntimeException &&
                        ((StatusRuntimeException) err).getStatus().getCode() == Status.NOT_FOUND.getCode()
        ));
    }

    @Test
    void updateFlow_shouldSaveAndReturnProto_whenFound() {
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(converter.toEntity(proto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(converter.toProto(entity)).thenReturn(proto);
        UpdateFlowRequest req = UpdateFlowRequest.newBuilder().setFlow(proto).build();

        service.updateFlow(req, updateObs);

        verify(repository).save(entity);
        verify(updateObs).onNext(updateCaptor.capture());
        assertEquals(proto, updateCaptor.getValue().getFlow());
        verify(updateObs).onCompleted();
    }

    @Test
    void deleteFlow_shouldError_whenInvalidId() {
        DeleteFlowRequest req = DeleteFlowRequest.newBuilder().setId("bad").build();

        service.deleteFlow(req, deleteObs);

        verify(deleteObs).onError(argThat(err ->
                err instanceof StatusRuntimeException &&
                        ((StatusRuntimeException) err).getStatus().getCode() == Status.INVALID_ARGUMENT.getCode()
        ));
        verify(deleteObs, never()).onNext(any());
    }

    @Test
    void deleteFlow_shouldReturnTrue_whenExisted() {
        when(repository.existsById(entity.getId())).thenReturn(true);
        DeleteFlowRequest req = DeleteFlowRequest.newBuilder().setId(entity.getId().toHexString()).build();

        service.deleteFlow(req, deleteObs);

        verify(repository).deleteById(entity.getId());
        verify(deleteObs).onNext(deleteCaptor.capture());
        assertTrue(deleteCaptor.getValue().getSuccess());
        verify(deleteObs).onCompleted();
    }

    @Test
    void deleteFlow_shouldReturnFalse_whenNotExisted() {
        when(repository.existsById(entity.getId())).thenReturn(false);
        DeleteFlowRequest req = DeleteFlowRequest.newBuilder().setId(entity.getId().toHexString()).build();

        service.deleteFlow(req, deleteObs);

        verify(deleteObs).onNext(deleteCaptor.capture());
        assertFalse(deleteCaptor.getValue().getSuccess());
        verify(deleteObs).onCompleted();
    }

    @Test
    void listFlows_shouldReturnAll() {
        when(repository.findAllByTeamId("team")).thenReturn(List.of(entity));
        when(converter.toProto(entity)).thenReturn(proto);
        ListFlowsRequest req = ListFlowsRequest.newBuilder().setTeamId("team").build();

        service.listFlows(req, listObs);

        verify(listObs).onNext(listCaptor.capture());
        assertEquals(List.of(proto), listCaptor.getValue().getFlowsList());
        verify(listObs).onCompleted();
    }
}
