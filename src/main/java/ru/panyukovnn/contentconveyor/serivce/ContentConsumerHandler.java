package ru.panyukovnn.contentconveyor.serivce;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.panyukovnn.contentconveyor.dto.ConsumeContentRequest;
import ru.panyukovnn.contentconveyor.model.ConveyorType;
import ru.panyukovnn.contentconveyor.model.content.Content;
import ru.panyukovnn.contentconveyor.model.content.ContentType;
import ru.panyukovnn.contentconveyor.model.Source;
import ru.panyukovnn.contentconveyor.model.event.ProcessingEvent;
import ru.panyukovnn.contentconveyor.serivce.domain.ContentDomainService;
import ru.panyukovnn.contentconveyor.serivce.domain.ProcessingEventDomainService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentConsumerHandler {

    private final ContentDomainService contentDomainService;
    private final ProcessingEventDomainService processingEventDomainService;

    @Transactional
    public void handleConsumeContent(ConsumeContentRequest consumeContentRequest) {
        if (ConveyorType.valueOf(consumeContentRequest.getConveyorType().toUpperCase()) != ConveyorType.JUST_RETELLING) {
            throw new ValidationException("Задан неподдерживаемый тип конвейера");
        }

        Content content = Content.builder()
            .link(consumeContentRequest.getLink())
            .type(ContentType.valueOf(consumeContentRequest.getContentType().toUpperCase()))
            .source(Source.valueOf(consumeContentRequest.getSource().toUpperCase()))
            .title(consumeContentRequest.getTitle())
            .meta(consumeContentRequest.getMeta())
            .publicationDate(consumeContentRequest.getPublicationDate())
            .content(consumeContentRequest.getContent())
            .build();

        contentDomainService.save(content);

        ConveyorType conveyorType = ConveyorType.valueOf(consumeContentRequest.getConveyorType().toUpperCase());

        ProcessingEvent processingEvent = ProcessingEvent.builder()
            .contentId(content.getId())
            .type(conveyorType.getStartEventType())
            .conveyorType(conveyorType)
            .build();
        processingEventDomainService.save(processingEvent);
    }
}
