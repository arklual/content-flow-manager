package ru.arklual.telegramparser.config;

import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.client.TDLibSettings;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.*;
import org.springframework.boot.ApplicationArguments;
import ru.arklual.telegramparser.converters.FlowConverter;
import ru.arklual.telegramparser.dto.protobuf.Flow;
import ru.arklual.telegramparser.dto.protobuf.PostProto;
import ru.arklual.telegramparser.entities.FlowEntity;
import ru.arklual.telegramparser.factories.TdSettingsFactory;
import ru.arklual.telegramparser.registries.QrCodeRegistry;
import ru.arklual.telegramparser.repositories.FlowRepository;
import ru.arklual.telegramparser.service.TelegramClientManager;

import java.nio.file.*;
import java.util.List;

import static org.mockito.Mockito.*;

class TelegramClientsStartupRunnerTest {

    @TempDir
    Path tempDir;

    @Mock private TelegramClientManager manager;
    @Mock private TdSettingsFactory tdSettingsFactory;
    @Mock private QrCodeRegistry qrCodeRegistry;
    @Mock private FlowRepository flowRepository;
    @Mock private FlowConverter flowConverter;
    @Mock private ApplicationArguments appArgs;

    private TelegramClientsStartupRunner runner;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        runner = new TelegramClientsStartupRunner(
                manager,
                tdSettingsFactory,
                qrCodeRegistry,
                tempDir.toString(),
                flowRepository,
                flowConverter
        );
    }

    @Test
    void testRun_shouldDoNothing_whenSessionDirDoesNotExist() throws Exception {
        Path nonExistent = tempDir.resolve("nope");
        TelegramClientsStartupRunner localRunner = new TelegramClientsStartupRunner(
                manager,
                tdSettingsFactory,
                qrCodeRegistry,
                nonExistent.toString(),
                flowRepository,
                flowConverter
        );

        localRunner.run(appArgs);

        verifyNoInteractions(manager, flowRepository, flowConverter);
    }

    @Test
    void testRun_shouldStartClientsAndAddListeners() throws Exception {
        Path team1 = Files.createDirectory(tempDir.resolve("team1"));
        Path team2 = Files.createDirectory(tempDir.resolve("team2"));

        when(manager.getClient("team1")).thenReturn(null);
        when(manager.getClient("team2")).thenReturn(null);
        when(tdSettingsFactory.buildForTeam(anyString())).thenReturn(mock(TDLibSettings.class));

        FlowEntity entity = mock(FlowEntity.class);
        Flow flow = Flow.newBuilder()
                .setTeamId("team1")
                .setSource(PostProto.Source.newBuilder()
                        .setTelegramSource(PostProto.TelegramSource.newBuilder()
                                .setChatId("12345").build()))
                .build();

        when(flowRepository.findAll()).thenReturn(List.of(entity));
        when(flowConverter.toProto(entity)).thenReturn(flow);

        runner.run(appArgs);

        verify(manager).startClientViaQr(eq("team1"), any(), eq(qrCodeRegistry));
        verify(manager).startClientViaQr(eq("team2"), any(), eq(qrCodeRegistry));
        verify(manager).addListener("12345", "team1");
    }

    @Test
    void testStartOrRestoreClient_shouldSkip_whenAlreadyRunning() throws Exception {
        Path teamPath = Files.createDirectory(tempDir.resolve("existingTeam"));
        String teamId = "existingTeam";

        when(manager.getClient(teamId)).thenReturn(Mockito.mock(SimpleTelegramClient.class));

        var m = TelegramClientsStartupRunner.class
                .getDeclaredMethod("startOrRestoreClient", Path.class);
        m.setAccessible(true);
        m.invoke(runner, teamPath);

        verify(manager, never()).startClientViaQr(any(), any(), any());
    }
}
