package br.edu.ifba.inf008.plugins.ecommerce.discount;

import br.edu.ifba.inf008.plugins.ecommerce.model.Order;

public class CouponDiscountPolicy implements DiscountPolicy{
    private String coupon;
    private final int percentage = 10;

    public CouponDiscountPolicy(String coupon) {
        this.coupon = coupon;
    }

    @Override
    public double calculateDiscount(Order order) {
        return (order.calculateSubtotal() * percentage) / 100;
    }
}
