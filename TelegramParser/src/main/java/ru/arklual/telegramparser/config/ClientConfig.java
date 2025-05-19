package ru.arklual.telegramparser.config;

import it.tdlight.client.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.arklual.telegramparser.factories.TdSettingsFactory;


@Configuration
public class ClientConfig {

***REMOVED******REMOVED***@Value("${api_id}")
***REMOVED******REMOVED***private int apiId;

***REMOVED******REMOVED***@Value("${api_hash}")
***REMOVED******REMOVED***private String apiHash;

***REMOVED******REMOVED***@Value("${session_dir}")
***REMOVED******REMOVED***private String sessionDir;

***REMOVED******REMOVED***@Bean
***REMOVED******REMOVED***public TdSettingsFactory tdSettingsFactory() {
***REMOVED******REMOVED******REMOVED******REMOVED***return new TdSettingsFactory(apiId, apiHash, sessionDir);
***REMOVED******REMOVED***}

***REMOVED******REMOVED***@Bean(destroyMethod = "close")
***REMOVED******REMOVED***public SimpleTelegramClientFactory simpleTelegramClientFactory() {
***REMOVED******REMOVED******REMOVED******REMOVED***return new SimpleTelegramClientFactory();
***REMOVED******REMOVED***}


}
