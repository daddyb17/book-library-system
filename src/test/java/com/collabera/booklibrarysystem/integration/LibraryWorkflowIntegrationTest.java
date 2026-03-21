package com.collabera.booklibrarysystem.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.collabera.booklibrarysystem.repository.BookRepository;
import com.collabera.booklibrarysystem.repository.BookCatalogEntryRepository;
import com.collabera.booklibrarysystem.repository.BorrowerRepository;
import com.collabera.booklibrarysystem.repository.LoanRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LibraryWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCatalogEntryRepository bookCatalogEntryRepository;

    @Autowired
    private BorrowerRepository borrowerRepository;

    @BeforeEach
    void cleanDatabase() {
        loanRepository.deleteAll();
        bookRepository.deleteAll();
        bookCatalogEntryRepository.deleteAll();
        borrowerRepository.deleteAll();
    }

    @Test
    void bookCannotBeBorrowedTwiceUntilItIsReturned() throws Exception {
        long borrowerOneId = createBorrower("Alice Johnson", "alice@example.com");
        long borrowerTwoId = createBorrower("Bob Smith", "bob@example.com");
        long bookId = createBook("9780132350884", "Clean Code", "Robert C. Martin");

        mockMvc.perform(post("/api/v1/borrowers/{borrowerId}/borrowed-books/{bookId}", borrowerOneId, bookId))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(post("/api/v1/borrowers/{borrowerId}/borrowed-books/{bookId}", borrowerTwoId, bookId))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.title").value("Request conflict"));

        mockMvc.perform(get("/api/v1/books"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].available").value(false));

        mockMvc.perform(delete("/api/v1/borrowers/{borrowerId}/borrowed-books/{bookId}", borrowerOneId, bookId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("RETURNED"));

        mockMvc.perform(post("/api/v1/borrowers/{borrowerId}/borrowed-books/{bookId}", borrowerTwoId, bookId))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("ACTIVE"));

        assertThat(loanRepository.findAll()).hasSize(2);
    }

    @Test
    void conflictingBookRegistrationForExistingIsbnIsRejected() throws Exception {
        createBook("9780132350884", "Clean Code", "Robert C. Martin");

        mockMvc.perform(post("/api/v1/books")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "isbn": "9780132350884",
                      "title": "Clean Architecture",
                      "author": "Robert C. Martin"
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.title").value("Request conflict"));
    }

    private long createBorrower(String name, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/borrowers")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "name": "%s",
                      "email": "%s"
                    }
                    """.formatted(name, email)))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("id").asLong();
    }

    private long createBook(String isbn, String title, String author) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/books")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "isbn": "%s",
                      "title": "%s",
                      "author": "%s"
                    }
                    """.formatted(isbn, title, author)))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("id").asLong();
    }
}
