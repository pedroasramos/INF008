package br.edu.ifba.inf008.plugins.ecommerce.model;

import br.edu.ifba.inf008.plugins.ecommerce.discount.DiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.payment.Payable;
import br.edu.ifba.inf008.plugins.ecommerce.payment.PaymentStatus;
import br.edu.ifba.inf008.plugins.ecommerce.shipping.ShippingPolicy;

import java.util.ArrayList;
import java.util.List;

public class Order {

    private int order_id;
    private List<OrderItem> orderItems = new ArrayList<>();
    private DiscountPolicy discountPolicy;
    private ShippingPolicy shippingPolicy;
    private Payable paymentMethod;
    private OrderStatus status;
    private double subtotal;
    private double discount;
    private double shippingCost;
    private double total;

    public void addItem(CartItem cartItem){
        OrderItem orderItem = new OrderItem(cartItem.getProduct(), cartItem.getQuantity(), cartItem.getPrice());
        orderItems.add(orderItem);
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public double calculateSubtotal(){
        subtotal = 0;
        for(OrderItem orderItem : orderItems){
            subtotal += orderItem.calculateSubtotal();
        }
        return subtotal;
    }

    public double calculateDiscount(){
        discount = discountPolicy.calculateDiscount(this);
        return discount;
    }

    public double calculateShipping(){
        shippingCost = shippingPolicy.calculateShipping(this);
        return shippingCost;
    }

    public double calculateTotal(){
        total = 0;
        total = (calculateSubtotal() - calculateDiscount()) + calculateShipping();
        return total;
    }

    public void setDiscountPolicy(DiscountPolicy discountPolicy) {
        this.discountPolicy = discountPolicy;
    }

    public void setShippingPolicy(ShippingPolicy shippingPolicy) {
        this.shippingPolicy = shippingPolicy;
    }

    public void setPaymentMethod(Payable paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void processPayment(){
        PaymentStatus paymentStatus;
        paymentStatus = paymentMethod.pay(calculateTotal());
        convert(paymentStatus);
    }

    private void convert(PaymentStatus paymentStatus){
        switch (paymentStatus){
            case PAID:
                status = OrderStatus.PAID;
                break;
            case INVALID:
                status = OrderStatus.INVALID_PAYMENT;
                break;
            case PENDING:
                status = OrderStatus.PENDING;
                break;
            case FAILED:
                status = OrderStatus.CANCELLED;
                break;
        }
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}