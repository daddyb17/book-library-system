package com.collabera.booklibrarysystem.util;

import com.collabera.booklibrarysystem.dto.BookResponse;
import com.collabera.booklibrarysystem.dto.BorrowerResponse;
import com.collabera.booklibrarysystem.dto.LoanResponse;
import com.collabera.booklibrarysystem.dto.PageResponse;
import com.collabera.booklibrarysystem.model.Book;
import com.collabera.booklibrarysystem.model.Borrower;
import com.collabera.booklibrarysystem.model.Loan;
import java.util.List;
import org.springframework.data.domain.Page;

public final class ResponseMapper {

    private ResponseMapper() {
    }

    public static BorrowerResponse toResponse(Borrower borrower) {
        return new BorrowerResponse(
            borrower.getId(),
            borrower.getName(),
            borrower.getEmail(),
            borrower.getCreatedAt()
        );
    }

    public static BookResponse toResponse(Book book, boolean available) {
        return new BookResponse(
            book.getId(),
            book.getIsbn(),
            book.getTitle(),
            book.getAuthor(),
            available,
            book.getCreatedAt()
        );
    }

    public static LoanResponse toActiveLoanResponse(Loan loan) {
        return new LoanResponse(
            loan.getId(),
            loan.getBorrower().getId(),
            loan.getBook().getId(),
            loan.getBorrowedAt(),
            "ACTIVE"
        );
    }

    public static LoanResponse toReturnedLoanResponse(Loan loan) {
        return new LoanResponse(
            loan.getId(),
            loan.getBorrower().getId(),
            loan.getBook().getId(),
            loan.getReturnedAt(),
            "RETURNED"
        );
    }

    public static <T> PageResponse<T> toPageResponse(Page<T> page, List<String> sort) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            sort
        );
    }
}
