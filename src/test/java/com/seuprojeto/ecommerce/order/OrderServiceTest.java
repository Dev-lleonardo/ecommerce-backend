package com.seuprojeto.ecommerce.order;

import com.seuprojeto.ecommerce.cart.Cart;
import com.seuprojeto.ecommerce.cart.CartItem;
import com.seuprojeto.ecommerce.cart.CartRepository;
import com.seuprojeto.ecommerce.exception.BusinessException;
import com.seuprojeto.ecommerce.exception.ResourceNotFoundException;
import com.seuprojeto.ecommerce.product.Product;
import com.seuprojeto.ecommerce.role.Role;
import com.seuprojeto.ecommerce.user.User;
import com.seuprojeto.ecommerce.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Leo")
                .email("leo@test.local")
                .password("secret")
                .roles(Set.of(Role.builder().name("ROLE_USER").build()))
                .build();
        product = Product.builder()
                .id(20L)
                .name("Mouse")
                .price(BigDecimal.TEN)
                .stock(5)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null)
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createFromCartShouldThrowWhenCartIsEmpty() {
        Cart cart = Cart.builder().id(10L).user(user).build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        assertThrows(BusinessException.class, () -> orderService.createFromCart());
    }

    @Test
    void createFromCartShouldCreateOrderAndClearCart() {
        Cart cart = Cart.builder().id(10L).user(user).build();
        cart.getItems().add(CartItem.builder().cart(cart).product(product).quantity(2).build());

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return order;
        });

        var response = orderService.createFromCart();

        assertEquals(OrderStatus.PENDING, response.status());
        assertEquals(new BigDecimal("20"), response.total());
        assertEquals(0, cart.getItems().size());
        verify(cartRepository).save(cart);
    }

    @Test
    void findByIdShouldHideOrderFromAnotherUser() {
        User owner = User.builder().id(99L).email("owner@test.local").password("secret").build();
        Order order = Order.builder().id(100L).user(owner).status(OrderStatus.PENDING).items(new ArrayList<>()).build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThrows(ResourceNotFoundException.class, () -> orderService.findById(order.getId()));
    }

    @Test
    void confirmShouldDecrementStockAndMarkOrderAsConfirmed() {
        OrderItem item = OrderItem.builder().product(product).quantity(2).unitPrice(BigDecimal.TEN).build();
        Order order = Order.builder()
                .id(100L)
                .user(user)
                .status(OrderStatus.PENDING)
                .total(new BigDecimal("20"))
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();
        item.setOrder(order);
        order.getItems().add(item);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        var response = orderService.confirm(order.getId());

        assertEquals(OrderStatus.CONFIRMED, response.status());
        assertEquals(3, product.getStock());
    }

    @Test
    void cancelShouldThrowWhenOrderIsConfirmed() {
        Order order = Order.builder()
                .id(100L)
                .user(user)
                .status(OrderStatus.CONFIRMED)
                .items(new ArrayList<>())
                .build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () -> orderService.cancel(order.getId()));
    }
}
