package ru.panyukovnn.contentconveyor.serivce;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.panyukovnn.contentconveyor.client.feign.TgChatsCollectorFeignClient;
import ru.panyukovnn.contentconveyor.dto.common.CommonRequest;
import ru.panyukovnn.contentconveyor.dto.common.CommonResponse;
import ru.panyukovnn.contentconveyor.dto.searchchathistory.SearchChatHistoryRequest;
import ru.panyukovnn.contentconveyor.dto.searchchathistory.SearchChatHistoryResponse;
import ru.panyukovnn.contentconveyor.util.JsonUtil;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TgChatsCollectorClientService {

    private static final SearchChatHistoryResponse FALLBACK_RESPONSE = SearchChatHistoryResponse.builder()
        .chatId(0L)
        .messages(List.of())
        .totalCount(0)
        .build();

    private final JsonUtil jsonUtil;
    private final TgChatsCollectorFeignClient tgChatsCollectorFeignClient;

    public SearchChatHistoryResponse fetchLastDayChatHistory(Long chatId, Long topicId) {
        try {
            SearchChatHistoryRequest searchChatHistoryRequest = SearchChatHistoryRequest.builder()
                .chatId(chatId)
                .topicId(topicId)
                .dateFrom(LocalDateTime.now().minusDays(1))
                .returnFromDb(false)
                .build();

            CommonRequest<SearchChatHistoryRequest> commonRequest = CommonRequest.<SearchChatHistoryRequest>builder()
                .body(searchChatHistoryRequest)
                .build();

            CommonResponse<SearchChatHistoryResponse> commonResponse = tgChatsCollectorFeignClient.postSearchChatHistory(commonRequest);

            if (StringUtils.isNotBlank(commonResponse.getErrorMessage())) {
                log.warn("Получен ответ с ошибкой от tg-chats-collector: {}", jsonUtil.toJson(commonResponse));

                return FALLBACK_RESPONSE;
            }

            return commonResponse.getBody();
        } catch (FeignException e) {
            log.warn("Ошибка при отправке запроса в tg-chats-collector, статус ответа: {}. Тело ответа: {}", e.status(), e.responseBody(), e);
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при отправке запроса в tg-chats-collector: {}", e.getMessage(), e);
        }

        return FALLBACK_RESPONSE;
    }
}
