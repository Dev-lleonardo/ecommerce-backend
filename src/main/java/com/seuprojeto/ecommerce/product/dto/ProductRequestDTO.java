package com.seuprojeto.ecommerce.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequestDTO(

        @NotBlank(message = "Nome do produto é obrigatório")
        @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
        String name,

        @NotBlank(message = "Descrição é obrigatória")
        @Size(min = 10, max = 255, message = "Descrição deve ter entre 10 e 255 caracteres")
        String description,

        @NotNull(message = "Preço é obrigatório")
        @Positive(message = "Preço deve ser maior que zero")
        BigDecimal price,

        @NotNull(message = "Estoque é obrigatório")
        @Positive(message = "Estoque deve ser maior que zero")
        Integer stock,

        @NotNull(message = "Categoria é obrigatória")
        Long categoryId
) {
}





