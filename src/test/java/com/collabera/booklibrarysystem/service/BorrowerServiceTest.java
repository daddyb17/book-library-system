package com.collabera.booklibrarysystem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.collabera.booklibrarysystem.dto.CreateBorrowerRequest;
import com.collabera.booklibrarysystem.exception.DuplicateResourceException;
import com.collabera.booklibrarysystem.model.Borrower;
import com.collabera.booklibrarysystem.repository.BorrowerRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BorrowerServiceTest {

    @Mock
    private BorrowerRepository borrowerRepository;

    private BorrowerService borrowerService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-03-21T10:15:30Z"), ZoneOffset.UTC);
        borrowerService = new BorrowerService(borrowerRepository, fixedClock);
    }

    @Test
    void registerBorrowerStoresTrimmedNameAndNormalizedEmail() {
        when(borrowerRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(borrowerRepository.save(any(Borrower.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Borrower borrower = borrowerService.registerBorrower(
            new CreateBorrowerRequest(" Alice Johnson ", " Alice@Example.com ")
        );

        assertThat(borrower.getName()).isEqualTo("Alice Johnson");
        assertThat(borrower.getEmail()).isEqualTo("alice@example.com");
        assertThat(borrower.getCreatedAt()).isEqualTo(Instant.parse("2026-03-21T10:15:30Z"));
    }

    @Test
    void registerBorrowerRejectsDuplicateEmail() {
        when(borrowerRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> borrowerService.registerBorrower(
            new CreateBorrowerRequest("Alice Johnson", "alice@example.com")
        ))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("alice@example.com");

        verify(borrowerRepository).existsByEmail("alice@example.com");
    }
}
