package com.collabera.booklibrarysystem.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.collabera.booklibrarysystem.validation.IsbnSupport;
import org.junit.jupiter.api.Test;

class IsbnSupportTest {

    @Test
    void canonicalizeRemovesSeparatorsAndUppercasesCheckDigit() {
        assertThat(IsbnSupport.canonicalize("0-8044-2957-x")).isEqualTo("080442957X");
    }

    @Test
    void validIsbnSupportsIsbn10AndIsbn13() {
        assertThat(IsbnSupport.isValid("0-8044-2957-X")).isTrue();
        assertThat(IsbnSupport.isValid("978-0-13-235088-4")).isTrue();
    }

    @Test
    void invalidIsbnIsRejected() {
        assertThat(IsbnSupport.isValid("not-an-isbn")).isFalse();
        assertThat(IsbnSupport.isValid("9780132350885")).isFalse();
    }
}
