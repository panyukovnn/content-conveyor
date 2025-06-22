package ru.panyukovnn.contentconveyor.serivce.loader.impl;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.transaction.annotation.Transactional;
import ru.panyukovnn.contentconveyor.AbstractTest;
import ru.panyukovnn.contentconveyor.model.content.Content;
import ru.panyukovnn.contentconveyor.model.content.ContentType;
import ru.panyukovnn.contentconveyor.model.content.Lang;
import ru.panyukovnn.contentconveyor.model.Source;
import ru.panyukovnn.contentconveyor.testutils.TestFileUtil;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HabrLoaderTest extends AbstractTest {

    @Test
    @Transactional
    public void when_findDataToLoad_then_success() throws IOException {
        String link = "https://habr.com/ru/articles/123456/";
        String habrArticlePage = TestFileUtil.readFileFromResources("mockdata/service/loader/habr/habr-article.html");

        Document habrArticlePageDoc = Jsoup.parse(habrArticlePage);

        try (MockedStatic<Jsoup> jsoupMock = Mockito.mockStatic(Jsoup.class)) {
            Connection connectionMock = Mockito.mock(Connection.class);
            jsoupMock.when(() -> Jsoup.connect(link))
                .thenReturn(connectionMock);
            Mockito.when(connectionMock.userAgent("Mozilla/5.0")).thenReturn(connectionMock);
            Mockito.when(connectionMock.get()).thenReturn(habrArticlePageDoc);

            Content content = habrLoader.load(link);

            Content dbContent = contentRepository.findByLink(link)
                .orElseThrow();

            assertAll(
                () -> assertEquals(dbContent, content),
                () -> assertEquals(link, content.getLink()),
                () -> assertEquals(Lang.RU, content.getLang()),
                () -> assertEquals(ContentType.ARTICLE, content.getType()),
                () -> assertEquals(Source.JAVA_HABR, content.getSource()),
                () -> assertEquals("Распределённые транзакции", content.getTitle()),
                () -> assertEquals(LocalDateTime.of(2023, 10, 22, 11, 31, 4), content.getPublicationDate()),
                () -> assertNotNull(content.getContent()),
                () -> assertTrue(content.getContent().contains("На собеседованиях на позицию middle/senior разработчика часто задают вопросы по распределенным транзакциям в микросервисной архитектуре."))
            );
        }
    }
}