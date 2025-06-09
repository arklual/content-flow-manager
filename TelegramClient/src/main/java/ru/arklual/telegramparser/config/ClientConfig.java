package ru.arklual.telegramparser.config;

import it.tdlight.client.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.arklual.telegramparser.factories.TdSettingsFactory;


@Configuration
public class ClientConfig {

    @Value("${api_id}")
    private int apiId;

    @Value("${api_hash}")
    private String apiHash;

    @Value("${session_dir}")
    private String sessionDir;

    @Bean
    public TdSettingsFactory tdSettingsFactory() {
        return new TdSettingsFactory(apiId, apiHash, sessionDir);
    }

    @Bean(destroyMethod = "close")
    public SimpleTelegramClientFactory simpleTelegramClientFactory() {
        return new SimpleTelegramClientFactory();
    }


}
