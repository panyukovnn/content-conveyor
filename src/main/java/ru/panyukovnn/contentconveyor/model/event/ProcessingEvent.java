package ru.panyukovnn.contentconveyor.model.event;

import jakarta.persistence.*;
import lombok.*;
import ru.panyukovnn.contentconveyor.model.AuditableEntity;
import ru.panyukovnn.contentconveyor.model.ConveyorType;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "processing_events")
public class ProcessingEvent extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Тип события
     */
    @Enumerated(EnumType.STRING)
    private ProcessingEventType type;
    /**
     * Идентификатор материала
     */
    private UUID contentId;
    /**
     * Тип конвейера
     */
    @Enumerated(EnumType.STRING)
    private ConveyorType conveyorType;
    /**
     * Идентификатор промта
     */
    private UUID promptId;
    /**
     * Идентификатор группы контента
     */
    private UUID contentBatchId;
    /**
     * Идентификатор канала отправки
     */
    private UUID publishingChannelId;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProcessingEvent processingEvent = (ProcessingEvent) o;
        return Objects.equals(id, processingEvent.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
