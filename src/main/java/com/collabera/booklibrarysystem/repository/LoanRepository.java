package com.collabera.booklibrarysystem.repository;

import com.collabera.booklibrarysystem.model.Loan;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    Optional<Loan> findByBook_IdAndReturnedAtIsNull(Long bookId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select loan
        from Loan loan
        where loan.borrower.id = :borrowerId
          and loan.book.id = :bookId
          and loan.returnedAt is null
        """)
    Optional<Loan> findActiveLoanForUpdate(@Param("borrowerId") Long borrowerId, @Param("bookId") Long bookId);

    @Query("""
        select loan.book.id
        from Loan loan
        where loan.book.id in :bookIds
          and loan.returnedAt is null
        """)
    Set<Long> findActiveBookIds(@Param("bookIds") Collection<Long> bookIds);
}
