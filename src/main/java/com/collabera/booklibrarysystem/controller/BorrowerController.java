package com.collabera.booklibrarysystem.controller;

import com.collabera.booklibrarysystem.dto.BorrowerResponse;
import com.collabera.booklibrarysystem.dto.CreateBorrowerRequest;
import com.collabera.booklibrarysystem.service.BorrowerService;
import com.collabera.booklibrarysystem.util.ResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/borrowers")
@Tag(name = "Borrowers", description = "Borrower registration endpoints.")
public class BorrowerController {

    private final BorrowerService borrowerService;

    @PostMapping
    @Operation(
        summary = "Register a borrower",
        responses = {
            @ApiResponse(responseCode = "201", description = "Borrower registered successfully"),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid borrower payload",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Borrower email already exists",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
        }
    )
    public ResponseEntity<BorrowerResponse> registerBorrower(@Valid @RequestBody CreateBorrowerRequest request) {
        BorrowerResponse response = ResponseMapper.toResponse(borrowerService.registerBorrower(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
