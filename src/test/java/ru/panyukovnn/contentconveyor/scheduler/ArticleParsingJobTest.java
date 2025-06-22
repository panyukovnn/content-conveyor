package ru.panyukovnn.contentconveyor.scheduler;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.panyukovnn.contentconveyor.model.content.Content;
import ru.panyukovnn.contentconveyor.model.Source;
import ru.panyukovnn.contentconveyor.model.event.ProcessingEventType;
import ru.panyukovnn.contentconveyor.repository.ContentRepository;
import ru.panyukovnn.contentconveyor.serivce.autodatafinder.AutoDataFinder;
import ru.panyukovnn.contentconveyor.serivce.domain.ProcessingEventDomainService;
import ru.panyukovnn.contentconveyor.serivce.loader.DataLoader;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@Disabled // TODO восстановить тесты
@ExtendWith(MockitoExtension.class)
public class ArticleParsingJobTest {

    @Mock
    private DataLoader mockHabrLoader;
    @Mock
    private AutoDataFinder mockHabrDataFinder;
    @Mock
    private ContentRepository mockContentRepository;
    @Mock
    private ProcessingEventDomainService processingEventDomainService;

    @InjectMocks
    private ArticleParsingJob articleParsingJob;

    @Test
    void when_parseHabr_then_success() {
        Content lastContent = new Content();
        lastContent.setLink("https://habr.com/article/1");
        lastContent.setSource(Source.JAVA_HABR);

        List<String> foundedLinks = List.of(
            "https://habr.com/article/2",
            "https://habr.com/article/3"
        );

        Content newContent = new Content();
        newContent.setLink("https://habr.com/article/2");

        when(mockContentRepository.findTopBySourceOrderByPublicationDateDesc(Source.JAVA_HABR))
            .thenReturn(Optional.of(lastContent));
        when(mockHabrDataFinder.findDataToLoad()).thenReturn(foundedLinks);
        when(mockHabrLoader.load("https://habr.com/article/2")).thenReturn(newContent);

        // Act
        articleParsingJob.parseHabr();

        // Assert
        verify(mockHabrDataFinder).findDataToLoad();
        verify(mockHabrLoader).load("https://habr.com/article/2");
        verify(processingEventDomainService).save(argThat(event ->
                event.getType() == ProcessingEventType.RATE_RAW_MATERIAL
        ));
    }

    @Test
    void when_parseHabr_withEmptyFoundedLinks_then_doNothing() {
        Content lastContent = new Content();
        lastContent.setLink("https://habr.com/article/1");
        lastContent.setSource(Source.JAVA_HABR);

        List<String> emptyFoundedLinks = Collections.emptyList();

        when(mockContentRepository.findTopBySourceOrderByPublicationDateDesc(Source.JAVA_HABR))
            .thenReturn(Optional.of(lastContent));
        when(mockHabrDataFinder.findDataToLoad()).thenReturn(emptyFoundedLinks);

        // Act
        articleParsingJob.parseHabr();

        // Assert
        verify(mockHabrDataFinder).findDataToLoad();
        verify(mockHabrLoader, never()).load(anyString());
        verify(processingEventDomainService, never()).save(any());
    }
}
