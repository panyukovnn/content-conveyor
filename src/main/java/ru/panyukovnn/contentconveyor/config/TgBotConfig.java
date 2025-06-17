package ru.panyukovnn.contentconveyor.config;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.panyukovnn.contentconveyor.property.TgBotProperties;

@Configuration
public class TgBotConfig {

    @Bean
    public TelegramBot telegramBot(TgBotProperties tgBotProperties) {
        return new TelegramBot(tgBotProperties.getToken());
    }
}
