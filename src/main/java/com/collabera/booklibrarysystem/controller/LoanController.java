package com.collabera.booklibrarysystem.controller;

import com.collabera.booklibrarysystem.dto.LoanResponse;
import com.collabera.booklibrarysystem.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/borrowers/{borrowerId}/borrowed-books")
@Tag(name = "Loans", description = "Borrow and return operations on behalf of a borrower.")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/{bookId}")
    @Operation(
        summary = "Borrow a book copy",
        responses = {
            @ApiResponse(responseCode = "201", description = "Book borrowed successfully"),
            @ApiResponse(
                responseCode = "404",
                description = "Borrower or book was not found",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Book copy is already borrowed",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
        }
    )
    public ResponseEntity<LoanResponse> borrowBook(@PathVariable Long borrowerId, @PathVariable Long bookId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.borrowBook(borrowerId, bookId));
    }

    @DeleteMapping("/{bookId}")
    @Operation(
        summary = "Return a borrowed book copy",
        responses = {
            @ApiResponse(responseCode = "200", description = "Book returned successfully"),
            @ApiResponse(
                responseCode = "404",
                description = "Borrower, book, or active loan was not found",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
        }
    )
    public ResponseEntity<LoanResponse> returnBook(@PathVariable Long borrowerId, @PathVariable Long bookId) {
        return ResponseEntity.ok(loanService.returnBook(borrowerId, bookId));
    }
}
