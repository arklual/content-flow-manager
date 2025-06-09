package ru.arklual.crm.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.arklual.crm.dto.protobuf.ApprovalServiceGrpc;
import ru.arklual.crm.dto.protobuf.FlowServiceGrpc;
import ru.arklual.crm.dto.protobuf.TelegramClientServiceGrpc;

@Configuration
public class GrpcClientConfig {

    @Value("${grpc.telegram.host}")
    private String telegramHost;

    @Value("${grpc.telegram.port}")
    private int telegramPort;

    @Value("${grpc.approval.host}")
    private String approvalHost;

    @Value("${grpc.approval.port}")
    private int approvalPort;

    @Bean
    public ManagedChannel telegramChannel() {
        return ManagedChannelBuilder
                .forAddress(telegramHost, telegramPort)
                .usePlaintext()
                .build();
    }

    @Bean
    public ManagedChannel approvalChannel() {
        return ManagedChannelBuilder
                .forAddress(approvalHost, approvalPort)
                .usePlaintext()
                .build();
    }

    @Bean
    public FlowServiceGrpc.FlowServiceBlockingStub flowServiceStub(ManagedChannel telegramChannel) {
        return FlowServiceGrpc.newBlockingStub(telegramChannel);
    }

    @Bean
    public TelegramClientServiceGrpc.TelegramClientServiceBlockingStub telegramClientStub(ManagedChannel telegramChannel) {
        return TelegramClientServiceGrpc.newBlockingStub(telegramChannel);
    }

    @Bean
    public ApprovalServiceGrpc.ApprovalServiceBlockingStub approvalServiceStub(ManagedChannel approvalChannel) {
        return ApprovalServiceGrpc.newBlockingStub(approvalChannel);
    }

}
