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

    public double calculateSubtotal(){
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
        switch (paymentStatus){
            case PAID:
                confirm();
                break;
            case INVALID:
                invalid();
                break;
            case PENDING:
                pending();
                break;
            case FAILED:
                cancel();
                break;
        }
    }

    public void confirm(){
        status = OrderStatus.PAID;
    }

    public void pending(){
        status = OrderStatus.PENDING;
    }

    public void invalid(){
        status = OrderStatus.INVALID_PAYMENT;
    }

    public void cancel(){
        status = OrderStatus.CANCELLED;
    }

    public OrderStatus getStatus() {
        return status;
    }
}