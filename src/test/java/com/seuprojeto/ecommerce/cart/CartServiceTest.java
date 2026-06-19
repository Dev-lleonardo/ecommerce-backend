package com.seuprojeto.ecommerce.cart;

import com.seuprojeto.ecommerce.cart.dto.CartItemRequestDTO;
import com.seuprojeto.ecommerce.exception.BusinessException;
import com.seuprojeto.ecommerce.exception.ResourceNotFoundException;
import com.seuprojeto.ecommerce.product.Product;
import com.seuprojeto.ecommerce.product.ProductRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Leo").email("leo@test.local").password("secret").build();
        cart = Cart.builder().id(10L).user(user).build();
        product = Product.builder()
                .id(20L)
                .name("Mouse")
                .price(BigDecimal.TEN)
                .stock(5)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null)
        );
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addItemShouldCreateCartItemWhenProductIsNotInCart() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());
        when(cartRepository.save(cart)).thenReturn(cart);

        var response = cartService.addItem(new CartItemRequestDTO(product.getId(), 2));

        assertEquals(1, response.items().size());
        assertEquals(2, response.items().get(0).quantity());
        assertEquals(new BigDecimal("20"), response.total());
        verify(cartRepository).save(cart);
    }

    @Test
    void addItemShouldThrowWhenStockIsInsufficient() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        assertThrows(BusinessException.class,
                () -> cartService.addItem(new CartItemRequestDTO(product.getId(), 6)));
    }

    @Test
    void updateItemShouldThrowWhenItemDoesNotExist() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.updateItem(product.getId(), new CartItemRequestDTO(product.getId(), 1)));
    }

    @Test
    void removeItemShouldRemoveExistingItem() {
        CartItem item = CartItem.builder().cart(cart).product(product).quantity(1).build();
        cart.getItems().add(item);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartRepository.save(cart)).thenReturn(cart);

        var response = cartService.removeItem(product.getId());

        assertEquals(0, response.items().size());
        assertEquals(BigDecimal.ZERO, response.total());
    }

    @Test
    void clearCartShouldRemoveAllItems() {
        cart.getItems().add(CartItem.builder().cart(cart).product(product).quantity(1).build());
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        cartService.clearCart();

        assertEquals(0, cart.getItems().size());
        verify(cartRepository).save(cart);
    }
}
