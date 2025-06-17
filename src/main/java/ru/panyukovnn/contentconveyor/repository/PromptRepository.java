package ru.panyukovnn.contentconveyor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.panyukovnn.contentconveyor.model.Prompt;

import java.util.UUID;

public interface PromptRepository extends JpaRepository<Prompt, UUID> {

}
