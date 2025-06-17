package ru.panyukovnn.contentconveyor.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.panyukovnn.contentconveyor.model.ConveyorTag;
import ru.panyukovnn.contentconveyor.model.ConveyorType;
import ru.panyukovnn.contentconveyor.model.content.Content;
import ru.panyukovnn.contentconveyor.model.Source;
import ru.panyukovnn.contentconveyor.model.event.ProcessingEvent;
import ru.panyukovnn.contentconveyor.repository.ContentRepository;
import ru.panyukovnn.contentconveyor.serivce.autodatafinder.AutoDataFinder;
import ru.panyukovnn.contentconveyor.serivce.domain.ProcessingEventDomainService;
import ru.panyukovnn.contentconveyor.serivce.loader.DataLoader;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SourceParsingJob {

    private final DataLoader habrLoader;
    private final AutoDataFinder habrDataFinder;
    private final ContentRepository contentRepository;
    private final ProcessingEventDomainService processingEventDomainService;

    @Async("sourceParsingScheduler")
    @Scheduled(cron = "${retelling.scheduled-jobs.source-parsing.habr-cron}")
    public void parseHabr() {
        try {
            List<String> foundedLinks = habrDataFinder.findDataToLoad();

            Content lastContent = contentRepository.findTopBySourceOrderByPublicationDateDesc(Source.HABR)
                .orElse(null);
            List<String> linksToLoad = defineLinksToLoad(foundedLinks, lastContent);

            linksToLoad.forEach(link -> {
                try {
                    Content content = habrLoader.load(link);

                    if (!processingEventDomainService.existsByContentId(content.getId())) {
                        ConveyorType ratingAndRetelling = ConveyorType.RATING_AND_RETELLING;
                        ProcessingEvent processingEvent = ProcessingEvent.builder()
                            .type(ratingAndRetelling.getStartEventType())
                            .conveyorType(ratingAndRetelling)
                            .conveyorTag(ConveyorTag.JAVA_HABR)
                            .contentId(content.getId())
                            .build();

                        processingEventDomainService.save(processingEvent);
                    }
                } catch (Exception e) {
                    log.error("Ошибка при загрузке содержимого статьи с habr: " + e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            log.error("Ошибка при парсинге habr: {}", e.getMessage(), e);
        }
    }

    protected List<String> defineLinksToLoad(List<String> foundedLinks, Content lastContent) {
        if (lastContent != null && foundedLinks.contains(lastContent.getLink())) {
            return foundedLinks.stream()
                .dropWhile(link -> !link.equals(lastContent.getLink()))
                .toList();
        }

        return foundedLinks;
    }
}
