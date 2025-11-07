package com.paragon.domain.models.valueobjects;

import com.paragon.domain.exceptions.valueobject.PlaintextPasswordException;
import com.paragon.domain.exceptions.valueobject.PlaintextPasswordExceptionInfo;
import lombok.Getter;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class PlaintextPassword extends ValueObject {
    private final String value;

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    private PlaintextPassword(String value) {
        this.value = value;
    }

    public static PlaintextPassword of(String plaintext) {
        assertValidPassword(plaintext);
        return new PlaintextPassword(plaintext);
    }

    public static PlaintextPassword generate() {
        String plaintext = generatePlaintext();
        return new PlaintextPassword(plaintext);
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return List.of(value);
    }

    private static String generatePlaintext() {
        SecureRandom random = new SecureRandom();

        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";

        StringBuilder password = new StringBuilder(12);

        password.append(uppercase.charAt(random.nextInt(uppercase.length())));
        password.append(lowercase.charAt(random.nextInt(lowercase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(SPECIAL_CHARACTERS.charAt(random.nextInt(SPECIAL_CHARACTERS.length())));

        String allCharacters = uppercase + lowercase + digits + SPECIAL_CHARACTERS;
        for (int i = 4; i < 12; i++) {
            password.append(allCharacters.charAt(random.nextInt(allCharacters.length())));
        }

        return shuffleString(password.toString(), random);
    }

    private static String shuffleString(String input, SecureRandom random) {
        List<Character> characters = new ArrayList<>();
        for (char c : input.toCharArray()) {
            characters.add(c);
        }
        Collections.shuffle(characters, random);

        StringBuilder result = new StringBuilder(characters.size());
        for (char c : characters) {
            result.append(c);
        }
        return result.toString();
    }

    private static void assertValidPassword(String value) {
        if (value == null || value.isBlank()) {
            throw new PlaintextPasswordException(PlaintextPasswordExceptionInfo.missingValue());
        }

        if (value.length() < MIN_LENGTH) {
            throw new PlaintextPasswordException(PlaintextPasswordExceptionInfo.tooShort(MIN_LENGTH));
        }

        if (value.length() > MAX_LENGTH) {
            throw new PlaintextPasswordException(PlaintextPasswordExceptionInfo.tooLong(MAX_LENGTH));
        }

        if (value.chars().noneMatch(Character::isUpperCase)) {
            throw new PlaintextPasswordException(PlaintextPasswordExceptionInfo.missingUppercase());
        }

        if (value.chars().noneMatch(Character::isLowerCase)) {
            throw new PlaintextPasswordException(PlaintextPasswordExceptionInfo.missingLowercase());
        }

        if (value.chars().noneMatch(Character::isDigit)) {
            throw new PlaintextPasswordException(PlaintextPasswordExceptionInfo.missingDigit());
        }

        if (value.chars().noneMatch(ch -> SPECIAL_CHARACTERS.indexOf(ch) >= 0)) {
            throw new PlaintextPasswordException(PlaintextPasswordExceptionInfo.missingSpecialCharacter());
        }

        if (value.chars().anyMatch(Character::isWhitespace)) {
            throw new PlaintextPasswordException(PlaintextPasswordExceptionInfo.containsWhitespace());
        }
    }
}