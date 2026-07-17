package br.edu.ifba.inf008.plugins.ecommerce.service;

import br.edu.ifba.inf008.plugins.ecommerce.exception.DuplicateEntityException;
import br.edu.ifba.inf008.plugins.ecommerce.exception.EntityNotFoundException;
import br.edu.ifba.inf008.plugins.ecommerce.model.Customer;
import br.edu.ifba.inf008.plugins.ecommerce.repository.CustomerRepository;
import br.edu.ifba.inf008.plugins.ecommerce.repository.CustomerRepositoryImp;

import java.util.List;

public class CustomerService {

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public void save(Customer customer) {
        if (repository.existsByEmail(customer.getEmail())) {
            throw new DuplicateEntityException("A customer with that email already exists: " + customer.getEmail());
        }
        repository.save(customer);
    }

    public Customer findById(int customer_id) {
        Customer customer = repository.findById(customer_id);
        if (customer == null) {
            throw new EntityNotFoundException("Customer", customer_id);
        }
        return customer;
    }

    public Customer findByEmail(String email) {
        Customer customer = repository.findByEmail(email);
        if (customer == null) {
            throw new EntityNotFoundException("Customer with email " + email + " not found.");
        }
        return customer;
    }

    public void update(Customer customer) {
        if (!repository.existsById(customer.getCustomer_id())) {
            throw new EntityNotFoundException("Customer", customer.getCustomer_id());
        }
        repository.update(customer);
    }

    public void remove(int customer_id) {
        if (!repository.existsById(customer_id)) {
            throw new EntityNotFoundException("Customer", customer_id);
        }
        repository.delete(customer_id);
    }

    public List<Customer> findAll() {
        return repository.findAll();
    }
}
