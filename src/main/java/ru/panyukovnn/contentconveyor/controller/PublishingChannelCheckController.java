package ru.panyukovnn.contentconveyor.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.panyukovnn.contentconveyor.serivce.PublishingChannelCheckService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/publishingChannels/check")
public class PublishingChannelCheckController {

    private final PublishingChannelCheckService publishingChannelCheckService;

    @PostMapping("/{publishingChannelId}")
    public void postPublicationChannelCheck(@PathVariable("publishingChannelId") UUID publishingChannelId) {
        publishingChannelCheckService.checkPublishingChannel(publishingChannelId);
    }
}
