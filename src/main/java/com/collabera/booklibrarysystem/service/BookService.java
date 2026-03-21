package com.collabera.booklibrarysystem.service;

import com.collabera.booklibrarysystem.dto.BookResponse;
import com.collabera.booklibrarysystem.dto.CreateBookRequest;
import com.collabera.booklibrarysystem.dto.PageResponse;
import com.collabera.booklibrarysystem.exception.BusinessConflictException;
import com.collabera.booklibrarysystem.exception.ResourceNotFoundException;
import com.collabera.booklibrarysystem.model.Book;
import com.collabera.booklibrarysystem.model.BookCatalogEntry;
import com.collabera.booklibrarysystem.repository.BookCatalogEntryRepository;
import com.collabera.booklibrarysystem.repository.BookRepository;
import com.collabera.booklibrarysystem.repository.LoanRepository;
import com.collabera.booklibrarysystem.util.ResponseMapper;
import com.collabera.booklibrarysystem.util.SortParser;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BookService {

    private static final Map<String, String> ALLOWED_SORT_FIELDS = Map.of(
        "id", "id",
        "isbn", "catalogEntry.isbn",
        "title", "catalogEntry.title",
        "author", "catalogEntry.author",
        "createdAt", "createdAt"
    );

    private final BookCatalogEntryRepository bookCatalogEntryRepository;
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final Clock clock;

    public BookService(
        BookCatalogEntryRepository bookCatalogEntryRepository,
        BookRepository bookRepository,
        LoanRepository loanRepository,
        Clock clock
    ) {
        this.bookCatalogEntryRepository = bookCatalogEntryRepository;
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
        this.clock = clock;
    }

    @Transactional
    public Book registerBook(CreateBookRequest request) {
        String isbn = normalize(request.isbn());
        String title = normalize(request.title());
        String author = normalize(request.author());

        BookCatalogEntry catalogEntry = resolveCatalogEntry(isbn, title, author);
        Book book = new Book(catalogEntry, Instant.now(clock));
        return bookRepository.save(book);
    }

    public PageResponse<BookResponse> listBooks(int page, int size, String sortExpression) {
        Sort sort = SortParser.parse(sortExpression, ALLOWED_SORT_FIELDS);
        Page<Book> books = bookRepository.findAll(PageRequest.of(page, size, sort));

        List<Long> bookIds = books.getContent().stream().map(Book::getId).toList();
        Set<Long> borrowedBookIds = bookIds.isEmpty() ? Set.of() : loanRepository.findActiveBookIds(bookIds);
        Page<BookResponse> responsePage = books.map(book -> ResponseMapper.toResponse(book, !borrowedBookIds.contains(book.getId())));

        return ResponseMapper.toPageResponse(responsePage, List.of(toNormalizedSort(sortExpression)));
    }

    public Book getBookForUpdateOrThrow(Long bookId) {
        return bookRepository.findByIdForUpdate(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found for id %d".formatted(bookId)));
    }

    private String normalize(String value) {
        return value.trim();
    }

    private BookCatalogEntry resolveCatalogEntry(String isbn, String title, String author) {
        BookCatalogEntry existingCatalogEntry = bookCatalogEntryRepository.findByIsbn(isbn).orElse(null);
        if (existingCatalogEntry != null) {
            validateCatalogDetails(existingCatalogEntry, title, author);
            return existingCatalogEntry;
        }

        BookCatalogEntry newCatalogEntry = new BookCatalogEntry(isbn, title, author, Instant.now(clock));
        try {
            return bookCatalogEntryRepository.saveAndFlush(newCatalogEntry);
        } catch (DataIntegrityViolationException exception) {
            BookCatalogEntry concurrentCatalogEntry = bookCatalogEntryRepository.findByIsbn(isbn)
                .orElseThrow(() -> exception);
            validateCatalogDetails(concurrentCatalogEntry, title, author);
            return concurrentCatalogEntry;
        }
    }

    private void validateCatalogDetails(BookCatalogEntry catalogEntry, String title, String author) {
        if (!catalogEntry.matchesCatalogDetails(title, author)) {
            throw new BusinessConflictException(
                "ISBN %s is already associated with a different title or author.".formatted(catalogEntry.getIsbn())
            );
        }
    }

    private String toNormalizedSort(String sortExpression) {
        String[] parts = sortExpression.split(",");
        return "%s,%s".formatted(parts[0].trim(), parts[1].trim().toLowerCase(Locale.ROOT));
    }
}
