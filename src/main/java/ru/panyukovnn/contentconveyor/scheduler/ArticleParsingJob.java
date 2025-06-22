package ru.panyukovnn.contentconveyor.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.panyukovnn.contentconveyor.model.ConveyorType;
import ru.panyukovnn.contentconveyor.model.Source;
import ru.panyukovnn.contentconveyor.model.content.Content;
import ru.panyukovnn.contentconveyor.model.event.ProcessingEvent;
import ru.panyukovnn.contentconveyor.serivce.articleparser.ArticleParser;
import ru.panyukovnn.contentconveyor.serivce.domain.ProcessingEventDomainService;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleParsingJob {

    private final Map<Source, ArticleParser> articleParserBySource;
    private final ProcessingEventDomainService processingEventDomainService;

    @Async("sourceParsingScheduler")
    @Scheduled(cron = "${retelling.scheduled-jobs.source-parsing.article-cron}")
    public void parseHabr() {
        Source.getArticleSources().forEach(articleSource -> {
            if (Source.JAVA_DZONE.equals(articleSource) || Source.JAVA_MEDIUM.equals(articleSource)) {
                return;
            }

            ArticleParser articleParser = articleParserBySource.get(articleSource);

            List<Content> contents = articleParser.loadNewContent();

            ConveyorType ratingAndRetelling = ConveyorType.RATING_AND_RETELLING;

            contents.forEach(content -> {
                ProcessingEvent processingEvent = ProcessingEvent.builder()
                    .type(ratingAndRetelling.getStartEventType())
                    .conveyorType(ratingAndRetelling)
                    .contentId(content.getId())
                    .build();

                processingEventDomainService.save(processingEvent);
            });
        });
    }


}
