package com.collabera.booklibrarysystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Borrow or return operation details.")
public record LoanResponse(
    Long loanId,
    Long borrowerId,
    Long bookId,
    Instant operationTimestamp,
    String status
) {
}
