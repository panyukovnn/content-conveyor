package ru.panyukovnn.contentconveyor.model.parsingjobinfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceDetails implements Serializable {

    private String tgChatNamePart;
    private String tgTopicNamePart;
}
