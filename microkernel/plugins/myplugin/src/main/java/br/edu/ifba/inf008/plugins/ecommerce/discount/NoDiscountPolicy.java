package br.edu.ifba.inf008.plugins.ecommerce.discount;

import br.edu.ifba.inf008.plugins.ecommerce.model.Order;

public class NoDiscountPolicy implements DiscountPolicy {
    @Override
    public double calculateDiscount(Order order) {
        return 0;
    }
}
