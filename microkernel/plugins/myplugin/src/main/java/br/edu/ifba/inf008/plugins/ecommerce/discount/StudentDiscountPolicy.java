package br.edu.ifba.inf008.plugins.ecommerce.discount;

import br.edu.ifba.inf008.plugins.ecommerce.model.Order;

public class StudentDiscountPolicy implements DiscountPolicy{
    private final int percentage = 15;

    @Override
    public double calculateDiscount(Order order) {
        return (order.calculateSubtotal() * percentage) / 100;
    }
}
