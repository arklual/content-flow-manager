package ru.arklual.telegramparser.factories;

import it.tdlight.client.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TdSettingsFactory {

***REMOVED******REMOVED***private final int apiId;
***REMOVED******REMOVED***private final String apiHash;
***REMOVED******REMOVED***private final String baseSessionDir;

***REMOVED******REMOVED***public TdSettingsFactory(int apiId, String apiHash, String baseSessionDir) {
***REMOVED******REMOVED******REMOVED******REMOVED***this.apiId = apiId;
***REMOVED******REMOVED******REMOVED******REMOVED***this.apiHash = apiHash;
***REMOVED******REMOVED******REMOVED******REMOVED***this.baseSessionDir = baseSessionDir;
***REMOVED******REMOVED***}

***REMOVED******REMOVED***public TDLibSettings buildForTeam(String teamId) {
***REMOVED******REMOVED******REMOVED******REMOVED***APIToken apiToken = new APIToken(apiId, apiHash);
***REMOVED******REMOVED******REMOVED******REMOVED***TDLibSettings settings = TDLibSettings.create(apiToken);

***REMOVED******REMOVED******REMOVED******REMOVED***Path sessionPath = Paths.get(baseSessionDir, teamId);
***REMOVED******REMOVED******REMOVED******REMOVED***settings.setDatabaseDirectoryPath(sessionPath.resolve("data"));
***REMOVED******REMOVED******REMOVED******REMOVED***settings.setDownloadedFilesDirectoryPath(sessionPath.resolve("downloads"));

***REMOVED******REMOVED******REMOVED******REMOVED***return settings;
***REMOVED******REMOVED***}
}
