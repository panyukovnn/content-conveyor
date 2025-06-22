package ru.panyukovnn.contentconveyor.serivce.articleparser;

import ru.panyukovnn.contentconveyor.model.Source;
import ru.panyukovnn.contentconveyor.model.content.Content;

import java.util.List;

public interface ArticleParser {

    List<Content> loadNewContent();

    Source getSource();
}
