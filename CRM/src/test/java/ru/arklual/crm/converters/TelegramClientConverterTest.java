package ru.arklual.crm.converters;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.arklual.crm.dto.protobuf.TelegramClientProto;
import ru.arklual.crm.dto.responses.TelegramClientStatusResponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TelegramClientConverterTest {

    @Autowired
    private TelegramClientConverter converter;

    @Test
    void testFromGrpc() {
        UUID uuid = UUID.randomUUID();
        TelegramClientProto.ClientStatusResponse grpc = TelegramClientProto.ClientStatusResponse.newBuilder()
                .setTeamId(uuid.toString())
                .setState("READY")
                .setIsRunning(true)
                .build();

        TelegramClientStatusResponse result = converter.fromGrpc(grpc);

        assertThat(result).isNotNull();
        assertThat(result.getTeamId()).isEqualTo(uuid);
        assertThat(result.getState()).isEqualTo("READY");
        assertThat(result.isRunning()).isTrue();
    }
}
