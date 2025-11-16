package ru.panyukovnn.contentconveyor.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.panyukovnn.contentconveyor.dto.searchchathistory.SearchChatHistoryResponse;
import ru.panyukovnn.contentconveyor.dto.searchchathistory.TgMessageDto;
import ru.panyukovnn.contentconveyor.model.ConveyorType;
import ru.panyukovnn.contentconveyor.model.Source;
import ru.panyukovnn.contentconveyor.model.content.Content;
import ru.panyukovnn.contentconveyor.model.content.ContentType;
import ru.panyukovnn.contentconveyor.model.event.ProcessingEvent;
import ru.panyukovnn.contentconveyor.model.parsingjobinfo.ParsingJobInfo;
import ru.panyukovnn.contentconveyor.model.parsingjobinfo.SourceDetails;
import ru.panyukovnn.contentconveyor.serivce.TgChatsCollectorClientService;
import ru.panyukovnn.contentconveyor.serivce.domain.ContentDomainService;
import ru.panyukovnn.contentconveyor.serivce.domain.ParsingJobInfoDomainService;
import ru.panyukovnn.contentconveyor.serivce.domain.ProcessingEventDomainService;
import ru.panyukovnn.contentconveyor.serivce.telegram.TgSender;
import ru.panyukovnn.contentconveyor.util.JsonUtil;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParsingJob {

    private final JsonUtil jsonUtil;
    private final TgSender tgSender;
    private final ContentDomainService contentDomainService;
    private final ParsingJobInfoDomainService parsingJobInfoDomainService;
    private final ProcessingEventDomainService processingEventDomainService;
    private final TgChatsCollectorClientService tgChatsCollectorClientService;

    @Async("parsingJobScheduler")
    @Scheduled(cron = "${retelling.scheduled-jobs.source-parsing.tg-cron}")
    public void parsing() {
        List<ParsingJobInfo> dailyParsingJobs = parsingJobInfoDomainService.findDailyParsingJobs();

        dailyParsingJobs.forEach(parsingJob -> {
            try {
                if (Source.TG.equals(parsingJob.getSource())) {
                    SourceDetails sourceDetails = parsingJob.getSourceDetails();

                    SearchChatHistoryResponse searchChatHistoryResponse = tgChatsCollectorClientService.fetchLastDayChatHistory(
                        sourceDetails.getTgChatId(),
                        sourceDetails.getTgTopicId()
                    );

                    if (searchChatHistoryResponse.getChatId() == 0L) {
                        tgSender.sendDebugMessage("Ошибка получения сообщений из чата: " + sourceDetails);

                        return;
                    }

                    if (CollectionUtils.isEmpty(searchChatHistoryResponse.getMessages())) {
                        tgSender.sendDebugMessage("Не найдены сообщения в чате для пересказа: " + sourceDetails);

                        return;
                    }

                    startConveyor(parsingJob, searchChatHistoryResponse);
                }
            } catch (Exception e) {
                log.error("Ошибка ежедневного сбора информации из источника: {}", e.getMessage(), e);
            }
        });
    }

    private void startConveyor(ParsingJobInfo parsingJob, SearchChatHistoryResponse searchChatHistoryResponse) {
        UUID parentBatchId = UUID.randomUUID();
        UUID childBatchId = UUID.randomUUID();

        TgMessageDto earliestTgMessageDto = searchChatHistoryResponse.getMessages().get(0);

        Content content = Content.builder()
            .link(searchChatHistoryResponse.getChatId().toString())
            .type(ContentType.TG_MESSAGE_BATCH)
            .source(Source.TG)
            .title(searchChatHistoryResponse.getChatTitle() + " / " + searchChatHistoryResponse.getTopicName() + " - за последние 24 часа")
            .publicationDate(earliestTgMessageDto.getDateTime())
            .content(jsonUtil.toJson(searchChatHistoryResponse.getMessages()))
            .parentBatchId(parentBatchId)
            .childBatchId(childBatchId)
            .build();

        contentDomainService.save(content);

        ConveyorType conveyorType = parsingJob.getConveyorType();

        ProcessingEvent reduceProcessingEvent = ProcessingEvent.builder()
            .type(conveyorType.getStartEventType())
            .conveyorType(conveyorType)
            .contentId(null)
            .contentBatchId(parentBatchId)
            .promptId(parsingJob.getPrompt().getId())
            .publishingChannelSetsId(parsingJob.getPublishingChannelSetsId())
            .build();
        processingEventDomainService.save(reduceProcessingEvent);
    }
}
