package com.billingsystem.security;

import java.io.Serializable;

public record UserPrincipal(
        Long userId,
        String username,
        String role
) implements Serializable {}