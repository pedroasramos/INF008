package br.edu.ifba.inf008.plugins.ecommerce.controller;

import br.edu.ifba.inf008.plugins.ecommerce.model.Customer;
import br.edu.ifba.inf008.plugins.ecommerce.service.CustomerService;

import java.util.List;

public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    public void createCustomer(String name, String email) {
        Customer customer = new Customer(name, email);
        customerService.save(customer);
    }

    public Customer getCustomer(int customerId) {
        return customerService.findById(customerId);
    }

    public Customer getCustomerByEmail(String email) {
        return customerService.findByEmail(email);
    }

    public List<Customer> listCustomers() {
        return customerService.findAll();
    }

    public void updateCustomer(int customerId, String name, String email) {
        Customer customer = new Customer(customerId, name, email);
        customerService.update(customer);
    }

    public void deleteCustomer(int customerId) {
        customerService.remove(customerId);
    }
}
