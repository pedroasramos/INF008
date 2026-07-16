package br.edu.ifba.inf008.plugins.ecommerce.service;

import br.edu.ifba.inf008.plugins.ecommerce.model.Cart;
import br.edu.ifba.inf008.plugins.ecommerce.model.Product;
import br.edu.ifba.inf008.plugins.ecommerce.repository.CartRepositoryImp;
import br.edu.ifba.inf008.plugins.ecommerce.repository.ProductRepositoryImp;

public class CartService {

    private CartRepositoryImp cartRepository;
    private ProductRepositoryImp productRepository;

    public CartService(CartRepositoryImp cartRepository, ProductRepositoryImp productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public void addProduct(int cart_id, int product_id, int quantity){
        Cart cart = cartRepository.findById(cart_id);
        Product product = productRepository.findById(product_id);
        if(cart != null &&
           product != null &&
           product.hasStock(quantity)) {
            cart.addProduct(product, quantity);
            cartRepository.save(cart);
        }
    }

    public void removeProduct(int cart_id, int product_id){
        Cart cart = cartRepository.findById(cart_id);
        Product product = productRepository.findById(product_id);
        if(cart != null && product != null){
            cart.removeProduct(product);
            cartRepository.update(cart);
        }
    }

    public Cart getCart(int cart_id){
        return cartRepository.findById(cart_id);
    }

    public void clearCart(int cart_id){
        Cart cart = cartRepository.findById(cart_id);
        cart.clear();
        cartRepository.update(cart);
    }
}
