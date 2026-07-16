package br.edu.ifba.inf008.plugins.ecommerce.repository;

import br.edu.ifba.inf008.plugins.ecommerce.model.Order;

import java.util.List;

public class OrderRepositoryImp implements OrderRepository{

    @Override
    public void save(Order order) {

    }

    @Override
    public Order find(int order_id) {
        return null;
    }

    @Override
    public void update(Order order) {

    }

    @Override
    public List<Order> findAll() {
        return List.of();
    }

    @Override
    public void delete(int order_id) {

    }
}
