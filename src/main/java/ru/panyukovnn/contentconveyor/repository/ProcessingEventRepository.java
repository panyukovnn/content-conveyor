package ru.panyukovnn.contentconveyor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.panyukovnn.contentconveyor.model.event.ProcessingEvent;
import ru.panyukovnn.contentconveyor.model.event.ProcessingEventType;

import java.util.List;
import java.util.UUID;

public interface ProcessingEventRepository extends JpaRepository<ProcessingEvent, UUID> {

    boolean existsByContentId(UUID contentId);

    List<ProcessingEvent> findAllByTypeIn(List<ProcessingEventType> processingEventTypes);
}
