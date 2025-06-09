package ru.arklual.telegramparser.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "s3")
public class S3Properties {

    private String bucketName;
    private String endpoint;

}
