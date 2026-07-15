package br.edu.ifba.inf008.plugins.ecommerce.shipping;

import br.edu.ifba.inf008.plugins.ecommerce.model.Order;

public class StandardShinppingPolicy implements ShippingPolicy{
    private final double fixedPrice = 20;

    @Override
    public double calculateShipping(Order order) {
        return fixedPrice;
    }
}
