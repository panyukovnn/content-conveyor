package ru.panyukovnn.contentconveyor.serivce;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.panyukovnn.contentconveyor.model.PublishingChannel;
import ru.panyukovnn.contentconveyor.serivce.domain.PublishingChannelDomainService;
import ru.panyukovnn.contentconveyor.serivce.telegram.TgSender;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublishingChannelCheckService {

    private final TgSender tgSender;
    private final PublishingChannelDomainService publishingChannelDomainService;

    public void checkPublishingChannel(UUID publishingChannelId) {
        PublishingChannel publishingChannel = publishingChannelDomainService.findById(publishingChannelId)
            .orElseThrow();

        tgSender.sendMessage(publishingChannel, "Тест");
    }
}
