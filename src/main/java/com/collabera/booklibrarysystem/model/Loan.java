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
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "borrower_id", nullable = false)
    private Borrower borrower;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "borrowed_at", nullable = false)
    private Instant borrowedAt;

    @Column(name = "returned_at")
    private Instant returnedAt;

    @Column(name = "active_loan_book_id")
    private Long activeLoanBookId;

    @Version
    @Column(name = "row_version", nullable = false)
    private Long rowVersion;

    public Loan(Borrower borrower, Book book, Instant borrowedAt) {
        this.borrower = borrower;
        this.book = book;
        this.borrowedAt = borrowedAt;
        this.activeLoanBookId = book.getId();
    }

    public void markReturned(Instant returnedAt) {
        this.returnedAt = returnedAt;
        this.activeLoanBookId = null;
    }

    public boolean isActive() {
        return returnedAt == null;
    }
}
