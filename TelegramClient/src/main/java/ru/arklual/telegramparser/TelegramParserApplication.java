package ru.arklual.telegramparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class TelegramParserApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramParserApplication.class, args);
    }

}
