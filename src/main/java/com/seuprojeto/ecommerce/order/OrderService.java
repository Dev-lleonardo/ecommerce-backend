package com.seuprojeto.ecommerce.order;

import com.seuprojeto.ecommerce.cart.Cart;
import com.seuprojeto.ecommerce.cart.CartRepository;
import com.seuprojeto.ecommerce.exception.BusinessException;
import com.seuprojeto.ecommerce.exception.ResourceNotFoundException;
import com.seuprojeto.ecommerce.order.dto.OrderItemResponseDTO;
import com.seuprojeto.ecommerce.order.dto.OrderResponseDTO;
import com.seuprojeto.ecommerce.user.User;
import com.seuprojeto.ecommerce.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponseDTO createFromCart() {
        User user = getCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("Carrinho não encontrado"));

        if (cart.getItems().isEmpty()) {
            throw new BusinessException("O carrinho está vazio");
        }

        cart.getItems().forEach(cartItem -> {
            if (cartItem.getProduct().getStock() < cartItem.getQuantity()) {
                throw new BusinessException(
                        "Estoque insuficiente para: " + cartItem.getProduct().getName()
                                + ". Disponível: " + cartItem.getProduct().getStock());
            }
        });

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> OrderItem.builder()
                        .product(cartItem.getProduct())
                        .quantity(cartItem.getQuantity())
                        .unitPrice(cartItem.getProduct().getPrice())
                        .build())
                .toList();

        BigDecimal total = orderItems.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .total(total)
                .createdAt(LocalDateTime.now())
                .items(orderItems)
                .build();

        orderItems.forEach(item -> item.setOrder(order));
        orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        return toResponseDTO(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> findMyOrders() {
        return orderRepository.findByUser(getCurrentUser())
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> findAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO findById(Long id) {
        User user = getCurrentUser();
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido não encontrado: " + id));

        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Pedido não encontrado: " + id);
        }

        return toResponseDTO(order);
    }

    @Transactional
    public OrderResponseDTO confirm(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido não encontrado: " + id));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(
                    "Apenas pedidos PENDING podem ser confirmados. Status atual: "
                            + order.getStatus());
        }

        order.getItems().forEach(item -> {
            int novoEstoque = item.getProduct().getStock() - item.getQuantity();
            if (novoEstoque < 0) {
                throw new BusinessException(
                        "Estoque insuficiente para: " + item.getProduct().getName());
            }
            item.getProduct().setStock(novoEstoque);
        });

        order.setStatus(OrderStatus.CONFIRMED);
        return toResponseDTO(orderRepository.save(order));
    }

    @Transactional
    public OrderResponseDTO cancel(Long id) {
        User user = getCurrentUser();
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido não encontrado: " + id));

        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Pedido não encontrado: " + id);
        }

        if (order.getStatus() == OrderStatus.CONFIRMED) {
            throw new BusinessException("Pedidos confirmados não podem ser cancelados");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Pedido já está cancelado");
        }

        order.setStatus(OrderStatus.CANCELLED);
        return toResponseDTO(orderRepository.save(order));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário não encontrado: " + email));
    }

    private OrderResponseDTO toResponseDTO(Order order) {
        List<OrderItemResponseDTO> items = order.getItems().stream()
                .map(item -> new OrderItemResponseDTO(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))))
                .toList();

        return new OrderResponseDTO(
                order.getId(),
                order.getStatus(),
                order.getTotal(),
                order.getCreatedAt(),
                items);
    }
}