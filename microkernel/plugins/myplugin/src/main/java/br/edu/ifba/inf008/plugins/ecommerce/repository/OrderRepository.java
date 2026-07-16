package br.edu.ifba.inf008.plugins.ecommerce.repository;

import br.edu.ifba.inf008.plugins.ecommerce.model.Order;

import java.util.List;

public interface OrderRepository {
    public void save(Order order);
    public Order find(int order_id);
    public void update(Order order);
    public List<Order> findAll();
    public void delete(int order_id);
}
