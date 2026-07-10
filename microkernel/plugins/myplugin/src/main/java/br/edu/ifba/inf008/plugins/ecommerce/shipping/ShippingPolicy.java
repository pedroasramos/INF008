package br.edu.ifba.inf008.plugins.ecommerce.shipping;

import br.edu.ifba.inf008.plugins.ecommerce.model.Order;

public interface ShippingPolicy {
    public double calculateShipping(Order order);
}
