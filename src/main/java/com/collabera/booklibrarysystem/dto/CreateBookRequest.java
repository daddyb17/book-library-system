package com.collabera.booklibrarysystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload used to register a book copy.")
public record CreateBookRequest(
    @Schema(example = "9780132350884")
    @NotBlank(message = "ISBN is required.")
    @Size(max = 20, message = "ISBN must be at most 20 characters.")
    String isbn,

    @Schema(example = "Clean Code")
    @NotBlank(message = "Book title is required.")
    @Size(max = 255, message = "Book title must be at most 255 characters.")
    String title,

    @Schema(example = "Robert C. Martin")
    @NotBlank(message = "Book author is required.")
    @Size(max = 255, message = "Book author must be at most 255 characters.")
    String author
) {
}
