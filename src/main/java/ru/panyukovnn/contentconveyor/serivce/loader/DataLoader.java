package ru.panyukovnn.contentconveyor.serivce.loader;

import ru.panyukovnn.contentconveyor.model.content.Content;
import ru.panyukovnn.contentconveyor.model.Source;

public interface DataLoader {

    Content load(String link);

    Source getSource();
}
