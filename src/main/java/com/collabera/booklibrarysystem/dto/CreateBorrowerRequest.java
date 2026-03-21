package com.collabera.booklibrarysystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload used to register a borrower.")
public record CreateBorrowerRequest(
    @Schema(example = "Alice Johnson")
    @NotBlank(message = "Borrower name is required.")
    @Size(max = 100, message = "Borrower name must be at most 100 characters.")
    String name,

    @Schema(example = "alice@example.com")
    @NotBlank(message = "Borrower email is required.")
    @Email(message = "Borrower email must be a valid email address.")
    @Size(max = 320, message = "Borrower email must be at most 320 characters.")
    String email
) {
}
