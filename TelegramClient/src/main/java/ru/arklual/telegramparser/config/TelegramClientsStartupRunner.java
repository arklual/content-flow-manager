package ru.arklual.telegramparser.config;

import it.tdlight.client.TDLibSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.arklual.telegramparser.converters.FlowConverter;
import ru.arklual.telegramparser.dto.protobuf.Flow;
import ru.arklual.telegramparser.entities.FlowEntity;
import ru.arklual.telegramparser.factories.TdSettingsFactory;
import ru.arklual.telegramparser.repositories.FlowRepository;
import ru.arklual.telegramparser.registries.QrCodeRegistry;
import ru.arklual.telegramparser.service.TelegramClientManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;


@Component
@Order(1)
@Slf4j
public class TelegramClientsStartupRunner implements ApplicationRunner {

    private final TelegramClientManager manager;
    private final TdSettingsFactory tdSettingsFactory;
    private final QrCodeRegistry qrCodeRegistry;
    private final Path baseSessionDir;
    private final FlowRepository flowRepository;
    private final FlowConverter flowConverter;

    public TelegramClientsStartupRunner(
            TelegramClientManager manager,
            TdSettingsFactory tdSettingsFactory,
            QrCodeRegistry qrCodeRegistry,
            @Value("${session_dir}") String baseSessionDir,
            FlowRepository flowRepository, FlowConverter flowConverter) {
        this.manager = manager;
        this.tdSettingsFactory = tdSettingsFactory;
        this.qrCodeRegistry = qrCodeRegistry;
        this.baseSessionDir = Paths.get(baseSessionDir);
        this.flowRepository = flowRepository;
        this.flowConverter = flowConverter;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (!Files.isDirectory(baseSessionDir)) {
            return;
        }

        try (Stream<Path> teamDirs = Files.list(baseSessionDir)) {
            teamDirs
                    .filter(Files::isDirectory)
                    .forEach(this::startOrRestoreClient);
        }

        List<FlowEntity> flows = flowRepository.findAll();
        for (FlowEntity flowEntity : flows) {
            Flow flow = flowConverter.toProto(flowEntity);
            manager.addListener(flow.getSource().getTelegramSource().getChatId(), flow.getTeamId());
        }
    }

    private void startOrRestoreClient(Path teamDir) {
        String teamId = teamDir.getFileName().toString();
        if (manager.getClient(teamId) != null) {
            return;
        }
        TDLibSettings settings = tdSettingsFactory.buildForTeam(teamId);
        manager.startClientViaQr(teamId, settings, qrCodeRegistry);
        log.info("Started Telegram client for team: {}", teamId);
    }
}
