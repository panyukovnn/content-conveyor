package ru.panyukovnn.contentconveyor.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.panyukovnn.contentconveyor.client.feign.TgChatsCollectorFeignClient;
import ru.panyukovnn.contentconveyor.dto.common.CommonRequest;
import ru.panyukovnn.contentconveyor.dto.common.CommonResponse;
import ru.panyukovnn.contentconveyor.dto.searchchathistory.SearchChatHistoryRequest;
import ru.panyukovnn.contentconveyor.dto.searchchathistory.SearchChatHistoryResponse;
import ru.panyukovnn.contentconveyor.model.parsingjobinfo.ParsingJobInfo;
import ru.panyukovnn.contentconveyor.serivce.domain.ParsingJobInfoDomainService;
import ru.panyukovnn.contentconveyor.util.JsonUtil;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestController {

    private final JsonUtil jsonUtil;
    private final TgChatsCollectorFeignClient tgChatsCollectorFeignClient;
    private final ParsingJobInfoDomainService parsingJobInfoDomainService;

    @GetMapping
    public String test() {
        List<ParsingJobInfo> dailyParsingJobs = parsingJobInfoDomainService.findDailyParsingJobs();

        ParsingJobInfo parsingJobInfo = dailyParsingJobs.get(0);

        try {
            SearchChatHistoryRequest searchChatHistoryRequest = SearchChatHistoryRequest.builder()
                .chatId(parsingJobInfo.getSourceDetails().getTgChatId())
                .topicId(parsingJobInfo.getSourceDetails().getTgTopicId())
                .dateFrom(LocalDateTime.now().minusDays(1))
                .returnFromDb(false)
                .build();

            CommonRequest<SearchChatHistoryRequest> commonRequest = CommonRequest.<SearchChatHistoryRequest>builder()
                .body(searchChatHistoryRequest)
                .build();

            CommonResponse<SearchChatHistoryResponse> commonResponse = tgChatsCollectorFeignClient.postSearchChatHistory(commonRequest);

            if (StringUtils.isNotBlank(commonResponse.getErrorMessage())) {
                log.warn("Получен ответ с ошибкой от tg-chats-collector: {}", jsonUtil.toJson(commonResponse));

                return "FALLBACK_RESPONSE";
            }

            return jsonUtil.toJson(commonResponse.getBody());
        } catch (FeignException e) {
            log.warn("Ошибка при отправке запроса в tg-chats-collector, статус ответа: {}. Тело ответа: {}", e.status(), e.contentUTF8(), e);
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при отправке запроса в tg-chats-collector: {}", e.getMessage(), e);
        }

        return "FALLBACK_RESPONSE";
    }
}
