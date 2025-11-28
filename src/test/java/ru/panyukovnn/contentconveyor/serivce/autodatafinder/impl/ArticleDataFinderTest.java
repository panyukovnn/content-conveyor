package ru.panyukovnn.contentconveyor.serivce.autodatafinder.impl;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import ru.panyukovnn.contentconveyor.AbstractTest;
import ru.panyukovnn.contentconveyor.testutils.TestFileUtil;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleDataFinderTest extends AbstractTest {

    @Test
    public void when_findDataToLoad_then_success() throws IOException {
        String habrJavaHubPage = TestFileUtil.readFileFromResources("mockdata/service/autodatafinder/habr/habr-java-articles-hub.html");

        Document javaHubPageDoc = Jsoup.parse(habrJavaHubPage);

        try (MockedStatic<Jsoup> jsoupMock = Mockito.mockStatic(Jsoup.class)) {
            Connection connectionMock = Mockito.mock(Connection.class);
            jsoupMock.when(() -> Jsoup.connect("https://habr.com/ru/hubs/java/articles/"))
                .thenReturn(connectionMock);
            Mockito.when(connectionMock.userAgent("Mozilla/5.0")).thenReturn(connectionMock);
            Mockito.when(connectionMock.get()).thenReturn(javaHubPageDoc);

            List<String> articleLinks = habrDataFinder.findDataToLoad();

            assertThat(articleLinks)
                .hasSize(20)
                .contains("https://habr.com/ru/articles/971350/",
                    "https://habr.com/ru/companies/otus/articles/971192/",
                    "https://habr.com/ru/articles/970730/",
                    "https://habr.com/ru/companies/ru_mts/articles/970176/",
                    "https://habr.com/ru/companies/compo/articles/970776/",
                    "https://habr.com/ru/companies/haulmont/articles/970556/",
                    "https://habr.com/ru/articles/970388/",
                    "https://habr.com/ru/articles/969926/",
                    "https://habr.com/ru/articles/969820/",
                    "https://habr.com/ru/articles/969730/",
                    "https://habr.com/ru/articles/969386/",
                    "https://habr.com/ru/companies/timeweb/articles/966650/",
                    "https://habr.com/ru/companies/pvs-studio/articles/969540/",
                    "https://habr.com/ru/articles/969410/",
                    "https://habr.com/ru/companies/axiomjdk/articles/969344/",
                    "https://habr.com/ru/articles/968968/",
                    "https://habr.com/ru/companies/spring_aio/articles/968898/",
                    "https://habr.com/ru/articles/968844/",
                    "https://habr.com/ru/articles/968808/",
                    "https://habr.com/ru/companies/otus/articles/968028/");
        }
    }
}