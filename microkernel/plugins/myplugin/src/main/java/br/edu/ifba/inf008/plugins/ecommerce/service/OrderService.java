package br.edu.ifba.inf008.plugins.ecommerce.service;

import br.edu.ifba.inf008.plugins.ecommerce.discount.DiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.exception.EntityNotFoundException;
import br.edu.ifba.inf008.plugins.ecommerce.model.*;
import br.edu.ifba.inf008.plugins.ecommerce.payment.Payable;
import br.edu.ifba.inf008.plugins.ecommerce.repository.*;
import br.edu.ifba.inf008.plugins.ecommerce.shipping.ShippingPolicy;

import java.util.List;

public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

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
        if (cart == null) {
            throw new EntityNotFoundException("Cart", cart_id);
        }

        Order order = new Order();
        for(CartItem cartItem : cart.getItems()){
            order.addItem(cartItem);
        }
        order.setDiscountPolicy(discountPolicy);
        order.setShippingPolicy(shippingPolicy);
        order.setPaymentMethod(paymentMethod);
        order.processPayment();
        switch (order.getStatus()){
            case PAID:
                for(CartItem cartItem : cart.getItems()){
                    Product product = cartItem.getProduct();
                    product.decreaseStock(cartItem.getQuantity());
                    productRepository.update(product);
                }
                cart.clear();
                cartRepository.update(cart);
                break;
            case PENDING:
            case INVALID_PAYMENT:
            case CANCELLED:
                break;
        }
        orderRepository.save(order);
    }

    public Order findById(int order_id){
        Order order = orderRepository.find(order_id);
        if(order == null){
            throw new EntityNotFoundException("Order", order_id);
        }
        return order;
    }

    public List<Order> findAll(){
        return orderRepository.findAll();
    }

    public void cancelOrder(int order_id){
        Order order = findById(order_id);
        if(order.getStatus() == OrderStatus.PAID){
            for(OrderItem orderItem : order.getOrderItems()){
                Product product = orderItem.getProduct();
                product.increaseStock(orderItem.getQuantity());
                productRepository.update(product);
            }
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.update(order);
        }
    }

    public void remove(int order_id){
        Order order = findById(order_id);
        if(order.getStatus() != OrderStatus.PAID){
            orderRepository.delete(order_id);
        }
    }
}
