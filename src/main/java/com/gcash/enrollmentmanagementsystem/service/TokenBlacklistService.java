package com.gcash.enrollmentmanagementsystem.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final Set<String> tokenBlacklist = ConcurrentHashMap.newKeySet();

    public void blacklist(String token) {
        tokenBlacklist.add(token);
    }

    public boolean isBlacklisted(String token) {
        return tokenBlacklist.contains(token);
    }
}
