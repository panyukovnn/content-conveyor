package ru.panyukovnn.contentconveyor.serivce.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.LinkPreviewOptions;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.panyukovnn.contentconveyor.exception.TgSendingException;
import ru.panyukovnn.contentconveyor.model.publishingchannels.PublishingChannel;
import ru.panyukovnn.contentconveyor.property.HardcodedPublishingProperties;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TgSender {

    private final TelegramBot telegramBot;
    private final TgMessagePreparer tgMessagePreparer;
    private final HardcodedPublishingProperties hardcodedPublishingProperties;

    public void sendMessage(PublishingChannel publishingChannel, String message) {
        this.sendMessage(publishingChannel.getChatId(), publishingChannel.getTopicId(), message);
    }

    public void sendDebugMessage(String message) {
        this.sendMessage(hardcodedPublishingProperties.getDebugChatId(), hardcodedPublishingProperties.getDebugTopicId(), message);
    }

    public void sendMessage(Long chatId, String message) {
        sendMessage(chatId, null, message);
    }

    public void sendMessage(Long chatId, Long messageThreadId, String message) {
        List<String> splitLongMessages = tgMessagePreparer.prepareTgMessage(message);

        splitLongMessages.forEach(splitMessage -> executeSendMessage(chatId, messageThreadId, splitMessage));
    }

    private void executeSendMessage(Long chatId, Long messageThreadId, String message) {
        if (messageThreadId == -1) {
            log.info("Прервана отправка сообщения, поскольку задан топик '-1': {}", message);

            return;
        }

        SendMessage request = new SendMessage(chatId, message)
            .parseMode(ParseMode.Markdown)
            .linkPreviewOptions(new LinkPreviewOptions()
                .isDisabled(true));

        if (messageThreadId != null) {
            request.messageThreadId(messageThreadId.intValue());
        }

        SendResponse response = telegramBot.execute(request);

        if (!response.isOk()) {
            if (response.description().contains("can't parse entities")) {
                log.error("При отправке сообщения в телеграм возникла ошибка парсинга Markdown: {}", response.description());

                request.parseMode(ParseMode.HTML);

                SendResponse secondResponse = telegramBot.execute(request);

                if (!secondResponse.isOk()) {
                    throw new TgSendingException("694f", "Ошибка отправки сообщения в телеграм: " + response.description());
                }

                return;
            }

            throw new TgSendingException("694f", "Ошибка отправки сообщения в телеграм: " + response.description());
        }
    }
}
