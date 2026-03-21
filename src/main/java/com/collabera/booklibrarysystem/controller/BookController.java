package com.collabera.booklibrarysystem.controller;

import com.collabera.booklibrarysystem.dto.BookResponse;
import com.collabera.booklibrarysystem.dto.CreateBookRequest;
import com.collabera.booklibrarysystem.dto.PageResponse;
import com.collabera.booklibrarysystem.service.BookService;
import com.collabera.booklibrarysystem.util.ResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/books")
@Tag(name = "Books", description = "Book registration and listing endpoints.")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    @Operation(
        summary = "Register a book copy",
        responses = {
            @ApiResponse(responseCode = "201", description = "Book copy registered successfully"),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid book payload",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                responseCode = "409",
                description = "ISBN conflicts with an existing catalog entry",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
        }
    )
    public ResponseEntity<BookResponse> registerBook(@Valid @RequestBody CreateBookRequest request) {
        BookResponse response = ResponseMapper.toResponse(bookService.registerBook(request), true);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
        summary = "List books",
        responses = {
            @ApiResponse(responseCode = "200", description = "Books retrieved successfully"),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid pagination or sort parameters",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
        }
    )
    public PageResponse<BookResponse> listBooks(
        @Parameter(description = "Zero-based page index", example = "0")
        @RequestParam(defaultValue = "0")
        @Min(value = 0, message = "Page must be zero or greater.")
        int page,
        @Parameter(description = "Page size between 1 and 100", example = "20")
        @RequestParam(defaultValue = "20")
        @Min(value = 1, message = "Page size must be at least 1.")
        @Max(value = 100, message = "Page size must be at most 100.")
        int size,
        @Parameter(
            description = "Sort expression in the format '<field>,<direction>'. Allowed fields: id, isbn, title, author, createdAt.",
            example = "title,asc"
        )
        @RequestParam(defaultValue = "title,asc")
        String sort
    ) {
        return bookService.listBooks(page, size, sort);
    }
}
