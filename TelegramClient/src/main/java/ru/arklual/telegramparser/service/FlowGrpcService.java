package ru.arklual.telegramparser.service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.types.ObjectId;
import org.springframework.grpc.server.service.GrpcService;
import ru.arklual.telegramparser.converters.FlowConverter;
import ru.arklual.telegramparser.dto.protobuf.*;
import ru.arklual.telegramparser.entities.FlowEntity;
import ru.arklual.telegramparser.repositories.FlowRepository;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class FlowGrpcService extends FlowServiceGrpc.FlowServiceImplBase {

    private final FlowRepository repository;
    private final FlowConverter flowConverter;
    private final TelegramClientManager telegramClientManager;

    public FlowGrpcService(FlowRepository repository, FlowConverter flowConverter, TelegramClientManager telegramClientManager) {
        this.repository = repository;
        this.flowConverter = flowConverter;
        this.telegramClientManager = telegramClientManager;
    }


    @Override
    public void createFlow(CreateFlowRequest req, StreamObserver<CreateFlowResponse> resp) {
        FlowEntity toSave = flowConverter.toEntity(req.getFlow());
        FlowEntity saved = repository.save(toSave);
        Flow flowProto = flowConverter.toProto(saved);
        telegramClientManager.addListener(flowProto.getSource().getTelegramSource().getChatId(), flowProto.getTeamId());
        resp.onNext(CreateFlowResponse.newBuilder().setFlow(flowProto).build());
        resp.onCompleted();
    }


    @Override
    public void getFlow(GetFlowRequest req, StreamObserver<GetFlowResponse> resp) {
        repository.findById(new ObjectId(req.getId())).ifPresentOrElse(entity -> {
            Flow flowProto = flowConverter.toProto(entity);
            resp.onNext(GetFlowResponse.newBuilder().setFlow(flowProto).build());
            resp.onCompleted();
        }, () -> resp.onError(Status.NOT_FOUND.withDescription("Flow not found: " + req.getId()).asRuntimeException()));
    }


    @Override
    public void updateFlow(UpdateFlowRequest req, StreamObserver<UpdateFlowResponse> resp) {
        Flow proto = req.getFlow();
        ObjectId oid;
        try {
            oid = new ObjectId(proto.getId());
        } catch (IllegalArgumentException e) {
            resp.onError(Status.INVALID_ARGUMENT.withDescription("Invalid ID: " + proto.getId()).asRuntimeException());
            return;
        }

        repository.findById(oid).ifPresentOrElse(existing -> {
            FlowEntity updated = flowConverter.toEntity(proto);
            updated.setId(oid);
            FlowEntity saved = repository.save(updated);
            Flow flowProto = flowConverter.toProto(saved);
            resp.onNext(UpdateFlowResponse.newBuilder().setFlow(flowProto).build());
            resp.onCompleted();
        }, () -> resp.onError(Status.NOT_FOUND.withDescription("Flow not found: " + proto.getId()).asRuntimeException()));
    }


    @Override
    public void deleteFlow(DeleteFlowRequest request, StreamObserver<DeleteFlowResponse> responseObserver) {
        String id = request.getId();
        ObjectId oid;
        try {
            oid = new ObjectId(id);
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid Flow ID: " + id).asRuntimeException());
            return;
        }

        boolean existed = repository.existsById(oid);
        if (existed) {
            repository.deleteById(oid);
        }

        responseObserver.onNext(DeleteFlowResponse.newBuilder().setSuccess(existed).build());
        responseObserver.onCompleted();
    }

    @Override
    public void listFlows(ListFlowsRequest req, StreamObserver<ListFlowsResponse> resp) {
        List<Flow> protos = repository.findAllByTeamId(req.getTeamId()).stream().map(flowConverter::toProto).collect(Collectors.toList());

        resp.onNext(ListFlowsResponse.newBuilder().addAllFlows(protos).build());
        resp.onCompleted();
    }


}
