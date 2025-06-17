package ru.panyukovnn.contentconveyor.serivce.tgchatscollector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import ru.panyukovnn.contentconveyor.client.feign.TgChatsCollectorFeignClient;
import ru.panyukovnn.contentconveyor.dto.TgConveyorRequest;
import ru.panyukovnn.contentconveyor.dto.chathistory.ChatHistoryResponse;
import ru.panyukovnn.contentconveyor.model.ConveyorTag;
import ru.panyukovnn.contentconveyor.model.ConveyorType;
import ru.panyukovnn.contentconveyor.model.Prompt;
import ru.panyukovnn.contentconveyor.model.PublishingChannel;
import ru.panyukovnn.contentconveyor.model.content.Content;
import ru.panyukovnn.contentconveyor.model.content.ContentType;
import ru.panyukovnn.contentconveyor.model.Source;
import ru.panyukovnn.contentconveyor.model.event.ProcessingEvent;
import ru.panyukovnn.contentconveyor.model.event.ProcessingEventType;
import ru.panyukovnn.contentconveyor.serivce.domain.ContentDomainService;
import ru.panyukovnn.contentconveyor.serivce.domain.ProcessingEventDomainService;
import ru.panyukovnn.contentconveyor.serivce.domain.PromptDomainService;
import ru.panyukovnn.contentconveyor.serivce.domain.PublishingChannelDomainService;
import ru.panyukovnn.contentconveyor.util.JsonUtil;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TgChatsCollectorHandler {

    private final JsonUtil jsonUtil;
    private final TransactionTemplate transactionTemplate;
    private final PromptDomainService promptDomainService;
    private final ContentDomainService contentDomainService;
    private final TgChatsCollectorFeignClient tgChatsCollectorFeignClient;
    private final ProcessingEventDomainService processingEventDomainService;
    private final PublishingChannelDomainService publishingChannelDomainService;

    public void handleChatMessages(TgConveyorRequest tgConveyorRequest) {
        ChatHistoryResponse chatHistory = tgChatsCollectorFeignClient.getChatHistory(
            tgConveyorRequest.getPublicChatName(),
            tgConveyorRequest.getPrivateChatNamePart(),
            tgConveyorRequest.getTopicName(),
            tgConveyorRequest.getLimit(),
            tgConveyorRequest.getDateFrom(),
            tgConveyorRequest.getDateTo());

        if (CollectionUtils.isEmpty(chatHistory.getMessageBatches())) {
            log.warn("Не найдено ни одного сообщения в указанном чате/топике");

            return;
        }

        transactionTemplate.execute(tx -> {
            PublishingChannel publishingChannel = definePublishingChannel(tgConveyorRequest);

            Prompt prompt = promptDomainService.save(Prompt.builder()
                .mapPrompt(tgConveyorRequest.getMapPrompt())
                .reducePrompt(tgConveyorRequest.getReducePrompt())
                .build());

            createContents(chatHistory, prompt.getId(), publishingChannel.getId());

            return null;
        });
    }

    private void createContents(ChatHistoryResponse chatHistory, UUID promptId, UUID publishingChannelId) {
        UUID parentBatchId = UUID.randomUUID();
        UUID childBatchId = UUID.randomUUID();

        chatHistory.getMessageBatches().forEach(messagesBatch -> {
//            LocalDate firstMessageDate = messagesBatch.getMessages().get(0).
//            LocalDate lastMessageDate = chatHistory.getLastMessageDateTime().toLocalDate();

            Content content = Content.builder()
                .link(chatHistory.getChatId().toString())
                .type(ContentType.TG_MESSAGE_BATCH)
                .source(Source.TG)
//                .title(chatHistory.getChatTitle() + "/" + chatHistory.getTopicName() + " " + firstMessageDate + " - " + lastMessageDate)
                .title(chatHistory.getChatTitle() + "/" + chatHistory.getTopicName())
                .meta(null)
                .publicationDate(chatHistory.getFirstMessageDateTime())
                .content(jsonUtil.toJson(messagesBatch.getMessages()))
                .parentBatchId(parentBatchId)
                .childBatchId(childBatchId)
                .build();

            contentDomainService.save(content);
        });

        ProcessingEvent reduceProcessingEvent = ProcessingEvent.builder()
            .type(ProcessingEventType.MAP)
            .conveyorTag(ConveyorTag.TG_MESSAGE_BATCH)
            .conveyorType(ConveyorType.MAP_REDUCE)
            .contentId(null)
            .contentBatchId(parentBatchId)
            .promptId(promptId)
            .publishingChannelId(publishingChannelId)
            .build();
        processingEventDomainService.save(reduceProcessingEvent);
    }

    private PublishingChannel definePublishingChannel(TgConveyorRequest tgConveyorRequest) {
        return publishingChannelDomainService.findByExternalId(tgConveyorRequest.getPublishingChannelExternalId())
            .orElseGet(() -> publishingChannelDomainService.save(PublishingChannel.builder()
                .externalId(tgConveyorRequest.getPublishingChannelExternalId())
                .chatId(tgConveyorRequest.getChatId())
                .topicId(tgConveyorRequest.getTopicId())
                .build()));
    }
}
