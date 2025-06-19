package ru.panyukovnn.contentconveyor.serivce;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.panyukovnn.contentconveyor.client.feign.TgChatsCollectorFeignClient;
import ru.panyukovnn.contentconveyor.dto.chathistory.ChatHistoryResponse;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TgChatsCollectorClientService {

    private static final ChatHistoryResponse FALLBACK_RESPONSE = ChatHistoryResponse.builder()
        .chatId(0L)
        .messageBatches(List.of())
        .totalCount(0)
        .build();

    private final TgChatsCollectorFeignClient tgChatsCollectorFeignClient;

    public ChatHistoryResponse getLastDayChatHistory(String privateChatNamePart, String topicNamePart) {
        try {
            return tgChatsCollectorFeignClient.getChatHistory(
                null,
                privateChatNamePart,
                topicNamePart,
                null,
                LocalDateTime.now().minusDays(1),
                null
            );
        } catch (FeignException e) {
            log.warn("Ошибка при отправке запроса в tg-chats-collector, статус ответа: {}. Тело ответа: {}", e.status(), e.responseBody(), e);
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при отправке запроса в tg-chats-collector: {}", e.getMessage(), e);
        }

        return FALLBACK_RESPONSE;
    }
}
