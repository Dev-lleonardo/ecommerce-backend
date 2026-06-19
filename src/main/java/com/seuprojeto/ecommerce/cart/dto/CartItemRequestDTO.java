package com.seuprojeto.ecommerce.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartItemRequestDTO(

        @NotNull(message = "Produto e obrigatorio")
        Long productId,

        @NotNull(message = "Quantidade e obrigatoria")
        @Positive(message = "Quantidade deve ser maior que zero")
        Integer quantity
) {
}
