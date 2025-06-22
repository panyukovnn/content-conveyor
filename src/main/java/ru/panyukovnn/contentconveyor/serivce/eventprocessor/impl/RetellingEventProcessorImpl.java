package ru.panyukovnn.contentconveyor.serivce.eventprocessor.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.panyukovnn.contentconveyor.client.OpenAiClient;
import ru.panyukovnn.contentconveyor.model.content.Content;
import ru.panyukovnn.contentconveyor.model.event.ProcessingEvent;
import ru.panyukovnn.contentconveyor.model.event.ProcessingEventType;
import ru.panyukovnn.contentconveyor.property.HardcodedPromptProperties;
import ru.panyukovnn.contentconveyor.serivce.domain.ContentDomainService;
import ru.panyukovnn.contentconveyor.serivce.domain.ProcessingEventDomainService;
import ru.panyukovnn.contentconveyor.serivce.eventprocessor.EventProcessor;
import ru.panyukovnn.contentconveyor.util.JsonUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetellingEventProcessorImpl implements EventProcessor {

    private final JsonUtil jsonUtil;
    private final OpenAiClient openAiClient;
    private final ContentDomainService contentDomainService;
    private final HardcodedPromptProperties hardcodedPromptProperties;
    private final ProcessingEventDomainService processingEventDomainService;

    @Override
    public void process(ProcessingEvent processingEvent) {
        Content content = findContent(processingEvent);

        String prompt = switch (content.getSource()) {
            case JAVA_HABR -> hardcodedPromptProperties.getJavaArticleRetelling();
            case JAVA_DZONE -> hardcodedPromptProperties.getJavaArticleRetelling();
            case JAVA_MEDIUM -> hardcodedPromptProperties.getJavaArticleRetelling();
            case TG -> hardcodedPromptProperties.getTgMessageBatchRetelling();
        };

        String retellingResponse = openAiClient.promptingCall(processingEvent.getType().name(), prompt, content.getContent());

        Content retelledContent = Content.builder()
            .link(content.getLink())
            .type(content.getType())
            .source(content.getSource())
            .title(content.getTitle())
            .meta(null)
            .publicationDate(content.getPublicationDate())
            .content(retellingResponse)
            .parentBatchId(content.getChildBatchId())
            .childBatchId(null)
            .build();
        contentDomainService.save(retelledContent);

        processingEvent.setType(ProcessingEventType.PUBLISHING);
        processingEvent.setContentId(retelledContent.getId());
        processingEventDomainService.save(processingEvent);

        log.info("Успешно выполнен пересказ материала по тегу: {}. Название материала: {}", processingEvent.getType().name(), content.getTitle());
    }

    private Content findContent(ProcessingEvent processingEvent) {
        Content content = contentDomainService.findById(processingEvent.getContentId())
            .orElse(null);

        if (content == null) {
            processingEventDomainService.delete(processingEvent);

            throw new EntityNotFoundException("Не удалось выполнить пересказ материала, поскольку не найден контент, событие будет удалено");
        }

        return content;
    }

    @Override
    public ProcessingEventType getProcessingEventType() {
        return ProcessingEventType.RETELLING;
    }

}
