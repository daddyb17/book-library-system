package com.collabera.booklibrarysystem.validation;

import java.util.Locale;

public final class IsbnSupport {

    private IsbnSupport() {
    }

    public static String canonicalize(String rawIsbn) {
        if (rawIsbn == null) {
            return null;
        }
        return rawIsbn
            .trim()
            .replace("-", "")
            .replace(" ", "")
            .toUpperCase(Locale.ROOT);
    }

    public static boolean isValid(String rawIsbn) {
        String isbn = canonicalize(rawIsbn);
        if (isbn == null) {
            return false;
        }
        if (isbn.length() == 10) {
            return isValidIsbn10(isbn);
        }
        if (isbn.length() == 13) {
            return isValidIsbn13(isbn);
        }
        return false;
    }

    private static boolean isValidIsbn10(String isbn) {
        int checksum = 0;
        for (int index = 0; index < 10; index++) {
            char character = isbn.charAt(index);
            int digit;
            if (index == 9 && character == 'X') {
                digit = 10;
            } else if (Character.isDigit(character)) {
                digit = character - '0';
            } else {
                return false;
            }
            checksum += digit * (10 - index);
        }
        return checksum % 11 == 0;
    }

    private static boolean isValidIsbn13(String isbn) {
        int checksum = 0;
        for (int index = 0; index < 13; index++) {
            char character = isbn.charAt(index);
            if (!Character.isDigit(character)) {
                return false;
            }
            int digit = character - '0';
            checksum += digit * (index % 2 == 0 ? 1 : 3);
        }
        return checksum % 10 == 0;
    }
}
