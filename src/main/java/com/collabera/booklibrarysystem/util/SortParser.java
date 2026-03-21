package com.collabera.booklibrarysystem.util;

import com.collabera.booklibrarysystem.exception.BadRequestException;
import java.util.Map;
import org.springframework.data.domain.Sort;

public final class SortParser {

    private SortParser() {
    }

    public static Sort parse(String sortExpression, Map<String, String> allowedFields) {
        String[] parts = sortExpression.split(",");
        if (parts.length != 2) {
            throw new BadRequestException("Sort must be provided in the format '<field>,<direction>'.");
        }

        String field = parts[0].trim();
        String directionToken = parts[1].trim();
        String mappedField = allowedFields.get(field);
        if (mappedField == null) {
            throw new BadRequestException("Unsupported sort field: %s".formatted(field));
        }

        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(directionToken);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Unsupported sort direction: %s".formatted(directionToken));
        }

        return Sort.by(direction, mappedField);
    }
}
