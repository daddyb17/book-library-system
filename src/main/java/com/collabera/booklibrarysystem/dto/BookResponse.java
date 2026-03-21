package com.collabera.booklibrarysystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Book copy details.")
public record BookResponse(
    Long id,
    String isbn,
    String title,
    String author,
    boolean available,
    Instant createdAt
) {
}
