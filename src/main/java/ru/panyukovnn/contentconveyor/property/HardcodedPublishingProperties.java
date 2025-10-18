package ru.panyukovnn.contentconveyor.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "retelling.hardcoded-publishing-channels")
public class HardcodedPublishingProperties {

    private Long chatId;
    private Long rateTgTopicId;

    private Long debugChatId;
    private Long debugTopicId;

    private Long javaDzoneTopicId;
    private Long javaMediumTopicId;
    private Long tgMessageBatchTopicId;

}
