package com.seuprojeto.ecommerce.order.dto;

import com.seuprojeto.ecommerce.order.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(
        Long id,
        OrderStatus status,
        BigDecimal total,
        LocalDateTime createdAt,
        List<OrderItemResponseDTO> items
) {}