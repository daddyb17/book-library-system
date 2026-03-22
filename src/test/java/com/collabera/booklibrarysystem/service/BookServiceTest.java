package com.collabera.booklibrarysystem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.collabera.booklibrarysystem.dto.CreateBookRequest;
import com.collabera.booklibrarysystem.exception.BadRequestException;
import com.collabera.booklibrarysystem.exception.BusinessConflictException;
import com.collabera.booklibrarysystem.model.Book;
import com.collabera.booklibrarysystem.model.BookCatalogEntry;
import com.collabera.booklibrarysystem.repository.BookCatalogEntryRepository;
import com.collabera.booklibrarysystem.repository.BookRepository;
import com.collabera.booklibrarysystem.repository.LoanRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookCatalogEntryRepository bookCatalogEntryRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LoanRepository loanRepository;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-03-21T10:15:30Z"), ZoneOffset.UTC);
        bookService = new BookService(bookCatalogEntryRepository, bookRepository, loanRepository, fixedClock);
    }

    @Test
    void registerBookRejectsDifferentCatalogDetailsForSameIsbn() {
        when(bookCatalogEntryRepository.findByIsbn("9780132350884"))
            .thenReturn(Optional.of(new BookCatalogEntry("9780132350884", "Clean Code", "Robert C. Martin", Instant.now())));

        assertThatThrownBy(() -> bookService.registerBook(
            new CreateBookRequest("9780132350884", "Clean Architecture", "Robert C. Martin")
        ))
            .isInstanceOf(BusinessConflictException.class)
            .hasMessageContaining("9780132350884");
    }

    @Test
    void registerBookReusesExistingCatalogDetailsForAdditionalCopies() {
        BookCatalogEntry existingCatalogEntry = new BookCatalogEntry("9780132350884", "Clean Code", "Robert C. Martin", Instant.now());
        when(bookCatalogEntryRepository.findByIsbn("9780132350884")).thenReturn(Optional.of(existingCatalogEntry));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book savedBook = bookService.registerBook(
            new CreateBookRequest("978-0-13-235088-4", "clean code", "robert c. martin")
        );

        assertThat(savedBook.getIsbn()).isEqualTo("9780132350884");
        assertThat(savedBook.getTitle()).isEqualTo("Clean Code");
        assertThat(savedBook.getAuthor()).isEqualTo("Robert C. Martin");
        assertThat(savedBook.getCreatedAt()).isEqualTo(Instant.parse("2026-03-21T10:15:30Z"));
    }

    @Test
    void registerBookAcceptsConcurrentEquivalentCatalogInsert() {
        BookCatalogEntry existingCatalogEntry = new BookCatalogEntry("9780132350884", "Clean Code", "Robert C. Martin", Instant.now());
        when(bookCatalogEntryRepository.findByIsbn("9780132350884"))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(existingCatalogEntry));
        when(bookCatalogEntryRepository.saveAndFlush(any(BookCatalogEntry.class)))
            .thenThrow(new DataIntegrityViolationException("duplicate key"));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book savedBook = bookService.registerBook(
            new CreateBookRequest("9780132350884", "Clean Code", "Robert C. Martin")
        );

        assertThat(savedBook.getIsbn()).isEqualTo("9780132350884");
        assertThat(savedBook.getTitle()).isEqualTo("Clean Code");
    }

    @Test
    void registerBookRejectsConcurrentConflictingCatalogInsert() {
        BookCatalogEntry existingCatalogEntry = new BookCatalogEntry("9780132350884", "Clean Code", "Robert C. Martin", Instant.now());
        when(bookCatalogEntryRepository.findByIsbn("9780132350884"))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(existingCatalogEntry));
        when(bookCatalogEntryRepository.saveAndFlush(any(BookCatalogEntry.class)))
            .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> bookService.registerBook(
            new CreateBookRequest("9780132350884", "Clean Architecture", "Robert C. Martin")
        ))
            .isInstanceOf(BusinessConflictException.class)
            .hasMessageContaining("9780132350884");
    }

    @Test
    void registerBookRejectsInvalidIsbn() {
        assertThatThrownBy(() -> bookService.registerBook(
            new CreateBookRequest("not-an-isbn", "Clean Code", "Robert C. Martin")
        ))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("ISBN must be a valid ISBN-10 or ISBN-13.");

        verifyNoInteractions(bookCatalogEntryRepository, bookRepository);
    }
}
