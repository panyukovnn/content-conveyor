package ru.panyukovnn.contentconveyor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.panyukovnn.contentconveyor.model.content.ContentRate;

import java.util.UUID;

public interface ContentRateRepository extends JpaRepository<ContentRate, UUID> {
}
