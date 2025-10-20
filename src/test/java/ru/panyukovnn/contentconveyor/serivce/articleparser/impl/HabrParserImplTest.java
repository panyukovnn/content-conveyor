package ru.panyukovnn.contentconveyor.serivce.articleparser.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.panyukovnn.contentconveyor.model.content.Content;
import ru.panyukovnn.contentconveyor.serivce.autodatafinder.impl.HabrDataFinder;
import ru.panyukovnn.contentconveyor.serivce.domain.ContentDomainService;
import ru.panyukovnn.contentconveyor.serivce.loader.impl.HabrLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HabrParserImplTest {

    @Mock
    private HabrLoader habrLoader;
    @Mock
    private HabrDataFinder habrDataFinder;
    @Mock
    private ContentDomainService contentDomainService;

    @InjectMocks
    private HabrParserImpl habrParserImpl;

    @Test
    void when_defineLinksToLoad_withLastContent_then_success() {
        var foundedLinks = List.of(
            "link1",
            "link2",
            "link3",
            "link4"
        );

        var lastContent = new Content();
        lastContent.setLink("link3");

        var result = habrParserImpl.defineLinksToLoad(foundedLinks, lastContent);

        assertEquals(2, result.size());
        assertEquals("link1", result.get(0));
        assertEquals("link2", result.get(1));
    }

    @Test
    void when_defineLinksToLoad_withNoLastContent_then_success() {
        var foundedLinks = List.of(
            "link1",
            "link2",
            "link3",
            "link4"
        );

        var result = habrParserImpl.defineLinksToLoad(foundedLinks, null);

        assertEquals(foundedLinks, result);
    }
}