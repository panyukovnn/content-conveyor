package ru.panyukovnn.contentconveyor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.panyukovnn.contentconveyor.model.Prompt;

import java.util.UUID;

@RepositoryRestResource
public interface PromptRepository extends JpaRepository<Prompt, UUID> {

}
