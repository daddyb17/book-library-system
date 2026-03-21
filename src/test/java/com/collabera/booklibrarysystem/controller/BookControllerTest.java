package com.collabera.booklibrarysystem.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.collabera.booklibrarysystem.dto.BookResponse;
import com.collabera.booklibrarysystem.dto.PageResponse;
import com.collabera.booklibrarysystem.exception.ApiExceptionHandler;
import com.collabera.booklibrarysystem.service.BookService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookController.class)
@Import(ApiExceptionHandler.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
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
}
