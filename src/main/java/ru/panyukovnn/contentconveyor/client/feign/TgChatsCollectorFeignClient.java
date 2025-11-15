package ru.panyukovnn.contentconveyor.client.feign;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.panyukovnn.contentconveyor.dto.common.CommonRequest;
import ru.panyukovnn.contentconveyor.dto.common.CommonResponse;
import ru.panyukovnn.contentconveyor.dto.searchchathistory.SearchChatHistoryRequest;
import ru.panyukovnn.contentconveyor.dto.searchchathistory.SearchChatHistoryResponse;

@FeignClient(url = "${retelling.integration.tg-chats-collector.host}/tg-chats-collector/api/v1", name = "tg-chats-collector")
public interface TgChatsCollectorFeignClient {

    @PostMapping("/search-chat-history")
    CommonResponse<SearchChatHistoryResponse> postSearchChatHistory(@RequestBody @Valid CommonRequest<SearchChatHistoryRequest> searchChatHistory);
}
