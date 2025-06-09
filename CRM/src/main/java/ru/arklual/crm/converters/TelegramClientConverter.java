package ru.arklual.crm.converters;

import org.springframework.stereotype.Component;
import ru.arklual.crm.dto.responses.TelegramClientStatusResponse;
import ru.arklual.crm.dto.protobuf.TelegramClientProto;

import java.util.UUID;

@Component
public class TelegramClientConverter {

    public TelegramClientStatusResponse fromGrpc(TelegramClientProto.ClientStatusResponse grpc) {
        return TelegramClientStatusResponse.builder()
                .teamId(UUID.fromString(grpc.getTeamId()))
                .state(grpc.getState())
                .isRunning(grpc.getIsRunning())
                .build();
    }

}
