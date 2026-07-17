package br.edu.ifba.inf008.plugins.ecommerce.repository;

import br.edu.ifba.inf008.plugins.ecommerce.model.Customer;

import java.util.List;

public interface CustomerRepository {
    public void save(Customer customer);
    public boolean existsById(int customer_id);
    public boolean existsByEmail(String email);
    public Customer findById(int customer_id);
    public Customer findByEmail(String email);
    public void update(Customer customer);
    public void delete(int customer_id);
    public List<Customer> findAll();
}
