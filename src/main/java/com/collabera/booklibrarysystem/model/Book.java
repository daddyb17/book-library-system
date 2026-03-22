package com.collabera.booklibrarysystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "catalog_entry_id", nullable = false)
    private BookCatalogEntry catalogEntry;

    @Version
    @Column(name = "row_version", nullable = false)
    private Long rowVersion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Book(BookCatalogEntry catalogEntry, Instant createdAt) {
        this.catalogEntry = catalogEntry;
        this.createdAt = createdAt;
    }

    public String getIsbn() {
        return catalogEntry.getIsbn();
    }

    public String getTitle() {
        return catalogEntry.getTitle();
    }

    public String getAuthor() {
        return catalogEntry.getAuthor();
    }
}
