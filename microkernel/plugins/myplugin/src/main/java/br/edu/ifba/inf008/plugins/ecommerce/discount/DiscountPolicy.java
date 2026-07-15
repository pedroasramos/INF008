package br.edu.ifba.inf008.plugins.ecommerce.discount;

import br.edu.ifba.inf008.plugins.ecommerce.model.Order;

public interface DiscountPolicy {
    public double calculateDiscount(Order order);
}
