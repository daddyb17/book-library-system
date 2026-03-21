package com.collabera.booklibrarysystem.repository;

import com.collabera.booklibrarysystem.model.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowerRepository extends JpaRepository<Borrower, Long> {

    boolean existsByEmail(String email);
}
