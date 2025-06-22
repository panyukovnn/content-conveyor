package ru.panyukovnn.contentconveyor.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum Source {

    JAVA_HABR(true),
    JAVA_MEDIUM(true),
    JAVA_DZONE(true),
    TG(false);

    private final boolean isArticle;

    public static List<Source> getArticleSources() {
        return Arrays.stream(Source.values())
            .filter(Source::isArticle)
            .toList();
    }
}
