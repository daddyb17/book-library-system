package com.collabera.booklibrarysystem.service;

import com.collabera.booklibrarysystem.dto.CreateBorrowerRequest;
import com.collabera.booklibrarysystem.exception.DuplicateResourceException;
import com.collabera.booklibrarysystem.exception.ResourceNotFoundException;
import com.collabera.booklibrarysystem.model.Borrower;
import com.collabera.booklibrarysystem.repository.BorrowerRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BorrowerService {

    private final BorrowerRepository borrowerRepository;
    private final Clock clock;

    public BorrowerService(BorrowerRepository borrowerRepository, Clock clock) {
        this.borrowerRepository = borrowerRepository;
        this.clock = clock;
    }

    @Transactional
    public Borrower registerBorrower(CreateBorrowerRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (borrowerRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("Borrower email is already registered: %s".formatted(normalizedEmail));
        }

        Borrower borrower = new Borrower(request.name().trim(), normalizedEmail, Instant.now(clock));
        return borrowerRepository.save(borrower);
    }

    public Borrower getBorrowerOrThrow(Long borrowerId) {
        return borrowerRepository.findById(borrowerId)
            .orElseThrow(() -> new ResourceNotFoundException("Borrower not found for id %d".formatted(borrowerId)));
    }

    // Store emails in lowercase so the unique constraint behaves consistently across environments.
    String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
