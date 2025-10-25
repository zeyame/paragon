package com.paragon.domain.interfaces;

public interface TokenHasher {
    String hash(String plainToken);
}