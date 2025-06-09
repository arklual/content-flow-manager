package ru.arklual.telegramparser.factories;

import it.tdlight.client.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TdSettingsFactory {

    private final int apiId;
    private final String apiHash;
    private final String baseSessionDir;

    public TdSettingsFactory(int apiId, String apiHash, String baseSessionDir) {
        this.apiId = apiId;
        this.apiHash = apiHash;
        this.baseSessionDir = baseSessionDir;
    }

    public TDLibSettings buildForTeam(String teamId) {
        APIToken apiToken = new APIToken(apiId, apiHash);
        TDLibSettings settings = TDLibSettings.create(apiToken);

        Path sessionPath = Paths.get(baseSessionDir, teamId);
        settings.setDatabaseDirectoryPath(sessionPath.resolve("data"));
        settings.setDownloadedFilesDirectoryPath(sessionPath.resolve("downloads"));

        return settings;
    }
}
