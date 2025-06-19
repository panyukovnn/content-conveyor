package ru.panyukovnn.contentconveyor.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.panyukovnn.contentconveyor.model.parsingjobinfo.ParsingFrequency;
import ru.panyukovnn.contentconveyor.model.parsingjobinfo.ParsingJobInfo;

import java.util.List;
import java.util.UUID;

@RepositoryRestResource
public interface ParsingJobInfoRepository extends JpaRepository<ParsingJobInfo, UUID> {

    @EntityGraph(attributePaths = {"prompt", "publishingChannel"})
    List<ParsingJobInfo> findByFrequency(ParsingFrequency frequency);
}
