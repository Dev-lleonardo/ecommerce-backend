package com.seuprojeto.ecommerce.user;

public record UserRequestDTO(
        String name,
        String email,
        String password
) {}
