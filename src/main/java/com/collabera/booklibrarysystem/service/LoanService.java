package com.collabera.booklibrarysystem.service;

import com.collabera.booklibrarysystem.dto.LoanResponse;
import com.collabera.booklibrarysystem.exception.BusinessConflictException;
import com.collabera.booklibrarysystem.exception.ResourceNotFoundException;
import com.collabera.booklibrarysystem.model.Book;
import com.collabera.booklibrarysystem.model.Borrower;
import com.collabera.booklibrarysystem.model.Loan;
import com.collabera.booklibrarysystem.repository.LoanRepository;
import com.collabera.booklibrarysystem.util.ResponseMapper;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LoanService {

    private final BorrowerService borrowerService;
    private final BookService bookService;
    private final LoanRepository loanRepository;
    private final Clock clock;

    public LoanService(
        BorrowerService borrowerService,
        BookService bookService,
        LoanRepository loanRepository,
        Clock clock
    ) {
        this.borrowerService = borrowerService;
        this.bookService = bookService;
        this.loanRepository = loanRepository;
        this.clock = clock;
    }

    @Transactional
    public LoanResponse borrowBook(Long borrowerId, Long bookId) {
        Borrower borrower = borrowerService.getBorrowerOrThrow(borrowerId);

        // Lock the book row so concurrent borrow or return operations cannot create two active loans.
        Book book = bookService.getBookForUpdateOrThrow(bookId);
        if (loanRepository.findByBook_IdAndReturnedAtIsNull(book.getId()).isPresent()) {
            throw new BusinessConflictException("Book %d is already borrowed.".formatted(bookId));
        }

        Loan loan = loanRepository.save(new Loan(borrower, book, Instant.now(clock)));
        return ResponseMapper.toActiveLoanResponse(loan);
    }

    @Transactional
    public LoanResponse returnBook(Long borrowerId, Long bookId) {
        borrowerService.getBorrowerOrThrow(borrowerId);
        bookService.getBookForUpdateOrThrow(bookId);

        Loan loan = loanRepository.findActiveLoanForUpdate(borrowerId, bookId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Active loan not found for borrower %d and book %d".formatted(borrowerId, bookId)
            ));

        loan.markReturned(Instant.now(clock));
        return ResponseMapper.toReturnedLoanResponse(loan);
    }
}
