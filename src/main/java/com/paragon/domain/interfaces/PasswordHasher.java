package com.paragon.domain.interfaces;

public interface PasswordHasher {
    String hash(String plainText);
    boolean verify(String plainText, String hashedPassword);
}
