package ru.panyukovnn.contentconveyor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.panyukovnn.contentconveyor.model.PublishingChannel;

import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource
public interface PublishingChannelRepository extends JpaRepository<PublishingChannel, UUID> {

    Optional<PublishingChannel> findByExternalId(String externalId);
}
