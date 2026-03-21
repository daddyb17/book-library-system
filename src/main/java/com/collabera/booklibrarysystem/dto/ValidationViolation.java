package com.collabera.booklibrarysystem.dto;

public record ValidationViolation(
    String field,
    String message
) {
}
