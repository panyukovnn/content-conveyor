package ru.panyukovnn.contentconveyor.serivce.eventprocessor;

import ru.panyukovnn.contentconveyor.model.event.ProcessingEvent;
import ru.panyukovnn.contentconveyor.model.event.ProcessingEventType;

public interface EventProcessor {

    void process(ProcessingEvent processingEvent);

    ProcessingEventType getProcessingEventType();
}
