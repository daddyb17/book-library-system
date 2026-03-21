package com.collabera.booklibrarysystem.repository;

import com.collabera.booklibrarysystem.model.BookCatalogEntry;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookCatalogEntryRepository extends JpaRepository<BookCatalogEntry, Long> {

    Optional<BookCatalogEntry> findByIsbn(String isbn);
}
