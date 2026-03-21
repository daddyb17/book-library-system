package com.collabera.booklibrarysystem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.collabera.booklibrarysystem.dto.LoanResponse;
import com.collabera.booklibrarysystem.exception.BusinessConflictException;
import com.collabera.booklibrarysystem.exception.ResourceNotFoundException;
import com.collabera.booklibrarysystem.model.Book;
import com.collabera.booklibrarysystem.model.BookCatalogEntry;
import com.collabera.booklibrarysystem.model.Borrower;
import com.collabera.booklibrarysystem.model.Loan;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private BorrowerService borrowerService;

    @Mock
    private BookService bookService;

    @Mock
    private LoanRepository loanRepository;

    private LoanService loanService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-03-21T10:15:30Z"), ZoneOffset.UTC);
        loanService = new LoanService(borrowerService, bookService, loanRepository, fixedClock);
    }

    @Test
    void borrowBookRejectsAlreadyBorrowedCopy() {
        Borrower borrower = new Borrower("Alice", "alice@example.com", Instant.now());
        Book book = new Book(new BookCatalogEntry("9780132350884", "Clean Code", "Robert C. Martin", Instant.now()), Instant.now());
        ReflectionTestUtils.setField(book, "id", 2L);

        when(borrowerService.getBorrowerOrThrow(1L)).thenReturn(borrower);
        when(bookService.getBookForUpdateOrThrow(2L)).thenReturn(book);
        when(loanRepository.findByBook_IdAndReturnedAtIsNull(2L)).thenReturn(Optional.of(new Loan(borrower, book, Instant.now())));

        assertThatThrownBy(() -> loanService.borrowBook(1L, 2L))
            .isInstanceOf(BusinessConflictException.class)
            .hasMessageContaining("2");
    }

    @Test
    void returnBookRejectsMissingActiveLoan() {
        when(borrowerService.getBorrowerOrThrow(1L)).thenReturn(new Borrower("Alice", "alice@example.com", Instant.now()));
        when(bookService.getBookForUpdateOrThrow(2L)).thenReturn(
            new Book(new BookCatalogEntry("9780132350884", "Clean Code", "Robert C. Martin", Instant.now()), Instant.now())
        );
        when(loanRepository.findActiveLoanForUpdate(1L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.returnBook(1L, 2L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Active loan not found");
    }

    @Test
    void borrowBookReturnsActiveLoanResponse() {
        Borrower borrower = new Borrower("Alice", "alice@example.com", Instant.now());
        Book book = new Book(new BookCatalogEntry("9780132350884", "Clean Code", "Robert C. Martin", Instant.now()), Instant.now());
        Loan savedLoan = new Loan(borrower, book, Instant.parse("2026-03-21T10:15:30Z"));
        ReflectionTestUtils.setField(borrower, "id", 1L);
        ReflectionTestUtils.setField(book, "id", 2L);
        ReflectionTestUtils.setField(savedLoan, "id", 10L);

        when(borrowerService.getBorrowerOrThrow(1L)).thenReturn(borrower);
        when(bookService.getBookForUpdateOrThrow(2L)).thenReturn(book);
        when(loanRepository.findByBook_IdAndReturnedAtIsNull(2L)).thenReturn(Optional.empty());
        when(loanRepository.save(org.mockito.ArgumentMatchers.any(Loan.class))).thenReturn(savedLoan);

        LoanResponse response = loanService.borrowBook(1L, 2L);

        assertThat(response.operationTimestamp()).isEqualTo(Instant.parse("2026-03-21T10:15:30Z"));
        assertThat(response.status()).isEqualTo("ACTIVE");
    }
}
