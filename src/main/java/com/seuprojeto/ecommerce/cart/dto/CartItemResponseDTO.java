package com.seuprojeto.ecommerce.cart.dto;

import java.math.BigDecimal;

public record CartItemResponseDTO(
        Long productId,
        String name,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
