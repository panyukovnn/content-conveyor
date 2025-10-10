package ru.panyukovnn.contentconveyor.serivce.articleparser.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.panyukovnn.contentconveyor.model.Source;
import ru.panyukovnn.contentconveyor.model.content.Content;
import ru.panyukovnn.contentconveyor.serivce.articleparser.ArticleParser;
import ru.panyukovnn.contentconveyor.serivce.autodatafinder.impl.HabrDataFinder;
import ru.panyukovnn.contentconveyor.serivce.domain.ContentDomainService;
import ru.panyukovnn.contentconveyor.serivce.loader.impl.HabrLoader;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class HabrParserImpl implements ArticleParser {

    private final HabrLoader habrLoader;
    private final HabrDataFinder habrDataFinder;
    private final ContentDomainService contentDomainService;

    @Override
    public List<Content> loadNewContent() {
        try {
            List<String> foundedLinks = habrDataFinder.findDataToLoad();

            Content lastContent = contentDomainService.findTopBySourceOrderByPublicationDateDesc(Source.JAVA_HABR)
                .orElse(null);
            List<String> linksToLoad = defineLinksToLoad(foundedLinks, lastContent);

            return linksToLoad.stream()
                .flatMap(link -> {
                    try {
                        return Stream.of(habrLoader.load(link));
                    } catch (Exception e) {
                        log.error("Ошибка при загрузке содержимого статьи с habr: " + e.getMessage(), e);
                    }

                    return Stream.empty();
                })
                .toList();
        } catch (Exception e) {
            log.error("Ошибка при парсинге статей: {}", e.getMessage(), e);
        }

        return List.of();
    }

    @Override
    public Source getSource() {
        return Source.JAVA_HABR;
    }

    protected List<String> defineLinksToLoad(List<String> foundedLinks, Content lastContent) {
        if (lastContent != null && foundedLinks.contains(lastContent.getLink())) {
            return foundedLinks.stream()
                .dropWhile(link -> !link.equals(lastContent.getLink()))
                .skip(1)
                .toList();
        }

        return foundedLinks;
    }
}
