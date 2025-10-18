package ru.panyukovnn.contentconveyor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.panyukovnn.contentconveyor.model.publishingchannels.PublishingChannel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource
public interface PublishingChannelRepository extends JpaRepository<PublishingChannel, UUID> {

    Optional<PublishingChannel> findByExternalId(String externalId);

    List<PublishingChannel> findByPublishingChannelSetsId(UUID publishingChannelSetsId);
}
