package com.collabera.booklibrarysystem.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.collabera.booklibrarysystem.dto.LoanResponse;
import com.collabera.booklibrarysystem.exception.ApiExceptionHandler;
import com.collabera.booklibrarysystem.exception.ResourceNotFoundException;
import com.collabera.booklibrarysystem.service.LoanService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LoanController.class)
@Import(ApiExceptionHandler.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanService loanService;

    @Test
    void borrowBookReturnsCreatedResponse() throws Exception {
        when(loanService.borrowBook(1L, 2L))
            .thenReturn(new LoanResponse(10L, 1L, 2L, Instant.parse("2026-03-21T10:15:30Z"), "ACTIVE"));

        mockMvc.perform(post("/api/v1/borrowers/1/borrowed-books/2"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void returnBookMapsMissingLoanToNotFound() throws Exception {
        when(loanService.returnBook(1L, 2L))
            .thenThrow(new ResourceNotFoundException("Active loan not found for borrower 1 and book 2"));

        mockMvc.perform(delete("/api/v1/borrowers/1/borrowed-books/2"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Resource not found"));
    }
}
