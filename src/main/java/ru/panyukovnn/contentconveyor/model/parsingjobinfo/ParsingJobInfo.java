package ru.panyukovnn.contentconveyor.model.parsingjobinfo;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.panyukovnn.contentconveyor.model.*;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "parsing_job_info")
public class ParsingJobInfo extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Источник информации
     */
    @Enumerated(EnumType.STRING)
    private Source source;
    /**
     * Частота запуска
     */
    @Enumerated(EnumType.STRING)
    private ParsingFrequency frequency;
    /**
     * Подробности об источнике (отличается в зависимости от источника)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private SourceDetails sourceDetails;
    /**
     * Тип конвейера
     */
    @Enumerated(EnumType.STRING)
    private ConveyorType conveyorType;
    /**
     * Промт
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private Prompt prompt;
    /**
     * Канал публикации
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private PublishingChannel publishingChannel;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ParsingJobInfo content = (ParsingJobInfo) o;
        return Objects.equals(id, content.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
