package br.edu.ifba.inf008.plugins.ecommerce.controller;

import br.edu.ifba.inf008.plugins.ecommerce.model.Cart;
import br.edu.ifba.inf008.plugins.ecommerce.service.CartService;

public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    public Cart createCart(int customerId) {
        return cartService.createCart(customerId);
    }

    public void addProductToCart(int cartId, int productId, int quantity) {
        cartService.addProduct(cartId, productId, quantity);
    }

    public void removeProductFromCart(int cartId, int productId) {
        cartService.removeProduct(cartId, productId);
    }

    public Cart getCart(int cartId) {
        return cartService.getCart(cartId);
    }

    public void clearCart(int cartId) {
        cartService.clearCart(cartId);
    }
}
