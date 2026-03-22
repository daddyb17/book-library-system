package com.collabera.booklibrarysystem.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.collabera.booklibrarysystem.dto.BookResponse;
import com.collabera.booklibrarysystem.dto.PageResponse;
import com.collabera.booklibrarysystem.exception.ApiExceptionHandler;
import com.collabera.booklibrarysystem.model.Book;
import com.collabera.booklibrarysystem.model.BookCatalogEntry;
import com.collabera.booklibrarysystem.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookController.class)
@Import(ApiExceptionHandler.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    @Test
    void listBooksReturnsPagedResponse() throws Exception {
        PageResponse<BookResponse> response = new PageResponse<>(
            List.of(new BookResponse(1L, "9780132350884", "Clean Code", "Robert C. Martin", true, Instant.parse("2026-03-21T10:15:30Z"))),
            0,
            20,
            1,
            1,
            true,
            true,
            List.of("title,asc")
        );
        when(bookService.listBooks(0, 20, "title,asc")).thenReturn(response);

        mockMvc.perform(get("/api/v1/books"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].title").value("Clean Code"))
            .andExpect(jsonPath("$.sort[0]").value("title,asc"));
    }

    @Test
    void listBooksRejectsInvalidPageSize() throws Exception {
        mockMvc.perform(get("/api/v1/books").param("size", "101"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Bad request"));
    }

    @Test
    void registerBookRejectsInvalidIsbn() throws Exception {
        mockMvc.perform(post("/api/v1/books")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Payload("not-an-isbn", "Clean Code", "Robert C. Martin"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void registerBookReturnsCanonicalIsbnFromService() throws Exception {
        when(bookService.registerBook(any())).thenReturn(
            new Book(new BookCatalogEntry("9780132350884", "Clean Code", "Robert C. Martin", Instant.parse("2026-03-21T10:15:30Z")), Instant.parse("2026-03-21T10:15:30Z"))
        );

        mockMvc.perform(post("/api/v1/books")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Payload("978-0-13-235088-4", "Clean Code", "Robert C. Martin"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.isbn").value("9780132350884"));
    }

    private record Payload(String isbn, String title, String author) {
    }
}
