package com.seuprojeto.ecommerce.role.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleRequestDTO(

        @NotBlank(message = "Nome da role é obrigatório")
        String name

) {}

