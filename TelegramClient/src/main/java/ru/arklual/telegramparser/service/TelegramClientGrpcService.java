package ru.arklual.telegramparser.service;

import io.grpc.stub.StreamObserver;
import it.tdlight.client.TDLibSettings;
import org.springframework.grpc.server.service.GrpcService;
import ru.arklual.telegramparser.dto.protobuf.TelegramClientProto.*;
import ru.arklual.telegramparser.dto.protobuf.TelegramClientServiceGrpc;
import ru.arklual.telegramparser.factories.TdSettingsFactory;
import ru.arklual.telegramparser.registries.QrCodeRegistry;
import ru.arklual.telegramparser.registries.StatesRegistry;

import java.util.*;

@GrpcService
public class TelegramClientGrpcService extends TelegramClientServiceGrpc.TelegramClientServiceImplBase {

    private final TelegramClientManager manager;
    private final TdSettingsFactory tdSettingsFactory;
    private final QrCodeRegistry qrCodeRegistry;
    private final StatesRegistry statesRegistry;

    public TelegramClientGrpcService(TelegramClientManager manager,
                                     TdSettingsFactory tdSettingsFactory,
                                     QrCodeRegistry qrCodeRegistry,
                                     StatesRegistry statesRegistry) {
        this.manager = manager;
        this.tdSettingsFactory = tdSettingsFactory;
        this.qrCodeRegistry = qrCodeRegistry;
        this.statesRegistry = statesRegistry;
    }

    @Override
    public void startClient(TeamId request, StreamObserver<MessageResponse> responseObserver) {
        String teamId = request.getTeamId();
        if (manager.getClient(teamId) != null) {
            responseObserver.onNext(MessageResponse.newBuilder()
                    .setMessage("Client already started")
                    .build());
            responseObserver.onCompleted();
            return;
        }
        TDLibSettings settings = tdSettingsFactory.buildForTeam(teamId);
        manager.startClientViaQr(teamId, settings, qrCodeRegistry);
        responseObserver.onNext(MessageResponse.newBuilder()
                .setMessage("Telegram client started via QR for team " + teamId)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void stopClient(TeamId request, StreamObserver<MessageResponse> responseObserver) {
        String teamId = request.getTeamId();
        manager.stopClient(teamId);
        responseObserver.onNext(MessageResponse.newBuilder()
                .setMessage("Telegram client stopped for team " + teamId)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getClientStatus(TeamId request, StreamObserver<ClientStatusResponse> responseObserver) {
        String teamId = request.getTeamId();
        String state = statesRegistry.getState(teamId);
        boolean isRunning = state != null;
        if (!isRunning) {
            responseObserver.onNext(ClientStatusResponse.newBuilder()
                    .setTeamId(teamId)
                    .setState("Not started")
                    .setIsRunning(false)
                    .build());
        } else {
            responseObserver.onNext(ClientStatusResponse.newBuilder()
                    .setTeamId(teamId)
                    .setState(state)
                    .setIsRunning(true)
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getQrLink(TeamId request, StreamObserver<LinkResponse> responseObserver) {
        String teamId = request.getTeamId();
        String qrLink = qrCodeRegistry.getQrCode(teamId);
        if (qrLink == null) {
            responseObserver.onError(new RuntimeException("QR code not generated yet"));
            return;
        }
        responseObserver.onNext(LinkResponse.newBuilder()
                .setLink(qrLink)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void listClients(com.google.protobuf.Empty request, StreamObserver<ListClientsResponse> responseObserver) {
        List<String> teamIds = manager.getAllTeamIds();
        responseObserver.onNext(ListClientsResponse.newBuilder()
                .addAllTeamIds(teamIds)
                .build());
        responseObserver.onCompleted();
    }

}
