package com.collabera.booklibrarysystem.exception;

import com.collabera.booklibrarysystem.dto.ValidationViolation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Resource not found", exception.getMessage(), request);
    }

    @ExceptionHandler({BusinessConflictException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ProblemDetail> handleConflict(Exception exception, HttpServletRequest request) {
        String detail = exception instanceof DataIntegrityViolationException
            ? "The request violated a database constraint."
            : exception.getMessage();
        return buildResponse(HttpStatus.CONFLICT, "Request conflict", detail, request);
    }

    @ExceptionHandler({
        BadRequestException.class,
        ConstraintViolationException.class,
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ProblemDetail> handleBadRequest(Exception exception, HttpServletRequest request) {
        ProblemDetail problem = createProblem(HttpStatus.BAD_REQUEST, "Bad request", extractDetail(exception), request);
        if (exception instanceof ConstraintViolationException constraintViolationException) {
            List<ValidationViolation> violations = constraintViolationException.getConstraintViolations().stream()
                .map(violation -> new ValidationViolation(violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();
            problem.setProperty("violations", violations);
        }
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationFailure(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        ProblemDetail problem = createProblem(
            HttpStatus.BAD_REQUEST,
            "Validation failed",
            "The request payload contains invalid values.",
            request
        );

        List<ValidationViolation> violations = exception.getBindingResult().getFieldErrors().stream()
            .map(this::toViolation)
            .toList();
        problem.setProperty("violations", violations);

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception exception, HttpServletRequest request) {
        return buildResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal server error",
            "An unexpected error occurred while processing the request.",
            request
        );
    }

    private ResponseEntity<ProblemDetail> buildResponse(
        HttpStatus status,
        String title,
        String detail,
        HttpServletRequest request
    ) {
        return ResponseEntity.status(status).body(createProblem(status, title, detail, request));
    }

    private ProblemDetail createProblem(
        HttpStatus status,
        String title,
        String detail,
        HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    private String extractDetail(Exception exception) {
        if (exception instanceof ConstraintViolationException) {
            return "One or more request parameters are invalid.";
        }
        if (exception instanceof HttpMessageNotReadableException) {
            return "The request body could not be parsed.";
        }
        if (exception instanceof MethodArgumentTypeMismatchException mismatchException) {
            return "Parameter '%s' has an invalid value.".formatted(mismatchException.getName());
        }
        return exception.getMessage();
    }

    private ValidationViolation toViolation(FieldError fieldError) {
        return new ValidationViolation(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
