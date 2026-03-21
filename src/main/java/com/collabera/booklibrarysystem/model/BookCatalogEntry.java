package com.collabera.booklibrarysystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.Instant;

@Entity
@Table(
    name = "book_catalog_entries",
    uniqueConstraints = @UniqueConstraint(name = "uk_book_catalog_entries_isbn", columnNames = "isbn")
)
public class BookCatalogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String isbn;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Version
    @Column(name = "row_version", nullable = false)
    private Long rowVersion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected BookCatalogEntry() {
    }

    public BookCatalogEntry(String isbn, String title, String author, Instant createdAt) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.createdAt = createdAt;
    }

    public boolean matchesCatalogDetails(String candidateTitle, String candidateAuthor) {
        return title.equalsIgnoreCase(candidateTitle) && author.equalsIgnoreCase(candidateAuthor);
    }

    public Long getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public Long getRowVersion() {
        return rowVersion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
