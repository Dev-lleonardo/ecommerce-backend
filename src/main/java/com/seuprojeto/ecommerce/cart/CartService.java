package com.seuprojeto.ecommerce.cart;

import com.seuprojeto.ecommerce.cart.dto.CartItemRequestDTO;
import com.seuprojeto.ecommerce.cart.dto.CartItemResponseDTO;
import com.seuprojeto.ecommerce.cart.dto.CartResponseDTO;
import com.seuprojeto.ecommerce.exception.BusinessException;
import com.seuprojeto.ecommerce.exception.ResourceNotFoundException;
import com.seuprojeto.ecommerce.product.Product;
import com.seuprojeto.ecommerce.product.ProductRepository;
import com.seuprojeto.ecommerce.user.User;
import com.seuprojeto.ecommerce.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartResponseDTO getCart() {
        Cart cart = getOrCreateCart();
        return toResponseDTO(cart);
    }

    @Transactional
    public CartResponseDTO addItem(CartItemRequestDTO dto) {
        Cart cart = getOrCreateCart();
        Product product = getProduct(dto.productId());
        validateStock(product, dto.quantity());

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseGet(() -> {
                    CartItem newItem = CartItem.builder()
                            .cart(cart)
                            .product(product)
                            .quantity(0)
                            .build();
                    cart.getItems().add(newItem);
                    return newItem;
                });

        int newQuantity = item.getQuantity() + dto.quantity();
        validateStock(product, newQuantity);
        item.setQuantity(newQuantity);

        return toResponseDTO(cartRepository.save(cart));
    }

    @Transactional
    public CartResponseDTO updateItem(Long productId, CartItemRequestDTO dto) {
        if (!productId.equals(dto.productId())) {
            throw new BusinessException("Produto da URL deve ser o mesmo do corpo da requisicao");
        }

        Cart cart = getOrCreateCart();
        Product product = getProduct(productId);
        validateStock(product, dto.quantity());

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new ResourceNotFoundException("Item nao encontrado no carrinho"));

        item.setQuantity(dto.quantity());
        return toResponseDTO(cartRepository.save(cart));
    }

    @Transactional
    public CartResponseDTO removeItem(Long productId) {
        Cart cart = getOrCreateCart();
        Product product = getProduct(productId);

        boolean removed = cart.getItems().removeIf(item -> item.getProduct().getId().equals(product.getId()));
        if (!removed) {
            throw new ResourceNotFoundException("Item nao encontrado no carrinho");
        }

        return toResponseDTO(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart() {
        Cart cart = getOrCreateCart();
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart() {
        User user = getCurrentUser();
        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.builder()
                        .user(user)
                        .build()));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado: " + email));
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado: " + productId));
    }

    private void validateStock(Product product, int quantity) {
        if (product.getStock() == null || product.getStock() < quantity) {
            throw new BusinessException("Estoque insuficiente para: " + product.getName()
                    + ". Disponivel: " + product.getStock());
        }
    }

    private CartResponseDTO toResponseDTO(Cart cart) {
        List<CartItemResponseDTO> items = cart.getItems().stream()
                .map(item -> new CartItemResponseDTO(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getProduct().getPrice(),
                        item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemResponseDTO::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponseDTO(cart.getId(), items, total);
    }
}
