package br.edu.ifba.inf008.plugins.ecommerce.service;

import br.edu.ifba.inf008.plugins.ecommerce.exception.EntityNotFoundException;
import br.edu.ifba.inf008.plugins.ecommerce.exception.InsufficientStockException;
import br.edu.ifba.inf008.plugins.ecommerce.model.Cart;
import br.edu.ifba.inf008.plugins.ecommerce.model.Product;
import br.edu.ifba.inf008.plugins.ecommerce.repository.CartRepository;
import br.edu.ifba.inf008.plugins.ecommerce.repository.CartRepositoryImp;
import br.edu.ifba.inf008.plugins.ecommerce.repository.ProductRepository;
import br.edu.ifba.inf008.plugins.ecommerce.repository.ProductRepositoryImp;

public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public Cart createCart(int customerId){
        Cart cart = new Cart(customerId);
        cartRepository.save(cart);
        return cart;
    }

    public void addProduct(int cart_id, int product_id, int quantity){
        Cart cart = cartRepository.findById(cart_id);
        Product product = productRepository.findById(product_id);

        if (cart == null) throw new EntityNotFoundException("Cart", cart_id);
        if (product == null) throw new EntityNotFoundException("Product", product_id);

        if (!product.hasStock(quantity)) {
            throw new InsufficientStockException(product.getName(), quantity, product.getStock());
        }

        cart.addProduct(product, quantity);
        cartRepository.update(cart);
    }

    public void removeProduct(int cart_id, int product_id){
        Cart cart = cartRepository.findById(cart_id);
        Product product = productRepository.findById(product_id);

        if (cart == null) throw new EntityNotFoundException("Cart", cart_id);
        if (product == null) throw new EntityNotFoundException("Product", product_id);

        cart.removeProduct(product);
        cartRepository.update(cart);
    }

    public Cart getCart(int cart_id){
        Cart cart = cartRepository.findById(cart_id);
        if (cart == null) throw new EntityNotFoundException("Cart", cart_id);
        return cart;
    }

    public void clearCart(int cart_id){
        Cart cart = cartRepository.findById(cart_id);
        if (cart == null) throw new EntityNotFoundException("Cart", cart_id);
        cart.clear();
        cartRepository.update(cart);
    }
}
