package com.collabera.booklibrarysystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Borrower details.")
public record BorrowerResponse(
    Long id,
    String name,
    String email,
    Instant createdAt
) {
}
