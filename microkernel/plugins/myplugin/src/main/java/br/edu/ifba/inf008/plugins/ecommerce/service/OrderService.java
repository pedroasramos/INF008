package br.edu.ifba.inf008.plugins.ecommerce.service;

import br.edu.ifba.inf008.plugins.ecommerce.discount.DiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.model.*;
import br.edu.ifba.inf008.plugins.ecommerce.payment.Payable;
import br.edu.ifba.inf008.plugins.ecommerce.repository.CartRepository;
import br.edu.ifba.inf008.plugins.ecommerce.repository.OrderRepository;
import br.edu.ifba.inf008.plugins.ecommerce.repository.ProductRepository;
import br.edu.ifba.inf008.plugins.ecommerce.shipping.ShippingPolicy;

public class OrderService {
    private OrderRepository orderRepository;
    private CartRepository cartRepository;
    private ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public void createOrder(int cart_id,
                            DiscountPolicy discountPolicy,
                            ShippingPolicy shippingPolicy,
                            Payable paymentMethod){
        Cart cart = cartRepository.findById(cart_id);
        Order order = new Order();
        for(CartItem cartItem : cart.getItems()){
            order.addItem(cartItem);
        }
        order.setDiscountPolicy(discountPolicy);
        order.setShippingPolicy(shippingPolicy);
        order.setPaymentMethod(paymentMethod);
        double total = order.calculateTotal();
        if(order.getStatus() == OrderStatus.PAID){
            Product product;
            for(CartItem cartItem : cart.getItems()){
                product = cartItem.getProduct();
                product.decreaseStock(cartItem.getQuantity());
                productRepository.update(product);
            }
            orderRepository.save(order);
            cart.clear();
            cartRepository.update(cart);
        }
    }
}
