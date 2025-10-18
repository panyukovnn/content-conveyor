package ru.panyukovnn.contentconveyor.serivce.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.panyukovnn.contentconveyor.model.publishingchannels.PublishingChannel;
import ru.panyukovnn.contentconveyor.repository.PublishingChannelRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublishingChannelDomainService {

    private final PublishingChannelRepository publishingChannelRepository;

    public PublishingChannel save(PublishingChannel publishingChannel) {
        return publishingChannelRepository.save(publishingChannel);
    }

    public Optional<PublishingChannel> findByExternalId(String externalId) {
        return publishingChannelRepository.findByExternalId(externalId);
    }

    public List<PublishingChannel> findByPublishingChannelSet(UUID publishingChannelSetsId) {
        return publishingChannelRepository.findByPublishingChannelSetsId(publishingChannelSetsId);
    }

    public Optional<PublishingChannel> findById(UUID publishingChannelId) {
        return publishingChannelRepository.findById(publishingChannelId);
    }
}
