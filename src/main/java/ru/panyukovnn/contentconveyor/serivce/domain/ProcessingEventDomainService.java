package ru.panyukovnn.contentconveyor.serivce.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.panyukovnn.contentconveyor.model.event.ProcessingEvent;
import ru.panyukovnn.contentconveyor.model.event.ProcessingEventType;
import ru.panyukovnn.contentconveyor.repository.ProcessingEventRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessingEventDomainService {

    private final ProcessingEventRepository processingEventRepository;

    public ProcessingEvent save(ProcessingEvent processingEvent) {
        return processingEventRepository.save(processingEvent);
    }

    public boolean existsByContentId(UUID contentId) {
        return processingEventRepository.existsByContentId(contentId);
    }

    public List<ProcessingEvent> findAllByTypeIn(List<ProcessingEventType> nonTerminalProcessingEventTypes) {
        return processingEventRepository.findAllByTypeIn(nonTerminalProcessingEventTypes);
    }

    public void delete(ProcessingEvent processingEvent) {
        processingEventRepository.delete(processingEvent);
    }
}
