package com.collabera.booklibrarysystem.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.collabera.booklibrarysystem.exception.ApiExceptionHandler;
import com.collabera.booklibrarysystem.exception.DuplicateResourceException;
import com.collabera.booklibrarysystem.model.Borrower;
import com.collabera.booklibrarysystem.service.BorrowerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BorrowerController.class)
@Import(ApiExceptionHandler.class)
class BorrowerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BorrowerService borrowerService;

    @Test
    void registerBorrowerReturnsCreatedResponse() throws Exception {
        when(borrowerService.registerBorrower(any()))
            .thenReturn(new Borrower("Alice Johnson", "alice@example.com", Instant.parse("2026-03-21T10:15:30Z")));

        mockMvc.perform(post("/api/v1/borrowers")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Payload("Alice Johnson", "alice@example.com"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Alice Johnson"))
            .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void registerBorrowerReturnsValidationErrors() throws Exception {
        mockMvc.perform(post("/api/v1/borrowers")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Payload("", "invalid-email"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Validation failed"))
            .andExpect(jsonPath("$.violations").isArray());
    }

    @Test
    void registerBorrowerMapsDuplicateEmailToConflict() throws Exception {
        when(borrowerService.registerBorrower(any()))
            .thenThrow(new DuplicateResourceException("Borrower email is already registered: alice@example.com"));

        mockMvc.perform(post("/api/v1/borrowers")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Payload("Alice Johnson", "alice@example.com"))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.title").value("Request conflict"));
    }

    private record Payload(String name, String email) {
    }
}
